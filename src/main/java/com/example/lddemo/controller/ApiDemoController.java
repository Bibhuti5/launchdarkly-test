package com.example.lddemo.controller;

import com.example.lddemo.service.FeatureFlagService;
import com.launchdarkly.sdk.EvaluationDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApiDemoController {

    private final FeatureFlagService featureFlagService;

    public ApiDemoController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam(defaultValue = "anonymous-user") String userId,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "free") String plan) {

        EvaluationDetail<Boolean> evaluation =
                featureFlagService.evaluateNewApiResponse(userId, email, plan);

        boolean isNewResponseEnabled = Boolean.TRUE.equals(evaluation.getValue());
        String evaluationReason = evaluation.getReason() != null
                ? evaluation.getReason().getKind().name()
                : "UNKNOWN";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("userId", userId);
        response.put("flagEnabled", isNewResponseEnabled);
        response.put("evaluationReason", evaluationReason);

        if (isNewResponseEnabled) {
            response.put("version", "v2");
            response.put("status", "FEATURE_ON");
            response.put("message", "Enhanced v2 API payload served via LaunchDarkly!");
            response.put("extraData", Map.of(
                    "discount", "20%",
                    "priority", "HIGH",
                    "tier", plan
            ));
        } else {
            response.put("version", "v1");
            response.put("status", "FEATURE_OFF");
            response.put("message", "Standard legacy v1 API response.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "launchDarklyInitialized", featureFlagService.isSdkInitialized()
        ));
    }
}
