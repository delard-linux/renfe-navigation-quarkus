#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper start-up script
# ----------------------------------------------------------------------------

set -e

MVNW_DIR=$(cd "$(dirname "$0")"; pwd -P)
WRAPPER_DIR="$MVNW_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

# Cargar variables de entorno desde .env si está presente
ENV_FILE="$MVNW_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  set -a
  . "$ENV_FILE"
  set +a
fi

if [ ! -d "$WRAPPER_DIR" ]; then
  echo "ERROR: No se encontró el directorio $WRAPPER_DIR" >&2
  echo "Asegúrate de haber incluido .mvn/wrapper en el repositorio." >&2
  exit 1
fi

# Determinar el ejecutable de Java
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD=$(command -v java 2>/dev/null || true)
fi

if [ ! -x "$JAVA_CMD" ]; then
  echo "ERROR: No se encontró Java. Configura JAVA_HOME apuntando a un JDK 21 o añade 'java' al PATH." >&2
  exit 1
fi

JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
JAVA_MAJOR=${JAVA_VERSION%%.*}
if [ "${JAVA_MAJOR:-0}" -lt 21 ]; then
  echo "ERROR: Se requiere JDK 21 o superior. Versión detectada: $JAVA_VERSION" >&2
  exit 1
fi

# Determinar la URL del wrapper desde las propiedades si existe
WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" 2>/dev/null | cut -d'=' -f2-)
if [ -z "$WRAPPER_URL" ]; then
  WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
fi

# Descargar el wrapper si no está presente
if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$WRAPPER_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --silent "$WRAPPER_URL" --output "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget --quiet "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "ERROR: No se pudo descargar $WRAPPER_URL" >&2
    echo "Instala curl o wget para continuar." >&2
    exit 1
  fi
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: No se pudo encontrar o descargar $WRAPPER_JAR" >&2
  exit 1
fi

JAVA_CMD_DIR=$(dirname "$JAVA_CMD")
JAVA_HOME_RESOLVED=$(cd "$JAVA_CMD_DIR/.." && pwd -P)

exec "$JAVA_CMD" -Dmaven.multiModuleProjectDirectory="$MVNW_DIR" \
  -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"

