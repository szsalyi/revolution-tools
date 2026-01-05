# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Revolution-tools** is a Java Spring Boot project that provides tools and utilities to analyze Evolution games.
This service integrates AI-powered analysis using Claude AI for advanced game data insights and pattern detection.
It includes modules for:
- Game session management and tracking
- AI-driven analysis and insights via Claude AI integration
- Statistical analysis and pattern detection
- RESTful API for external integrations

The primary goal is to facilitate advanced analysis and visualization of Evolution game data for researchers and enthusiasts.
The codebase is structured to facilitate maintainability, scalability, and testability.
The project emphasizes clean code principles, performance optimizations, and AI-driven insights.

## Project Context

- **Project Name**: revolution-tools
- **Purpose**: Evolution game data analysis and visualization with AI insights
- **Tech Stack**: Java 21, Spring Boot 3.3.5, Gradle, Claude AI, H2/PostgreSQL
- **Key Features**: Virtual threads, Circuit breaker patterns, AI analysis, Browser automation

## Claude Code Guidelines

### How Claude Should Help

Claude will assist with:
- Code generation and refactoring
- Architecture and design decisions
- Testing and debugging
- Documentation
- Performance optimization

### Project Structure

### Key Conventions

- Code should follow the existing PulseLive standards and patterns
- Document significant architectural decisions in the `docs/` folder
- Keep this file updated as the project evolves

## Development Workflow

1. Use Claude Code for iterative development and problem-solving
2. Create meaningful commit messages explaining the "why"
3. Maintain test coverage for critical functionality
4. Keep documentation up-to-date with code changes

### Setting Up the Project

#### Prerequisites
- **Java**: LTS version (25 for Spring Boot 4.x)
- **Build Tool**: Maven 4.+ or Gradle 10.0+
- **IDE**: IntelliJ IDEA, or Visual Studio Code with Java extensions

#### Environment Configuration
- Use `application.yml` for configuration
- Create `application-{profile}.yml` for environment-specific settings (dev, test, prod)
- Use Spring Profiles (`@Profile` annotation) to manage environment-specific beans
- Never commit secrets, use `.gitignore` for local configuration files

#### Virtual Threads Configuration (Java 21+)
Spring Boot 4.+ supports virtual threads for improved throughput and resource efficiency.

**RestTemplate with Virtual Thread Support**:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        return restTemplate;
    }
}
```

**Servlet Container Configuration for Virtual Threads**:
```yaml
# application.yaml
server:
  tomcat:
    threads:
      max: 200          # Virtual threads don't need many platform threads
      min-spare: 10
    accept-count: 100
```

**Benefits of Virtual Threads for This Service**:
- Efficient handling of I/O-bound operations
- Reduced memory footprint for thread-per-request model
- Better scalability without complex async/reactive code
- Simpler debugging and stack traces compared to reactive programming
- Seamless integration with Spring Boot 3.x and existing code

**Virtual Thread Best Practices**:
- Virtual threads are ideal for blocking I/O (like RestTemplate HTTP calls)
- Use platform threads via `@Bean` or executors only when needed for CPU-bound work
- Monitor virtual thread count via metrics: `jvm.threads.live`, `jvm.threads.peak`
- No need to change existing RestTemplate code; Spring handles virtual threads automatically
- Keep database operations and external service calls non-blocking or use connection pooling

#### Package Naming Conventions
- Follow reverse domain notation
- Avoid package names that are too broad (`util`, `helper`) or too specific
- Keep packages focused and cohesive

### Design Patterns & Best Practices

#### 1. Dependency Injection
- Use constructor injection (preferred) over field injection (`@Autowired`)
- Makes dependencies explicit and supports immutability

#### 2. Stateless Service Pattern
- Services contain business logic for request processing
- Services orchestrate external API calls
- No database persistence layer - stateless operations
- Services depend on external client interfaces

#### 3. DTO Pattern (Data Transfer Objects)
- Separate DTOs for requests and responses
- Protects entity models from API exposure
- Enables validation at API boundary

#### 4. Exception Handling
- Use `@ControllerAdvice` for global exception handling
- Create custom exceptions for domain-specific errors
- Return meaningful error responses

#### 5. Configuration Management
- Use Spring Boot's `@Configuration` for beans
- Externalize configuration using `application.yml`
- Use `@ConfigurationProperties` for type-safe properties

#### 7. Integration with External Services
- Use Spring's `RestTemplate` for HTTP calls (synchronous, blocking)
- Implement circuit breakers using Resilience4j
- Timeout and retry strategies for external API calls
- Log all external service interactions

#### 8. Caching Strategy
- Use Spring Cache Abstraction with `@Cacheable` for responses
- Cache stable data like configuration and validation rules
- Implement appropriate cache invalidation
- Consider TTL for cached security validations

#### 9. Logging Strategy
- Use SLF4J with Logback for logging
- Log at appropriate levels: DEBUG, INFO, WARN, ERROR
- Include contextual information (request IDs, user IDs)
- Use structured logging for better analysis

## Clean Code & Performance

### Clean Code Principles

#### KISS (Keep It Simple, Stupid)

- Write code that is easy to understand and maintain
- Avoid over-engineering; solve the problem at hand, not hypothetical future problems
- Break complex logic into smaller, single-purpose functions
- Use clear variable and function names that express intent
- Keep functions small (ideally under 20 lines); if longer, refactor into smaller units

#### DRY (Don't Repeat Yourself)

- Extract duplicated code into reusable functions or modules
- Create utility functions for common patterns
- Use constants for repeated values instead of magic numbers
- Leverage inheritance, composition, or mixins to avoid code duplication
- However, don't over-abstract; sometimes small duplication is acceptable

#### YAGNI (You Aren't Gonna Need It)

- Only write code that is needed today, not for hypothetical future features
- Don't add features "just in case" they might be useful
- Remove unused code, imports, and dependencies
- Avoid premature optimization that adds complexity without clear benefit

#### SOLID Principles

1. **S - Single Responsibility Principle (SRP)**
    - Each class/function should have a single, well-defined responsibility
    - Avoid "god" classes that do too much
    - Example: A User class should handle user data, not authentication logic

2. **O - Open/Closed Principle (OCP)**
    - Code should be open for extension but closed for modification
    - Use interfaces/abstract classes for extension points
    - Avoid modifying existing code when adding features; extend instead

3. **L - Liskov Substitution Principle (LSP)**
    - Subtypes must be substitutable for their base types
    - A derived class should not break the contract of its parent
    - Ensure derived classes honor the parent class's behavior expectations

4. **I - Interface Segregation Principle (ISP)**
    - Interfaces should be specific and focused, not generic
    - Clients should depend only on interfaces they actually use
    - Create multiple specific interfaces rather than one large interface

5. **D - Dependency Inversion Principle (DIP)**
    - Depend on abstractions, not concrete implementations
    - Inject dependencies rather than creating them inside functions
    - Use interfaces/types to define contracts between modules

#### Algorithm & Logic Efficiency

1. **Algorithmic Complexity**
    - Understand Big O notation; aim for O(n) or O(n log n), avoid O(n²) when possible
    - Use faster algorithms for common problems (binary search vs linear search)
    - Batch operations to reduce function call overhead
    - Cache results of expensive operations

#### Java-Specific Best Practices

1. **Resource Management**
    - Use try-with-resources for automatic resource cleanup
    - Close `RestTemplate`, database connections, and file handles properly
    - Avoid memory leaks from unclosed resources

2. **Immutability & Thread Safety**
    - Prefer immutable objects where possible
    - Use `final` keyword for fields that shouldn't change
    - Be cautious with shared mutable state in concurrent environments
    - Spring Beans are singletons by default; ensure thread-safety

3. **Null Handling**
    - Use `Optional` instead of returning null
    - Avoid `NullPointerException` with proper null checks
    - Use `Objects.requireNonNull()` for precondition validation
    ```java
    // Good
    Optional<Payment> payment = paymentRepository.findById(id);
    return payment.orElseThrow(() -> new PaymentNotFoundException(id));

    // Avoid
    Payment payment = paymentRepository.findById(id);
    if (payment == null) { // Fragile pattern
        throw new Exception();
    }
    ```

4. **String Operations**
    - Use `String.format()` or StringBuilder for complex concatenation
    - Avoid string concatenation in loops
    - Use `equals()` and `equalsIgnoreCase()` for string comparison

5. **Collections & Streams**
    - Use appropriate collection types (ArrayList, HashMap, HashSet)
    - Be aware of concurrent modification exceptions
    - Use Java Streams for functional data transformation
    - Filter early to avoid processing unnecessary data
    ```java
    // Efficient stream usage
    List<Payment> largePayments = payments.stream()
        .filter(p -> p.getAmount().compareTo(threshold) > 0)
        .map(PaymentMapper::toDTO)
        .collect(Collectors.toList());
    ```

6. **Exception Handling**
    - Catch specific exceptions, not `Exception` or `Throwable`
    - Log exceptions with context before re-throwing
    - Create custom exceptions for domain-specific errors
    - Don't use exceptions for flow control

7. **Naming Conventions**
    - **Classes**: PascalCase (e.g., `PaymentService`, `PaymentRequest`)
    - **Methods/Variables**: camelCase (e.g., `processPayment()`, `customerId`)
    - **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT_SECONDS`)
    - **Abbreviations**: Avoid (e.g., use `getId()` not `getID()`)
    - Use descriptive names that convey intent

8. **Code Comments**
    - Comment the "why", not the "what"
    - Avoid obvious comments
    - Keep comments updated with code changes
    - Use Javadoc for public APIs
    ```java
    // Good: explains business logic
    // occasionally returning transient errors
    public void retryPaymentProcessing(Payment payment) { }

    // Avoid: redundant comment
    // increment counter
    counter++;
    ```

## Testing Strategy

### Test Pyramid Approach
For a stateless API service with external integrations:

1. **Unit Tests (70%)** - Fast, isolated tests of services and validators
2. **Integration Tests (20%)** - Test controllers with mocked external services
3. **Contract Tests (10%)** - Test against external API contracts (mocked)

### Unit Testing
- Use JUnit 5 (Jupiter) and Mockito
- Test business logic and validation independently
- Aim for >80% code coverage for critical payment security logic
- Test error scenarios and edge cases

### Integration Testing
- Use `@WebMvcTest` for controller testing with mocked services
- Test HTTP status codes, response structure, and error handling


### Contract Testing with External Services
- Use WireMock or Testcontainers for mocking external API
- Test request/response contracts
- Test timeout and retry scenarios


### Signature Validation Testing
- Test valid signatures with correct secret key and salt
- Test invalid signatures (tampered payload, wrong key, expired timestamp)
- Test missing headers (signature, salt, timestamp)
- Test timestamp validation (too old, in future)
- Test constant-time comparison to prevent timing attacks

## Performance Optimization

### API Performance for suggesting numbers
- Implement response caching for frequently accessed security rules
- Use compression (gzip) for request/response bodies
- Implement connection pooling for HTTP client
- Set appropriate connection timeouts for  API calls

### Spring Boot Application Optimization
- Configure appropriate thread pool sizes for HTTP client requests
- Use RestTemplate with connection pooling for external API calls
- Implement rate limiting and circuit breakers for external API
- Monitor memory usage and heap size
- Implement HTTP connection pooling (HttpClient5 with connection manager)
- Use thread pool executors for async operations

## Configuration

#### Environment Variables

Set the following environment variables before running the application:

```bash
export CLAUDE_API_KEY="your-claude-api-key-here"
export CLAUDE_ENABLED=true
export CLAUDE_MODEL="claude-3-5-sonnet-20241022"
export CLAUDE_MAX_TOKENS=4096
export CLAUDE_TEMPERATURE=0.7
```

#### Application Configuration

Claude AI settings are configured in `application.yml`:

```yaml
claude:
  enabled: true
  api-key: ${CLAUDE_API_KEY}
  model: claude-3-5-sonnet-20241022
  max-tokens: 4096
  temperature: 0.7
  timeout-seconds: 60
```

#### Profile-Specific Configuration

- **Development (`application-dev.yml`)**: Uses same settings as base configuration
- **Production (`application-prod.yml`)**: Uses environment variables with production API key
- **Test (`application-test.yml`)**: Claude may be disabled or mocked

## Analysis Types

The following analysis types are available via `GameDataAnalysisRequest.AnalysisType`:

| Type | Description | Use Case |
|------|-------------|----------|
| `PATTERN_DETECTION` | Detect patterns in game behavior | Identify betting patterns, win/loss streaks |
| `STRATEGY_ANALYSIS` | Analyze gameplay strategy | Evaluate decision-making patterns |
| `RISK_ASSESSMENT` | Assess risk patterns | Identify high-risk behaviors |
| `OUTCOME_PREDICTION` | Predict likely outcomes | Forecast based on historical data |
| `ANOMALY_DETECTION` | Detect anomalies or unusual behavior | Flag suspicious activities |
| `PERFORMANCE_ANALYSIS` | Analyze performance metrics | Evaluate player performance trends |
| `GENERAL_INSIGHTS` | General insights and recommendations | Broad analysis and recommendations |

## Resilience Patterns

#### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      claudeAI:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 3
        permitted-number-of-calls-in-half-open-state: 2
        wait-duration-in-open-state: 30s
        failure-rate-threshold: 50
```

**Behavior**:
- Monitors last 10 calls
- Opens circuit after 50% failure rate (with min 3 calls)
- Waits 30 seconds before attempting half-open state
- Allows 2 test calls in half-open state

#### Retry Configuration

```yaml
resilience4j:
  retry:
    instances:
      claudeAI:
        max-attempts: 3
        wait-duration: 2s
        exponential-backoff-multiplier: 2
```

**Behavior**:
- Retries up to 3 times
- Initial wait: 2 seconds
- Exponential backoff: 2s → 4s → 8s

## Error Handling

### Custom Exception: ClaudeAIException

Thrown when Claude API calls fail.

**HTTP Status**: 503 Service Unavailable

**Example Response**:
```json
{
  "status": 503,
  "message": "Failed to analyze with Claude AI: Connection timeout",
  "timestamp": "2025-11-29T10:30:00"
}
```

### Fallback Responses

When the circuit breaker is open or retries are exhausted:

```json
{
  "content": "Claude AI service is temporarily unavailable. Please try again later.",
  "success": false,
  "errorMessage": "Service temporarily unavailable: Circuit breaker open",
  "timestamp": "2025-11-29T10:30:00"
}
```

### Future Enhancements

Planned improvements:
1. **Streaming Responses**: Implement SSE for real-time analysis streaming
2. **Response Caching**: Cache common analysis results
3. **Batch Processing**: Queue and batch multiple analysis requests
4. **Advanced Prompts**: Template library for common analysis types
5. **Multi-Model Support**: Support for different Claude models based on use case

---

**Last Updated**: 2025-11-29
