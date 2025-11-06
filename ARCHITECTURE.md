# Hexagonal Architecture - Renfe Navigation Quarkus

## Layer Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     INPUT ADAPTERS                                │
│  (Infrastructure Layer - Input Adapters)                          │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐     │
│  │         REST API (TrainResource)                       │     │
│  │  - GET /trains                                         │     │
│  │  - DTOs, Mappers, Validations                          │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                            │
│  (Application Layer - Use Cases)                                 │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  SearchTrainsService                                   │     │
│  │  - Business logic orchestration                        │     │
│  │  - Operation logging                                   │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                                │
│  (Domain Layer - Core Business Logic)                             │
│                                                                   │
│  ┌─────────────────────┐        ┌─────────────────────────┐       │
│  │   Ports (Input)    │        │    Ports (Output)       │       │
│  │                     │        │                         │       │
│  │  SearchTrainsUseCase│        │  TrainScraperPort       │       │
│  └─────────────────────┘        └─────────────────────────┘       │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐     │
│  │              Domain Models                             │     │
│  │  - Train                                               │     │
│  │  - FareOption                                          │     │
│  │  - TrainsResponse                                      │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    OUTPUT ADAPTERS                               │
│  (Infrastructure Layer - Output Adapters)                        │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  TrainScraperAdapter (pending implementation)          │     │
│  │  - Renfe integration                                   │     │
│  │  - Web scraping / API calls                            │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

1. **HTTP Request** → TrainResource (REST Controller)
2. **Validation** → Bean Validation on parameters
3. **Mapping** → DTO to Domain Models
4. **Use Case** → SearchTrainsService (Application Layer)
5. **Domain Logic** → Processing in domain layer
6. **Output Port** → Call to TrainScraperAdapter
7. **External System** → Renfe website/API
8. **Response** → Domain → DTO → JSON mapping

## Hexagonal Architecture Principles

### ✅ Framework Independence
The domain does not depend on Quarkus or any external framework.

### ✅ Testable
Each layer can be tested independently using port mocks.

### ✅ UI Independence
The REST API is just an adapter. Others can be added (GraphQL, gRPC, etc.).

### ✅ Database/External Services Independence
Output adapters are interchangeable without affecting the domain.

### ✅ Business Rules at the Center
All business knowledge is in the domain layer.

## Technologies by Layer

| Layer           | Technologies                                         |
|-----------------|------------------------------------------------------|
| **Input**       | JAX-RS, RESTEasy Reactive, Bean Validation, OpenAPI |
| **Application** | CDI, JBoss Logging                                  |
| **Domain**      | Java POJOs (no dependencies)                        |
| **Output**      | To be implemented (Playwright, HttpClient, etc.)    |

## Advantages of this Architecture

1. **Maintainability**: Infrastructure changes do not affect the domain
2. **Testability**: Easy to create unit tests with mocks
3. **Scalability**: New adapters can be added without changing the core
4. **Clarity**: Clear separation of responsibilities
5. **Reusability**: Business logic is independent of technology
