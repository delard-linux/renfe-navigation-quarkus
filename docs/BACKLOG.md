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
- Los ejemplos de INTEGRATION_EXAMPLES.md fallan por timeout ahora
- Aceptar las cookies en el resultado de los trenes
- Permitir la configuración de headless y slow-mo por parametros maven
- Numero de adultos tiene que ser string
- MCP endpoint instead REST
- Meter los endpoints REST y MCP de las estaciones
- Incluir la gestión del error de la Queue en la compra del billete
- Mejora cobertura, tests, y limpieza de warnings de construcion y tests con maven
- Control de excepción y timeouts ante problema de disponibilidad de billetes u otros errores
- Eliminacíon del error de minimo de tres letras cuando se buscan mas de una palabra
- Flexibilizar Error: Each word in the search text must have at least 3 characters
- Revisar tests de IT deshabilitados de PlaywrightSearchTrainsServiceIT
- Opciones de headless=false no funcionan

## Current pending

- Scaffolding de tool MCP para compra de billetes

## Next

- Manejar la validationException que es una Runtime no declarada en los servicios.. puede saltar en un REST o en un MCP
- renfe.responses-dir=target/responses
- Support multiple passenger types (not only adults)
- Add spec-kit
- SonarQube integration
- Linting integration
- Security validation


## Warnings to be reviewed

- Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
- OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
- WARNING: A Java agent has been loaded dynamically (/home/daviddrosadelgado/.m2/repository/net/bytebuddy/byte-buddy-agent/1.17.5/byte-buddy-agent-1.17.5.jar)
- WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
- WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
- WARNING: Dynamic loading of agents will be disallowed by default in a future release
