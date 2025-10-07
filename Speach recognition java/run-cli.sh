#!/bin/bash

# Run script for Speech Recognition CLI Application

set -e  # Exit on any error

echo "=== Starting Speech Recognition CLI Application ==="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or later to run this application"
    exit 1
fi

# Check if the JAR file exists
JAR_FILE="target/speech-recognition-app-1.0.0-jar-with-dependencies.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: Application JAR not found at $JAR_FILE"
    echo "Please build the application first by running: ./build.sh"
    exit 1
fi

# Check if model directory exists
if [ ! -d "model" ]; then
    echo "Warning: Model directory not found. Speech recognition may not work properly."
    echo "Please ensure the Vosk model is properly installed in the 'model' directory."
fi

echo "Starting CLI application..."
echo ""

# Run the application with CLI interface
# Create a clean environment to avoid snap conflicts from VS Code
env -i \
    HOME="$HOME" \
    USER="$USER" \
    PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" \
    LD_LIBRARY_PATH="/usr/lib/x86_64-linux-gnu:/lib/x86_64-linux-gnu" \
    JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64" \
    java -cp "$JAR_FILE" com.speechrecognition.SpeechRecognitionCLI
