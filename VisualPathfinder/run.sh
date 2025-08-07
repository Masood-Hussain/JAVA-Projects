#!/bin/bash
# Compile all Java files
mkdir -p bin
javac -d bin src/*.java src/algorithms/*.java src/utils/*.java
# Run the main class
java -cp bin VisualPathfinder.src.Main
