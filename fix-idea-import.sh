#!/bin/bash

# Fix IntelliJ IDEA Gradle Import Script
# This script helps resolve common Gradle import issues in IntelliJ IDEA

set -e

echo "🔧 Revolution Tools - IntelliJ IDEA Import Fix"
echo "=============================================="
echo ""

# Check Java version
echo "📋 Step 1: Checking Java version..."
if [ -n "$JAVA_HOME" ]; then
    echo "✅ JAVA_HOME is set: $JAVA_HOME"
    java -version 2>&1 | head -1
else
    echo "⚠️  JAVA_HOME not set. Setting to Java 21..."
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
    echo "✅ JAVA_HOME set to: $JAVA_HOME"
fi

# Verify Java 21 is available
JAVA_VERSION=$(java -version 2>&1 | grep -i version | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" != "21" ]; then
    echo "❌ ERROR: Java 21 is required but version $JAVA_VERSION found"
    echo ""
    echo "Please install Java 21:"
    echo "  brew install openjdk@21"
    echo ""
    echo "Then set JAVA_HOME:"
    echo "  export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
    exit 1
fi

echo "✅ Java 21 detected"
echo ""

# Clean Gradle caches
echo "📋 Step 2: Cleaning Gradle caches..."
rm -rf .gradle
rm -rf build
rm -rf .idea
rm -rf out
echo "✅ Local caches cleaned"
echo ""

# Stop any running Gradle daemons
echo "📋 Step 3: Stopping Gradle daemons..."
./gradlew --stop
echo "✅ Gradle daemons stopped"
echo ""

# Refresh dependencies
echo "📋 Step 4: Refreshing Gradle dependencies..."
./gradlew clean build --refresh-dependencies --no-daemon
echo "✅ Dependencies refreshed and project built successfully"
echo ""

# Generate IntelliJ IDEA files
echo "📋 Step 5: Generating IntelliJ IDEA configuration..."
./gradlew cleanIdea idea --no-daemon
echo "✅ IntelliJ IDEA files generated"
echo ""

echo "✅ All steps completed successfully!"
echo ""
echo "📝 Next steps in IntelliJ IDEA:"
echo "  1. Close IntelliJ IDEA if it's open"
echo "  2. Reopen the project: File → Open → Select this folder"
echo "  3. When prompted, click 'Trust Project'"
echo "  4. Wait for Gradle sync to complete (watch bottom status bar)"
echo "  5. Enable annotation processing:"
echo "     Preferences → Build, Execution, Deployment → Compiler → Annotation Processors"
echo "     ☑️ Enable annotation processing"
echo "  6. Install Lombok plugin if not already installed:"
echo "     Preferences → Plugins → Search 'Lombok' → Install"
echo "  7. Restart IntelliJ IDEA if you installed Lombok"
echo ""
echo "📚 For detailed setup instructions, see: INTELLIJ_SETUP.md"
echo ""
echo "🚀 Ready to import into IntelliJ IDEA!"
