# Roulette Discipline Assistant - Architecture Design

## Core Philosophy
**Behavioral Control, Not Prediction**
- Each spin is independent
- Patterns are variance, not signals
- The tool is a coach/guardrail, not a betting system

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│         (RouletteSessionController)                         │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│              Session Orchestrator                            │
│         (RouletteSessionService)                            │
│  - Manage session lifecycle                                  │
│  - Enforce discipline rules                                  │
│  - Coordinate all components                                 │
└─────┬─────────┬────────────┬───────────────┬───────────────┘
      │         │            │               │
      ▼         ▼            ▼               ▼
┌──────────┐ ┌─────────┐ ┌────────────┐ ┌──────────────┐
│  Spin    │ │ Pattern │ │    Rule    │ │   Discipline │
│ History  │ │Analyzer │ │  Validator │ │   Enforcer   │
│ Manager  │ │         │ │            │ │              │
└──────────┘ └─────────┘ └────────────┘ └──────────────┘
      │           │             │               │
      └───────────┴─────────────┴───────────────┘
                      │
                      ▼
              ┌──────────────┐
              │Alert System  │
              │              │
              └──────────────┘
```

## Core Components

### 1. Session Manager
**Responsibility**: Manage roulette session lifecycle and state

**Features**:
- Start/stop sessions
- Track bankroll, profit/loss
- Enforce session limits (time, spins, profit targets)
- Session history and analytics

**Key Methods**:
```java
RouletteSession startSession(SessionConfig config)
void recordSpin(Long sessionId, SpinResult spin)
void recordBet(Long sessionId, BetRequest bet)
SessionStatus checkSessionStatus(Long sessionId)
void forceStopSession(Long sessionId, StopReason reason)
```

### 2. Spin History Manager
**Responsibility**: Track and analyze spin history

**Features**:
- Store all spins for a session
- Provide history windows (last N spins)
- Calculate statistics (number frequencies, colors, sections)
- Detect sequences

**Data Tracked**:
- Spin number (0-36)
- Color (RED, BLACK, GREEN)
- Section (FIRST_12, SECOND_12, THIRD_12)
- Dozen (1st, 2nd, 3rd)
- Column (1, 2, 3)
- Even/Odd
- High/Low

### 3. Pattern Analyzer
**Responsibility**: Identify patterns from experience-based rules

**Patterns Detected**:
1. **Hot Numbers**: Numbers that appeared multiple times recently
2. **Neighbors**: ±1, ±2 positions on the wheel
3. **Section Clustering**: 1-2 dominant table sections
4. **Missing Numbers**: 6-8 numbers that haven't appeared (exclusion strategy)
5. **Double Digit Patterns**: 29 → 11, 15 → 6, etc.
6. **Repeating Numbers**: Same number in consecutive spins

**Output**:
```java
List<Pattern> analyzePatterns(List<RouletteSpin> history)
List<Integer> suggestNumbers(List<RouletteSpin> history, int maxSuggestions)
```

### 4. Rule Validator
**Responsibility**: Validate bets against defined rules and patterns

**Validations**:
- Bet matches current pattern strategy
- Bet is in suggested numbers
- Bet size within limits
- Not betting on excluded numbers
- Alarm if betting on numbers not in history/rules

**Key Methods**:
```java
ValidationResult validateBet(BetRequest bet, RouletteSession session)
List<Alert> checkRuleViolations(BetRequest bet, List<Pattern> patterns)
```

### 5. Discipline Enforcer
**Responsibility**: Enforce behavioral controls

**Controls**:
- **Stop-Loss**: Hard -20% bankroll stop
- **Take-Profit**: +70%, +130% profit targets
- **Stake Control**: Flat betting only (20-40% per spin)
- **Tilt Detection**: Rapid stake increases → lock
- **Max Spins**: e.g., 100 spins per session
- **Max Time**: e.g., 2 hours per session
- **Cooldown**: e.g., 30 minutes between sessions

**Key Methods**:
```java
boolean shouldStopSession(RouletteSession session)
boolean isTilting(List<BetRequest> recentBets)
boolean isStakeValid(BigDecimal stake, SessionConfig config)
DisciplineStatus checkDiscipline(RouletteSession session)
```

### 6. Alert/Alarm System
**Responsibility**: Notify user of violations and warnings

**Alert Types**:
- **CRITICAL**: Stop-loss hit, take-profit reached, tilt detected
- **WARNING**: Giving back profit, bet doesn't match pattern
- **INFO**: New pattern detected, suggestion available

**Alert Channels**:
- Real-time WebSocket alerts
- Console logging
- Database persistence
- Future: Email, SMS, push notifications

## Domain Model

### RouletteSession
```java
- id: Long
- sessionId: String (UUID)
- startTime: LocalDateTime
- endTime: LocalDateTime
- status: SessionStatus (ACTIVE, STOPPED, COMPLETED, LOCKED)
- initialBankroll: BigDecimal
- currentBankroll: BigDecimal
- peakProfit: BigDecimal
- currentProfit: BigDecimal
- totalSpins: Integer
- totalBets: Integer
- stopReason: StopReason
- config: SessionConfig
```

### SessionConfig
```java
- initialBankroll: BigDecimal
- stopLossPercent: Integer (default: -20%)
- takeProfitLevels: List<Integer> (default: [70, 130])
- flatBetPercent: Integer (20-40%)
- maxSpins: Integer
- maxDurationMinutes: Integer
- cooldownMinutes: Integer
- enablePatternSuggestions: Boolean
```

### RouletteSpin
```java
- id: Long
- sessionId: Long
- spinNumber: Integer (0-36)
- color: Color (RED, BLACK, GREEN)
- timestamp: LocalDateTime
- section: Section
- dozen: Integer
- column: Integer
- isEven: Boolean
- isHigh: Boolean
```

### BetRequest
```java
- sessionId: Long
- numbers: List<Integer>
- stakePerNumber: BigDecimal
- totalStake: BigDecimal
- betType: BetType (STRAIGHT, NEIGHBORS, SECTION, etc.)
- timestamp: LocalDateTime
```

### Pattern
```java
- type: PatternType
- numbers: List<Integer>
- confidence: Double
- validUntilSpins: Integer
- description: String
```

### Alert
```java
- sessionId: Long
- type: AlertType
- severity: AlertSeverity
- message: String
- timestamp: LocalDateTime
- acknowledged: Boolean
```

## REST API Endpoints

### Session Management
```
POST   /api/roulette/sessions              # Start new session
GET    /api/roulette/sessions/{id}         # Get session details
DELETE /api/roulette/sessions/{id}         # Stop session
GET    /api/roulette/sessions/active       # Get active session
GET    /api/roulette/sessions/history      # Get session history
```

### Spin Recording
```
POST   /api/roulette/sessions/{id}/spins   # Record a spin result
GET    /api/roulette/sessions/{id}/spins   # Get spin history
```

### Betting
```
POST   /api/roulette/sessions/{id}/bets    # Submit bet for validation
GET    /api/roulette/sessions/{id}/bets    # Get bet history
```

### Analysis
```
GET    /api/roulette/sessions/{id}/patterns     # Get detected patterns
GET    /api/roulette/sessions/{id}/suggestions  # Get number suggestions
GET    /api/roulette/sessions/{id}/statistics   # Get session stats
```

### Alerts
```
GET    /api/roulette/sessions/{id}/alerts       # Get active alerts
POST   /api/roulette/sessions/{id}/alerts/{id}/ack  # Acknowledge alert
```

## Key Algorithms

### 1. Hot Number Detection
```
Algorithm:
1. Get last N spins (configurable, e.g., 50)
2. Count frequency of each number
3. Numbers appearing > threshold (e.g., 3 times) = HOT
4. Sort by frequency
5. Return top K numbers
```

### 2. Neighbor Calculation
```
Wheel Order: 0,32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26

Algorithm:
1. Find position of number on wheel
2. Get ±N positions (e.g., ±2)
3. Return neighbor numbers
```

### 3. Section Clustering
```
Sections:
- ZERO (0)
- VOISINS (neighbors of zero)
- TIERS (opposite of zero)
- ORPHELINS (orphans)

Algorithm:
1. Get last N spins
2. Map each to section
3. Count section frequencies
4. If 1-2 sections > 70% → CLUSTERING detected
```

### 4. Missing Numbers Detection
```
Algorithm:
1. Get last N spins (e.g., 100)
2. Track which numbers appeared
3. Numbers not appearing in last N spins = MISSING
4. If missing count >= 6-8 → EXCLUSION strategy available
```

### 5. Tilt Detection
```
Algorithm:
1. Get last M bets (e.g., 5)
2. Calculate stake growth rate
3. If stake increased > X% (e.g., 50%) in Y bets → TILT
4. Lock session temporarily
```

## Configuration

### Default Rules
```yaml
roulette:
  session:
    default-bankroll: 100.00
    stop-loss-percent: -20
    take-profit-levels: [70, 130]
    max-spins: 150
    max-duration-minutes: 120
    cooldown-minutes: 30

  betting:
    flat-bet-min-percent: 20
    flat-bet-max-percent: 40
    max-numbers-per-bet: 10
    allow-progressions: false

  patterns:
    hot-number-window: 50
    hot-number-threshold: 3
    missing-number-window: 100
    missing-number-min: 6
    neighbor-range: 2
    pattern-confidence-min: 0.6

  discipline:
    enable-tilt-detection: true
    tilt-stake-increase-threshold: 50
    tilt-bet-count-window: 5
    enable-profit-protection: true
    profit-giveback-warning-percent: 30
```

## Implementation Phases

### Phase 1: Core Foundation (MVP)
- ✅ Session management
- ✅ Spin history tracking
- ✅ Basic stop-loss/take-profit
- ✅ Flat betting validation
- ✅ REST API

### Phase 2: Pattern Analysis
- ✅ Hot number detection
- ✅ Neighbor calculation
- ✅ Missing number identification
- ✅ Number suggestions

### Phase 3: Advanced Discipline
- ✅ Tilt detection
- ✅ Profit protection
- ✅ Alert system
- ✅ Rule violation tracking

### Phase 4: Enhanced Analysis
- ✅ Section clustering
- ✅ Double-digit patterns
- ✅ Advanced statistics
- ✅ Session analytics

### Phase 5: UI/UX
- Fast spin entry interface
- Real-time session dashboard
- Visual pattern display
- Alert notifications

## Success Metrics to Track

1. **Session Outcomes**
   - Win rate (sessions ending in profit)
   - Average profit per winning session
   - Average loss per losing session

2. **Discipline Adherence**
   - Stop-loss obedience rate
   - Take-profit obedience rate
   - Rule violation count
   - Tilt event frequency

3. **Profit Protection**
   - Peak profit vs final profit
   - Profit giveback percentage
   - Sessions stopped at profit targets

4. **Behavioral Metrics**
   - Average session duration
   - Cooldown compliance
   - Stake discipline violations
   - Pattern suggestion usage rate

## Database Schema

```sql
CREATE TABLE roulette_sessions (
    id BIGINT PRIMARY KEY,
    session_id VARCHAR(36) UNIQUE,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20),
    initial_bankroll DECIMAL(10,2),
    current_bankroll DECIMAL(10,2),
    peak_profit DECIMAL(10,2),
    current_profit DECIMAL(10,2),
    total_spins INT,
    total_bets INT,
    stop_reason VARCHAR(50),
    config_json TEXT
);

CREATE TABLE roulette_spins (
    id BIGINT PRIMARY KEY,
    session_id BIGINT,
    spin_number INT,
    color VARCHAR(10),
    timestamp TIMESTAMP,
    section VARCHAR(20),
    dozen INT,
    column_num INT
);

CREATE TABLE roulette_bets (
    id BIGINT PRIMARY KEY,
    session_id BIGINT,
    numbers TEXT, -- JSON array
    stake_per_number DECIMAL(10,2),
    total_stake DECIMAL(10,2),
    bet_type VARCHAR(20),
    timestamp TIMESTAMP,
    validated BOOLEAN,
    validation_result TEXT
);

CREATE TABLE roulette_alerts (
    id BIGINT PRIMARY KEY,
    session_id BIGINT,
    alert_type VARCHAR(50),
    severity VARCHAR(20),
    message TEXT,
    timestamp TIMESTAMP,
    acknowledged BOOLEAN
);
```

## Next Steps

1. Create domain entities
2. Implement session management service
3. Build spin history manager
4. Create pattern analyzer
5. Implement discipline enforcer
6. Build REST API
7. Add configuration
8. Create tests
9. Build simple UI

This architecture is **focused, clean, and purpose-built** for the roulette discipline assistant!
