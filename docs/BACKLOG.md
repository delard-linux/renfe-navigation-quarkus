# Backlog

[← Back to README](../README.md)

## Completed

- Cyclomatic complexity checks in `pom.xml`
- Validation of required fields and adult count in service
- Relative dates to avoid failures across months
- Playwright configuration by test profile aligned with unit tests
- Centralize test documentation and commands (README links to docs/TEST.md)
- Refactor stations JSON: service design and file location
- data/estacionesEstaticas.js en properties, se carga y se valida, busqueda de estación por criterio
- Reorganizar el md de test y vincular los reports de prj, surefire y coverage
- La carga de la estructura de estaciones en una cache para no ir a buscarlo cada vez y parsear el JSON
- Error de adultos + de 1
- Meter el search de estaciones del servicio desde arriba a playwright,  para que al servicio le lleguen estaciones correctas y tenga menos código , con gestion de errores y validaciones. El servicio de playwright se tiene que limpiar y tambien el test ya que no tiene sentido tanto test ya que la mayoría de las validaciones se hacen en la capa superior
- Add remaining validations (stations, dates, passengers)
- Clarify behavior when multiple stations match the same string
- test integrado ida funcionando
- test de servicio trenes de playwright con ida y vuelta y que cuente mas de un tren en la respuesta
- falla con busquedas de ida y vuelta
- Los EXAMPLES.md fallan por timeout ahora
- Aceptar las cookies en el resultado de los trenes
- Permitir la configuración de headless y slow-mo por parametros maven

## Current pending

- Numero de adultos tiene que ser string
- MCP endpoint instead REST

## Next


- Manejar la validationException que es una Runtime no declarada en los servicios.. puede saltar en un REST o en un MCP
- renfe.responses-dir=target/responses
- Support multiple passenger types (not only adults)
- Add spec-kit
- SonarQube integration
- Linting integration
- Security validation


