@echo off
title Compilation Atilfaz IPTV PC Portable
echo.
echo  =========================================
echo    COMPILATION ATILFAZ IPTV - Version PC
echo  =========================================
echo.
echo Compilation en cours, veuillez patienter...
echo (Premiere compilation : 3-5 minutes)
echo.

cd /d "%~dp0.."

call gradlew.bat :desktopApp:jar

if %errorlevel% equ 0 (
    echo.
    echo  ===========================================
    echo   Compilation reussie !
    echo  ===========================================
    echo.
    echo  Le fichier portable est :
    echo  desktopApp\build\libs\desktopApp.jar
    echo.
    echo  Pour lancer l'application :
    echo  - Double-cliquez sur LANCER_SUR_PC.bat
    echo  - OU : java -jar desktopApp\build\libs\desktopApp.jar
    echo.
    echo  CONSEIL : Installez VLC pour lire les flux IPTV
    echo  https://www.videolan.org/vlc/
    echo.
) else (
    echo.
    echo  [ERREUR] La compilation a echoue.
    echo  Verifiez que Java 17 et le JDK sont installes.
    echo  https://adoptium.net/ (choisir JDK 17)
    echo.
)

pause
