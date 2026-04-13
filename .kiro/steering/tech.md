# Tech Stack & Build

## Language & Runtime

- **Java 8** source/target (must compile and run on JDK 8+, tested through JDK 22 and GraalVM Native)
- Do NOT use Java features above 8 in production code (lambdas and streams are fine; `var`, records, sealed classes are not)

## Build System

- **Apache Maven** multi-module project
- Parent POM: `pom.xml` at repo root
- All modules inherit `io.github.xuse:querydsl-sql-extension-parent`

### Common Commands

```bash
# Full build (compile + test + package)
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests for a single module
mvn test -pl querydsl-sql-extension

# Generate JaCoCo coverage report (output: target/test-report/)
mvn test
```

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
