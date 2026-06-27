@echo off
title Compilation Atilfaz IPTV PC Portable
echo.
echo  =========================================
echo    COMPILATION ATILFAZ IPTV - Version PC
echo  =========================================
echo.
echo Compilation en cours, veuillez patienter...
echo (Premiere compilation : 3-10 minutes selon votre connexion)
echo.

cd /d "%~dp0.."

call gradlew.bat :desktopApp:packageUberJarForCurrentOS

if %errorlevel% equ 0 (
    echo.
    echo  ===========================================
    echo   Compilation reussie !
    echo  ===========================================
    echo.
    echo  Le fichier portable JAR est dans :
    echo  desktopApp\build\compose\jars\
    echo.
    echo  Pour lancer l'application :
    echo  - Double-cliquez sur LANCER_SUR_PC.bat
    echo  - OU : java -jar desktopApp\build\compose\jars\AtilfazIPTV-windows-x64-1.0.0.jar
    echo.
    echo  IMPORTANT : VLC doit etre installe pour lire les flux IPTV.
    echo  Telechargez VLC : https://www.videolan.org/vlc/
    echo.
) else (
    echo.
    echo  [ERREUR] La compilation a echoue.
    echo.
    echo  Verifiez que vous avez :
    echo  1. Java JDK 17+ installe : https://adoptium.net/ (JDK 17 LTS)
    echo  2. Une connexion Internet pour telecharger les dependances
    echo.
)

pause
