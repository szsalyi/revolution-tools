# IntelliJ IDEA Setup Guide

This guide helps you properly import and configure the Revolution Tools project in IntelliJ IDEA.

## Prerequisites

- **IntelliJ IDEA**: 2023.3 or later (Ultimate or Community Edition)
- **Java 21**: Must be installed via Homebrew or manual installation
- **Gradle**: Will be managed by Gradle Wrapper (included in project)

## Step 1: Configure Java 21

### Option A: Via IntelliJ IDEA

1. Open IntelliJ IDEA
2. Go to **File → Project Structure** (⌘;)
3. Under **Project Settings → Project**:
   - **SDK**: Select or add Java 21
     - Click **Add SDK → Download JDK**
     - Choose **Oracle OpenJDK version 21** or use existing Homebrew Java 21
     - Location: `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`
   - **Language Level**: 21 - Pattern matching for switch

### Option B: Set JAVA_HOME (if not auto-detected)

Add to your shell profile (`~/.zshrc` or `~/.bash_profile`):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

Then reload:
```bash
source ~/.zshrc
```

## Step 2: Import the Project

### Fresh Import

1. **Close any existing project**
2. From IntelliJ IDEA start screen, click **Open**
3. Navigate to `/Users/szabolcssalyi/Code/Private/revolution-tools`
4. Select the project folder and click **Open**
5. In the **Trust Project** dialog, click **Trust Project**

### Import as Gradle Project

IntelliJ IDEA should automatically detect the Gradle project. If not:

1. Look for the notification: "Gradle build scripts found"
2. Click **Load Gradle Project**
3. Wait for Gradle to sync (this may take a few minutes)

## Step 3: Configure Gradle Settings

1. Go to **Preferences** (⌘,) → **Build, Execution, Deployment → Build Tools → Gradle**
2. Set the following:
   - **Build and run using**: Gradle (recommended for consistency)
   - **Run tests using**: Gradle
   - **Gradle JVM**: Project SDK (Java 21)
   - **Gradle**: Use Gradle from 'gradle-wrapper.properties'

3. Click **OK**

## Step 4: Enable Annotation Processing (for Lombok)

1. Go to **Preferences** (⌘,) → **Build, Execution, Deployment → Compiler → Annotation Processors**
2. Check **Enable annotation processing**
3. Set **Store generated sources relative to**: Module content root
4. Click **OK**

## Step 5: Install Lombok Plugin (if not already installed)

1. Go to **Preferences** (⌘,) → **Plugins**
2. Search for "Lombok"
3. Install the **Lombok** plugin by Michail Plushnikov
4. Restart IntelliJ IDEA

## Step 6: Refresh Gradle Dependencies

If dependencies are not importing correctly:

### Method 1: Gradle Tool Window

1. Open **View → Tool Windows → Gradle** (or click Gradle tab on right side)
2. Click the **Reload All Gradle Projects** button (🔄 icon)
3. Wait for sync to complete

### Method 2: Via Command Line

In the IntelliJ IDEA Terminal:

```bash
./gradlew clean build --refresh-dependencies
```

### Method 3: Invalidate Caches

If still having issues:

1. Go to **File → Invalidate Caches**
2. Select **Invalidate and Restart**
3. Wait for IntelliJ IDEA to restart and reindex

## Step 7: Verify Setup

### Run Tests

1. Open **View → Tool Windows → Gradle**
2. Expand **revolution-tools → Tasks → verification**
3. Double-click **test**
4. All 26 tests should pass ✅

### Run Application

1. Navigate to `src/main/java/com/revolution/tools/RevolutionToolsApplication.java`
2. Right-click the file
3. Select **Run 'RevolutionToolsApplication'**
4. Application should start on port 8080

## Troubleshooting

### Issue: "Cannot resolve symbol" errors

**Solutions:**
1. **Reimport Gradle Project**:
   - Gradle tool window → Right-click project → Reimport Gradle Project

2. **Rebuild Project**:
   - **Build → Rebuild Project**

3. **Clear Caches**:
   - **File → Invalidate Caches → Invalidate and Restart**

### Issue: Lombok annotations not recognized

**Solutions:**
1. Ensure Lombok plugin is installed
2. Enable annotation processing (Step 4)
3. Reimport Gradle project
4. Restart IDE

### Issue: Java version mismatch

**Symptoms:**
- Error: "java: invalid source release: 21"
- Error: "Unsupported class file major version"

**Solutions:**
1. Verify Project SDK:
   - **File → Project Structure → Project → SDK** = Java 21

2. Verify Module SDK:
   - **File → Project Structure → Modules** → Select each module → **Dependencies** tab
   - **Module SDK** = Project SDK

3. Verify Gradle JVM:
   - **Preferences → Build Tools → Gradle → Gradle JVM** = Java 21

### Issue: Gradle sync fails

**Solutions:**
1. **Check Internet Connection** (Gradle downloads dependencies)

2. **Use Gradle Wrapper**:
   ```bash
   ./gradlew wrapper --gradle-version 8.11.1
   ```

3. **Clean Gradle Cache**:
   ```bash
   rm -rf ~/.gradle/caches
   ./gradlew clean build --refresh-dependencies
   ```

4. **Check for Proxy Settings**:
   - **Preferences → Appearance & Behavior → System Settings → HTTP Proxy**

### Issue: Playwright dependencies missing

**Solution:**
After first build, install Playwright browsers:
```bash
./gradlew build
```

This will automatically download Playwright browser binaries on first run.

### Issue: Spring Boot DevTools causing hot reload issues

**Solution:**
If you don't want auto-restart:
1. Remove or comment out `developmentOnly 'org.springframework.boot:spring-boot-devtools'` in `build.gradle`
2. Reimport Gradle project

## Recommended IntelliJ IDEA Plugins

For optimal development experience:

1. **Lombok** (Required) - Handles Lombok annotations
2. **Spring Boot** (Recommended) - Spring Boot support
3. **Spring Data JPA Buddy** (Optional) - JPA entity management
4. **Rainbow Brackets** (Optional) - Bracket colorization
5. **Key Promoter X** (Optional) - Learn keyboard shortcuts

Install via: **Preferences → Plugins → Marketplace**

## Running Configuration

### Spring Boot Run Configuration

IntelliJ IDEA should auto-create a Spring Boot run configuration. To verify:

1. **Run → Edit Configurations**
2. Look for **Spring Boot → RevolutionToolsApplication**
3. Ensure:
   - **Main class**: `com.revolution.tools.RevolutionToolsApplication`
   - **Use classpath of module**: revolution-tools.main
   - **JRE**: 21

### Environment Variables

To set environment variables for Claude AI:

1. **Run → Edit Configurations**
2. Select **RevolutionToolsApplication**
3. Under **Environment variables**, add:
   ```
   CLAUDE_API_KEY=your-api-key-here
   CLAUDE_ENABLED=true
   ```
4. Click **OK**

## Code Style Settings (Optional)

To match project conventions:

1. **Preferences → Editor → Code Style → Java**
2. Click **Scheme gear icon** → **Import Scheme** → **IntelliJ IDEA code style XML**
3. Or manually set:
   - **Tab size**: 4
   - **Indent**: 4
   - **Continuation indent**: 8

## Useful Keyboard Shortcuts

| Action | Shortcut (macOS) |
|--------|------------------|
| Run Application | ⌃⌥R |
| Run Tests | ⌃⌥R |
| Rebuild Project | ⌘⇧F9 |
| Reimport Gradle | ⌘⇧I (in Gradle window) |
| Run Anything | ⌃⌃ (double Control) |
| Search Everywhere | ⇧⇧ (double Shift) |
| Navigate to Class | ⌘O |
| Navigate to File | ⌘⇧O |
| Find Usages | ⌥F7 |
| Format Code | ⌥⌘L |

## Next Steps

After successful setup:

1. ✅ Verify all tests pass
2. ✅ Run the application
3. ✅ Test REST endpoints via Postman or curl
4. ✅ Review `CLAUDE.md` for project guidelines
5. ✅ Start developing!

## Getting Help

If you continue to have issues:

1. Check IntelliJ IDEA logs: **Help → Show Log in Finder**
2. Check Gradle build output in Build tool window
3. Review error messages in Event Log (bottom right)
4. Try running Gradle commands directly in terminal first

---

**Last Updated**: 2025-11-29
