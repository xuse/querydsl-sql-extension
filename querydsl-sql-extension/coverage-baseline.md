# JaCoCo Coverage Baseline Report

**Generated**: 2026-05-28  
**Module**: querydsl-sql-extension  
**Test Suite**: 967 tests (0 failures, 11 skipped)  
**JaCoCo Version**: 0.8.14  
**Report Location**: `querydsl-sql-extension/target/test-report/index.html`

## Build Command

```bash
mvn clean test -pl querydsl-sql-extension -Dgpg.skip=true
```

## Exclusions Confirmed

| Exclusion | Status | Mechanism |
|-----------|--------|-----------|
| `com.github.xuse.querydsl.asm` package | ✅ Excluded | JaCoCo `<excludes>` in parent POM |
| `@lombok.Generated` classes | ✅ Excluded | `lombok.addLombokGeneratedAnnotation=true` + JaCoCo 0.8.14 auto-exclusion |
| `com.github.xuse.querydsl.spring.core.resource` | ✅ Excluded | JaCoCo `<excludes>` in parent POM |
| `com.github.xuse.querydsl.util.JDKEnvironment` | ✅ Excluded | JaCoCo `<excludes>` in parent POM |

## Per-Package Coverage Summary

| Package | Line Coverage | Branch Coverage | Total Lines | Priority |
|---------|:------------:|:--------------:|:-----------:|:--------:|
| `com.github.xuse.querydsl.config` | 69.0% | 50.8% | 336 | Low |
| `com.github.xuse.querydsl.init` | 58.5% | 43.2% | 552 | Low |
| `com.github.xuse.querydsl.init.csv` | 85.1% | 81.6% | 931 | — |
| `com.github.xuse.querydsl.jmx` | 80.9% | 71.6% | 215 | — |
| `com.github.xuse.querydsl.lambda` | 81.6% | 40.9% | 320 | Medium |
| `com.github.xuse.querydsl.repository` | 60.9% | 50.4% | 999 | — |
| `com.github.xuse.querydsl.sql` | 74.9% | 57.6% | 1149 | — |
| `com.github.xuse.querydsl.sql.column` | 64.5% | 65.2% | 445 | — |
| `com.github.xuse.querydsl.sql.dbmeta` | 69.1% | 45.2% | 699 | — |
| `com.github.xuse.querydsl.sql.ddl` | 77.4% | 62.3% | 1526 | High |
| `com.github.xuse.querydsl.sql.dialect` | 60.7% | 48.6% | 1188 | Medium |
| `com.github.xuse.querydsl.sql.dml` | 70.3% | 57.0% | 1415 | Medium |
| `com.github.xuse.querydsl.sql.expression` | 72.7% | 47.1% | 1377 | High |
| `com.github.xuse.querydsl.sql.log` | 71.3% | 53.8% | 164 | — |
| `com.github.xuse.querydsl.sql.partitions` | 66.3% | 53.1% | 249 | Medium |
| `com.github.xuse.querydsl.sql.routing` | 94.1% | 96.7% | 68 | High |
| `com.github.xuse.querydsl.sql.support` | 75.1% | 63.0% | 369 | — |
| `com.github.xuse.querydsl.types` | 95.1% | 100.0% | 162 | Low |
| `com.github.xuse.querydsl.util` | 84.2% | 83.2% | 3824 | Low |
| `com.github.xuse.querydsl.util.collection` | 76.3% | 67.2% | 637 | Low |
| `com.github.xuse.querydsl.util.io` | 90.2% | 57.9% | 41 | Low |
| `com.github.xuse.querydsl.util.lang` | 57.2% | 58.7% | 790 | — |
| `com.querydsl.core.types` | 92.4% | 100.0% | 66 | — |
| `com.querydsl.sql` | 91.2% | 73.9% | 160 | — |

## Packages with Line Coverage Below 50%

**None** — All packages have line coverage above 50%.

The lowest line coverage packages are:
1. `com.github.xuse.querydsl.util.lang` — 57.2%
2. `com.github.xuse.querydsl.init` — 58.5%
3. `com.github.xuse.querydsl.sql.dialect` — 60.7%
4. `com.github.xuse.querydsl.repository` — 60.9%

## Packages with Branch Coverage Below 50%

| Package | Branch Coverage | Gap to 50% |
|---------|:--------------:|:----------:|
| `com.github.xuse.querydsl.lambda` | 40.9% | -9.1% |
| `com.github.xuse.querydsl.init` | 43.2% | -6.8% |
| `com.github.xuse.querydsl.sql.dbmeta` | 45.2% | -4.8% |
| `com.github.xuse.querydsl.sql.expression` | 47.1% | -2.9% |
| `com.github.xuse.querydsl.sql.dialect` | 48.6% | -1.4% |

## Coverage by Priority Tier

### High Priority (sql.expression, sql.routing, sql.ddl)

| Package | Line % | Branch % | Target Line | Target Branch |
|---------|:------:|:--------:|:-----------:|:-------------:|
| `sql.expression` | 72.7% | 47.1% | ≥60% | ≥50% |
| `sql.routing` | 94.1% | 96.7% | ≥60% | ≥50% |
| `sql.ddl` | 77.4% | 62.3% | ≥60% | ≥50% |

**Aggregate**: Line 75.0% ✅ | Branch 53.4% ✅ (targets: ≥60% line, ≥50% branch)

> Note: `sql.expression` branch coverage (47.1%) is slightly below the 50% target.

### Medium Priority (sql.dml, sql.partitions, lambda, sql.dialect)

| Package | Line % | Branch % | Target Line | Target Branch |
|---------|:------:|:--------:|:-----------:|:-------------:|
| `sql.dml` | 70.3% | 57.0% | ≥55% | ≥45% |
| `sql.partitions` | 66.3% | 53.1% | ≥55% | ≥45% |
| `lambda` | 81.6% | 40.9% | ≥55% | ≥45% |
| `sql.dialect` | 60.7% | 48.6% | ≥55% | ≥45% |

**Aggregate**: Line 68.0% ✅ | Branch 51.5% ✅ (targets: ≥55% line, ≥45% branch)

> Note: `lambda` branch coverage (40.9%) is below the 45% target.

### Low Priority (util, types, config, init)

| Package | Line % | Branch % | Target Line |
|---------|:------:|:--------:|:-----------:|
| `util` | 84.2% | 83.2% | ≥40% |
| `util.collection` | 76.3% | 67.2% | ≥40% |
| `util.io` | 90.2% | 57.9% | ≥40% |
| `types` | 95.1% | 100.0% | ≥40% |
| `config` | 69.0% | 50.8% | ≥40% |
| `init` | 58.5% | 43.2% | ≥40% |

**Aggregate**: Line 79.5% ✅ (target: ≥40% line)

## Key Observations

1. **All coverage targets are already met at the aggregate level** — the existing 967 tests provide good baseline coverage.
2. **Branch coverage gaps** exist in `lambda` (40.9%), `init` (43.2%), and `sql.expression` (47.1%).
3. **`sql.routing`** has excellent coverage (94.1% line / 96.7% branch) despite being marked as high priority — likely due to recent test additions.
4. **`types`** package already has 95.1% line / 100% branch coverage.
5. **Largest code volume** is in `util` (3824 lines) and `sql.ddl` (1526 lines).
6. **`ReflectCodec`** and `ReflectCodecRecord`** have 0% coverage (169 and 230 missed lines respectively) — these are key targets for Phase 2 tests.
7. Several classes in `sql.expression` have 0% coverage: `AliasMapBeans`, `DiscontinuousFieldBeans`, `QAliasBeansDiscontinuous`, `QAliasBeansContinuous`, `QBeansDiscontinuous`.
