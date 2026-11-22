@echo off
echo Compilando Shuffling The Life...
echo.

REM Crear directorio para clases compiladas si no existe
if not exist "target\classes" mkdir "target\classes"

REM Establecer classpath
set CLASSPATH=target\classes

REM Compilar archivos Java
javac -d target\classes -cp "target\classes" ^
  src\main\java\config\DefaultSettings.java ^
  src\main\java\logic\Progreso.java ^
  src\main\java\logic\Chronometer.java ^
  src\main\java\dto\*.java ^
  src\main\java\entity\*.java ^
  src\main\java\visual\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilacion exitosa!
    echo.
    echo Para ejecutar el juego:
    echo java -cp "target\classes" visual.Game
) else (
    echo Error en la compilacion.
    pause
)