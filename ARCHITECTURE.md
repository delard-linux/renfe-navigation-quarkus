# Arquitectura Hexagonal - Renfe Navigation Quarkus

## Diagrama de Capas

```
┌─────────────────────────────────────────────────────────────────┐
│                     ADAPTADORES DE ENTRADA                      │
│  (Infrastructure Layer - Input Adapters)                        │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐     │
│  │         REST API (TrainResource)                       │     │
│  │  - GET /trains                                         │     │
│  │  - GET /trains-flow                                    │     │
│  │  - DTOs, Mappers, Validations                          │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     CAPA DE APLICACIÓN                          │
│  (Application Layer - Use Cases)                                │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  SearchTrainsService                                   │     │
│  │  SearchTrainsFlowService                               │     │
│  │  - Orquestación de lógica de negocio                   │     │
│  │  - Logging de operaciones                              │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       CAPA DE DOMINIO                           │
│  (Domain Layer - Core Business Logic)                           │
│                                                                 │
│  ┌─────────────────────┐        ┌─────────────────────────┐     │
│  │   Ports (Input)     │        │    Ports (Output)       │     │
│  │                     │        │                         │     │
│  │  SearchTrainsUseCase│        │  TrainScraperPort       │     │
│  │  SearchTrainsFlow   │        │  FlowScraperPort        │     │
│  │  UseCase            │        │                         │     │
│  └─────────────────────┘        └─────────────────────────┘     │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐     │
│  │              Domain Models                             │     │
│  │  - Train                                               │     │
│  │  - FareOption                                          │     │
│  │  - TrainsResponse                                      │     │
│  │  - FlowResponse                                        │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    ADAPTADORES DE SALIDA                        │
│  (Infrastructure Layer - Output Adapters)                       │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  TrainScraperAdapter (pendiente implementación)        │     │
│  │  FlowScraperAdapter (pendiente implementación)         │     │
│  │  - Integración con Renfe                               │     │
│  │  - Web scraping / API calls                            │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

## Flujo de Datos

1. **Request HTTP** → TrainResource (REST Controller)
2. **Validación** → Bean Validation en parámetros
3. **Mapping** → DTO a Domain Models
4. **Use Case** → SearchTrainsService (Application Layer)
5. **Domain Logic** → Procesamiento en capa de dominio
6. **Port Output** → Llamada a TrainScraperAdapter
7. **External System** → Renfe website/API
8. **Response** → Mapeo Domain → DTO → JSON

## Principios de Arquitectura Hexagonal

### ✅ Independencia del Framework
El dominio no depende de Quarkus ni de ningún framework externo.

### ✅ Testeable
Cada capa puede ser testeada independientemente usando mocks de los puertos.

### ✅ Independencia de UI
La API REST es solo un adaptador. Se pueden añadir otros (GraphQL, gRPC, etc.).

### ✅ Independencia de BD/Servicios Externos
Los adaptadores de salida son intercambiables sin afectar el dominio.

### ✅ Reglas de Negocio en el Centro
Todo el conocimiento del negocio está en la capa de dominio.

## Tecnologías por Capa

| Capa           | Tecnologías                                         |
|----------------|-----------------------------------------------------|
| **Entrada**    | JAX-RS, RESTEasy Reactive, Bean Validation, OpenAPI |
| **Aplicación** | CDI, JBoss Logging                                  |
| **Dominio**    | Java POJOs (sin dependencias)                       |
| **Salida**     | A implementar (Playwright, HttpClient, etc.)        |

## Ventajas de esta Arquitectura

1. **Mantenibilidad**: Cambios en infraestructura no afectan al dominio
2. **Testabilidad**: Fácil crear tests unitarios con mocks
3. **Escalabilidad**: Se pueden añadir nuevos adaptadores sin cambiar el core
4. **Claridad**: Separación clara de responsabilidades
5. **Reutilización**: La lógica de negocio es independiente de la tecnología

