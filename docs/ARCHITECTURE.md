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
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐      │
│  │         MCP Server (TrainsMcpResource)                 │      │
│  │  - Tool: search_trains                                 │      │
│  │  - SSE Transport, JSON-RPC 2.0                         │      │
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

### REST API Flow
1. HTTP request → `TrainResource` (REST)
2. Validation → Bean Validation
3. Mapping → DTO ↔ Domain
4. Use case → `SearchTrainsService`
5. Domain logic
6. Output port → `TrainScraperPort`
7. External system (Renfe)
8. Response mapping to JSON

### MCP Server Flow
1. MCP request → `TrainsMcpResource` (SSE + JSON-RPC 2.0)
2. Tool call validation
3. Mapping → Domain parameters
4. Use case → `SearchTrainsService`
5. Domain logic
6. Output port → `TrainScraperPort`
7. External system (Renfe)
8. Response mapping to MCP tool result

## Principles

- Framework independent domain
- Highly testable with ports and mocks
- Adapters replaceable without touching the core
- Clear separation of concerns

## Technologies per Layer

| Layer           | Technologies                                         |
|-----------------|------------------------------------------------------|
| Input           | JAX-RS, RESTEasy Reactive, Bean Validation, OpenAPI, Quarkus MCP Server SSE |
| Application     | CDI, JBoss Logging                                  |
| Domain          | Plain Java (no external deps)                        |
| Output          | Playwright 1.56 (Chromium), Jsoup 1.18, HTTP Client, ConcurrentHashMap (Cache) |

## See Also

- **[Integration Examples](./INTEGRATION_EXAMPLES.md)**: REST API and MCP Server usage examples
- **[MCP Setup](./MCP_SETUP.md)**: Model Context Protocol server configuration for AI tools
- **[Testing](./TEST.md)**: Unit and integration testing guide


