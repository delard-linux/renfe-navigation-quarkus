# Quick Start Guide - Renfe Navigation Quarkus

## Quick Start

```bash
# 1. Build the project
./mvnw clean package -DskipTests

# 2. Run in development mode (with hot reload)
./mvnw quarkus:dev

# 3. Access the application
http://localhost:8000
```

> 💡 Before running the commands, create a `.env` file (not versioned) with `JAVA_HOME=/path/to/your/jdk-25` so the wrapper uses the correct JDK.

## Available Endpoints

### 1. Search Trains
```bash
GET /trains?origin=OURENSE&destination=MADRID&date_out=2025-12-15&adults=1
```

### 2. API Documentation
- Swagger UI: http://localhost:8000/swagger-ui
- OpenAPI Spec: http://localhost:8000/openapi

## Project Structure

```
├── domain/                  # Business core
│   ├── model/              # Entities
│   └── port/               # Interfaces (contracts)
│       ├── input/          # Use cases
│       └── output/         # External services
├── application/            # Application logic
│   └── service/            # Use case implementation
└── infrastructure/         # Technical details
    └── adapter/
        ├── input/          # REST API
        └── output/         # External implementations
```

## Pending Implementation

The following adapter is created but empty (returns placeholder data):

1. **TrainScraperAdapter** - `src/.../infrastructure/adapter/output/TrainScraperAdapter.java`
   - Implement train scraping with Playwright/Selenium

## Testing

```bash
# Run tests
./mvnw test

# Test a specific endpoint
./mvnw test -Dtest=PlaywrightSearchTrainsServiceTest#searchTrainsReturnsOutboundResults
```

## Production Build

```bash
# Normal JAR
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar

# Native image (requires GraalVM)
./mvnw package -Pnative
./target/renfe-navigation-quarkus-1.0.0-SNAPSHOT-runner
```


## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Port
quarkus.http.port=8000

# Log level
quarkus.log.category."com.renfe".level=INFO
```

## Logs

The system generates structured logs:
- `[REQUEST]` - Request start
- `[SUCCESS]` - Successful operation
- `[ERROR]` - Errors

## Troubleshooting

### Error: Port 8000 already in use
```properties
# Change in application.properties
quarkus.http.port=8080
```

### Error: Java version
```bash
# Requires Java 25+
java -version
```

### Compilation error
```bash
# Clean and rebuild
./mvnw clean install -U
```

## Next Steps

1. Implement `TrainScraperAdapter` with your scraping logic
2. Add unit tests for services
3. Configure CI/CD
4. Add metrics and monitoring

## References

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Architecture](./ARCHITECTURE.md)
- [Usage Examples](./EXAMPLES.md)
- [Complete README](./README.md)
