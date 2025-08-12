#!/bin/bash

# Simple run script without color codes for compatibility
cd "/home/masood/Documents/GitHub/DSA-Projects/Real-Time Face Recognition"

echo "Real-Time Face Recognition System"
echo "=================================="

# Check if compiled
if [ ! -d "target/classes" ]; then
    echo "Compiling project..."
    mvn compile -q
fi

# Run with GUI
echo "Starting Face Recognition System with GUI..."
echo "Note: Close the GUI window to stop the application"
echo ""

# Set TERM to avoid color issues
export TERM=dumb
MAVEN_OPTS="-Djansi.force=false -Djansi.disable=true" mvn exec:java -Dexec.mainClass="com.facerecognition.Main" -Dexec.args="--gui" -q
