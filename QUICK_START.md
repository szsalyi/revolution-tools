# Quick Start Guide - Web Scraping

## Setup (5 minutes)

### 1. Set Environment Variables

```bash
export POKERSTARS_USERNAME="your_username"
export POKERSTARS_PASSWORD="your_password"
export PLAYWRIGHT_HEADLESS="false"  # Show browser for testing
```

### 2. Run the Application

```bash
./gradlew bootRun
```

### 3. Start a Scraping Session

```bash
curl -X POST http://localhost:8080/api/game-sessions \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "table-123",
    "gameType": "TEXAS_HOLDEM"
  }'
```

Response:
```json
{
  "sessionId": "abc-123-def",
  "status": "INITIATED"
}
```

## What Happens Next

1. ✅ Browser opens automatically
2. ✅ Logs into PokerStars
3. ✅ Navigates to the game table
4. ✅ Starts scraping every 5 seconds
5. ✅ Evaluates rules (pot size, stack, premium hands)
6. ✅ Sends notifications when rules trigger

## Watch the Logs

```bash
# In another terminal
tail -f logs/revolution-tools.log | grep "Rule triggered"
```

You'll see notifications like:
```
╔════════════════════════════════════════════════════════════════╗
║ NOTIFICATION: RULE_TRIGGERED                                   ║
╠════════════════════════════════════════════════════════════════╣
║ Session: abc-123-def                                           ║
║ Severity: MEDIUM                                               ║
║ Title: Large Pot Size Detection                                ║
╠════════════════════════════════════════════════════════════════╣
║ Large pot detected: $150.50 (threshold: $100.00) in FLOP phase ║
╠════════════════════════════════════════════════════════════════╣
║ Rule: Large Pot Size Detection                                 ║
║ Confidence: 75.25%                                              ║
╚════════════════════════════════════════════════════════════════╝
```

## Stop Session

```bash
curl -X DELETE http://localhost:8080/api/game-sessions/abc-123-def
```

## Configure Rules

Edit `application.yml`:

```yaml
pokerstars:
  scraping:
    interval-seconds: 3  # Scrape every 3 seconds (default: 5)

revolution:
  rules:
    notification-cooldown-seconds: 30  # Cool-down between notifications
```

## Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/game-sessions` | POST | Start scraping |
| `/api/game-sessions/{id}` | GET | Get session details |
| `/api/game-sessions/active` | GET | List active sessions |
| `/api/game-sessions/{id}` | DELETE | Stop scraping |
| `/actuator/health` | GET | Health check |

## Next Steps

1. ✅ **Customize selectors** in `application.yml` to match actual PokerStars UI
2. ✅ **Add custom rules** by creating new classes in `src/main/java/com/revolution/tools/rule/impl/`
3. ✅ **Add notification channels** (email, Slack, etc.) in `NotificationServiceImpl`
4. ✅ **Analyze with Claude AI** using `/api/game-sessions/{id}/analyze` endpoint

See `WEB_SCRAPING_GUIDE.md` for detailed documentation.
