# Roulette Discipline Assistant - Quick Start

## What We Built

A **behavioral control system** for roulette that:
- ✅ Enforces stop-loss and take-profit
- ✅ Detects hot numbers, neighbors, missing numbers
- ✅ Validates bets against patterns
- ✅ Alerts when rules are violated
- ✅ Prevents tilt and emotional betting
- ✅ Tracks session performance

## Quick Test

### 1. Start Application
```bash
./gradlew bootRun
```

### 2. Start a Session
```bash
curl -X POST http://localhost:8080/api/roulette/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "initialBankroll": 100.00,
    "stopLossPercent": -20,
    "takeProfitLevels": [70, 130],
    "flatBetPercent": 30,
    "maxSpins": 150,
    "maxDurationMinutes": 120
  }'
```

### 3. Record Some Spins
```bash
# Record spin: 17
curl -X POST http://localhost:8080/api/roulette/sessions/{sessionId}/spins \
  -H "Content-Type: application/json" \
  -d '{"spinNumber": 17}'

# Record more spins
curl -X POST http://localhost:8080/api/roulette/sessions/{sessionId}/spins \
  -H "Content-Type: application/json" \
  -d '{"spinNumber": 23}'
```

### 4. Get Pattern Suggestions
```bash
curl http://localhost:8080/api/roulette/sessions/{sessionId}/patterns
```

### 5. Place a Bet (Validation)
```bash
curl -X POST http://localhost:8080/api/roulette/sessions/{sessionId}/bets \
  -H "Content-Type: application/json" \
  -d '{
    "numbers": [17, 23, 12],
    "stakePerNumber": 10.00,
    "betType": "MULTI_STRAIGHT"
  }'
```

The system will tell you if:
- ✅ Bet matches detected patterns
- ⚠️  Bet violates discipline rules
- ❌ Stake is too high/low
- 🔔 Tilt detected

## Next: Build the Services

I'm creating the core services now. Once complete, all the above will work!
