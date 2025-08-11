# Real-Time Face Recognition System

A comprehensive Java application for real-time face recognition using OpenCV and JavaCV. This system provides both GUI and command-line interfaces for detecting and recognizing faces from webcam input with professional-grade architecture and comprehensive testing.

## Features

- **Real-time Face Detection**: Uses Haar Cascade classifiers for fast and accurate face detection
- **Face Recognition**: Implements face embedding generation using histogram, LBP, and edge features
- **Person Registration**: Easy registration of new persons with face data storage
- **Database Management**: SQLite database for storing face embeddings and person information
- **Dual Interface**: Both Swing GUI and command-line interfaces available
- **Configurable System**: Externalized configuration with application.properties
- **Professional Architecture**: Clean OOP design with proper exception handling
- **Comprehensive Testing**: JUnit 5 tests with nested test structure
- **Utility Functions**: Common helper methods and validation utilities

## System Requirements

- **Java**: JDK 11 or higher
- **Maven**: 3.6 or higher
- **Webcam**: Any USB or built-in camera
- **Operating System**: Windows, macOS, or Linux
- **Memory**: Minimum 2GB RAM recommended

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── facerecognition/
│   │           ├── Main.java                     # Main entry point
│   │           ├── config/
│   │           │   └── ConfigurationManager.java # Configuration management
│   │           ├── core/
│   │           │   ├── FaceDetector.java         # Haar cascade face detection
│   │           │   └── FaceRecognizer.java       # Face recognition & embeddings
│   │           ├── database/
│   │           │   └── DatabaseManager.java      # SQLite database operations
│   │           ├── exception/                    # Custom exception classes
│   │           │   ├── FaceRecognitionException.java
│   │           │   ├── FaceDetectionException.java
│   │           │   └── DatabaseException.java
│   │           ├── gui/
│   │           │   └── FaceRecognitionGUI.java   # Swing GUI interface
│   │           └── util/
│   │               └── FaceRecognitionUtils.java # Utility functions
│   └── resources/
│       ├── application.properties                # Configuration file
│       ├── haarcascade_frontalface_alt.xml      # Face detection model
│       └── logback.xml                          # Logging configuration
├── test/
│   └── java/
│       └── com/
│           └── facerecognition/
│               └── FaceRecognitionTest.java      # Comprehensive test suite
pom.xml                                          # Maven dependencies & build config
.gitignore                                       # Git ignore rules
run.sh                                          # Startup script
```

## Configuration

The application uses `application.properties` for configuration management. Key settings include:

- **Camera Settings**: Resolution, frame rate
- **Recognition Settings**: Threshold, distance parameters
- **Database Settings**: File location, initialization
- **Face Detection Settings**: Scale factor, neighbor detection
- **GUI Settings**: Window dimensions, title

## Installation & Setup

### 1. Clone or Extract the Project

```bash
cd "/home/masood/Documents/GitHub/DSA-Projects/Real-Time Face Recognition"
```

### 2. Install Maven (if not already installed)

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install maven
```

**CentOS/RHEL/Fedora:**
```bash
sudo yum install maven    # CentOS/RHEL
sudo dnf install maven    # Fedora
```

**macOS:**
```bash
brew install maven
```

**Windows:**
Download from https://maven.apache.org/download.cgi and add to PATH

### 3. Verify Java and Maven Installation

```bash
java -version   # Should be 11 or higher
mvn -version    # Should be 3.6 or higher
```

### 4. Install Dependencies

```bash
mvn clean install
```

This will download all required dependencies including:
- JavaCV Platform (includes OpenCV binaries)
- SQLite JDBC driver
- Logback for logging
- JUnit for testing

## Running the Application

### Option 1: GUI Interface (Recommended)

```bash
mvn exec:java -Dexec.mainClass="com.facerecognition.Main" -Dexec.args="--gui"
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/real-time-face-recognition-1.0-SNAPSHOT-shaded.jar --gui
```

### Option 2: Command Line Interface

```bash
mvn exec:java -Dexec.mainClass="com.facerecognition.Main"
```

Or with the JAR:

```bash
java -jar target/real-time-face-recognition-1.0-SNAPSHOT-shaded.jar
```

## How to Use

### Using the GUI Interface

1. **Start the Application**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.facerecognition.Main" -Dexec.args="--gui"
   ```

2. **Start Camera**: Click the "Start Camera" button to begin face detection

3. **Register a Person**:
   - Enter the person's name in the text field
   - Ensure the person's face is clearly visible to the camera
   - Click "Register Person"
   - The system will capture their face and store the embedding

4. **Recognition**: Once registered, the system will automatically recognize and label faces

5. **Manage Persons**: Use the person list to view and delete registered individuals

### Using the Command Line Interface

1. **Start the Application**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.facerecognition.Main"
   ```

2. **Automatic Operation**: The camera window will open and start detecting faces immediately

3. **Registration**: Use the GUI interface for person registration (command-line registration can be added if needed)

## Configuration

### Recognition Threshold

The recognition threshold can be adjusted in `Main.java`:

```java
private static final double RECOGNITION_THRESHOLD = 0.6;
```

- **Lower values (0.3-0.5)**: More strict matching, fewer false positives
- **Higher values (0.6-0.8)**: More lenient matching, may allow more false positives

### Camera Settings

Camera parameters can be modified in `Main.java`:

```java
private static final int CAMERA_WIDTH = 640;
private static final int CAMERA_HEIGHT = 480;
private static final int FRAME_RATE = 30;
```

### Face Detection Parameters

Fine-tune detection in `FaceDetector.java`:

```java
private static final double SCALE_FACTOR = 1.1;
private static final int MIN_NEIGHBORS = 5;
private static final Size MIN_SIZE = new Size(30, 30);
private static final Size MAX_SIZE = new Size(300, 300);
```

## Database

The application uses SQLite database (`face_recognition.db`) to store:

- **persons**: Person names and metadata
- **face_embeddings**: Face feature vectors for recognition

The database is automatically created on first run and includes:
- Foreign key constraints
- Indexes for performance
- Automatic timestamp tracking

## Troubleshooting

### Camera Issues

1. **Camera not detected**:
   - Ensure camera is connected and not being used by other applications
   - Try changing camera index in `Main.java` (default is 0)

2. **Permission denied**:
   - On Linux: Add user to video group: `sudo usermod -a -G video $USER`
   - On macOS: Grant camera permissions in System Preferences

### Dependencies Issues

1. **JavaCV native libraries not found**:
   ```bash
   mvn clean install -U
   ```

2. **OutOfMemoryError**:
   ```bash
   export MAVEN_OPTS="-Xmx2g -Xms1g"
   mvn exec:java ...
   ```

### Face Recognition Issues

1. **Poor recognition accuracy**:
   - Ensure good lighting conditions
   - Register multiple images of the same person
   - Adjust recognition threshold
   - Keep face centered and at moderate distance

2. **No faces detected**:
   - Check camera positioning
   - Ensure adequate lighting
   - Verify Haar cascade model is loaded

## Performance Tips

1. **For better accuracy**:
   - Use consistent lighting during registration and recognition
   - Register faces from multiple angles
   - Keep the camera at eye level
   - Ensure faces are clearly visible (not partially obscured)

2. **For better performance**:
   - Close other applications using the camera
   - Use a dedicated GPU if available
   - Reduce camera resolution if experiencing lag

## Extending the System

### Adding New Features

1. **Custom Face Recognition Models**:
   - Replace the basic feature extraction in `FaceRecognizer.java`
   - Integrate deep learning models like FaceNet or ArcFace

2. **Additional Databases**:
   - Modify `DatabaseManager.java` to support MySQL, PostgreSQL, etc.

3. **REST API**:
   - Add Spring Boot for web interface and API endpoints

4. **Multiple Cameras**:
   - Extend `Main.java` to support multiple camera inputs

### Code Structure

The application follows a clean modular architecture:

- **Main.java**: Entry point and orchestration
- **core/**: Core face detection and recognition logic
- **database/**: Data persistence layer
- **gui/**: User interface components

## Logging

Logs are written to:
- **Console**: Real-time application status
- **face-recognition.log**: Detailed file logging

Log levels can be configured in `src/main/resources/logback.xml`.

## License

This project is for educational and research purposes. Please ensure compliance with OpenCV license terms when using this software.

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review the log files for error details
3. Ensure all system requirements are met
4. Verify camera and permissions setup

## Future Enhancements

- Integration with deep learning models (TensorFlow, PyTorch)
- Web-based interface
- Mobile app companion
- Cloud storage integration
- Advanced authentication features
- Multi-face recognition in single frame
- Real-time performance analytics
