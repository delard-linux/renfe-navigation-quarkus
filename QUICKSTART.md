# Quick Start Guide - Renfe Navigation Quarkus

## Inicio Rápido

```bash
# 1. Compilar el proyecto
mvn clean package -DskipTests

# 2. Ejecutar en modo desarrollo (con hot reload)
mvn quarkus:dev

# 3. Acceder a la aplicación
http://localhost:8000
```

## Endpoints Disponibles

### 1. Buscar Trenes
```bash
GET /trains?origin=OURENSE&destination=MADRID&date_out=2025-12-15&adults=1
```

### 2. Flujo Completo
```bash
GET /trains-flow?origin=OURENSE&destination=MADRID&date_out=2025-12-15&adults=1
```

### 3. Documentación API
- Swagger UI: http://localhost:8000/swagger-ui
- OpenAPI Spec: http://localhost:8000/openapi

## Estructura del Proyecto

```
├── domain/                  # Núcleo del negocio
│   ├── model/              # Entidades
│   └── port/               # Interfaces (contratos)
│       ├── input/          # Casos de uso
│       └── output/         # Servicios externos
├── application/            # Lógica de aplicación
│   └── service/            # Implementación de casos de uso
└── infrastructure/         # Detalles técnicos
    └── adapter/
        ├── input/          # REST API
        └── output/         # Implementaciones externas
```

## Pendiente de Implementar

Los siguientes adaptadores están creados, pero vacíos (retornan datos placeholder):

1. **TrainScraperAdapter** - `src/.../infrastructure/adapter/output/TrainScraperAdapter.java`
   - Implementar scraping de trenes con Playwright/Selenium

2. **FlowScraperAdapter** - `src/.../infrastructure/adapter/output/FlowScraperAdapter.java`
   - Implementar navegación completa por el sitio de Renfe

## Testing

```bash
# Ejecutar tests
mvn test

# Test de un endpoint específico
mvn test -Dtest=TrainResourceTest#testGetTrainsEndpoint
```

## Compilación para Producción

```bash
# JAR normal
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar

# Imagen nativa (requiere GraalVM)
mvn package -Pnative
./target/renfe-navigation-quarkus-1.0.0-SNAPSHOT-runner
```

## Docker

```bash
# Construir imagen
docker build -f src/main/docker/Dockerfile.jvm -t renfe-navigation .

# Ejecutar contenedor
docker run -p 8000:8000 renfe-navigation
```

## Configuración

Editar `src/main/resources/application.properties`:

```properties
# Puerto
quarkus.http.port=8000

# Nivel de logs
quarkus.log.category."com.renfe".level=INFO
```

## Logs

El sistema genera logs estructurados:
- `[REQUEST]` - Inicio de petición
- `[SUCCESS]` - Operación exitosa
- `[ERROR]` - Errores
- `[FLOW REQUEST]` / `[FLOW SUCCESS]` / `[FLOW ERROR]` - Operaciones de flujo

## Troubleshooting

### Error: Puerto 8000 ya en uso
```properties
# Cambiar en application.properties
quarkus.http.port=8080
```

### Error: Java version
```bash
# Requiere Java 17+
java -version
```

### Error de compilación
```bash
# Limpiar y recompilar
mvn clean install -U
```

## Próximos Pasos

1. Implementar `TrainScraperAdapter` con tu lógica de scraping
2. Implementar `FlowScraperAdapter` con tu navegación
3. Añadir tests unitarios para los servicios
4. Configurar CI/CD
5. Añadir métricas y monitoreo

## Referencias

- [Documentación Quarkus](https://quarkus.io/guides/)
- [Arquitectura Hexagonal](./ARCHITECTURE.md)
- [Ejemplos de uso](./EXAMPLES.md)
- [README completo](./README.md)

