@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  MelodyMatrixViewers startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and MELODY_MATRIX_VIEWERS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\MelodyMatrixViewers-0.0.1-alpha.jar;%APP_HOME%\lib\fxgl-21.1.jar;%APP_HOME%\lib\charts-21.0.7.jar;%APP_HOME%\lib\atlantafx-base-2.0.1.jar;%APP_HOME%\lib\log4j-core-2.23.1.jar;%APP_HOME%\lib\fxgl-io-21.1.jar;%APP_HOME%\lib\fxgl-entity-21.1.jar;%APP_HOME%\lib\fxgl-gameplay-21.1.jar;%APP_HOME%\lib\fxgl-scene-21.1.jar;%APP_HOME%\lib\fxgl-core-21.1.jar;%APP_HOME%\lib\kotlin-stdlib-2.0.0.jar;%APP_HOME%\lib\javafx-media-21.0.2-mac.jar;%APP_HOME%\lib\javafx-media-21.0.2.jar;%APP_HOME%\lib\javafx-fxml-21.0.2-mac.jar;%APP_HOME%\lib\javafx-fxml-21.0.2.jar;%APP_HOME%\lib\countries-21.0.3.jar;%APP_HOME%\lib\heatmap-21.0.3.jar;%APP_HOME%\lib\toolboxfx-21.0.3.jar;%APP_HOME%\lib\javafx-controls-21.0.2-mac.jar;%APP_HOME%\lib\javafx-controls-21.0.2.jar;%APP_HOME%\lib\lifecycle-4.0.17.jar;%APP_HOME%\lib\javafx-swing-21.0.1.jar;%APP_HOME%\lib\audio-4.0.17.jar;%APP_HOME%\lib\storage-4.0.17.jar;%APP_HOME%\lib\util-4.0.17.jar;%APP_HOME%\lib\javafx-graphics-21.0.2-mac.jar;%APP_HOME%\lib\javafx-graphics-21.0.2.jar;%APP_HOME%\lib\javafx-base-21.0.2-mac.jar;%APP_HOME%\lib\javafx-base-21.0.2.jar;%APP_HOME%\lib\jackson-annotations-2.14.2.jar;%APP_HOME%\lib\jackson-core-2.14.2.jar;%APP_HOME%\lib\jackson-databind-2.14.2.jar;%APP_HOME%\lib\logback-classic-1.3.9.jar;%APP_HOME%\lib\log4j-api-2.23.1.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\logback-core-1.3.9.jar;%APP_HOME%\lib\slf4j-api-2.0.7.jar;%APP_HOME%\lib\toolbox-21.0.5.jar


@rem Execute MelodyMatrixViewers
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %MELODY_MATRIX_VIEWERS_OPTS%  -classpath "%CLASSPATH%" be.codewriter.melodymatrix.view.TestLauncher %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable MELODY_MATRIX_VIEWERS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%MELODY_MATRIX_VIEWERS_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
