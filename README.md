# LaunchDarkly + Spring Boot Demo

A complete, runnable Spring Boot application demonstrating real-time feature flag evaluation with LaunchDarkly.

## Project Structure

- `LaunchDarklyConfig.java` – Configures a singleton `LDClient` bean (supports both live streaming and offline mode).
- `FeatureFlagService.java` – Builds an `LDContext` with user attributes (`userId`, `email`, `plan`) and evaluates flags with `boolVariationDetail`.
- `ApiDemoController.java` – Exposes `GET /api/v1/data` and `GET /api/v1/health` to demonstrate dynamic response switching when toggling flags in the LaunchDarkly portal.
- `ApiDemoControllerTest.java` – Offline integration tests powered by LaunchDarkly's `TestData` source.

## Quick Start

### 1. Create a Flag in LaunchDarkly
1. Log in to your free LaunchDarkly account.
2. Create a **Boolean** flag with key: `enable-new-api-response`.
3. Copy your **Server-side SDK key** (`sdk-...`) from **Project settings -> Environments -> Test**.

### 2. Run the Application
```bash
export LAUNCHDARKLY_SDK_KEY="sdk-your-actual-sdk-key-here"
mvn spring-boot:run
```

*(Tip: To run locally without a LaunchDarkly key yet, run `LAUNCHDARKLY_OFFLINE=true mvn spring-boot:run`.)*

### 3. Test Dynamic Response Toggling
Call the endpoint while the flag is **OFF** in the portal:
```bash
curl "http://localhost:8080/api/v1/data?userId=user-123&email=tester@gmail.com&plan=pro"
```

Toggle `enable-new-api-response` **ON** in the LaunchDarkly portal, click **Review and save**, and run the exact same `curl` command again without restarting the server—the response switches from `v1` to `v2` within ~100–200ms.

### 4. Run Offline Automated Tests
```bash
mvn test
```
"# launchdarkly-test" 
