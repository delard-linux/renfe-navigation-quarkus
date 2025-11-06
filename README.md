# Renfe Navigation Quarkus

REST API microservice developed with Quarkus for Renfe train search, implemented using hexagonal architecture.

## Architecture

The project follows hexagonal architecture (Ports & Adapters) with the following structure:

```
src/main/java/com/renfe/navigation/
├── domain/                          # Domain layer (core)
│   ├── model/                       # Domain entities
│   │   ├── Train.java
│   │   ├── FareOption.java
│   │   ├── TrainsResponse.java
│   │   └── FlowResponse.java
│   └── port/                        # Ports (interfaces)
│       ├── input/                   # Input ports (use cases)
│       │   ├── SearchTrainsUseCase.java
│       │   └── SearchTrainsFlowUseCase.java
│       └── output/                  # Output ports
│           ├── TrainScraperPort.java
│           └── FlowScraperPort.java
├── application/                     # Application layer
│   └── service/                     # Use case implementation
│       ├── SearchTrainsService.java
│       └── SearchTrainsFlowService.java
└── infrastructure/                  # Infrastructure layer (adapters)
    └── adapter/
        ├── input/                   # Input adapters
        │   └── rest/                # REST API
        │       ├── TrainResource.java
        │       ├── dto/             # DTOs for the API
        │       │   ├── TrainDTO.java
        │       │   ├── FareOptionDTO.java
        │       │   ├── TrainsResponseDTO.java
        │       │   └── FlowResponseDTO.java
        │       └── mapper/          # Domain <-> DTO Mappers
        │           └── TrainMapper.java
        └── output/                  # Output adapters
            ├── TrainScraperAdapter.java
            └── FlowScraperAdapter.java
```

## Features

- ✅ Hexagonal architecture (Ports & Adapters)
- ✅ REST API with Quarkus RESTEasy Reactive
- ✅ Integrated OpenAPI/Swagger documentation
- ✅ Parameter validation with Bean Validation
- ✅ Structured logging management
- ✅ Dependency injection with CDI
- ✅ DTOs separated from domain
- ✅ CORS enabled

## Playwright Installation on Windows

To run E2E tests that use Playwright, first make sure you have Java and Maven installed.

### Required Maven Dependencies

Before installing Playwright, make sure your `pom.xml` includes the following dependencies:

#### Playwright Dependency

```xml
<!-- Playwright for Web Scraping -->
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.56.0</version>
</dependency>
```

#### exec-maven-plugin

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.3.0</version>
</plugin>
```

### Install Playwright (browser download)

Run the following Maven command to download Playwright browsers:

#### Bash
```bash
./mvnw org.codehaus.mojo:exec-maven-plugin:3.3.0:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

#### PowerShell
```powershell
./mvnw --% org.codehaus.mojo:exec-maven-plugin:3.3.0:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

This will download the necessary browsers (Chromium, Firefox, WebKit) and their system dependencies to `%USERPROFILE%\AppData\Local\ms-playwright`.

## Endpoints

### GET /trains

Searches for trains between two stations.

**Parameters:**
- `origin` (required): Origin station (e.g., "OURENSE")
- `destination` (required): Destination station (e.g., "MADRID")
- `date_out` (required): Outbound date in YYYY-MM-DD format
- `date_return` (optional): Return date in YYYY-MM-DD format
- `adults` (optional, default=1): Number of adult passengers (1-8)

**Example:**
```bash
curl "http://localhost:8000/trains?origin=OURENSE&destination=MADRID&date_out=2025-11-15&adults=2"
```

### GET /trains-flow

Executes the complete flow from Renfe's main page to the search.

**Parameters:** Same as `/trains`

**Example:**
```bash
curl "http://localhost:8000/trains-flow?origin=OURENSE&destination=MADRID&date_out=2025-11-15"
```

## Requirements

- Java 25+
- Maven 3.8+
- Configure `JAVA_HOME` pointing to your local JDK 25 installation (you can create it in an unversioned `.env` file).

## Execution

### Development mode (with hot reload)

```bash
./mvnw quarkus:dev
```

The application will be available at:
- API: http://localhost:8000
- Swagger UI: http://localhost:8000/swagger-ui
- OpenAPI spec: http://localhost:8000/openapi

### Compile and run

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Compile native image (requires GraalVM)

```bash
./mvnw package -Pnative
./target/renfe-navigation-quarkus-1.0.0-SNAPSHOT-runner
```

## Configuration

Configuration is located in `src/main/resources/application.properties`:

```properties
# HTTP Port
quarkus.http.port=8000

# Log level
quarkus.log.level=INFO
quarkus.log.category."com.renfe".level=INFO

# OpenAPI
quarkus.swagger-ui.path=/swagger-ui
```

> 💡 Create a local `.env` file (excluded from version control) and define `JAVA_HOME=/path/to/your/jdk-25`. The `./mvnw` script will use that variable to run Maven with the correct JDK. If you need to customize Playwright, add variables like `PLAYWRIGHT_BROWSERS_PATH` in the same file.

## Logs

The application includes structured logs at multiple levels:

- `[REQUEST]` - Request start with parameters
- `[SUCCESS]` - Successful completion with metrics
- `[ERROR]` - Errors with stack trace
- `[FLOW REQUEST]` - Flow start
- `[FLOW SUCCESS]` - Successful flow completion
- `[FLOW ERROR]` - Flow errors

## Pending Implementation

The following adapters are created but pending implementation:

- `TrainScraperAdapter`: Train scraping logic (use Playwright or similar)
- `FlowScraperAdapter`: Complete navigation flow logic

Currently they return empty/placeholder responses to allow architecture startup and testing.

## Testing

The project follows a clear separation between **unit tests** (fast, isolated) and **integration tests** (slower, real interactions).

### Test Structure

```
src/test/java/com/delard/renfe/navigation/     # Unit Tests (Maven Surefire)
├── infrastructure/service/                     # Unit tests with mocks
│   └── PlaywrightSearchTrainsServiceTest.java

src/it/java/com/delard/renfe/navigation/      # Integration Tests (Maven Failsafe)
├── application/rest/                           # Integration tests (REST resources)
│   └── TrainResourceIT.java                    # All REST endpoint integration tests
├── infrastructure/service/                     # Integration tests (services)
│   └── PlaywrightSearchTrainsServiceIT.java
├── support/config/                             # Execution profiles and shared configuration
│   ├── PlaywrightDebugNoHeadlessProfile.java
│   └── PlaywrightRealProfile.java
└── support/stub/                               # Stubs and test doubles for external ports
    └── StubTrainScraperAdapter.java
```

### Unit Tests (`src/test/java`)

Unit tests are fast, isolated tests that use mocks to test individual components without external dependencies.

#### Current Unit Tests

**`PlaywrightSearchTrainsServiceTest.java`**
- **Purpose**: Tests the `PlaywrightSearchTrainsService` class in isolation
- **What it tests**:
  - Train search orchestration logic
  - Interaction with mocked dependencies (Playwright, parsers, storage)
  - Form data building and submission
  - Result extraction and transformation
- **Mocked dependencies**:
  - `PlaywrightConfig` - Configuration settings
  - `RenfeCommonService` - Station lookup and date formatting
  - `TrainHtmlParser` - HTML parsing logic
  - `ResponseStorageService` - Response storage
  - `PlaywrightFactory` - Playwright instance creation
  - All Playwright objects (Browser, Page, Context, etc.)
- **Test scenario**: Verifies that when all dependencies return expected values, the service correctly orchestrates the train search flow and returns the expected results

#### Unit Test Characteristics

- ✅ **Fast execution**: Run in milliseconds
- ✅ **Isolated**: No external dependencies (all mocked)
- ✅ **No Quarkus context**: Uses plain JUnit 5 + Mockito
- ✅ **Naming convention**: `*Test.java`
- ✅ **Coverage**: JaCoCo tracks coverage for unit tests only

#### Running Unit Tests

```bash
# Run all unit tests
./mvnw test

# Run specific unit test class
./mvnw test -Dtest=PlaywrightSearchTrainsServiceTest

# Run unit tests with coverage report
./mvnw clean test
```

### Test Coverage with JaCoCo

The project uses **JaCoCo** (Java Code Coverage) to measure unit test coverage. Coverage reports are generated automatically when running unit tests.

#### Coverage Configuration

- **Minimum line coverage**: 60%
- **Minimum branch coverage**: 50%
- **Scope**: Only unit tests (`src/test/java`), excludes integration tests (`src/it/java`)

#### Viewing Coverage Reports

After running unit tests, coverage reports are generated in:

```
target/site/jacoco/index.html
```

**To view the coverage report:**

1. Run unit tests:
   ```bash
   ./mvnw clean test
   ```

2. Open the HTML report:
   ```bash
   # On Linux/Mac
   xdg-open target/site/jacoco/index.html
   
   # On Windows
   start target/site/jacoco/index.html
   ```

3. Or navigate to `target/site/jacoco/index.html` in your browser

#### Coverage Report Structure

The JaCoCo report provides:
- **Overall coverage**: Package-level and class-level coverage metrics
- **Line coverage**: Percentage of lines executed by tests
- **Branch coverage**: Percentage of branches (if/else, switch) covered
- **Missing lines**: Highlighted in red, showing untested code
- **Covered lines**: Highlighted in green

#### Coverage Goals

| Metric | Target | Current |
|--------|--------|---------|
| Line Coverage | ≥ 60% | See report |
| Branch Coverage | ≥ 50% | See report |

**Note**: Coverage thresholds are goals, not enforced. The build will show warnings if coverage is below targets but will not fail. This allows gradual improvement of test coverage.

#### Generating Coverage Report Only

```bash
# Generate coverage report without running tests
./mvnw jacoco:report

# Check coverage thresholds
./mvnw jacoco:check
```

### Integration Tests (`src/it/java`)

Integration tests verify interactions between layers using real Quarkus context and may interact with external services.

#### Current Integration Tests

**`TrainResourceIT.java`**
- **Purpose**: Comprehensive integration tests for REST endpoints with real Quarkus context
- **What it tests**: 
  - HTTP request/response cycle
  - Request validation (Bean Validation)
  - DTO mapping and response structure
  - Error handling (400 errors for invalid inputs)
  - Multiple endpoints (`/trains` and `/trains-flow`)
  - Debug output with formatted JSON responses
- **Test cases**: 7 tests covering valid requests, return dates, validation errors, edge cases, and debug output
- **Uses**: `@QuarkusTest`, REST Assured, test profiles

**`PlaywrightSearchTrainsServiceIT.java`**
- **Purpose**: Integration test of Playwright service with real browser
- **What it tests**: Real Playwright interactions with Renfe website

#### Running Integration Tests

To run integration tests that use Playwright with real browser automation, use one of the following Maven commands according to your terminal:

**Bash:**
```bash
./mvnw verify -Dit.test="com.delard.renfe.navigation.application.rest.TrainResourceIT" -DskipITs=false
```

This will run the integration tests with Playwright, allowing you to observe real browser automation on the Renfe website.


```bash
# Run all integration tests
./mvnw integration-test -DskipTests -DskipITs=false

### Test Execution Commands Summary

```bash
# Unit tests only (with coverage)
./mvnw clean test

# Integration tests only (skip unit tests)
./mvnw integration-test -DskipTests -DskipITs=false

# Both unit and integration tests
./mvnw verify -DskipITs=false

# Generate coverage report
./mvnw jacoco:report

# Check coverage thresholds
./mvnw jacoco:check
```

### Best Practices

1. **Write unit tests first**: Fast feedback during development
2. **Mock external dependencies**: Keep unit tests isolated and fast
3. **Use integration tests sparingly**: Only for critical paths and layer interactions
4. **Maintain coverage**: Aim for at least 60% line coverage in unit tests
5. **Review coverage reports**: Identify untested code paths regularly
6. **Keep tests independent**: Each test should run independently
7. **Use descriptive test names**: Test names should describe what they verify

## Technologies

- Quarkus 3.27.0 (LTS)
- Java 25
- RESTEasy Reactive + Jackson
- SmallRye OpenAPI
- Hibernate Validator
- JBoss Logging
- JaCoCo 0.8.14 (Code Coverage)

## License

MIT

## Troubleshooting Playwright

If you encounter errors when running tests with Playwright, verify the following:

1. **Browsers not installed**: Run the Playwright installation command shown above.

2. **Timeout in tests**: If tests fail due to timeout, verify that browsers are correctly installed and that there are no processes blocked by antivirus/firewall.

3. **Incompatible version**: Make sure the Playwright version in `pom.xml` matches the installed browsers. If you change the version, reinstall the browsers.

4. **Environment variable (optional)**: If you prefer to use a custom location for browsers, you can configure:
   ```cmd
   set PLAYWRIGHT_BROWSERS_PATH=C:\custom\path\ms-playwright
   ```
