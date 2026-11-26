package com.example.lddemo.service;

import com.launchdarkly.sdk.ContextBuilder;
import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private final LDClient ldClient;
    private final String newApiResponseFlagKey;

    public FeatureFlagService(
            LDClient ldClient,
            @Value("${launchdarkly.flags.new-api-response}") String newApiResponseFlagKey) {
        this.ldClient = ldClient;
        this.newApiResponseFlagKey = newApiResponseFlagKey;
    }

    public EvaluationDetail<Boolean> evaluateNewApiResponse(String userId, String email, String plan) {
        ContextBuilder builder = LDContext.builder(userId)
                .name("User-" + userId);

        if (email != null && !email.isBlank()) {
            builder.set("email", email);
        }
        if (plan != null && !plan.isBlank()) {
            builder.set("plan", plan);
        }

        LDContext context = builder.build();
        return ldClient.boolVariationDetail(newApiResponseFlagKey, context, false);
    }

    public boolean isSdkInitialized() {
        return ldClient.isInitialized();
    }
}
