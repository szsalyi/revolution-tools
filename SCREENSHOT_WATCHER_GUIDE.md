# Screenshot Watcher - User Guide

**Last Updated:** 2025-12-27
**Status:** ✅ Ready for Production Testing

---

## 🎯 Overview

The **Screenshot Watcher** is an automated system that monitors a folder for new roulette screenshots and automatically analyzes them using Claude Vision AI. This eliminates manual data entry and makes your workflow seamless.

### How It Works

```
1. You play roulette on Evolution Gaming
2. Press screenshot hotkey (Cmd+Shift+4 on macOS)
3. Capture the winning number history strip
4. Screenshot auto-saves to watched folder
   ↓
5. App detects new file within 1 second
6. Claude Vision analyzes screenshot automatically
7. Winning numbers extracted
8. Active session updated automatically
9. Hot numbers detected
10. Bet suggestions generated
   ↓
11. You decide what to bet (or stop if alerted)
```

### Safety

✅ **100% Safe from Casino Detection**
- No browser automation
- No DOM scraping
- Just file system monitoring
- Takes screenshots like any normal user
- Casino sees: Normal player behavior

---

## 🚀 Quick Start

### Step 1: Enable the Screenshot Watcher

Edit `application.yml` or set environment variable:

```yaml
# Option 1: Edit application.yml
roulette:
  screenshot:
    watcher:
      enabled: true
      directory: ${user.home}/Desktop/RouletteScreenshots
```

**OR**

```bash
# Option 2: Environment variable
export ROULETTE_SCREENSHOT_WATCHER_ENABLED=true
export ROULETTE_SCREENSHOT_DIR=~/Desktop/RouletteScreenshots
```

### Step 2: Set Your CLAUDE_API_KEY

```bash
export CLAUDE_API_KEY="your-claude-api-key-here"
```

### Step 3: Start the Application

```bash
./gradlew bootRun
```

**Expected Output:**
```
✅ Screenshot watcher started successfully!
   Watching directory: /Users/yourname/Desktop/RouletteScreenshots
   Auto-analysis: ENABLED
   Archive processed: true

📸 Ready for screenshots! Press your screenshot hotkey (Cmd+Shift+4) and capture the roulette history.
```

### Step 4: Start a Roulette Session

```bash
curl -X POST http://localhost:8080/api/roulette/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "initialBankroll": 500.00,
    "stopLossPercent": -20,
    "takeProfitLevels": [70, 130],
    "maxSpins": 100,
    "maxDurationMinutes": 90
  }'
```

**Save the sessionId from the response.**

### Step 5: Take Your First Screenshot

1. Open Evolution Roulette in your browser
2. Wait for the history strip to be visible
3. Press **Cmd+Shift+4** (macOS) or **Win+Shift+S** (Windows)
4. Capture the winning number history strip
5. Screenshot auto-saves to `~/Desktop/RouletteScreenshots`

**What happens:**
```
📸 New screenshot detected: Screenshot 2025-12-27 at 10.30.15.png
🔍 Analyzing screenshot with Claude Vision...
✅ Analysis complete!
   📊 Extracted 20 numbers
   🎯 Confidence: 95.0%
   💾 Session updated: 20 spins added
   🔥 Hot numbers: [17, 32, 25]
   💡 Suggested: [17, 32, 25, 34, 6, 27]
   🎰 Section: VOISINS_DU_ZERO (60% activity)
```

### Step 6: Continue Playing

- Take screenshots after each spin (or every few spins)
- App auto-updates your session
- Check suggestions in real-time
- Monitor for stop-loss/take-profit alerts

---

## 📁 Directory Structure

After running for a while, your screenshot directory looks like this:

```
~/Desktop/RouletteScreenshots/
├── processed/                      # Successfully analyzed screenshots
│   ├── 20251227_103015_Screenshot.png
│   ├── 20251227_103045_Screenshot.png
│   └── ...
│
├── errors/                         # Failed screenshots (for review)
│   ├── no-history-detected/
│   │   └── 20251227_104000_Screenshot.png
│   ├── analysis-failed/
│   │   └── 20251227_105000_Screenshot.png
│   └── processing-error/
│       └── 20251227_110000_Screenshot.png
│
└── (New screenshots appear here temporarily, then move to processed/ or errors/)
```

---

## 🎮 User Workflows

### Workflow 1: Fully Automated (Recommended)

```
1. Start session
2. Play roulette
3. Take screenshot every 5-10 spins
4. App auto-updates everything
5. Check dashboard occasionally for alerts
6. Stop when alerted (stop-loss or take-profit)
```

**Pros:**
- Zero manual data entry
- Fast workflow
- Real-time analysis

**Cons:**
- Requires Claude AI API key (costs ~$0.01 per screenshot)
- Need to take screenshots manually

---

### Workflow 2: Hybrid (Screenshots + Manual)

```
1. Start session
2. Take screenshot to import history (20+ numbers)
3. Then use manual quick spin entry for new spins
4. Periodically take screenshots to verify sync
```

**Pros:**
- Fast initial setup (paste history)
- Manual entry for real-time spins (faster than screenshot)
- Screenshots as backup/verification

**Cons:**
- Need to switch between manual and screenshot modes

---

### Workflow 3: Manual Upload (No Watcher)

If you don't want the automatic watcher, you can manually upload screenshots:

```bash
# Disable watcher
roulette.screenshot.watcher.enabled: false

# Upload via API
curl -X POST "http://localhost:8080/api/roulette/sessions/{sessionId}/history/from-screenshot?autoUpdate=true" \
  -F "screenshot=@/path/to/screenshot.png"
```

---

## 🧪 Testing the Screenshot Watcher

### Test 1: Basic Screenshot Detection

1. Start the app with watcher enabled
2. Create a test image: `~/Desktop/RouletteScreenshots/test.png`
3. Watch the logs for detection

**Expected Log:**
```
📸 New screenshot detected: test.png
🔍 Analyzing screenshot with Claude Vision: test.png
```

### Test 2: Real Evolution Roulette Screenshot

1. Open Evolution Roulette (demo mode is fine)
2. Take screenshot of history strip
3. Wait 1-2 seconds
4. Check logs for analysis results

**Expected Log:**
```
✅ Analysis complete!
   📊 Extracted 20 numbers
   🎯 Confidence: 95.0%
```

### Test 3: Session Auto-Update

1. Start a roulette session (note sessionId)
2. Take roulette screenshot
3. Check session via API:

```bash
curl http://localhost:8080/api/roulette/sessions/{sessionId}
```

**Expected:** totalSpins should increase by number of extracted numbers

### Test 4: Error Handling (No History Visible)

1. Take screenshot of random image (not roulette)
2. Watch for error handling

**Expected Log:**
```
❌ No game history detected in screenshot
   Notes: No history strip visible in image
```

---

## 🎯 Taking Good Screenshots

### What to Capture

✅ **DO Capture:**
- The winning number history strip (horizontal row of numbers)
- Make sure numbers are clearly visible
- Include as many numbers as possible (20-50 is ideal)
- Full frame of history strip

❌ **DON'T Capture:**
- Blurry images
- Partial numbers cut off
- Too much extra content (just focus on history)
- Dark/low contrast screenshots

### Screenshot Tips

1. **Focus on history strip only** - No need to capture entire screen
2. **Good lighting** - Make sure browser is in light mode (if possible)
3. **Full resolution** - Don't zoom out too much
4. **Steady hand** - Avoid blurry screenshots
5. **Wait for spin to complete** - Make sure latest number is visible

### Example Good Screenshot

```
┌─────────────────────────────────────────────────────┐
│  32 | 15 | 19 | 4 | 21 | 2 | 25 | 17 | 34 | 6 | ... │  ← History strip
└─────────────────────────────────────────────────────┘
```

---

## 🔧 Configuration Options

### application.yml Settings

```yaml
roulette:
  screenshot:
    watcher:
      # Enable/disable watcher
      enabled: true

      # Watch directory (can use ${user.home} for user's home folder)
      directory: ${user.home}/Desktop/RouletteScreenshots

      # Delay before processing (ms) - allows file to be fully written
      process-delay-ms: 500

      # Archive successfully processed screenshots
      archive-processed: true

      # Delete processed screenshots (false = move to processed/)
      delete-after-processing: false

      # Move failed screenshots to errors/ folder
      move-errors: true
```

### Environment Variables

```bash
# Enable/disable watcher
export ROULETTE_SCREENSHOT_WATCHER_ENABLED=true

# Custom screenshot directory
export ROULETTE_SCREENSHOT_DIR=/custom/path/to/screenshots

# Claude API key (required for analysis)
export CLAUDE_API_KEY=your-api-key-here
```

---

## 📊 API Endpoints

### Manual Screenshot Upload (Alternative to Watcher)

```bash
# Analyze screenshot and auto-update session
POST /api/roulette/sessions/{sessionId}/history/from-screenshot?autoUpdate=true
Content-Type: multipart/form-data
Body: screenshot file

# Just analyze (don't update session)
POST /api/roulette/sessions/{sessionId}/history/from-screenshot?autoUpdate=false

# Analyze without session (standalone)
POST /api/roulette/history/analyze-screenshot
```

### Example with curl

```bash
# Auto-update session with screenshot
curl -X POST "http://localhost:8080/api/roulette/sessions/abc123/history/from-screenshot?autoUpdate=true" \
  -F "screenshot=@~/Desktop/roulette-history.png"

# Response:
{
  "historyNumbers": [32, 15, 19, 4, 21, 2, 25, 17, 34, 6, ...],
  "currentWinner": 6,
  "extractedSpinCount": 20,
  "confidence": 0.95,
  "sessionUpdated": true,
  "spinsAdded": 20,
  "detectedHotNumbers": [17, 32, 25],
  "suggestedNumbers": [17, 32, 25, 34, 6, 27],
  "sectionAnalysis": "VOISINS_DU_ZERO",
  "success": true
}
```

---

## ⚠️ Troubleshooting

### Issue: "Screenshot watcher not starting"

**Check:**
1. Is `roulette.screenshot.watcher.enabled` set to `true`?
2. Does the directory exist? (Auto-created if not, but check permissions)
3. Check logs for error messages

**Solution:**
```bash
# Create directory manually
mkdir -p ~/Desktop/RouletteScreenshots

# Set permissions (macOS/Linux)
chmod 755 ~/Desktop/RouletteScreenshots
```

---

### Issue: "No screenshots detected"

**Check:**
1. Are you saving screenshots to the correct directory?
2. Check directory path in logs
3. File format: Must be PNG, JPG, JPEG, GIF, or BMP

**Solution:**
```bash
# Verify watcher is running
curl http://localhost:8080/api/roulette/health

# Check logs for:
"✅ Screenshot watcher started successfully!"
"Watching directory: /Users/yourname/Desktop/RouletteScreenshots"
```

---

### Issue: "Screenshot detected but not analyzed"

**Check:**
1. Is Claude API key set? (`CLAUDE_API_KEY` environment variable)
2. Check logs for Claude API errors
3. Check file size (very large files may timeout)

**Solution:**
```bash
# Verify Claude API key
echo $CLAUDE_API_KEY

# Test Claude API manually
curl -X POST http://localhost:8080/api/roulette/history/analyze-screenshot \
  -F "screenshot=@~/Desktop/test-screenshot.png"
```

---

### Issue: "Analysis failed - no history detected"

**Possible Causes:**
1. Screenshot doesn't contain history strip
2. Numbers are blurry or cut off
3. Screenshot is too dark/low contrast

**Solution:**
- Take a new screenshot focusing on history strip
- Make sure numbers are clearly visible
- Check errors/ folder for failed screenshots
- Review screenshots manually to see what Claude sees

---

### Issue: "Confidence score is low (<80%)"

**What it means:**
- Claude is uncertain about some numbers
- Image quality may be poor

**Solution:**
- Retake screenshot with better quality
- Manual verification recommended
- Check warnings in response

---

### Issue: "No active session - history not imported"

**What it means:**
- Watcher detected screenshot, but no session is running
- History was extracted but not saved anywhere

**Solution:**
1. Start a session first:
```bash
curl -X POST http://localhost:8080/api/roulette/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "initialBankroll": 500.00,
    "stopLossPercent": -20,
    "takeProfitLevels": [70, 130]
  }'
```
2. Then take screenshots

---

## 💰 Claude AI Costs

### Cost Estimation

**Claude 3.5 Sonnet Pricing (as of Dec 2024):**
- Input: $3.00 per million tokens
- Output: $15.00 per million tokens

**Typical Screenshot Analysis:**
- Input tokens: ~5,000 (image + prompt)
- Output tokens: ~500 (JSON response)
- **Cost per screenshot: ~$0.01 - $0.02**

**Example Monthly Costs:**
- 100 screenshots/month: ~$1-2
- 500 screenshots/month: ~$5-10
- 1000 screenshots/month: ~$10-20

### Cost Optimization Tips

1. **Don't screenshot every spin**
   - Screenshot every 5-10 spins instead
   - Use manual quick spin entry for recent spins

2. **Use manual upload for initial history**
   - Start session with historical data (paste 50 numbers)
   - Then use screenshots for updates

3. **Disable watcher when not playing**
   - Set `enabled: false` when not actively playing

4. **Batch screenshots**
   - Take one screenshot with 20-50 numbers
   - Better than 20 screenshots with 1 number each

---

## 🎯 Best Practices

### 1. Session Management

- **Always start a session BEFORE taking screenshots**
- Close sessions when done (stop endpoint)
- One active session at a time recommended

### 2. Screenshot Timing

- **After multiple spins:** Screenshot every 5-10 spins
- **Not every spin:** Too frequent = higher costs
- **Before leaving:** Screenshot before closing casino

### 3. Quality Control

- **Review errors folder** periodically
- If confidence <80%, manually verify numbers
- Delete bad screenshots from processed/ folder

### 4. Backup Strategy

- Keep processed/ folder for audit trail
- Archive errors/ folder monthly
- Export session data regularly

---

## 📈 Advanced Usage

### Custom Screenshot Directory per Session

```yaml
roulette:
  screenshot:
    watcher:
      directory: ${HOME}/Gaming/Roulette-${SESSION_ID}
```

### Multiple Watchers (Different Games)

Create separate watchers for different games:
- `~/Desktop/RouletteScreenshots` → Roulette watcher
- `~/Desktop/BlackjackScreenshots` → Blackjack watcher (future)

### Webhook Notifications

Add webhook notification when screenshot processed (future feature):
```yaml
roulette:
  screenshot:
    watcher:
      webhook-url: http://localhost:3000/webhook/screenshot-processed
```

---

## 🔍 Monitoring & Debugging

### Enable Debug Logging

```yaml
logging:
  level:
    com.revolution.tools.roulette.service.ScreenshotWatcherService: DEBUG
    com.revolution.tools.roulette.service.impl.GameHistoryAnalyzerServiceImpl: DEBUG
```

### Check Watcher Status via API

```bash
# Health check
curl http://localhost:8080/api/roulette/health

# Session health (includes screenshot analysis status)
curl http://localhost:8080/api/roulette/sessions/{sessionId}/health
```

### Review Processed Screenshots

```bash
# List processed screenshots
ls -lh ~/Desktop/RouletteScreenshots/processed/

# Count successful analyses
ls ~/Desktop/RouletteScreenshots/processed/ | wc -l

# Check for errors
ls -lh ~/Desktop/RouletteScreenshots/errors/*/
```

---

## ✅ Success Checklist

Before using in production, verify:

- [ ] Screenshot watcher starts successfully
- [ ] Test screenshot is detected within 1 second
- [ ] Claude API key is set and working
- [ ] Session auto-updates with extracted numbers
- [ ] Hot numbers are detected correctly
- [ ] Suggestions are generated
- [ ] Processed screenshots are archived
- [ ] Failed screenshots move to errors/
- [ ] Stop-loss/take-profit alerts work
- [ ] Logs are clear and informative

---

## 🎓 Next Steps

Once you've tested the screenshot watcher:

1. **Build a frontend UI**
   - Real-time dashboard showing screenshots processed
   - Visual display of hot numbers and suggestions
   - Alert notifications

2. **Add screenshot history viewer**
   - See all processed screenshots
   - Review what Claude extracted
   - Compare with actual casino data

3. **Implement screenshot quality scoring**
   - Pre-analyze screenshot quality before sending to Claude
   - Warn if image is too blurry/dark
   - Auto-retry with better settings

4. **Add voice input integration**
   - Say "Seventeen" → auto-record spin
   - Combined with screenshot verification

---

## 🚀 Production Deployment

When ready for production:

```bash
# Build JAR
./gradlew clean bootJar

# Run in production mode
java -jar build/libs/revolution-tools-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --CLAUDE_API_KEY=your-key \
  --ROULETTE_SCREENSHOT_WATCHER_ENABLED=true
```

---

**Last Updated:** 2025-12-27
**Version:** 1.0
**Status:** Ready for Testing

**Questions?** Check the logs, review the API responses, and test with sample screenshots first!
