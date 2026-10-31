@echo off
cd /d "%~dp0"
java -jar "%~dp0bin\pediacenter.jar"
if errorlevel 1 pause
