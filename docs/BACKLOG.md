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
- la carga de la estructura de estaciones en una cache para no ir a buscarlo cada vez y parsear el JSON

## Next
- carga de estaciones cuando da mas de un resultado, controlar errores
- meter el search de estaciones del servicio desde arriba a playwright,  para que al servicio le lleguen estaciones correctas y tenga menos código , con gestion de errores
- Error de adultos + de 1
- test de servicio trenes de playwright con ida y vuelta y que cuente mas de un tren en la respuesta
- renfe.responses-dir=target/responses
- Add remaining validations (stations, dates, passengers)
- Support multiple passenger types (not only adults)
- Option to skip integration tests during build to speed up CI
- Clarify behavior when multiple stations match the same string
- MCP endpoint instead REST
- Add spec-kit
- SonarQube integration
- Linting integration
- Security validation


