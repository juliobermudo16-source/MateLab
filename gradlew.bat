@rem
@rem Lanzador del Gradle Wrapper para MateLab (Windows).
@rem
@rem El repositorio NO incluye gradle\wrapper\gradle-wrapper.jar.
@rem La primera vez ejecuta:
@rem     gradle wrapper --gradle-version 8.9 --distribution-type bin
@rem
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%
set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo ERROR: falta %WRAPPER_JAR% 1>&2
    echo Ejecuta una sola vez:  gradle wrapper --gradle-version 8.9 --distribution-type bin 1>&2
    exit /b 1
)

if defined JAVA_HOME (
    set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
    set JAVACMD=java.exe
)

"%JAVACMD%" %JAVA_OPTS% %GRADLE_OPTS% -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
