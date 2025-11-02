# Renfe Navigation Quarkus

Microservicio REST API desarrollado con Quarkus para búsqueda de trenes de Renfe, implementado usando arquitectura hexagonal.

## Arquitectura

El proyecto sigue la arquitectura hexagonal (Ports & Adapters) con la siguiente estructura:

```
src/main/java/com/renfe/navigation/
├── domain/                          # Capa de dominio (núcleo)
│   ├── model/                       # Entidades del dominio
│   │   ├── Train.java
│   │   ├── FareOption.java
│   │   ├── TrainsResponse.java
│   │   └── FlowResponse.java
│   └── port/                        # Puertos (interfaces)
│       ├── input/                   # Puertos de entrada (casos de uso)
│       │   ├── SearchTrainsUseCase.java
│       │   └── SearchTrainsFlowUseCase.java
│       └── output/                  # Puertos de salida
│           ├── TrainScraperPort.java
│           └── FlowScraperPort.java
├── application/                     # Capa de aplicación
│   └── service/                     # Implementación de casos de uso
│       ├── SearchTrainsService.java
│       └── SearchTrainsFlowService.java
└── infrastructure/                  # Capa de infraestructura (adaptadores)
    └── adapter/
        ├── input/                   # Adaptadores de entrada
        │   └── rest/                # REST API
        │       ├── TrainResource.java
        │       ├── dto/             # DTOs para la API
        │       │   ├── TrainDTO.java
        │       │   ├── FareOptionDTO.java
        │       │   ├── TrainsResponseDTO.java
        │       │   └── FlowResponseDTO.java
        │       └── mapper/          # Mappers Domain <-> DTO
        │           └── TrainMapper.java
        └── output/                  # Adaptadores de salida
            ├── TrainScraperAdapter.java
            └── FlowScraperAdapter.java
```

## Características

- ✅ Arquitectura hexagonal (Ports & Adapters)
- ✅ API REST con Quarkus RESTEasy Reactive
- ✅ Documentación OpenAPI/Swagger integrada
- ✅ Validación de parámetros con Bean Validation
- ✅ Gestión de logs estructurados
- ✅ Inyección de dependencias con CDI
- ✅ DTOs separados del dominio
- ✅ CORS habilitado

## Endpoints

### GET /trains

Busca trenes entre dos estaciones.

**Parámetros:**
- `origin` (required): Estación de origen (ej: "OURENSE")
- `destination` (required): Estación de destino (ej: "MADRID")
- `date_out` (required): Fecha de ida en formato YYYY-MM-DD
- `date_return` (optional): Fecha de vuelta en formato YYYY-MM-DD
- `adults` (optional, default=1): Número de pasajeros adultos (1-8)

**Ejemplo:**
```bash
curl "http://localhost:8000/trains?origin=OURENSE&destination=MADRID&date_out=2025-11-15&adults=2"
```

### GET /trains-flow

Ejecuta el flujo completo desde la página principal de Renfe hasta la búsqueda.

**Parámetros:** Los mismos que `/trains`

**Ejemplo:**
```bash
curl "http://localhost:8000/trains-flow?origin=OURENSE&destination=MADRID&date_out=2025-11-15"
```

## Requisitos

- Java 17+
- Maven 3.8+

## Ejecución

### Modo desarrollo (con hot reload)

```bash
mvnw quarkus:dev
```

La aplicación estará disponible en:
- API: http://localhost:8000
- Swagger UI: http://localhost:8000/swagger-ui
- OpenAPI spec: http://localhost:8000/openapi

### Compilar y ejecutar

```bash
mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Compilar imagen nativa (requiere GraalVM)

```bash
mvnw package -Pnative
./target/renfe-navigation-quarkus-1.0.0-SNAPSHOT-runner
```

## Configuración

La configuración se encuentra en `src/main/resources/application.properties`:

```properties
# Puerto HTTP
quarkus.http.port=8000

# Nivel de logs
quarkus.log.level=INFO
quarkus.log.category."com.renfe".level=INFO

# OpenAPI
quarkus.swagger-ui.path=/swagger-ui
```

## Logs

La aplicación incluye logs estructurados en múltiples niveles:

- `[REQUEST]` - Inicio de petición con parámetros
- `[SUCCESS]` - Finalización exitosa con métricas
- `[ERROR]` - Errores con stack trace
- `[FLOW REQUEST]` - Inicio de flujo
- `[FLOW SUCCESS]` - Finalización exitosa del flujo
- `[FLOW ERROR]` - Errores en el flujo

## Pendiente de implementación

Los siguientes adaptadores están creados pero pendientes de implementación:

- `TrainScraperAdapter`: Lógica de scraping de trenes (usar Playwright o similar)
- `FlowScraperAdapter`: Lógica del flujo completo de navegación

Actualmente devuelven respuestas vacías/placeholder para permitir el arranque y prueba de la arquitectura.

## Testing

```bash
mvnw test
```

## Docker

Crear imagen Docker:

```bash
mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t renfe-navigation-quarkus .
```

Ejecutar contenedor:

```bash
docker run -p 8000:8000 renfe-navigation-quarkus
```

## Tecnologías

- Quarkus 3.6.4
- Java 17
- RESTEasy Reactive + Jackson
- SmallRye OpenAPI
- Hibernate Validator
- JBoss Logging

## Licencia

MIT

## Running Playwright E2E tests on Windows

This section explains what you need on Windows to run the Playwright-based E2E tests and the exact commands to prepare the environment and run the tests from a cmd.exe prompt.

Checklist (quick)
- Node (LTS) installed and on PATH
- npm / npx available
- Playwright browsers installed (via npx) or a pre-populated browsers path
- Set `PLAYWRIGHT_BROWSERS_PATH` if you preinstalled browsers in a custom location
- Run tests with the `PlaywrightE2eProfile` Quarkus test profile

Prerequisites
- Java 17+ (project requirement)
- Maven available (you already have it)
- Node.js (LTS). You reported:
  - node --version -> v22.14.0
  - npx --version -> 11.6.1
  - npm --version -> 11.6.1

These versions are fine for Playwright. Keep Node on PATH so `npx` can run.

Install Playwright browsers (cmd.exe)

Use the following commands (run from project root or any folder). These install the browser binaries that Playwright needs so the Java runtime does not try to download them at test time.

```cmd
cd C:\PRJS\personal\renfe-navigation-quarkus
npx playwright --version
npx playwright install chromium
```

After successful download, Playwright browsers will be placed in your user local folder (for example: `C:\Users\<you>\AppData\Local\ms-playwright`).

Set PLAYWRIGHT_BROWSERS_PATH for tests (cmd.exe)

Point Playwright Java to the installed browsers to prevent runtime download attempts:

```cmd
set PLAYWRIGHT_BROWSERS_PATH=C:\Users\<your-user>\AppData\Local\ms-playwright
```

Run the E2E test (headless) with the provided Quarkus test profile

The repository includes a test profile `PlaywrightE2eProfile` (in `src/test/java`) that sets Playwright to headless mode and configures timeouts. Run the single test like this (cmd.exe):

```cmd
cd C:\PRJS\personal\renfe-navigation-quarkus
set PLAYWRIGHT_BROWSERS_PATH=C:\Users\<your-user>\AppData\Local\ms-playwright
mvn -Dtest=TrainResourceE2ETest -Dquarkus.test.profile=PlaywrightE2eProfile test
```

Notes
- If you want to observe the browser UI set `playwright.headless=false` in the test profile or use a dedicated profile for interactive runs.
- If Maven/Quarkus spawns Playwright and it attempts to download browsers at runtime, pre-installing the browsers as above avoids long delays or failures.

Troubleshooting (errors you may encounter)

1) "Failed to create driver" or InterruptedException during driver install
- Cause: Playwright Java tried to download and install browsers during runtime and the process was interrupted or blocked.
- Fixes:
  - Run `npx playwright install chromium` before the test so the driver/browsers are already present.
  - Set `PLAYWRIGHT_BROWSERS_PATH` to the folder containing the installed browsers.
  - Ensure antivirus/firewall is not blocking the temporary node process.
  - If you see a Node module not found error in a temp dir, remove stale temp directories under `%TEMP%` that start with `playwright-java-` and try again.

2) "Failed to read message" or IPC errors between Java and the Playwright node process
- Cause: The Playwright-native child process may crash or get killed (permissions, incompatible Node version, missing browser binary).
- Fixes:
  - Ensure the installed browsers version matches the Playwright Java version (use `npx playwright --version` and the Playwright Java dependency in `pom.xml`).
  - Set `PLAYWRIGHT_BROWSERS_PATH` as above.
  - Verify Node is accessible on PATH and is a supported version (Node 16+). You have Node v22 — that is supported in practice, but if you see incompatibilities try a Node LTS like 18 or 20.

3) SocketTimeoutException (test HTTP client times out)
- Cause: The server may have crashed while the test HTTP client waited for a response (e.g. Playwright failed while running the scraper and Quarkus shut down the request). The test client default timeout is limited.
- Fixes:
  - Pre-install browsers and set PLAYWRIGHT_BROWSERS_PATH to avoid long install timeouts.
  - Increase test-side HTTP timeout if needed (adjust RestAssured configuration or the test to wait longer).

Advanced tips
- To avoid Playwright Java attempting to download browsers at runtime entirely, set the environment variable at install-time:

```cmd
set PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
```

and then preinstall browsers using `npx playwright install`.

- If you run tests in CI (GitHub Actions, GitLab), prefer setting up the Playwright browsers in the CI image ahead of the test run or use the official Playwright action/container to ensure compatibility.

Summary
- You already have node/npx/npm installed — next steps are:
  1) Run `npx playwright install chromium` once to fetch browsers.
  2) Set `PLAYWRIGHT_BROWSERS_PATH` to the installation folder.
  3) Run the Maven test with the `PlaywrightE2eProfile`.

If you want, I can also:
- Add a small script `scripts/install-playwright.cmd` to automate these steps on Windows.
- Try to run the real Playwright-based E2E test here again and investigate remaining IPC errors further (requires iterating on Node/Playwright versions and env).
