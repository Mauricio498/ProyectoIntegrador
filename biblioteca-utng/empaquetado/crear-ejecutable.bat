@echo off
REM ===========================================================================
REM  BIBLIOTECA UTNG - Generador del ejecutable para Windows
REM
REM  Produce una carpeta lista para copiar a otra computadora, con Java
REM  incluido: en el equipo destino NO hace falta instalar nada.
REM
REM  Requisitos en ESTA computadora:
REM    - JDK 21 o superior (jpackage viene incluido)
REM    - Maven
REM
REM  Uso: doble clic, o desde la terminal:  empaquetado\crear-ejecutable.bat
REM ===========================================================================

setlocal
cd /d "%~dp0\.."


REM ---------------------------------------------------------------------------
REM  Windows no deja borrar ni sobrescribir un .exe que esta en ejecucion.
REM  Si quedo abierto de una compilacion anterior, se avisa antes de empezar.
REM ---------------------------------------------------------------------------
tasklist /FI "IMAGENAME eq BibliotecaUTNG.exe" 2>nul | find /I "BibliotecaUTNG.exe" >nul
if not errorlevel 1 (
    echo.
    echo ATENCION: BibliotecaUTNG.exe esta en ejecucion.
    echo Cierra la aplicacion antes de continuar, o el empaquetado fallara.
    echo.
    pause
)

echo.
echo [1/3] Compilando y generando el JAR unico...
call mvn clean package -q
if errorlevel 1 (
    echo.
    echo ERROR: fallo la compilacion. Revisa los mensajes de arriba.
    pause
    exit /b 1
)

if not exist "target\biblioteca-utng-1.0.0.jar" (
    echo.
    echo ERROR: no se genero target\biblioteca-utng-1.0.0.jar
    pause
    exit /b 1
)

echo.
echo [2/3] Preparando archivos...
if exist "dist\entrada" rmdir /s /q "dist\entrada"
mkdir "dist\entrada"
copy /y "target\biblioteca-utng-1.0.0.jar" "dist\entrada\" >nul

if exist "dist\ejecutable" rmdir /s /q "dist\ejecutable"

echo.
echo [3/3] Creando el ejecutable con jpackage...
jpackage ^
  --type app-image ^
  --name "BibliotecaUTNG" ^
  --app-version 1.0.0 ^
  --vendor "Universidad Tecnologica del Norte de Guanajuato" ^
  --description "Sistema de Gestion Bibliotecaria" ^
  --input "dist\entrada" ^
  --main-jar "biblioteca-utng-1.0.0.jar" ^
  --main-class utng.biblioteca.Launcher ^
  --icon "empaquetado\icono.ico" ^
  --dest "dist\ejecutable"

if errorlevel 1 (
    echo.
    echo NOTA: si el error menciona el icono, borra la linea --icon del script
    echo       o coloca un archivo icono.ico en la carpeta empaquetado\
    pause
    exit /b 1
)

REM La configuracion va FUERA del jar para poder cambiar el servidor
REM en cada computadora sin recompilar.
mkdir "dist\ejecutable\BibliotecaUTNG\config" 2>nul
copy /y "src\main\resources\config\database.properties" ^
        "dist\ejecutable\BibliotecaUTNG\config\" >nul

REM Los scripts de base de datos viajan junto a la aplicacion
mkdir "dist\ejecutable\BibliotecaUTNG\sql" 2>nul
copy /y "sql\*.sql" "dist\ejecutable\BibliotecaUTNG\sql\" >nul

echo.
echo ===========================================================================
echo  LISTO
echo.
echo  Carpeta generada:  dist\ejecutable\BibliotecaUTNG
echo  Ejecutable:        BibliotecaUTNG.exe
echo.
echo ===========================================================================
echo.
pause
