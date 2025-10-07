#!/bin/bash

# Development script for Speech Recognition Java Application
# This script provides common development tasks

show_help() {
    echo "Speech Recognition Application - Development Helper"
    echo ""
    echo "Usage: ./dev.sh [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  build       - Clean and build the application"
    echo "  run-gui     - Build and run the GUI version"
    echo "  run-cli     - Build and run the CLI version"
    echo "  clean       - Clean build artifacts"
    echo "  test        - Run tests (if any)"
    echo "  package     - Create distribution package"
    echo "  check-deps  - Check system dependencies"
    echo "  help        - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./dev.sh build"
    echo "  ./dev.sh run-gui"
    echo "  ./dev.sh clean"
}

check_dependencies() {
    echo "=== Checking System Dependencies ==="
    
    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        echo "✅ Java: $JAVA_VERSION"
    else
        echo "❌ Java: Not installed"
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | cut -d' ' -f3)
        echo "✅ Maven: $MVN_VERSION"
    else
        echo "❌ Maven: Not installed"
    fi
    
    # Check model directory
    if [ -d "model" ]; then
        echo "✅ Model directory: Present"
        if [ -f "model/am/final.mdl" ]; then
            echo "✅ Acoustic model: Present"
        else
            echo "⚠️  Acoustic model: Missing"
        fi
    else
        echo "❌ Model directory: Missing"
    fi
    
    echo ""
}

case "${1:-help}" in
    "build")
        ./build.sh
        ;;
    "run-gui")
        ./build.sh && ./run-gui.sh
        ;;
    "run-cli")
        ./build.sh && ./run-cli.sh
        ;;
    "clean")
        echo "=== Cleaning build artifacts ==="
        if command -v mvn &> /dev/null; then
            mvn clean
        else
            echo "Maven not found, removing target directory manually..."
            rm -rf target/
        fi
        echo "✅ Clean completed"
        ;;
    "test")
        echo "=== Running tests ==="
        mvn test
        ;;
    "package")
        echo "=== Creating package ==="
        ./build.sh
        echo "Package created in target/ directory"
        ;;
    "check-deps")
        check_dependencies
        ;;
    "help"|*)
        show_help
        ;;
esac
