#!/bin/sh

MAVEN_HOME="/opt/maven/apache-maven-3.9.11"
MVN_BIN="$MAVEN_HOME/bin/mvn"

# Forzar JDK 21 de adoptium para la ejecución de Maven
JAVA_HOME="/opt/java/jdk-21"
PATH="$JAVA_HOME/bin:$PATH"
export JAVA_HOME PATH

# Verificación de instalación de JDK 21
if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "Error: JDK 21 no encontrado en $JAVA_HOME" >&2
  echo "Instala o corrige la ruta de JDK 21 (Adoptium) en $JAVA_HOME" >&2
  exit 1
fi

# Validación de versión mayor = 21
JAVA_VER_STR=$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
JAVA_MAJOR=${JAVA_VER_STR%%.*}
if [ "$JAVA_MAJOR" != "21" ]; then
  echo "Error: JAVA_HOME apunta a versión $JAVA_VER_STR (mayor=$JAVA_MAJOR), se requiere 21" >&2
  exit 1
fi

if [ ! -x "$MVN_BIN" ]; then
  echo "Error: Maven no encontrado en $MVN_BIN" >&2
  echo "Asegúrate de que Maven esté instalado en /opt/maven/apache-maven-3.9.11" >&2
  exit 1
fi

exec "$MVN_BIN" "$@"


