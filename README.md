# Renfe Navigation Quarkus

REST API microservice built with Quarkus to search Renfe trains, following Hexagonal Architecture.

Helpful links:
- Architecture details: [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)
- API usage examples: [docs/EXAMPLES.md](./docs/EXAMPLES.md)
- Testing and debugging: [docs/TEST.md](./docs/TEST.md)
- Backlog / next steps: [docs/BACKLOG.md](./docs/BACKLOG.md)

## Architecture
See [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) for the layer diagram, data flow, and principles.

## Features

- ✅ Hexagonal architecture (Ports & Adapters)
- ✅ REST API with Quarkus RESTEasy Reactive
- ✅ Integrated OpenAPI/Swagger documentation
- ✅ Parameter validation with Bean Validation
- ✅ Structured logging management
- ✅ Dependency injection with CDI
- ✅ DTOs separated from domain
- ✅ CORS enabled

## Quick Start

```bash
# 1. Build the project
./mvnw clean package -DskipTests

# 2. Run in development mode (hot reload)
./mvnw quarkus:dev

# 3. Access the application
http://localhost:8000
```

Playwright installation and all test instructions have moved to [docs/TEST.md](./docs/TEST.md).

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

## Requirements

- Java 21+
- Maven 3.8+
- Configure `JAVA_HOME` pointing to your local JDK 21 installation (you can create it in an unversioned `.env` file).

## Environment Variables

The project uses environment variables for configuration. Create a `.env` file in the project root (this file is excluded from version control) to configure these variables.

### Required Environment Variables

#### `JAVA_HOME`
**Required**: Path to your JDK 21 installation.

**Example:**
```bash
# Linux/Mac
JAVA_HOME=/usr/lib/jvm/java-21-openjdk

# Windows
JAVA_HOME=C:\Program Files\Java\jdk-21
```

The Maven wrapper (`./mvnw`) automatically loads this variable from the `.env` file to use the correct JDK version.

### Optional Environment Variables

#### `PLAYWRIGHT_BROWSERS_PATH`
**Optional**: Custom path for Playwright browser installation.

If not set, Playwright will use the default location:
- **Linux/Mac**: `~/.cache/ms-playwright`
- **Windows**: `%USERPROFILE%\AppData\Local\ms-playwright`

**Example:**
```bash
# Linux/Mac
PLAYWRIGHT_BROWSERS_PATH=/custom/path/ms-playwright

# Windows
PLAYWRIGHT_BROWSERS_PATH=C:\custom\path\ms-playwright
```

### Example `.env` File

Create a `.env` file in the project root with the following content:

```bash
# Required: Java 21 installation path
JAVA_HOME=/usr/lib/jvm/java-21-openjdk

# Optional: Custom Playwright browsers path
# PLAYWRIGHT_BROWSERS_PATH=/custom/path/ms-playwright
```

> **Note**: The `.env` file is automatically excluded from version control via `.gitignore`. Never commit sensitive information or local paths to the repository.

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

> 💡 See the [Environment Variables](#environment-variables) section for detailed configuration instructions.

## Logs

The application includes structured logs at multiple levels:

- `[REQUEST]` - Request start with parameters
- `[SUCCESS]` - Successful completion with metrics
- `[ERROR]` - Errors with stack trace

## Pending Implementation

The following adapter is created but pending implementation:

- `TrainScraperAdapter`: Train scraping logic (use Playwright or similar)

Currently it returns empty/placeholder responses to allow architecture startup and testing.

## Testing
See [docs/TEST.md](./docs/TEST.md) for commands, structure, and VS Code debug instructions for `@QuarkusTest`.

## Technologies

- Quarkus 3.27.0 (LTS)
- Java 21
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
