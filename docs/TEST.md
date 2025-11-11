# Testing and Debugging

[← Back to README](../README.md)

This project separates fast unit tests, Quarkus integration tests, and end-to-end tests.

## Test layout

```
src/test/java/unit/         # Unit tests (JUnit + Mockito) - fast, isolated
src/test/java/integration/  # Integration tests (@QuarkusTest) - debuggable
src/test/java/e2e/          # E2E tests (@QuarkusIntegrationTest) - packaged app
```

## Running tests with Maven

- Unit tests only
```bash
./mvnw -Dtest='*Test' test
```

- Integration tests (@QuarkusTest) only
```bash
./mvnw -Dtest='*IT' test
```

- E2E tests (@QuarkusIntegrationTest)
```bash
./mvnw failsafe:integration-test -DskipTests -DskipITs=false -Dit.test=TrainResourceE2E
```

- All (unit + integration)
```bash
./mvnw verify -DskipITs=false
```

## Code coverage (unit tests)

JaCoCo reports are generated during unit tests:
```bash
./mvnw clean test
xdg-open target/site/jacoco/index.html  # Linux/Mac
```

## Debugging @QuarkusTest from VS Code

1. Use the task:
   - "maven: debug QuarkusTest (Surefire)" and enter your test class (e.g., `PlaywrightSearchTrainsServiceIT`)
2. Attach the debugger:
   - Launch config: "Attach to Quarkus Test Debugger" (port 5005)

Alternatively, from terminal:
```bash
./mvnw surefire:test -Dtest=PlaywrightSearchTrainsServiceIT -Dmaven.surefire.debug
# Then attach VS Code debugger to port 5005
```

## Test configuration

- Unit tests run without Quarkus and use mocks (no profile required)
- Integration tests use a dedicated test profile:
  - `@TestProfile(PlaywrightIntegrationTestProfile.class)`
  - Loads `src/test/resources/application-integration.properties`
  - You can toggle Playwright headless/slow-mo/timeouts there for debugging

Key Playwright options in the integration properties:
```properties
playwright.headless=false
playwright.slow-mo=4000
playwright.timeout-navigation-ms=60000
```

## Command reference (Maven/Quarkus)

Common commands for different test types and reports:

| Description | Command |
| :--- | :--- |
| Run a specific IT and generate site report | `./mvnw clean verify site -Dit.test=PlaywrightSearchTrainsServiceIT -DskipTests=true -DskipITs=false` |
| Run only unit and local integration tests (Surefire) | `./mvnw test -DskipITs=true` |
| Run only E2E tests (Failsafe) | `./mvnw verify -DskipTests=true -DskipITs=false` |
| Run all tests (unit + integration + e2e) | `./mvnw verify` |
| Run only unit tests | `./mvnw test -DskipITs=true -Dtest="**/*Test.java" -Dexcludes="**/*IT.java"` |
| Run all tests handled by Surefire | `./mvnw test -DskipITs=true -Dtest="**/*Test.java"` |
| Run unit tests, ignore failures, and generate site | `./mvnw clean test site -DskipITs=true -Dtest="**/*Test.java" -Dexcludes="**/*IT.java" -Dmaven.test.failure.ignore=true` |
| Run all tests and check JaCoCo | `./mvnw clean verify jacoco:check` |
| Run a specific @QuarkusTest in debug | `./mvnw surefire:test -Dtest=PlaywrightSearchTrainsServiceIT -Dmaven.surefire.debug` |

### Notes on IDE debugging

When running in debug (e.g., with `-Dmaven.surefire.debug`), Maven will print:

```
Listening for transport dt_socket at address: 5005
```

Then attach VS Code using the launch config "Attach to Quarkus Test Debugger" (port 5005).

