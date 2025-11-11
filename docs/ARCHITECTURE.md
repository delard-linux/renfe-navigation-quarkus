# Hexagonal Architecture

[← Back to README](../README.md)

## Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                     INPUT ADAPTERS                                │
│  (Infrastructure Layer - Input Adapters)                          │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐      │
│  │         REST API (TrainResource)                       │      │
│  │  - GET /trains                                         │      │
│  │  - DTOs, Mappers, Validations                          │      │
│  └────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                            │
│  (Application Layer - Use Cases)                                 │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐      │
│  │  SearchTrainsService                                   │      │
│  │  - Business logic orchestration                        │      │
│  │  - Operation logging                                   │      │
│  └────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                               │
│  (Domain Layer - Core Business Logic)                            │
│                                                                   │
│  ┌─────────────────────┐        ┌─────────────────────────┐      │
│  │   Ports (Input)     │        │    Ports (Output)       │      │
│  │  SearchTrainsUseCase│        │  TrainScraperPort       │      │
│  └─────────────────────┘        └─────────────────────────┘      │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐      │
│  │              Domain Models                             │      │
│  │  - Train, FareOption, TrainsResponse                   │      │
│  └────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    OUTPUT ADAPTERS                               │
│  (Infrastructure Layer - Output Adapters)                        │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐      │
│  │  TrainScraperAdapter (pending implementation)          │      │
│  │  - Renfe integration                                   │      │
│  │  - Web scraping / API calls                            │      │
│  └────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

1. HTTP request → `TrainResource` (REST)
2. Validation → Bean Validation
3. Mapping → DTO ↔ Domain
4. Use case → `SearchTrainsService`
5. Domain logic
6. Output port → `TrainScraperPort`
7. External system (Renfe)
8. Response mapping to JSON

## Principles

- Framework independent domain
- Highly testable with ports and mocks
- Adapters replaceable without touching the core
- Clear separation of concerns

## Technologies per Layer

| Layer           | Technologies                                         |
|-----------------|------------------------------------------------------|
| Input           | JAX-RS, RESTEasy Reactive, Bean Validation, OpenAPI |
| Application     | CDI, JBoss Logging                                  |
| Domain          | Plain Java (no external deps)                        |
| Output          | To be implemented (Playwright/HTTP client, etc.)     |


