@echo off
title Atilfaz IPTV - Version PC Portable
echo.
echo  ================================
echo    ATILFAZ IPTV - Version PC
echo  ================================
echo.

:: Verifier si Java 17+ est installe
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installe ou pas dans le PATH.
    echo.
    echo Telechargez Java 17 depuis : https://adoptium.net/
    echo (Choisir "Windows x64 JRE")
    echo.
    pause
    exit /b 1
)

echo Demarrage de Atilfaz IPTV...
echo.

:: Lancer le JAR depuis le repertoire du script
set SCRIPT_DIR=%~dp0
set JAR_FILE=%SCRIPT_DIR%build\libs\desktopApp.jar

if exist "%JAR_FILE%" (
    java -jar "%JAR_FILE%"
) else (
    echo [INFO] Le JAR portable n'est pas encore compile.
    echo.
    echo Pour compiler et creer le fichier portable :
    echo   gradlew :desktopApp:jar
    echo.
    echo Le fichier sera cree dans :
    echo   desktopApp\build\libs\desktopApp.jar
    echo.
    pause
)
