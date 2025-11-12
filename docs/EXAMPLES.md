# API Usage Examples

[← Back to README](../README.md)


Start server dev mode

```bash
./mvnw quarkus:dev
```

## 1. Simple train search (outbound only)

```bash
curl -X GET "http://localhost:8999/trains?origin=MADRID+%28TODAS%29&destination=BARCELONA+%28TODAS%29&date_out=2025-12-15&adults=1"
```

## 2. Search with outbound and return

```bash
curl -X GET "http://localhost:8999/trains?origin=BARCELONA+%28TODAS%29&destination=VALENCIA+%28TODAS%29&date_out=2025-12-20&date_return=2025-12-27&adults=2"
```

## 3. Search with multiple passengers

```bash
curl -X GET "http://localhost:8999/trains?origin=MADRID+%28TODAS%29&destination=SEVILLA&date_out=2025-11-25&adults=4"
```

## 5. With JSON formatting (using jq)

```bash
curl -s "http://localhost:8999/trains?origin=OURENSE&destination=MADRID+%28TODAS%29&date_out=2025-12-15&adults=1" | jq
```

## Example Response

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

## Swagger/OpenAPI

- Swagger UI: http://localhost:8999/swagger-ui
- OpenAPI: http://localhost:8999/openapi


