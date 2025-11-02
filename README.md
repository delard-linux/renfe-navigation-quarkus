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

