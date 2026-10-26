@echo off
cd /d "%~dp0"
java -jar bin\pediacenter.jar
if errorlevel 1 pause
