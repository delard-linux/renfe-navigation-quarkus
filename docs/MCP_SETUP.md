# MCP Server Setup for Cursor

This document explains how to configure Cursor IDE to use the Renfe Navigation MCP server.

## Server Information

- **Server URL**: `http://localhost:8999/mcp` (SSE endpoint)
- **Protocol**: MCP (Model Context Protocol) over HTTP/SSE using Quarkus MCP Server extension
- **Available Tool**: `getTrains` (exposed as MCP tool)
- **Extension**: `quarkus-mcp-server-sse` (official Quarkus extension)

## Prerequisites

1. Ensure the Quarkus server is running:
   ```bash
   ./mvnw quarkus:dev
   ```
   The server should be available at `http://localhost:8999`

## Quick Start

1. **Start the Quarkus server:**
   ```bash
   ./mvnw quarkus:dev
   ```

2. **Configure Cursor:**
   - Open Cursor Settings (Cmd/Ctrl + ,)
   - Search for "MCP" or "Model Context Protocol"
   - Add the server configuration (see details below)

3. **Restart Cursor** to load the MCP server configuration

## Configuration Methods

### Method 1: Cursor Settings UI (Easiest)

1. Open Cursor Settings: `Cmd/Ctrl + ,`
2. Search for "MCP" or navigate to Extensions → MCP
3. Click "Add Server" or "Edit in settings.json"
4. Add the following configuration:

```json
{
  "mcpServers": {
    "renfe-navigation": {
      "url": "http://localhost:8999/mcp",
      "transport": "http",
      "description": "Renfe Navigation MCP Server - Search for train schedules and fares"
    }
  }
}
```

### Method 2: Edit Settings JSON Directly

1. Press `Cmd/Ctrl + Shift + P` to open Command Palette
2. Type "Preferences: Open User Settings (JSON)" and select it
3. Add the configuration above to the JSON file
4. Save the file

**Settings file locations:**

- **macOS**: `~/Library/Application Support/Cursor/User/settings.json`
- **Linux**: `~/.config/Cursor/User/settings.json`
- **Windows**: `%APPDATA%\Cursor\User\settings.json`

### Method 3: Workspace Configuration

The project includes a `.vscode/settings.json` file with the MCP configuration. 
If Cursor respects VS Code workspace settings, this should work automatically.

### Method 4: SSE (Server-Sent Events) Configuration

The Quarkus MCP Server extension uses SSE by default. Configure it as:

```json
{
  "mcpServers": {
    "renfe-navigation": {
      "url": "http://localhost:8999/mcp",
      "transport": "sse",
      "description": "Renfe Navigation MCP Server - Search for train schedules and fares"
    }
  }
}
```

Note: The endpoint `/mcp` is the SSE endpoint provided by the Quarkus MCP Server extension.

## Starting the Server

Before using the MCP server in Cursor, make sure the Quarkus application is running:

```bash
# Development mode
./mvnw quarkus:dev

# Or run the packaged application
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

The server will start on `http://localhost:8999` by default.

## Verify Configuration

1. Restart Cursor after adding the configuration
2. The MCP server should appear in Cursor's MCP servers list
3. You should be able to use the `getTrains` tool in Cursor's AI chat

## Using the getTrains Tool

Once configured, you can ask Cursor's AI to search for trains using natural language:

```
Search for trains from OURENSE to MADRID on 2026-01-16 for 1 adult
```

Or with return trip:

```
Search for trains from OURENSE to MADRID on 2026-01-16 returning on 2026-01-20 for 2 adults
```

Cursor will automatically use the MCP tool to fetch the train information.

## Tool Parameters

The `getTrains` tool accepts the following parameters:

- **origin** (required): Station origin name (e.g., "OURENSE")
- **destination** (required): Station destination name (e.g., "MADRID")
- **dateOut** (required): Outbound date in format YYYY-MM-DD (e.g., "2026-01-16")
- **dateReturn** (optional): Return date in format YYYY-MM-DD (e.g., "2026-01-20")
- **adults** (optional): Number of adult passengers as string (1-8, e.g., "1"). Defaults to "1" if not provided

## Available MCP Methods

### 1. Initialize
```json
{
  "jsonrpc": "2.0",
  "method": "initialize",
  "id": "1",
  "params": {}
}
```

### 2. List Tools
```json
{
  "jsonrpc": "2.0",
  "method": "tools/list",
  "id": "2",
  "params": {}
}
```

### 3. Call Tool: getTrains
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "id": "3",
  "params": {
    "name": "getTrains",
    "arguments": {
      "origin": "OURENSE",
      "destination": "MADRID",
      "dateOut": "2026-01-16",
      "dateReturn": "2026-01-20",
      "adults": "1"
    }
  }
}
```

## Testing the Connection

You can test the MCP server using curl:

```bash
# Test initialize
curl -X POST http://localhost:8999/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "initialize",
    "id": "1",
    "params": {}
  }'

# Test tools/list
curl -X POST http://localhost:8999/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "id": "2",
    "params": {}
  }'

# Test getTrains tool
curl -X POST http://localhost:8999/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "3",
    "params": {
      "name": "getTrains",
      "arguments": {
        "origin": "OURENSE",
        "destination": "MADRID",
        "dateOut": "2026-01-16",
        "adults": "1"
      }
    }
  }'
```

## Troubleshooting

### Server not responding
- Ensure the Quarkus application is running
- Check that the port 8999 is not blocked by firewall
- Verify the server is accessible: `curl http://localhost:8999/mcp`

### Connection refused
- Check if the server is running: `./mvnw quarkus:dev`
- Verify the port in `application.properties`: `quarkus.http.port=8999`
- Check for port conflicts

### Tool not available
- Restart Cursor after adding the configuration
- Verify the MCP server appears in Cursor's MCP servers list
- Check that the server is running and accessible

### Tool execution errors
- Verify all required parameters are provided
- Check date format is YYYY-MM-DD
- Ensure station names are valid (case-sensitive)
- Check server logs for detailed error messages

## Additional Resources

- [MCP Protocol Specification](https://modelcontextprotocol.io)
- [Quarkus REST Documentation](https://quarkus.io/guides/rest)
- [Server-Sent Events (SSE) Guide](https://quarkus.io/guides/reactive-routes#server-sent-events)

