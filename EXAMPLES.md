# API Usage Examples

## 1. Simple train search (outbound only)

```bash
curl -X GET "http://localhost:8000/trains?origin=OURENSE&destination=MADRID&date_out=2025-12-15&adults=1"
```

## 2. Search with outbound and return

```bash
curl -X GET "http://localhost:8000/trains?origin=BARCELONA&destination=VALENCIA&date_out=2025-12-20&date_return=2025-12-27&adults=2"
```

## 3. Search with multiple passengers

```bash
curl -X GET "http://localhost:8000/trains?origin=MADRID&destination=SEVILLA&date_out=2025-11-25&adults=4"
```

## 4. Execute complete flow

```bash
curl -X GET "http://localhost:8000/trains-flow?origin=OURENSE&destination=MADRID&date_out=2025-12-01&adults=1"
```

## 5. With PowerShell (Windows)

```powershell
Invoke-RestMethod -Uri "http://localhost:8000/trains?origin=OURENSE&destination=MADRID&date_out=2025-12-15&adults=1" -Method Get
```

## 6. With JSON formatting (using jq)

```bash
curl -s "http://localhost:8000/trains?origin=OURENSE&destination=MADRID&date_out=2025-12-15&adults=1" | jq
```

## Example Response

```json
{
  "origin": "OURENSE",
  "destination": "MADRID",
  "date_out": "2025-12-15",
  "date_return": null,
  "adults": 1,
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
          "name": "Básico",
          "price": 45.50,
          "currency": "EUR",
          "code": "BAS",
          "tp_enlace": "12345-BAS",
          "features": ["Cancelación con cargo", "Cambio con cargo"]
        }
      ],
      "badges": ["Rápido", "WiFi"],
      "accessible": true,
      "eco_friendly": true
    }
  ],
  "trains_return": null
}
```

## Access Swagger UI

Open in browser: http://localhost:8000/swagger-ui

## View OpenAPI Specification

```bash
curl http://localhost:8000/openapi
```
