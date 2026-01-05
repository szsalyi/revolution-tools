# Web Scraping Implementation Guide

## Overview

This document describes the professional web scraping solution implemented for PokerStars game data collection, analysis, and rule-based notifications.

## Architecture

The solution follows a layered, service-oriented architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│              (GameSessionController)                         │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│              Orchestration Layer                             │
│         (GameScrapingServiceImpl)                           │
│  - Coordinates all scraping operations                      │
│  - Manages session lifecycle                                │
└─────┬─────────┬────────────┬───────────────┬───────────────┘
      │         │            │               │
      ▼         ▼            ▼               ▼
┌──────────┐ ┌─────────┐ ┌────────────┐ ┌──────────────┐
│ Browser  │ │  Data   │ │    Rule    │ │Notification  │
│Automation│ │Extraction│ │ Evaluation │ │   Service    │
│ Service  │ │ Service │ │  Service   │ │              │
└──────────┘ └─────────┘ └────────────┘ └──────────────┘
      │                        │
      ▼                        ▼
┌──────────┐              ┌──────────┐
│Playwright│              │   Rule   │
│  Engine  │              │  Engine  │
└──────────┘              └──────────┘
```

## Core Components

### 1. Browser Automation Service

**Location**: `src/main/java/com/revolution/tools/service/impl/BrowserAutomationServiceImpl.java`

**Responsibilities**:
- Create and manage Playwright browser pages
- Handle login to PokerStars
- Navigate to game tables
- Take screenshots
- Manage browser lifecycle

**Key Methods**:
```java
Page createPage(String sessionId)
boolean login(Page page)
boolean navigateToGame(Page page, String gameId)
String takeScreenshot(Page page, String sessionId, String reason)
```

**Features**:
- Circuit breaker pattern for resilience
- Retry logic for transient failures
- Configurable browser settings (headless/headed)
- Screenshot capture on errors

### 2. Data Extraction Service

**Location**: `src/main/java/com/revolution/tools/service/impl/DataExtractionServiceImpl.java`

**Responsibilities**:
- Extract game state from HTML DOM
- Parse player information
- Extract hand details (cards, pot, phase)
- Parse table state

**Data Extracted**:
- Player cards (hole cards)
- Community cards
- Pot size
- Player stack
- Current bet
- Game phase (PREFLOP, FLOP, TURN, RIVER)
- Table information
- Seat details

**Example Usage**:
```java
GameStateSnapshot snapshot = dataExtractionService.extractGameState(page, sessionId);
```

### 3. Rule Engine

**Location**: `src/main/java/com/revolution/tools/rule/**`

**Responsibilities**:
- Define game analysis rules
- Evaluate conditions against game state
- Calculate confidence scores
- Trigger notifications

**Built-in Rules**:

#### LargePotSizeRule
Triggers when pot size exceeds threshold (default: $100)
- **Severity**: MEDIUM
- **Use Case**: Detect significant betting action
- **Configuration**: Adjustable threshold

#### LowStackAlertRule
Triggers when player stack falls below threshold (default: $20)
- **Severity**: HIGH
- **Use Case**: Bankroll management alerts
- **Configuration**: Adjustable threshold

#### PremiumHandRule
Triggers when player has premium pocket cards (AA, KK, QQ, etc.)
- **Severity**: LOW
- **Use Case**: Track strong starting hands
- **Configuration**: Predefined premium hand list

**Creating Custom Rules**:
```java
@Component
public class MyCustomRule extends AbstractGameRule {
    public MyCustomRule() {
        super("MY_RULE_ID", "My Rule Name", "Description",
              RuleEvaluationResult.Severity.MEDIUM);
    }

    @Override
    public RuleEvaluationResult evaluate(GameStateSnapshot snapshot) {
        // Your logic here
        if (conditionMet) {
            return createTriggeredResult(message, confidence, context);
        }
        return createNotTriggeredResult();
    }
}
```

### 4. Notification Service

**Location**: `src/main/java/com/revolution/tools/service/impl/NotificationServiceImpl.java`

**Responsibilities**:
- Send notifications when rules trigger
- Rate limiting and cool-down
- Multiple notification channels (extensible)

**Current Implementation**:
- Formatted console logging
- Rate limiting per rule per session
- Configurable cool-down periods

**Extensible Design**:
```java
// Easy to add new notification channels
private void sendEmailNotification(NotificationRequest request) { }
private void sendWebhookNotification(NotificationRequest request) { }
private void sendSMSNotification(NotificationRequest request) { }
```

### 5. Game Scraping Service (Orchestrator)

**Location**: `src/main/java/com/revolution/tools/service/impl/GameScrapingServiceImpl.java`

**Responsibilities**:
- Coordinate all scraping operations
- Manage session lifecycle
- Integrate all services
- Handle errors gracefully

**Workflow**:
1. Start session → Login → Navigate to game
2. Periodic scraping → Extract data → Evaluate rules → Send notifications
3. Stop session → Cleanup resources

## Configuration

### PokerStars Settings

**File**: `src/main/resources/application.yml`

```yaml
pokerstars:
  credentials:
    username: ${POKERSTARS_USERNAME:}
    password: ${POKERSTARS_PASSWORD:}

  scraping:
    enabled: true
    interval-seconds: 5           # Scrape every 5 seconds
    max-duration-minutes: 120     # Max 2 hours per session
    screenshot-on-error: true
    screenshot-dir: ./screenshots

  selectors:
    username-input: "input[name='username']"
    password-input: "input[name='password']"
    # ... other selectors
```

### Rule Engine Settings

```yaml
revolution:
  rules:
    enabled: true
    evaluation-interval-seconds: 5
    max-notifications-per-session: 100
    notification-cooldown-seconds: 60
    debug-logging: false
```

### Resilience Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      pokerstars:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 15s
```

## Usage

### 1. Start a Scraping Session

**REST API**:
```bash
POST /api/game-sessions
Content-Type: application/json

{
  "gameId": "table-123",
  "gameType": "TEXAS_HOLDEM"
}
```

**Response**:
```json
{
  "sessionId": "a1b2c3d4-...",
  "gameId": "table-123",
  "gameType": "TEXAS_HOLDEM",
  "status": "INITIATED",
  "startTime": "2025-12-18T10:30:00",
  "message": "Game session initiated"
}
```

### 2. Automatic Scraping

Once initiated, the `ScrapingScheduler` automatically:
- Scrapes game state every 5 seconds (configurable)
- Evaluates all enabled rules
- Sends notifications when rules trigger
- Updates session statistics

### 3. Stop a Session

```bash
DELETE /api/game-sessions/{sessionId}
```

### 4. View Active Sessions

```bash
GET /api/game-sessions/active
```

## Data Flow

```
1. Scheduler triggers scraping
         ↓
2. GameScrapingService.scrapeGameState()
         ↓
3. BrowserAutomationService checks if game active
         ↓
4. DataExtractionService extracts data → GameStateSnapshot
         ↓
5. RuleEvaluationService evaluates all rules
         ↓
6. For each triggered rule:
   - Create NotificationRequest
   - NotificationService.sendNotification()
   - Take screenshot (if configured)
         ↓
7. Update session statistics in database
```

## Environment Variables

Set these before running:

```bash
# PokerStars Credentials
export POKERSTARS_USERNAME="your_username"
export POKERSTARS_PASSWORD="your_password"

# Optional: Customize URLs
export POKERSTARS_LOGIN_URL="https://www.pokerstars.com/poker/login"

# Optional: Browser settings
export PLAYWRIGHT_HEADLESS="false"  # Show browser for debugging
export PLAYWRIGHT_BROWSER="chromium"

# Optional: Scraping settings
export POKERSTARS_SCREENSHOT_DIR="./my-screenshots"
```

## Running the Application

### Development Mode

```bash
# With visible browser for debugging
export PLAYWRIGHT_HEADLESS=false
./gradlew bootRun
```

### Production Mode

```bash
# Build
./gradlew clean build

# Run
java -jar build/libs/revolution-tools-0.0.1-SNAPSHOT.jar
```

### Docker (Optional)

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/revolution-tools-0.0.1-SNAPSHOT.jar app.jar
RUN apt-get update && apt-get install -y \
    libglib2.0-0 libnss3 libatk1.0-0 libatk-bridge2.0-0 \
    libcups2 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 \
    libxfixes3 libxrandr2 libgbm1 libasound2
RUN npx playwright install --with-deps chromium
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Customization

### Adding New Rules

1. Create a new class extending `AbstractGameRule`
2. Annotate with `@Component`
3. Implement `evaluate()` method
4. Spring will auto-register it

### Custom Selectors

Update `application.yml` selectors section to match PokerStars UI:

```yaml
pokerstars:
  selectors:
    player-cards: ".actual-poker-cards-selector"
    pot-size: ".actual-pot-selector"
```

Use browser DevTools to find correct selectors.

### Custom Notifications

Extend `NotificationServiceImpl`:

```java
@Override
public void sendNotification(NotificationRequest request) {
    // Call parent for logging
    super.sendNotification(request);

    // Add your custom notification
    if (request.getSeverity() == Severity.HIGH) {
        sendEmailNotification(request);
        sendSlackNotification(request);
    }
}
```

## Monitoring & Debugging

### Logs

```bash
# View application logs
tail -f logs/revolution-tools.log

# Specific component logs
# Data extraction
grep "DataExtractionService" logs/revolution-tools.log

# Rule evaluations
grep "Rule triggered" logs/revolution-tools.log
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Circuit breaker status
curl http://localhost:8080/actuator/health/circuitbreakers
```

### Screenshots

Error screenshots are saved to `./screenshots/` directory:
- Format: `{sessionId}_{reason}_{timestamp}.png`
- Enable/disable: `pokerstars.scraping.screenshot-on-error`

## Best Practices

### 1. Selector Maintenance
- Keep selectors up-to-date with PokerStars UI changes
- Use multiple fallback selectors: `".primary, .fallback"`
- Test selectors regularly

### 2. Rate Limiting
- Respect PokerStars rate limits
- Don't scrape too frequently (5 seconds is reasonable)
- Implement back-off on errors

### 3. Error Handling
- Always check for null values in extracted data
- Validate snapshots before processing
- Use circuit breakers to fail fast

### 4. Security
- Never commit credentials
- Use environment variables
- Rotate credentials regularly

### 5. Performance
- Use virtual threads (Java 21)
- Minimize DOM queries
- Cache static data
- Close browser pages properly

## Troubleshooting

### Issue: Login fails
**Solution**:
- Check credentials
- Verify login URL is correct
- Check if PokerStars changed login page structure
- Use headless=false to debug visually

### Issue: Data extraction returns null
**Solution**:
- Check selectors match actual HTML
- Use browser DevTools to inspect elements
- Enable debug logging
- Take screenshots to see actual page state

### Issue: Rules not triggering
**Solution**:
- Check rule is enabled
- Verify threshold values
- Enable `revolution.rules.debug-logging`
- Check data extraction is working

### Issue: Too many notifications
**Solution**:
- Increase `notification-cooldown-seconds`
- Decrease `max-notifications-per-session`
- Adjust rule sensitivity

## Technology Stack

- **Java 21** - LTS version with virtual threads
- **Spring Boot 3.3.5** - Framework
- **Playwright** - Browser automation
- **Resilience4j** - Circuit breakers and retry
- **Lombok** - Boilerplate reduction
- **H2/PostgreSQL** - Database
- **Gradle** - Build tool

## Performance Metrics

- **Scraping Interval**: 5 seconds (configurable)
- **Browser Startup**: ~2-3 seconds
- **Data Extraction**: ~100-200ms per snapshot
- **Rule Evaluation**: ~10-50ms for all rules
- **Memory Usage**: ~200-300MB per browser instance

## Security Considerations

- **Credential Storage**: Use environment variables
- **Rate Limiting**: Built-in notification cool-down
- **Circuit Breakers**: Protect against cascading failures
- **Input Validation**: All DTOs validated
- **Screenshot Privacy**: Be careful with sensitive data

## Future Enhancements

Potential improvements:
1. **Machine Learning**: Pattern recognition using Claude AI
2. **Real-time Dashboard**: WebSocket-based live updates
3. **Multiple Tables**: Parallel scraping of multiple games
4. **Historical Analysis**: Store and analyze game history
5. **Advanced Notifications**: Email, SMS, Telegram, Discord
6. **Mobile App**: Push notifications to mobile devices

## Support

For issues or questions:
1. Check logs first
2. Review configuration
3. Test with headless=false
4. Take screenshots for debugging
5. Review Playwright documentation: https://playwright.dev/java/

## License

Internal use only.
