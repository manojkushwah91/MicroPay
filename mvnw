#!/usr/bin/env sh
set -eu

MAVEN_PROJECTBASEDIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Maven Wrapper JAR not found: $WRAPPER_JAR" >&2
  echo "Run: ./scripts/bootstrap-mvnw.sh" >&2
  exit 1
fi

exec java -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"


