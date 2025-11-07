#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper start-up script
# ----------------------------------------------------------------------------

set -e

MVNW_DIR=$(cd "$(dirname "$0")"; pwd -P)
WRAPPER_DIR="$MVNW_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

# Load environment variables from .env if present
ENV_FILE="$MVNW_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  set -a
  . "$ENV_FILE"
  set +a
fi

if [ ! -d "$WRAPPER_DIR" ]; then
  echo "ERROR: Directory $WRAPPER_DIR not found" >&2
  echo "Make sure you have included .mvn/wrapper in the repository." >&2
  exit 1
fi

# Determine Java executable
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD=$(command -v java 2>/dev/null || true)
fi

if [ ! -x "$JAVA_CMD" ]; then
  echo "ERROR: Java not found. Configure JAVA_HOME pointing to JDK 25 or add 'java' to PATH." >&2
  exit 1
fi

JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
JAVA_MAJOR=${JAVA_VERSION%%.*}
if [ "${JAVA_MAJOR:-0}" -lt 21 ]; then
  echo "ERROR: JDK 25 or higher is required. Detected version: $JAVA_VERSION" >&2
  exit 1
fi

# Determine wrapper URL from properties if it exists
WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" 2>/dev/null | cut -d'=' -f2-)
if [ -z "$WRAPPER_URL" ]; then
  WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
fi

# Download wrapper if not present
if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$WRAPPER_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --silent "$WRAPPER_URL" --output "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget --quiet "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "ERROR: Could not download $WRAPPER_URL" >&2
    echo "Install curl or wget to continue." >&2
    exit 1
  fi
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: Could not find or download $WRAPPER_JAR" >&2
  exit 1
fi

JAVA_CMD_DIR=$(dirname "$JAVA_CMD")
JAVA_HOME_RESOLVED=$(cd "$JAVA_CMD_DIR/.." && pwd -P)

exec "$JAVA_CMD" -Dmaven.multiModuleProjectDirectory="$MVNW_DIR" \
  -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"

