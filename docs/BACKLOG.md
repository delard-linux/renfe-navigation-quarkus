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

## Next
- Reorganizar el md de test y vincular los reports de prj, surefire y coverage
- la carga de estaciones en una cache, 
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


