# Quick Start Guide

## Prerequisites Setup

1. **Install Java 11+**:
   ```bash
   sudo apt install openjdk-11-jdk  # Ubuntu/Debian
   ```

2. **Install Maven**:
   ```bash
   sudo apt install maven  # Ubuntu/Debian
   ```

3. **Verify Installation**:
   ```bash
   java -version
   mvn -version
   ```

## Running the Application

### Method 1: Using the Startup Script (Recommended)
```bash
./run.sh
```

### Method 2: Direct Maven Commands

**GUI Mode:**
```bash
mvn exec:java -Dexec.mainClass="com.facerecognition.Main" -Dexec.args="--gui"
```

**Console Mode:**
```bash
mvn exec:java -Dexec.mainClass="com.facerecognition.Main"
```

### Method 3: Build and Run JAR
```bash
mvn clean package
java -jar target/real-time-face-recognition-1.0-SNAPSHOT-shaded.jar --gui
```

## Usage Steps

1. **Start Application** with GUI mode
2. **Click "Start Camera"** to begin face detection
3. **Register Person**:
   - Enter name in text field
   - Ensure face is visible to camera
   - Click "Register Person"
4. **Automatic Recognition**: System will recognize registered faces

## Troubleshooting

- **No camera detected**: Check camera permissions and connections
- **Build failures**: Ensure Java 11+ and Maven 3.6+ are installed
- **Poor recognition**: Ensure good lighting and clear face visibility

For detailed information, see the main README.md file.
