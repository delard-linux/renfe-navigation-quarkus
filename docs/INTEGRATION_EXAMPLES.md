# Integration Examples

[← Back to README](../README.md)

This document provides examples for integrating with the Renfe Navigation API through both **REST API** and **MCP Server** interfaces.

## Prerequisites

Start server in dev mode:

```bash
./mvnw quarkus:dev
```

The application will be available at:
- API: http://localhost:8999
- Swagger UI: http://localhost:8999/swagger-ui
- OpenAPI spec: http://localhost:8999/openapi
- MCP Server: http://localhost:8999/mcp

---

## REST API Examples

### 1. Simple train search (outbound only)

```bash
curl -X GET "http://localhost:8999/trains?origin=MADRID+%28TODAS%29&destination=BARCELONA+%28TODAS%29&date_out=2025-12-15&adults=1"
```

### 2. Search with outbound and return

```bash
curl -X GET "http://localhost:8999/trains?origin=BARCELONA+%28TODAS%29&destination=VALENCIA+%28TODAS%29&date_out=2025-12-20&date_return=2025-12-27&adults=2"
```

### 3. Search with multiple passengers

```bash
curl -X GET "http://localhost:8999/trains?origin=MADRID+%28TODAS%29&destination=SEVILLA&date_out=2025-11-25&adults=4"
```

### 4. With JSON formatting (using jq)

```bash
curl -s "http://localhost:8999/trains?origin=OURENSE&destination=MADRID+%28TODAS%29&date_out=2025-12-15&adults=1" | jq
```

### Example REST Response

```json
{
  "origin": "OURENSE",
  "destination": "MADRID",
  "date_out": "2025-12-15",
  "date_return": null,
  "adults": "1",
  "trains_out": [
    {
      "train_id": "12345",
      "service_type": "AVE",
      "departure_time": "08:30",
      "arrival_time": "12:15",
      "duration": "3h 45m",
      "price_from": 45.50,
      "currency": "EUR",
      "fares": [
        {
          "name": "Basic",
          "price": 45.50,
          "currency": "EUR",
          "code": "BAS",
          "tp_enlace": "12345-BAS",
          "features": ["Cancellation fee", "Change fee"]
        }
      ],
      "badges": ["Fast", "WiFi"],
      "accessible": true,
      "eco_friendly": true
    }
  ],
  "trains_return": null
}
```

---

## MCP Server Examples

The MCP (Model Context Protocol) server provides AI tools access to the train search functionality. It uses Server-Sent Events (SSE) and JSON-RPC 2.0.

### Configuration

First, configure MCP in your AI tool. See [MCP_SETUP.md](./MCP_SETUP.md) for detailed instructions.

**Cursor configuration example** (`~/.cursor/mcp_settings.json`):

```json
{
  "mcpServers": {
    "renfe-trains": {
      "command": "/usr/lib/jvm/java-21-openjdk/bin/java",
      "args": [
        "-Djava.util.logging.manager=org.jboss.logmanager.LogManager",
        "-jar",
        "/home/user/projects/renfe-navigation-quarkus/target/quarkus-app/quarkus-run.jar"
      ],
      "env": {
        "QUARKUS_PROFILE": "mcp",
        "QUARKUS_LAUNCH_DEVMODE": "false"
      }
    }
  }
}
```

### MCP Tool: search_trains

Once configured, the AI assistant can use the `search_trains` tool directly:

#### Example 1: Simple search

**Prompt to AI:**
```
Search for trains from MADRID to BARCELONA on 2025-12-15 for 1 adult
```

**Tool call executed by AI:**
```json
{
  "tool": "search_trains",
  "arguments": {
    "origin": "MADRID (TODAS)",
    "destination": "BARCELONA (TODAS)",
    "date_out": "2025-12-15",
    "adults": "1"
  }
}
```

#### Example 2: Round trip search

**Prompt to AI:**
```
I need trains from BARCELONA to VALENCIA, departing December 20 and returning December 27, for 2 adults
```

**Tool call executed by AI:**
```json
{
  "tool": "search_trains",
  "arguments": {
    "origin": "BARCELONA (TODAS)",
    "destination": "VALENCIA (TODAS)",
    "date_out": "2025-12-20",
    "date_return": "2025-12-27",
    "adults": "2"
  }
}
```

#### Example 3: Multiple passengers

**Prompt to AI:**
```
Find trains from MADRID to SEVILLA on November 25 for 4 adults
```

**Tool call executed by AI:**
```json
{
  "tool": "search_trains",
  "arguments": {
    "origin": "MADRID (TODAS)",
    "destination": "SEVILLA",
    "date_out": "2025-11-25",
    "adults": "4"
  }
}
```

### MCP Tool Response Format

The MCP server returns the same data structure as the REST API, but wrapped in MCP's tool result format:

```json
{
  "content": [
    {
      "type": "text",
      "text": "{\"origin\":\"OURENSE\",\"destination\":\"MADRID\",\"date_out\":\"2025-12-15\",\"date_return\":null,\"adults\":\"1\",\"trains_out\":[...],\"trains_return\":null}"
    }
  ]
}
```

### Testing MCP Manually

You can test the MCP endpoint directly with HTTP tools:

```bash
# Initialize MCP connection
curl -X POST http://localhost:8999/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "test-client",
        "version": "1.0.0"
      }
    },
    "id": 1
  }'

# Call search_trains tool
curl -X POST http://localhost:8999/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "search_trains",
      "arguments": {
        "origin": "OURENSE",
        "destination": "MADRID",
        "date_out": "2025-12-15",
        "adults": "1"
      }
    },
    "id": 2
  }'
```

---

## Documentation Links

- **OpenAPI/Swagger UI**: http://localhost:8999/swagger-ui
- **OpenAPI Specification**: http://localhost:8999/openapi
- **MCP Setup Guide**: [MCP_SETUP.md](./MCP_SETUP.md)
- **Architecture Documentation**: [ARCHITECTURE.md](./ARCHITECTURE.md)
- **Testing Guide**: [TEST.md](./TEST.md)

---

## Comparison: REST vs MCP

| Feature | REST API | MCP Server |
|---------|----------|------------|
| **Protocol** | HTTP/REST | SSE + JSON-RPC 2.0 |
| **Use Case** | Web apps, mobile apps, general clients | AI tools (Cursor, Claude Desktop) |
| **Authentication** | Standard HTTP auth | MCP protocol |
| **Documentation** | OpenAPI/Swagger | MCP tool schema |
| **Format** | JSON over HTTP | JSON-RPC wrapped JSON |
| **Access** | Direct HTTP calls | Through MCP client |

Both interfaces provide the same functionality and use the same underlying business logic (`SearchTrainsService`), following hexagonal architecture principles.

