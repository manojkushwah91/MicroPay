@ECHO OFF
SETLOCAL

SET "MAVEN_PROJECTBASEDIR=%~dp0"
IF "%MAVEN_PROJECTBASEDIR%"=="" SET "MAVEN_PROJECTBASEDIR=."
SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

SET "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

SET "JAVA_EXE=java"
IF DEFINED JAVA_HOME SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Maven Wrapper JAR not found: "%WRAPPER_JAR%"
  ECHO Run: powershell -ExecutionPolicy Bypass -File scripts\\bootstrap-mvnw.ps1
  EXIT /B 1
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
EXIT /B %ERRORLEVEL%


