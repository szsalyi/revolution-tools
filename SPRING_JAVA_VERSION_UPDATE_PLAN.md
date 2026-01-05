# Spring and Java Version Update Plan

## Current State (as of 2026-01-05)

### Current Versions
- **Spring Boot**: 3.3.5
- **Spring Dependency Management Plugin**: 1.1.6
- **Java Version**: 21 (LTS)
- **Gradle**: 9.2.1
- **Key Dependencies**:
  - Playwright: 1.48.0
  - Resilience4j: 2.2.0
  - REST Assured: 5.4.0
  - Lombok: 1.18.42

### Configuration Inconsistency Alert
⚠️ **Critical Issue**: The IntelliJ IDEA configuration in `build.gradle` (lines 110-111) specifies JDK 25, which conflicts with the Java 21 toolchain configuration. This needs to be resolved.

## Recommended Update Plan

### Phase 1: Fix Configuration Inconsistencies (Immediate)

#### Issue 1: IntelliJ IDEA JDK Mismatch
**Current State**: `build.gradle` lines 110-111 specify JDK 25
```gradle
jdkName = '25'
languageLevel = '25'
```

**Problem**:
- Java 25 is not yet released (scheduled for September 2025)
- Conflicts with Java 21 toolchain configuration
- IntelliJ IDEA may not recognize this JDK version

**Solution**: Update to match Java 21 LTS
```gradle
jdkName = '21'
languageLevel = '21'
```

#### Issue 2: Hardcoded Java Path
**Current State**: `gradle.properties` line 4 contains hardcoded macOS-specific Java path
```properties
org.gradle.java.home=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

**Problem**:
- Not portable across different operating systems
- Breaks for other developers who don't have Java in this location
- The Gradle toolchain configuration already handles Java version management

**Solution**: Remove this line and rely on Gradle toolchain auto-detection

### Phase 2: Spring Boot Update (Medium Priority)

#### Target: Spring Boot 3.4.x (Latest Stable)

**Current**: 3.3.5
**Target**: 3.4.1+ (Latest in 3.4.x series)

**Benefits**:
- Security patches and bug fixes
- Performance improvements
- New features in Spring Framework 6.2.x
- Better observability support
- Enhanced virtual threads support (Project Loom)

**Migration Steps**:
1. Review [Spring Boot 3.4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
2. Update `build.gradle` plugin version:
   ```gradle
   id 'org.springframework.boot' version '3.4.1'
   ```
3. Update dependency management plugin if needed:
   ```gradle
   id 'io.spring.dependency-management' version '1.1.7'
   ```
4. Test all endpoints and functionality
5. Check for deprecated API usage
6. Update any custom configurations affected by breaking changes

**Breaking Changes to Review**:
- Check if any auto-configuration classes have changed
- Review Actuator endpoint changes
- Verify Jackson serialization behavior
- Test caching mechanism compatibility
- Validate JPA/Hibernate behavior

**Testing Checklist**:
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Application starts successfully
- [ ] All REST endpoints respond correctly
- [ ] Database connectivity works
- [ ] Actuator endpoints accessible
- [ ] Cache operations function properly
- [ ] Resilience4j circuit breakers work

### Phase 3: Java Version Strategy (Long-term)

#### Current: Java 21 LTS (Recommended to Keep)

**Recommendation**: **Stay on Java 21 LTS**

**Rationale**:
- Java 21 is the current Long-Term Support (LTS) release
- Excellent Spring Boot 3.x support
- Stable and well-tested in production
- Supported until September 2029
- All modern features available (Virtual Threads, Pattern Matching, Records, etc.)

#### Future Consideration: Java 25 LTS (September 2025)

**Timeline**: Java 25 is scheduled for release in September 2025 and will be an LTS release

**When to Upgrade**:
- Wait at least 3-6 months after Java 25 GA release
- Ensure Spring Boot has official support
- Verify all dependencies are compatible
- Test thoroughly in non-production environments

**Pre-requisites for Java 25 Migration**:
1. Spring Boot 3.5.x or later (with Java 25 support)
2. All dependencies updated to Java 25 compatible versions
3. Gradle 9.x or later (verify compatibility)
4. Lombok updated to latest version
5. Complete testing in staging environment

### Phase 4: Dependency Updates (Continuous)

#### Priority Updates

1. **Resilience4j** (Current: 2.2.0)
   - Check for 2.3.x or later versions
   - Review changelog for new features and fixes

2. **Lombok** (Current: 1.18.42)
   - Update to latest 1.18.x version
   - Ensures Java 21+ compatibility

3. **Playwright** (Current: 1.48.0)
   - Update to latest version if available
   - Check for browser compatibility updates

4. **REST Assured** (Current: 5.4.0)
   - Update to latest 5.x version
   - Review API changes

5. **Apache HttpClient 5**
   - Check for security updates
   - Review performance improvements

## Implementation Timeline

### Immediate (This Week)
1. ✅ Fix IntelliJ IDEA JDK configuration (JDK 25 → 21)
2. ✅ Remove hardcoded Java path from gradle.properties
3. ✅ Verify project builds and runs correctly
4. ✅ Update documentation

### Short-term (Next 2-4 Weeks)
1. Update Spring Boot to 3.4.x
2. Update dependency management plugin
3. Run full test suite
4. Update minor dependencies (Lombok, Resilience4j, etc.)
5. Performance and security testing

### Medium-term (Next Quarter)
1. Monitor for Spring Boot 3.5.x release
2. Evaluate Java 25 development progress
3. Keep dependencies updated monthly
4. Review and update testing strategy

### Long-term (6-12 Months)
1. Plan Java 25 LTS migration (post-September 2025)
2. Evaluate Spring Boot 4.x (when announced)
3. Continuous security and performance monitoring

## Risk Assessment

### Low Risk
- Fixing IntelliJ IDEA configuration
- Removing hardcoded Java path
- Minor dependency updates (patch versions)

### Medium Risk
- Spring Boot 3.3.5 → 3.4.x update
- Major dependency updates (minor versions)
- Resilience4j updates (API changes possible)

### High Risk
- Java 21 → 25 migration (future)
- Spring Boot 3.x → 4.x (when released)
- Major architectural changes

## Rollback Strategy

### For Spring Boot Updates
1. Keep previous version in git history
2. Tag release before upgrade: `git tag pre-spring-3.4-upgrade`
3. Maintain detailed changelog of changes
4. Test rollback procedure in staging
5. Document any configuration changes needed for rollback

### For Java Version Updates
1. Maintain multi-JDK build capability
2. Use toolchain to specify exact version
3. Document JDK-specific features used
4. Test with both old and new JDK before full migration

## Testing Strategy

### Pre-Update Testing
- Document current performance baselines
- Capture current test coverage metrics
- Document all current dependencies
- Create snapshot of working configuration

### Post-Update Testing
- Unit tests (must be 100% passing)
- Integration tests
- Performance benchmarking (compare with baseline)
- Security scanning
- Load testing for critical endpoints
- Browser compatibility (for Playwright tests)

### Continuous Testing
- Automated dependency vulnerability scanning
- Regular performance monitoring
- Quarterly dependency review
- Annual major version review

## Documentation Updates Required

After each update phase:
1. Update README.md with new version requirements
2. Update CI/CD pipeline configurations
3. Update developer setup guide
4. Update deployment documentation
5. Document any breaking changes
6. Update API documentation if affected

## Resources and References

### Official Documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/)
- [Spring Boot 3.4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)
- [Java 25 Early Access](https://jdk.java.net/25/)
- [Gradle Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html)

### Migration Guides
- [Spring Boot 3.x Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Java Version Migration Best Practices](https://docs.oracle.com/en/java/javase/21/migrate/)

### Dependency Documentation
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Lombok Changelog](https://projectlombok.org/changelog)
- [Playwright Java](https://playwright.dev/java/)
- [REST Assured](https://rest-assured.io/)

## Success Criteria

### For Configuration Fixes
- ✅ Build completes without warnings
- ✅ IntelliJ IDEA imports project correctly
- ✅ All developers can build without environment-specific setup

### For Spring Boot Update
- ✅ All tests pass
- ✅ Application starts in < 10 seconds
- ✅ No performance degradation
- ✅ No new security vulnerabilities
- ✅ All endpoints respond as expected

### For Java Version Update
- ✅ All code compiles without errors
- ✅ No deprecated API warnings
- ✅ Performance improvement or maintained
- ✅ All dependencies compatible
- ✅ CI/CD pipeline successful

## Contact and Support

For questions or issues during updates:
1. Review this document
2. Check official Spring Boot documentation
3. Review dependency changelogs
4. Create issue in project repository
5. Consult Spring Boot community forums

---

**Document Version**: 1.0
**Last Updated**: 2026-01-05
**Next Review Date**: 2026-04-05
