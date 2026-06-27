@echo off
title Atilfaz IPTV - Version PC Portable
echo.
echo  ================================
echo    ATILFAZ IPTV - Version PC
echo  ================================
echo.

:: Verifier Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installe.
    echo.
    echo Telechargez Java 17 JRE depuis : https://adoptium.net/
    echo Choisissez : Windows x64, JRE, Version 17 LTS
    echo.
    pause
    exit /b 1
)

set SCRIPT_DIR=%~dp0
set JAR_DIR=%SCRIPT_DIR%build\compose\jars

:: Chercher le JAR dans le dossier de sortie Compose Desktop
set JAR_FILE=
for %%f in ("%JAR_DIR%\*.jar") do set JAR_FILE=%%f

if defined JAR_FILE (
    echo Lancement de Atilfaz IPTV...
    echo Fichier : %JAR_FILE%
    echo.
    java -jar "%JAR_FILE%"
) else (
    echo [INFO] Le fichier JAR portable n'existe pas encore.
    echo.
    echo Pour le compiler, lancez d'abord : BUILD_PORTABLE.bat
    echo.
    echo Ou depuis Android Studio / terminal dans le dossier racine :
    echo   gradlew :desktopApp:packageUberJarForCurrentOS
    echo.
    echo Le JAR sera cree dans :
    echo   desktopApp\build\compose\jars\
    echo.
    pause
)
