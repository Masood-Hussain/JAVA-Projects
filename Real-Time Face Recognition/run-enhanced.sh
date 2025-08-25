#!/bin/bash

# Enhanced Real-Time Face Recognition System Launcher
# Author: Face Recognition System
# Version: 2.0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ASCII Art Banner
print_banner() {
    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════════════════════════════════╗"
    echo "║                    ADVANCED REAL-TIME FACE RECOGNITION SYSTEM               ║"
    echo "║                                  Version 2.0                                ║"
    echo "╚══════════════════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Print colored status message
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Check system requirements
check_requirements() {
    print_status "Checking system requirements..."
    
    # Check Java version
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f 2)
        print_status "Java version: $JAVA_VERSION"
        
        # Check if Java 11 or higher
        JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d'.' -f 1)
        if [ "$JAVA_MAJOR" -lt 11 ] && [ "$JAVA_MAJOR" != "1" ]; then
            print_error "Java 11 or higher is required. Current version: $JAVA_VERSION"
            return 1
        fi
    else
        print_error "Java is not installed. Please install Java 11 or higher."
        return 1
    fi
    
    # Check camera availability
    if [ -e /dev/video0 ]; then
        print_status "Camera detected: /dev/video0"
    else
        print_warning "No camera detected at /dev/video0. The system may not work properly."
    fi
    
    # Check memory
    MEMORY_GB=$(free -g | awk '/^Mem:/{print $2}')
    if [ "$MEMORY_GB" -lt 2 ]; then
        print_warning "Low memory detected: ${MEMORY_GB}GB. Recommended: 4GB or more."
    else
        print_status "Memory: ${MEMORY_GB}GB"
    fi
    
    return 0
}

# Check if application is already built
check_build() {
    if [ -f "target/real-time-face-recognition-1.0-SNAPSHOT.jar" ]; then
        JAR_SIZE=$(du -h target/real-time-face-recognition-1.0-SNAPSHOT.jar | cut -f1)
        print_status "Application JAR found (${JAR_SIZE})"
        return 0
    else
        print_warning "Application JAR not found. Building..."
        return 1
    fi
}

# Build the application
build_application() {
    print_status "Building the application..."
    
    if command -v mvn &> /dev/null; then
        echo -e "${BLUE}Building with Maven...${NC}"
        mvn clean package -DskipTests -q
        
        if [ $? -eq 0 ]; then
            print_success "Application built successfully!"
            return 0
        else
            print_error "Build failed. Please check Maven configuration."
            return 1
        fi
    else
        print_error "Maven is not installed. Please install Maven to build the application."
        return 1
    fi
}

# Show usage help
show_help() {
    echo -e "${PURPLE}Usage: $0 [OPTIONS]${NC}"
    echo ""
    echo "Options:"
    echo "  --gui          Launch with graphical user interface (default)"
    echo "  --console      Launch with console interface"
    echo "  --build        Force rebuild the application"
    echo "  --check        Check system requirements only"
    echo "  --help         Show this help message"
    echo "  --version      Show version information"
    echo ""
    echo "Examples:"
    echo "  $0                    # Launch with GUI"
    echo "  $0 --gui             # Launch with GUI"
    echo "  $0 --console         # Launch with console"
    echo "  $0 --build --gui     # Rebuild and launch with GUI"
}

# Show version information
show_version() {
    echo -e "${CYAN}Advanced Real-Time Face Recognition System${NC}"
    echo -e "${PURPLE}Version: 2.0${NC}"
    echo -e "${BLUE}Author: Face Recognition System${NC}"
    echo -e "${GREEN}Features:${NC}"
    echo "  • Advanced face detection and recognition"
    echo "  • Real-time video processing"
    echo "  • Person database management"
    echo "  • Anti-spoofing protection"
    echo "  • Quality assessment"
    echo "  • Performance optimization"
    echo "  • Advanced settings configuration"
}

# Launch application
launch_application() {
    local mode=$1
    print_status "Launching Face Recognition System in $mode mode..."
    
    # Set JVM options for better performance
    JAVA_OPTS="-Xmx2g -Xms512m -XX:+UseG1GC -XX:+UseStringDeduplication"
    
    if [ "$mode" = "GUI" ]; then
        print_status "Starting GUI interface..."
        java $JAVA_OPTS -jar target/real-time-face-recognition-1.0-SNAPSHOT.jar --gui
    else
        print_status "Starting console interface..."
        java $JAVA_OPTS -jar target/real-time-face-recognition-1.0-SNAPSHOT.jar --console
    fi
}

# Main execution
main() {
    local mode="GUI"
    local force_build=false
    local check_only=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --gui)
                mode="GUI"
                shift
                ;;
            --console)
                mode="CONSOLE"
                shift
                ;;
            --build)
                force_build=true
                shift
                ;;
            --check)
                check_only=true
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            --version)
                show_version
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # Print banner
    print_banner
    
    # Check system requirements
    if ! check_requirements; then
        print_error "System requirements not met. Exiting."
        exit 1
    fi
    
    if [ "$check_only" = true ]; then
        print_success "System requirements check completed."
        exit 0
    fi
    
    # Check build status
    if [ "$force_build" = true ] || ! check_build; then
        if ! build_application; then
            print_error "Failed to build application. Exiting."
            exit 1
        fi
    fi
    
    # Launch application
    print_success "System is ready!"
    echo ""
    launch_application "$mode"
}

# Run main function with all arguments
main "$@"
