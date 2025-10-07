#!/bin/bash

# Build script for Speech Recognition Java Application
# This script compiles the project and creates the executable JAR

set -e  # Exit on any error

echo "=== Building Speech Recognition Application ==="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven to build this project"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found. Please run this script from the project root directory."
    exit 1
fi

echo "Cleaning previous builds..."
mvn clean

echo "Compiling and packaging application..."
mvn package

# Check if build was successful
if [ -f "target/speech-recognition-app-1.0.0-jar-with-dependencies.jar" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 Executable JAR created: target/speech-recognition-app-1.0.0-jar-with-dependencies.jar"
    echo ""
    echo "You can now run the application using:"
    echo "  ./run-gui.sh    - Run the GUI version"
    echo "  ./run-cli.sh    - Run the CLI version"
else
    echo "❌ Build failed! Check the output above for errors."
    exit 1
fi
