#!/bin/sh
#
# Lanzador del Gradle Wrapper para MateLab.
#
# NOTA IMPORTANTE
# ---------------
# El repositorio NO incluye el binario gradle/wrapper/gradle-wrapper.jar
# (los binarios no se versionan en este proyecto). La primera vez, ejecuta:
#
#     gradle wrapper --gradle-version 8.9 --distribution-type bin
#
# Ese comando descarga el .jar oficial y sustituye este script por el
# lanzador estandar de Gradle. El workflow de GitHub Actions ya lo hace solo.
#

APP_HOME=$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "ERROR: falta $WRAPPER_JAR" >&2
    echo "Ejecuta una sola vez:  gradle wrapper --gradle-version 8.9 --distribution-type bin" >&2
    exit 1
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" $JAVA_OPTS $GRADLE_OPTS \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain "$@"
