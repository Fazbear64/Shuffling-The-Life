@echo off
echo Compilando Shuffling The Life...
echo.

REM Crear directorio para clases compiladas si no existe
if not exist "target\classes" mkdir "target\classes"

REM Establecer classpath con Lombok
set CLASSPATH=target\classes;C:\Users\USUARIO\.m2\repository\org\projectlombok\lombok\1.18.48\lombok-1.18.48.jar

REM Compilar archivos Java con procesador de anotaciones Lombok
javac -d target\classes ^
  -cp "%CLASSPATH%" ^
  -processorpath "C:\Users\USUARIO\.m2\repository\org\projectlombok\lombok\1.18.48\lombok-1.18.48.jar" ^
  -sourcepath src\main\java ^
  src\main\java\config\DefaultSettings.java ^
  src\main\java\logic\Progreso.java ^
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