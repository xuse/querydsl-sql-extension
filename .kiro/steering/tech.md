# Tech Stack & Build

## Language & Runtime

- **Java 8** source/target (must compile and run on JDK 8+, tested through JDK 22 and GraalVM Native)
- Do NOT use Java features above 8 in production code (lambdas and streams are fine; `var`, records, sealed classes are not)
- **IMPORTANT**: Use **Java 17** (or higher) to compile and run tests. The compiled artifacts target Java 8 for runtime compatibility.
  - Test dependencies (Mockito 5.13.0, H2 2.3.232, JUnit Jupiter 5.11.3) require Java 11+
  - Maven compiler plugin configured with `source=1.8` and `target=1.8` ensures Java 8 compatibility for production code
  - Test code can use modern Java features (e.g., `assertInstanceOf` from JUnit 5.8+)

## Build System

- **Apache Maven** multi-module project
- Parent POM: `pom.xml` at repo root
- All modules inherit `io.github.xuse:querydsl-sql-extension-parent`

### Common Commands

```bash
# Full build (compile + test + package) - requires Java 17+
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests for a single module
mvn test -pl querydsl-sql-extension

# Generate JaCoCo coverage report (output: target/test-report/)
mvn test
```

### Build Requirements

- **Compile & Test**: Java 17+ (recommended: Java 17 or Java 21)
- **Runtime**: Java 8+ (compiled artifacts are Java 8 compatible)
- **Maven**: 3.6.0+

## Key Dependencies

| Dependency | Version | Scope |
|---|---|---|
| querydsl-sql | 5.0.0 | compile |
| Lombok | 1.18.32 | provided |
| JetBrains Annotations | 20.0.0 | provided |
| SLF4J API | 1.7.30 | compile |
| JUnit Jupiter | 5.11.3 | test |
| Mockito | 5.13.0 | test |
| Derby | 10.14.2.0 | test |
| H2 | 2.3.232 | test |

## Code Generation & Tooling

- **Lombok** — used project-wide. `lombok.config` at root sets `config.stopBubbling=true` and `lombok.addLombokGeneratedAnnotation=true` (JaCoCo excludes Lombok-generated code).
- **ASM 9** — bundled (shaded) in `com.github.xuse.querydsl.asm` for runtime bytecode generation of bean accessors. Do not use external ASM.
- **JaCoCo** — coverage plugin configured in parent POM; excludes ASM and Spring resource packages.

## Testing

- **JUnit 5 (Jupiter)** for all tests
- **Mockito** for mocking
- Embedded databases for unit tests: Apache Derby, H2
- MySQL and PostgreSQL drivers included for integration tests (require external DB)
- Test resources include SQL scripts (`test_script_derby.sql`, `test_script_mysql.sql`) and sample data files
