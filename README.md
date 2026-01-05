# Revolution Tools

AI-powered Evolution game data analysis platform built with Spring Boot 3 and Claude AI.

## 🚀 Quick Start

### Prerequisites

- **Java 21** (required)
- **Gradle** (included via wrapper)
- **IntelliJ IDEA** 2023.3+ (recommended)

### Installation

```bash
# Clone and navigate to project
cd /Users/szabolcssalyi/Code/Private/revolution-tools

# Set Java 21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

# Build and run tests
./gradlew clean build

# Run application
./gradlew bootRun
```

## 📦 Tech Stack

- **Java 21** with Virtual Threads
- **Spring Boot 3.3.5**
- **Claude AI** for intelligent analysis
- **Playwright** for browser automation
- **H2/PostgreSQL** for data persistence
- **Resilience4j** for circuit breaker patterns

## 🔧 IntelliJ IDEA Setup

### Option 1: Quick Fix (Recommended)

Run the automated fix script:

```bash
./fix-idea-import.sh
```

Then open the project in IntelliJ IDEA:
1. **File → Open** → Select this folder
2. Click **Trust Project**
3. Wait for Gradle sync
4. Install Lombok plugin if prompted

### Option 2: Manual Setup

See detailed instructions in [INTELLIJ_SETUP.md](./INTELLIJ_SETUP.md)

### Common Issues

**"Cannot resolve symbol" errors?**
```bash
# In IntelliJ IDEA:
# 1. Gradle tool window → Reload (🔄 icon)
# 2. File → Invalidate Caches → Invalidate and Restart
```

**Lombok not working?**
```bash
# 1. Install Lombok plugin: Preferences → Plugins → "Lombok"
# 2. Enable annotation processing:
#    Preferences → Compiler → Annotation Processors → ☑️ Enable
# 3. Restart IntelliJ IDEA
```

**Java version mismatch?**
```bash
# Set Java 21 in IntelliJ IDEA:
# File → Project Structure → Project → SDK: 21
# Preferences → Build Tools → Gradle → Gradle JVM: Java 21
```

## 📚 Project Structure

```
revolution-tools/
├── src/main/java/com/revolution/tools/
│   ├── RevolutionToolsApplication.java    # Main application
│   ├── client/                             # External API clients
│   │   └── ClaudeApiClient.java           # Claude AI HTTP client
│   ├── config/                             # Configuration classes
│   │   ├── ClaudeConfig.java              # Claude AI config
│   │   ├── ClaudeProperties.java          # Claude properties
│   │   ├── PlaywrightConfig.java          # Browser automation
│   │   └── RestTemplateConfig.java        # HTTP client config
│   ├── controller/                         # REST controllers
│   │   ├── ClaudeController.java          # Claude AI endpoints
│   │   ├── GameSessionController.java     # Game session management
│   │   └── HealthCheckController.java     # Health check
│   ├── dto/                                # Data Transfer Objects
│   │   ├── request/                        # Request DTOs
│   │   └── response/                       # Response DTOs
│   ├── entity/                             # JPA entities
│   │   └── GameSession.java               # Game session entity
│   ├── exception/                          # Custom exceptions
│   │   ├── ClaudeAIException.java         # Claude AI errors
│   │   ├── EvolutionGameException.java    # Game errors
│   │   └── GlobalExceptionHandler.java    # Global error handling
│   ├── repository/                         # Data repositories
│   │   └── GameSessionRepository.java     # Session data access
│   └── service/                            # Business logic
│       ├── ClaudeService.java             # Claude AI service interface
│       ├── GameSessionService.java        # Game session interface
│       └── impl/                           # Service implementations
└── src/main/resources/
    ├── application.yml                     # Main configuration
    ├── application-dev.yml                 # Development config
    ├── application-prod.yml                # Production config
    └── application-test.yml                # Test configuration
```

## 🤖 Claude AI Integration

### Configuration

Set environment variables:

```bash
export CLAUDE_API_KEY="your-claude-api-key"
export CLAUDE_ENABLED=true
export CLAUDE_MODEL="claude-3-5-sonnet-20241022"
```

### API Endpoints

#### Test Connection
```bash
curl http://localhost:8080/api/claude/test
```

#### Analyze Text
```bash
curl -X POST http://localhost:8080/api/claude/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Analyze this game pattern: increasing bets after losses",
    "temperature": 0.5
  }'
```

#### Analyze Game Data
```bash
curl -X POST http://localhost:8080/api/claude/analyze-game-data \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-123",
    "analysisType": "PATTERN_DETECTION",
    "gameData": [
      {"round": 1, "outcome": "win", "amount": 100}
    ],
    "includeInsights": true
  }'
```

### Analysis Types

- `PATTERN_DETECTION` - Identify betting patterns
- `STRATEGY_ANALYSIS` - Evaluate gameplay strategy
- `RISK_ASSESSMENT` - Assess risk patterns
- `OUTCOME_PREDICTION` - Predict likely outcomes
- `ANOMALY_DETECTION` - Detect unusual behavior
- `PERFORMANCE_ANALYSIS` - Analyze performance metrics
- `GENERAL_INSIGHTS` - General recommendations

For full Claude AI documentation, see [CLAUDE.md](./CLAUDE.md)

## 🎮 Game Session Management

### Create Session
```bash
curl -X POST http://localhost:8080/api/game-sessions \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "game-001",
    "gameType": "ROULETTE",
    "credentials": {
      "username": "user",
      "password": "pass"
    }
  }'
```

### Get Session
```bash
curl http://localhost:8080/api/game-sessions/{sessionId}
```

### Analyze Session with AI
```bash
curl -X POST http://localhost:8080/api/game-sessions/{sessionId}/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "analysisType": "STRATEGY_ANALYSIS",
    "includeStatistics": true,
    "includeInsights": true
  }'
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests ClaudeServiceImplTest

# Run with coverage
./gradlew test jacocoTestReport

# View test report
open build/reports/tests/test/index.html
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/api/health
```

### Actuator Endpoints
```bash
# Application health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Circuit breaker status
curl http://localhost:8080/actuator/health/claudeAI-circuitBreaker
```

## 🏗️ Build & Deployment

### Build JAR
```bash
./gradlew clean build
# Output: build/libs/revolution-tools-0.0.1-SNAPSHOT.jar
```

### Run JAR
```bash
java -jar build/libs/revolution-tools-0.0.1-SNAPSHOT.jar
```

### Docker (Future)
```bash
# Build image
docker build -t revolution-tools .

# Run container
docker run -p 8080:8080 \
  -e CLAUDE_API_KEY=your-key \
  revolution-tools
```

## 📝 Development

### Gradle Commands

```bash
# Clean build
./gradlew clean build

# Run application
./gradlew bootRun

# Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Refresh dependencies
./gradlew build --refresh-dependencies

# Generate IntelliJ IDEA files
./gradlew cleanIdea idea
```

### Useful Scripts

```bash
# Fix IntelliJ IDEA import issues
./fix-idea-import.sh

# Run tests with coverage
./gradlew clean test jacocoTestReport
```

## 🔒 Security

- **API Keys**: Never commit to version control
- **Environment Variables**: Use `.env` or system environment
- **HTTPS**: Required for production
- **Rate Limiting**: Implemented via Resilience4j
- **Data Privacy**: Anonymize PII before sending to Claude

## 📖 Documentation

- **[CLAUDE.md](./CLAUDE.md)** - Comprehensive project and AI integration guide
- **[INTELLIJ_SETUP.md](./INTELLIJ_SETUP.md)** - IntelliJ IDEA setup instructions
- **API Documentation** - Available via Swagger UI (future enhancement)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is proprietary and confidential.

## 🆘 Support

### Common Issues

1. **Build fails** - Check Java 21 is installed and `JAVA_HOME` is set
2. **Tests fail** - Run `./gradlew clean test --refresh-dependencies`
3. **IDE issues** - Run `./fix-idea-import.sh`
4. **Claude API errors** - Verify `CLAUDE_API_KEY` is set correctly

### Getting Help

- Check [INTELLIJ_SETUP.md](./INTELLIJ_SETUP.md) for IDE issues
- Check [CLAUDE.md](./CLAUDE.md) for AI integration questions
- Review test failures in `build/reports/tests/test/index.html`

---

**Built with** ❤️ **using Spring Boot 3 and Claude AI**

**Last Updated**: 2025-11-29
