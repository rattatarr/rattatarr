# Test Reference Guide

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
# Example: Run only ProfilesServiceTest
mvn test -Dtest=ProfilesServiceTest

# Example: Run only controller tests
mvn test -Dtest=*Controller*Test
```

### Run Tests by Package

```bash
# Run all tests in services package
mvn test -Dtest=com.rattatarr.rattatarr.services.*Test

# Run all tests in controllers package
mvn test -Dtest=com.rattatarr.rattatarr.controllers.*Test
```

### Run Tests with Coverage Report

```bash
# Generate coverage report (requires jacoco plugin)
mvn clean test jacoco:report

# View report at: target/site/jacoco/index.html
```

### Run Tests in Continuous Mode

```bash
# Re-run tests on file changes (requires maven-surefire-plugin configuration)
mvn test -Dsurefire.rerunFailingTestsCount=2
```

### Run Tests with Detailed Output

```bash
# Verbose output
mvn test -X

# Show stack traces
mvn test -e
```

### Skip Tests

```bash
# Build without running tests
mvn clean install -DskipTests

# Or use
mvn clean install -Dmaven.test.skip=true
```

## Test Results Location

- **Test Reports**: `target/surefire-reports/`
- **Compiled Tests**: `target/test-classes/`
- **Test Output**: Console and `target/surefire-reports/*.txt`
- **Jacoco Coverage Report**: `target/site/jacoco/index.html`

## Common Issues

### Tests Not Running

```bash
# Clean and rebuild
mvn clean test

# Update dependencies
mvn clean install -U
```

### Specific Test Failing

```bash
# Run with stack trace
mvn test -Dtest=TestClassName -e

# Debug mode
mvn test -Dtest=TestClassName -Dmaven.surefire.debug
```

## Continuous Integration

For CI/CD pipelines (GitHub Actions, Jenkins, etc.):

```bash
# Run tests and fail build on test failure
mvn clean test

# Run with XML reports for CI tools
mvn clean test -Dsurefire.useFile=true
```

## Test Development Tips

1. **Run tests frequently** during development
2. **Use TDD** - Write test first, then implementation
3. **Keep tests independent** - No shared state
4. **Mock external dependencies** - Faster, more reliable
5. **Test edge cases** - Null values, empty strings, etc.
6. **Follow naming conventions** - Clear, descriptive names
7. **One assertion per test** - When possible
8. **Use Given-When-Then** - Clear test structure

## Debugging Tests

```bash
# Run test with debug enabled (port 5005)
mvn test -Dtest=TestClassName -Dmaven.surefire.debug

# Then attach debugger to port 5005
```

## Performance Testing

```bash
# Run tests with profiler
mvn test -DargLine="-agentlib:hprof=cpu=samples"

# Check test execution times
mvn test | grep "Time elapsed"
```

## Test Examples

### Unit Test Example

```java

@Test
void testSaveValidProfile() {
    // Given
    when(repository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());
    when(repository.save(any(Profiles.class)))
            .thenReturn(testProfile);

    // When
    Profiles result = service.save(testProfile);

    // Then
    assertNotNull(result);
    verify(repository).save(testProfile);
}
```

### Validation Test Example

```java

@Test
void testBlankName() {
    CreateProfileRequestDTO dto =
            new CreateProfileRequestDTO("", UUID.randomUUID());

    Set<ConstraintViolation<CreateProfileRequestDTO>> violations =
            validator.validate(dto);

    assertFalse(violations.isEmpty());
}
```

### Service/Controller Test Example

```java

@ExtendWith(MockitoExtension.class)
class YourServiceTest {
    @Mock
    private YourRepository repository;

    @InjectMocks
    private YourService service;

    @Test
    void testYourFeature() {
        // Given
        // When
        // Then
    }
}
```