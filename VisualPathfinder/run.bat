@echo off
REM Compile all Java files
javac -d bin src/*.java src/algorithms/*.java src/utils/*.java
REM Run the main class
java -cp bin VisualPathfinder.src.Main
pause
