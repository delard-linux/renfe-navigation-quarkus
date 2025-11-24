# Fecha de salida: 2 meses desde hoy
DATE_OUT=$(date -d "+2 months" +%Y-%m-%d)

# Fecha de retorno: 3 días después de la salida
DATE_RETURN=$(date -d "+2 months +3 days" +%Y-%m-%d)

curl -X GET "http://localhost:8999/trains?origin=OURENSE&destination=MADRID%20%28TODAS%29&date_out=${DATE_OUT}&date_return=${DATE_RETURN}&adults=2" \
  -H "Accept: application/json"

