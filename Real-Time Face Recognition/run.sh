#!/bin/bash

# Real-Time Face Recognition System Startup Script
# This script provides easy ways to run the application with enhanced error handling

set -e  # Exit on any error

echo "======================================"
echo "Real-Time Face Recognition System v1.0"
echo "======================================"
echo

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

# Check Java version
if ! command -v java &> /dev/null; then
    print_error "Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    echo "Ubuntu/Debian: sudo apt install openjdk-11-jdk"
    echo "CentOS/RHEL: sudo yum install java-11-openjdk-devel"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{if($1 >= 9) print $1; else print $2}')
if [ "$JAVA_VERSION" -lt 11 ]; then
    print_error "Java 11 or higher is required. Current version: $JAVA_VERSION"
    echo "Please update your Java installation"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    echo "Ubuntu/Debian: sudo apt install maven"
    echo "CentOS/RHEL: sudo yum install maven"
    exit 1
fi

MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1 | grep -o '[0-9]\+\.[0-9]\+' | head -n 1)
MAVEN_MAJOR=$(echo "$MAVEN_VERSION" | cut -d '.' -f 1)
MAVEN_MINOR=$(echo "$MAVEN_VERSION" | cut -d '.' -f 2)

if [ "$MAVEN_MAJOR" -lt 3 ] || ([ "$MAVEN_MAJOR" -eq 3 ] && [ "$MAVEN_MINOR" -lt 6 ]); then
    print_warning "Maven 3.6+ recommended. Current version: $MAVEN_VERSION"
fi

print_status "Java version check passed (Java $JAVA_VERSION)"
print_status "Maven found (v$MAVEN_VERSION)"
echo

# Function to check system resources
check_system_resources() {
    print_info "Checking system resources..."
    
    # Check available memory
    if command -v free &> /dev/null; then
        AVAILABLE_MEM=$(free -m | awk '/^Mem:/{print $7}')
        if [ "$AVAILABLE_MEM" -lt 1024 ]; then
            print_warning "Low available memory: ${AVAILABLE_MEM}MB (recommended: 2GB+)"
        else
            print_status "Available memory: ${AVAILABLE_MEM}MB"
        fi
    fi
    
    # Check for camera devices
    if ls /dev/video* >/dev/null 2>&1; then
        CAMERA_COUNT=$(ls -1 /dev/video* | wc -l)
        print_status "Found $CAMERA_COUNT camera device(s)"
    else
        print_warning "No camera devices found in /dev/video*"
        print_info "Please ensure a camera is connected"
    fi
}

# Function to run GUI mode
run_gui() {
    print_info "Starting Face Recognition System with GUI..."
    print_info "Please wait while dependencies are resolved..."
    echo "Press Ctrl+C to stop the application"
    echo
    mvn exec:java -Dexec.mainClass="com.facerecognition.Main" -Dexec.args="--gui" -q
}

# Function to run console mode
run_console() {
    print_info "Starting Face Recognition System in console mode..."
    print_info "Please wait while dependencies are resolved..."
    echo "Press Ctrl+C to stop the application"
    echo
    mvn exec:java -Dexec.mainClass="com.facerecognition.Main" -q
}

# Function to run tests
run_tests() {
    print_info "Running comprehensive test suite..."
    echo
    mvn test -q
    if [ $? -eq 0 ]; then
        print_status "All tests passed successfully!"
    else
        print_error "Some tests failed. Please check the output above."
        exit 1
    fi
}

# Function to build JAR
build_jar() {
    print_info "Building executable JAR with all dependencies..."
    echo "This may take a few minutes..."
    echo
    mvn clean package -q
    
    if [ $? -eq 0 ]; then
        print_status "Build successful!"
        JAR_FILE="target/real-time-face-recognition-1.0-SNAPSHOT.jar"
        if [ -f "$JAR_FILE" ]; then
            JAR_SIZE=$(ls -lh "$JAR_FILE" | awk '{print $5}')
            print_info "JAR file created: $JAR_FILE (Size: $JAR_SIZE)"
            echo
            echo "You can now run with:"
            echo "  java -jar $JAR_FILE --gui"
            echo "  java -jar $JAR_FILE"
        fi
    else
        print_error "Build failed! Check Maven output above."
        exit 1
    fi
}

# Function to clean project
clean_project() {
    print_info "Cleaning project and temporary files..."
    mvn clean -q
    
    # Remove generated files
    rm -f face_recognition.db
    rm -f face-recognition.log*
    rm -f test_face_recognition.db
    rm -f *.tmp
    
    print_status "Project cleaned successfully"
}

# Function to show system information
show_system_info() {
    echo
    print_info "System Information:"
    echo "  OS: $(uname -s) $(uname -r)"
    echo "  Architecture: $(uname -m)"
    echo "  Java: $(java -version 2>&1 | head -n 1)"
    echo "  Maven: $(mvn -version 2>&1 | head -n 1)"
    
    if command -v free &> /dev/null; then
        TOTAL_MEM=$(free -h | awk '/^Mem:/{print $2}')
        USED_MEM=$(free -h | awk '/^Mem:/{print $3}')
        echo "  Memory: $USED_MEM used / $TOTAL_MEM total"
    fi
    
    if ls /dev/video* >/dev/null 2>&1; then
        echo "  Camera devices: $(ls /dev/video* | tr '\n' ' ')"
    fi
    echo
}

# Main menu
show_menu() {
    echo
    echo "Select an option:"
    echo "1) Run with GUI (recommended)"
    echo "2) Run in console mode"  
    echo "3) Build executable JAR"
    echo "4) Run comprehensive tests"
    echo "5) Clean project"
    echo "6) Show system information"
    echo "7) Check system resources"
    echo "0) Exit"
    echo
    read -p "Enter your choice [0-7]: " choice
    
    case $choice in
        1)
            echo
            run_gui
            ;;
        2)
            echo
            run_console
            ;;
        3)
            echo
            build_jar
            ;;
        4)
            echo
            run_tests
            ;;
        5)
            echo
            clean_project
            ;;
        6)
            show_system_info
            show_menu
            ;;
        7)
            echo
            check_system_resources
            echo
            show_menu
            ;;
        0)
            print_info "Goodbye!"
            exit 0
            ;;
        *)
            print_error "Invalid choice. Please select 0-7."
            show_menu
            ;;
    esac
}

# Check if arguments are provided
if [ $# -eq 0 ]; then
    # No arguments - show interactive menu
    check_system_resources
    show_menu
else
    # Arguments provided - direct execution
    case "$1" in
        --gui|-g)
            run_gui
            ;;
        --console|-c)
            run_console
            ;;
        --build|-b)
            build_jar
            ;;
        --test|-t)
            run_tests
            ;;
        --clean)
            clean_project
            ;;
        --help|-h)
            echo "Usage: $0 [OPTION]"
            echo
            echo "Options:"
            echo "  --gui, -g      Run with GUI interface"
            echo "  --console, -c  Run in console mode"
            echo "  --build, -b    Build executable JAR"
            echo "  --test, -t     Run tests"
            echo "  --clean        Clean project"
            echo "  --help, -h     Show this help"
            echo
            echo "If no option is provided, an interactive menu will be shown."
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
fi
        clean_project
        exit 0
        ;;
    --help|help|-h)
        echo "Usage: $0 [option]"
        echo "Options:"
        echo "  --gui      Start with GUI interface"
        echo "  --console  Start with console interface"
        echo "  --build    Build executable JAR"
        echo "  --test     Run tests"
        echo "  --clean    Clean project"
        echo "  --help     Show this help"
        exit 0
        ;;
esac

# Interactive mode
while true; do
    show_menu
    read -p "Enter your choice (1-6): " choice
    echo
    
    case $choice in
        1)
            run_gui
            break
            ;;
        2)
            run_console
            break
            ;;
        3)
            build_jar
            echo
            read -p "Press Enter to continue..."
            ;;
        4)
            run_tests
            echo
            read -p "Press Enter to continue..."
            ;;
        5)
            clean_project
            echo
            read -p "Press Enter to continue..."
            ;;
        6)
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid option. Please choose 1-6."
            echo
            ;;
    esac
done
