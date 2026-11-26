package com.example.lddemo.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LaunchDarklyConfig {

    private static final Logger log = LoggerFactory.getLogger(LaunchDarklyConfig.class);

    @Value("${launchdarkly.sdk-key}")
    private String sdkKey;

    @Value("${launchdarkly.offline:false}")
    private boolean offline;

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(LDClient.class)
    public LDClient ldClient() {
        if (offline) {
            log.info("Initializing LaunchDarkly LDClient in OFFLINE mode.");
            LDConfig config = new LDConfig.Builder()
                    .offline(true)
                    .build();
            return new LDClient("sdk-offline-key", config);
        }

        log.info("Initializing LaunchDarkly LDClient with streaming connection.");
        return new LDClient(sdkKey);
    }
}
