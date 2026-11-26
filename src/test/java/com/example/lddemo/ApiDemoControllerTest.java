package com.example.lddemo;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.integrations.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ApiDemoControllerTest {

    private static final String FLAG_KEY = "enable-new-api-response";

    @TestConfiguration
    static class TestLaunchDarklyConfig {

        @Bean
        public TestData testData() {
            return TestData.dataSource();
        }

        @Bean(destroyMethod = "close")
        @Primary
        public LDClient testLdClient(TestData testData) {
            LDConfig config = new LDConfig.Builder()
                    .dataSource(testData)
                    .events(com.launchdarkly.sdk.server.Components.noEvents())
                    .build();
            return new LDClient("sdk-test-key", config);
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TestData testData;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void shouldReturnV1ResponseWhenFlagIsOff() throws Exception {
        testData.update(testData.flag(FLAG_KEY).variationForAll(false));

        mockMvc.perform(get("/api/v1/data").param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v1"))
                .andExpect(jsonPath("$.status").value("FEATURE_OFF"))
                .andExpect(jsonPath("$.flagEnabled").value(false));
    }

    @Test
    void shouldReturnV2ResponseWhenFlagIsOn() throws Exception {
        testData.update(testData.flag(FLAG_KEY).variationForAll(true));

        mockMvc.perform(get("/api/v1/data")
                        .param("userId", "user-123")
                        .param("plan", "pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v2"))
                .andExpect(jsonPath("$.status").value("FEATURE_ON"))
                .andExpect(jsonPath("$.flagEnabled").value(true))
                .andExpect(jsonPath("$.extraData.discount").value("20%"));
    }

    @Test
    void shouldTargetSpecificBetaUserWhileKeepingOthersOff() throws Exception {
        testData.update(testData.flag(FLAG_KEY)
                .variationForAll(false)
                .variationForUser("beta-tester", true));

        mockMvc.perform(get("/api/v1/data").param("userId", "regular-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v1"));

        mockMvc.perform(get("/api/v1/data").param("userId", "beta-tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v2"));
    }
}
