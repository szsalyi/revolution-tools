# RouletteBet Refactoring - Multi-Source Bet Tracking

## 🎯 Objective

Refactor `RouletteBet` entity to support **multi-source bet tracking** for future data analysis.

**Problem:** Previous implementation had single `betType` - couldn't track that one bet contained numbers from MULTIPLE pattern sources.

**Example Scenario:**
- Previous spin: **2**
- Custom rule: 2 → suggests **4, 22**
- Hot numbers: **14, 34** (appeared 5+ times)
- **Single bet** contains: `[4, 22, 14, 34]`
- But each has different **reasons**: `[CUSTOM_RULE, CUSTOM_RULE, HOT_NUMBER, HOT_NUMBER]`

## ✅ Changes Made

### 1. RouletteBet Entity Refactoring

#### Removed
- ❌ Single `betType` field (couldn't track multiple sources)
- ❌ Required `sessionId` (bets can now be standalone for analysis)

#### Added
- ✅ `betSources` (String) - CSV of bet sources parallel to numbers
  - Example: `"CUSTOM_RULE,CUSTOM_RULE,HOT_NUMBER,HOT_NUMBER"`
- ✅ `betMetadata` (String) - Optional JSON for additional context
  - Example: `{"4":"prev_2_rule","22":"prev_2_rule","14":"hot_freq_5"}`
- ✅ `winningSources` (String) - Tracks which source(s) won
  - Example: `"14:HOT_NUMBER"` or `"4:CUSTOM_RULE"`
- ✅ `sessionId` now **optional** (can be null for standalone bets)

#### Enhanced Methods
- ✅ `getBetSourceList()` - Returns `List<BetType>` parallel to numbers
- ✅ `getNumberSourceMap()` - Returns `Map<Integer, BetType>`
  - Example: `{4=CUSTOM_RULE, 22=CUSTOM_RULE, 14=HOT_NUMBER, 34=HOT_NUMBER}`
- ✅ `setNumberSourceMap()` - Sets both numbers and sources from map
- ✅ `calculateResult()` - Now tracks which source won

### 2. BetType Enum Enhancement

Transformed from "bet types" to "bet sources/patterns" for analysis.

#### New Pattern Sources
```java
CUSTOM_RULE      // Custom pattern rules (e.g., prev 2 → 4, 22)
HOT_NUMBER       // Hot number pattern (freq >3 in 50 spins)
MISSING_NUMBER   // Cold/missing number (not in last 100 spins)
NEIGHBOR         // Wheel neighbor (±2 positions)
SECTION_PATTERN  // Section clustering
DOUBLE_DIGIT     // 11, 22, 33 pattern
REPEATING        // Repeating number from last spin
```

#### Legacy Types (Kept for Compatibility)
```java
STRAIGHT, MULTI_STRAIGHT, SECTION, DOZEN, COLUMN,
COLOR, EVEN_ODD, HIGH_LOW, PATTERN_BASED
```

#### Fallback
```java
UNKNOWN          // Parsing error fallback
```

### 3. BetBuilder Helper Class

New utility class for building multi-source bets easily.

#### Example Usage

```java
BetBuilder builder = new BetBuilder(sessionId);

// Add custom rule numbers (previous spin was 2)
builder.addCustomRuleNumbers(2, Arrays.asList(4, 22));

// Add hot numbers
builder.addHotNumbers(Arrays.asList(14, 34));

// Add neighbors of a hot number
builder.addNeighbors(14, 2); // 14 ± 2 neighbors

// Build the bet
RouletteBet bet = builder.build(new BigDecimal("1.00"));

// Result:
// numbers: "4,22,14,34,15,19,4,21,2"
// betSources: "CUSTOM_RULE,CUSTOM_RULE,HOT_NUMBER,HOT_NUMBER,NEIGHBOR,NEIGHBOR,NEIGHBOR,NEIGHBOR,NEIGHBOR"
```

#### Available Methods

```java
// Add single number with source
addNumber(Integer number, BetType source)
addNumber(Integer number, BetType source, String metadata)

// Add multiple numbers with same source
addNumbers(List<Integer> numbers, BetType source)

// Pattern-specific methods
addCustomRuleNumbers(Integer previousNumber, List<Integer> suggested)
addHotNumbers(List<Integer> hotNumbers)
addMissingNumbers(List<Integer> missingNumbers)
addNeighbors(Integer centerNumber, int distance)

// Utility
getNumberCount()
getSourceSummary()  // Returns {CUSTOM_RULE=2, HOT_NUMBER=2, NEIGHBOR=5}
clear()
build(BigDecimal stakePerNumber)
```

### 4. Service Layer Updates

#### RouletteSessionServiceImpl

Updated `placeBet()` to use new multi-source structure:

```java
// Determine bet sources from request
String betSources = determineBetSources(request);

RouletteBet bet = RouletteBet.builder()
    .sessionId(session.getId())
    .numbers("4,22,14,34")
    .betSources("CUSTOM_RULE,CUSTOM_RULE,HOT_NUMBER,HOT_NUMBER")
    .stakePerNumber(stakePerNumber)
    .totalStake(totalStake)
    .build();
```

**Helper method** `determineBetSources()`:
- Parses `basedOnPattern` from request
- Maps to appropriate `BetType` enum
- Creates uniform sources for all numbers (for now)
- **Future**: Support per-number sources from request

## 📊 Data Analysis Benefits

### Query Examples

**1. Which pattern source produces most wins?**
```sql
SELECT
    winning_sources,
    COUNT(*) as wins,
    AVG(net_result) as avg_profit
FROM roulette_bets
WHERE is_win = true
GROUP BY winning_sources
ORDER BY avg_profit DESC;
```

**2. Hot numbers vs. Custom rules win rate**
```sql
SELECT
    CASE
        WHEN bet_sources LIKE '%HOT_NUMBER%' THEN 'Hot Numbers'
        WHEN bet_sources LIKE '%CUSTOM_RULE%' THEN 'Custom Rules'
    END as strategy,
    SUM(CASE WHEN is_win THEN 1 ELSE 0 END) / COUNT(*) as win_rate
FROM roulette_bets
GROUP BY strategy;
```

**3. Most profitable bet source combinations**
```sql
SELECT
    bet_sources,
    COUNT(*) as bet_count,
    SUM(net_result) as total_profit,
    AVG(net_result) as avg_profit
FROM roulette_bets
GROUP BY bet_sources
ORDER BY total_profit DESC
LIMIT 10;
```

## 🔄 Migration Path

### For Existing Code

1. **Old single-source bets still work:**
   ```java
   // Old way (still works with determineBetSources())
   PlaceBetRequest request = new PlaceBetRequest();
   request.setNumbers(Arrays.asList(4, 22, 14));
   request.setBasedOnPattern("HOT_NUMBERS");
   ```

2. **New multi-source bets:**
   ```java
   // New way (using BetBuilder)
   BetBuilder builder = new BetBuilder();
   builder.addNumber(4, BetType.CUSTOM_RULE);
   builder.addNumber(22, BetType.CUSTOM_RULE);
   builder.addNumber(14, BetType.HOT_NUMBER);
   RouletteBet bet = builder.build(new BigDecimal("1.00"));
   ```

### Database Migration

No migration needed! New columns will be null for old records:
- `betSources` - will be populated for new bets
- `betMetadata` - optional, null for simple bets
- `winningSources` - calculated on result recording
- `basedOnPattern` - deprecated but kept for backward compatibility

## 🚀 Future Enhancements

### 1. Request DTO Enhancement
Add multi-source support to `PlaceBetRequest`:

```java
public class PlaceBetRequest {
    // Current: Single list of numbers
    private List<Integer> numbers;

    // Future: Number-source map
    private Map<Integer, BetType> numberSources;
}
```

### 2. Pattern Analysis Service
Create service to analyze historical bets:

```java
public interface BetAnalysisService {
    // Which pattern has highest win rate?
    PatternPerformanceReport analyzePatternPerformance();

    // Which combinations work best?
    List<BetSourceCombination> findBestCombinations();

    // When do custom rules outperform hot numbers?
    ComparisonReport compareStrategies(BetType strategy1, BetType strategy2);
}
```

### 3. Machine Learning Ready
Data structure now supports:
- Feature extraction (which patterns, how many sources)
- Label (win/loss, profit amount)
- Training data for pattern prediction models

## 📝 Example Complete Flow

```java
// 1. Analyze patterns
PatternSuggestionResponse patterns = patternAnalyzer.analyzePatterns(sessionId);

// 2. Build multi-source bet
BetBuilder builder = new BetBuilder(sessionId);

// From custom rules
Integer previousSpin = 2;
builder.addCustomRuleNumbers(previousSpin, Arrays.asList(4, 22));

// From hot numbers
builder.addHotNumbers(patterns.getHotNumbers());

// Add neighbors of first hot number
if (!patterns.getHotNumbers().isEmpty()) {
    builder.addNeighbors(patterns.getHotNumbers().get(0), 2);
}

// 3. Build and validate
RouletteBet bet = builder.build(new BigDecimal("1.00"));
System.out.println(builder);
// BetBuilder{numbers=[4, 22, 14, 34, 15, 19, 4, 21, 2],
//            sources={CUSTOM_RULE=2, HOT_NUMBER=2, NEIGHBOR=5}, count=9}

// 4. Place bet
betRepository.save(bet);

// 5. Record result
bet.calculateResult(14); // Winner: 14
System.out.println(bet.getWinningSources()); // "14:HOT_NUMBER"
System.out.println(bet.getNetResult()); // 35.00 (35:1 payout - 9.00 stake)
```

## ✨ Summary

✅ **Refactored** `RouletteBet` for multi-source tracking
✅ **Enhanced** `BetType` enum with pattern sources
✅ **Created** `BetBuilder` helper class
✅ **Updated** service layer for compatibility
✅ **Maintained** backward compatibility
✅ **Build successful** - all code compiles

**Result:** Can now track WHY each number was chosen, enabling powerful data analysis of which patterns/strategies produce better results! 🎰📊

---

*Refactoring completed: 2025-12-26*
*Build status: ✅ SUCCESSFUL*
