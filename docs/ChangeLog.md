## ChangeLog

**Version Numbering Scheme:**
The version number consists of two parts. 
The first number corresponds to the version of the querydsl library, and the second is the revision number of this extension framework. 

```
v{querydsl version} - r(extension version)
```

**v5.0.0-r173**  (2026-05)

* **New: `@PathBinder` annotation redesign** — Renamed `readConverter`/`writeConverter` to `fromDb`/`toDb` for clarity. Added `converterSource` for shared converter lookup, `skipTypeCheck` for custom JDBC Type scenarios, and `value` now defaults to empty (same-name binding).
* **New: `BuiltinConverters`** — Symmetric built-in type converters for DTO↔entity mapping: all numeric types, String↔Date/Timestamp/Time (JDBC standard format), long↔Date, and Enum conversions (CodeEnum by code, ordinary enum by ordinal, String by name).
* **New: `ConfigurationEx.registerDtoConverter()`** — Register custom global converters for DTO field mapping. Also `globalConverterSource` for shared converter ref lookup class.
* **New: `applyBatch()` for Update/Delete** — Batch update and delete using DTO objects with `@Condition` annotated fields as WHERE clause and remaining fields as SET clause. Generates `UPDATE SET ... WHERE ...` / `DELETE WHERE ...` with JDBC batch execution.
* **New: Batch insert/update with DTO** — `@PathBinder` DTOs in `populateBatch()` only include mapped columns; unmapped columns use database DEFAULT values.
* **New: `ConfigurationEx.batchNullStrategy`** — Controls null handling in batch insert (`AUTO_DEFAULT` / `AGGRESSIVE_DEFAULT` / null).
* **New: DDL dialect support expanded to 10 databases** — Added Oracle, SQL Server (2005/2008/2012), HSQLDB, SQLite, DB2, CUBRID. Existing MySQL, PostgreSQL, Derby, H2 dialects unchanged.
* **Fix: `CodecClassGenerator` package preservation** — Generated codec classes now reside in the same runtime package as the target bean, fixing `IllegalAccessError` on package-private inner classes.
* **Breaking: `@PathBinder` attribute names changed** — `readConverter` → `fromDb`, `readConverterRef` → `fromDbRef`, `writeConverter` → `toDb`, `writeConverterRef` → `toDbRef`.
* **Breaking: Type-mismatch enforcement** — When no converter is found between DTO field type and entity column type, an exception is thrown instead of silent pass-through. Use `@PathBinder(skipTypeCheck=true)` to opt out.
* **Breaking: Privilege detection** — Each database now uses native commands for privilege checks (e.g., MySQL `SHOW GRANTS`, PostgreSQL `pg_has_role`). `SimpleDetector` is `@Deprecated`; migrate if referenced directly.
* **Internal: Map implementations, JDKEnvironment, ProjectionsAlter caching** — No public API changes. Improved GraalVM Native Image compatibility.

**v5.0.0-r141**  (2025-09-25)
* Fix error in date truncation on date before 1970-01-01.    

**v5.0.0-r140**  (2025-09-10)

* Some fix on function `findByConditionBean`
* Utilities update.
* Unit test coverage to 75%.
* Revise Javadoc via AI translations.
* The UPDATE statement, when updating records, will by default update columns designated to auto-generate values.
* upgrade ASM to support class format of Java 21
* API adjust on CSV utilities.

* **v5.0.0-r130** (2024-11-27)

 * New modules for r2dbc and r2dbc-spring transaction, supports reactive programming. 
 * Supports DDL executing on H2DB.
 * Upgerade JUnit to Junit.jupiter

**v5.0.0-r120**  (2024-10-01)

* Project Split: Separated unused core classes into other projects. Created `querydsl-sql-extension-spring` for Spring integration. `querydsl-sql-extension` no longer depends on any Spring Framework and FastJSON.
* Automated Unit Testing and Reporting: During build time, unit tests are automatically executed and reports are generated. Current line coverage is 65%, with further improvements planned for the next version.
* Added MySQL Mock Driver: Introduced a MySQL mock driver in the test code to simulate MySQL database behavior during unit testing.
* Unit Test Enhancements: Fixed minor errors in utility classes under boundary conditions.
* Enhanced AlterTableQuery: Enhanced capabilities of `AlterTableQuery` to support column modifications, renaming, and other operations.

**v5.0.0-r110**  (2024-09-01)

* PostgreSQL DDL Support: Tested with PostgreSQL 10.3. General DDL operations are now supported.
* PostgreSQL Table Partitioning Support: 
  - Due to significant differences between PostgreSQL and MySQL partitioning mechanisms, currently only table partition creation and partition addition/removal are supported.
  - Hash partitioning is not supported.
  - Adding/removing partition configurations to/from existing tables is not supported (mechanically unfeasible, as PostgreSQL partitions are defined at table creation).
  - Partition reorganization is not supported.
  - The above feature discrepancies caused by database mechanism differences have no support plans at the moment.
* PostgreSQL support turned out to be more complex than anticipated. Consequently, the mechanisms for Schema retrieval and DDL generation were refactored.
* Batch Insert Performance Optimization: Enhanced data insertion performance under BatchInsert, especially for MySQL bulk inserts. Default switch from JDBC Batch to Bulk SQL statements, with performance improvements of up to approximately 65 times in certain scenarios.
* QueryDSL Insert Batch Adjustment: Added consistent parameter handling.
* Data Initialization Configuration: Added `setPrimaryKeys` to control whether primary key columns should be written to the database.

**v5.0.0-r104**  (2024-08-08)

* Support for GraalVM Native Mode usage.
* Support for using `record` types as entities in Java 16 and above.

**v5.0.0-r102**  (2024-07-24)

* Organize Javadoc by packages and change the package location of some classes. Add bilingual Javadocs for important classes.
* Support for external extensions to implement Facades independently. Extend various query APIs by implementing the `ExtensionQueryFactory` interface.
* Provide a `GenericRepository` class, enabling common repository features in Spring without writing any code. Supports multiple API styles.
* Support for POJO mapping without Query Class. Use Bean Class instead of Query Class. Use method reference `Lambda` instead of model fields.

**v5.0.0-r101**  (2024-06-28)

* Added a mechanism for the upper business layer to adjust table names, supporting scenarios where tables are split at the business layer.
* Support for MySQL Partition management. Supports partition types such as RANGE, LIST, HASH, and KEY, as well as operations like partition creation, adjustment, deletion, and reorganization.
* Supplemented missing interfaces for table constraints and indexes. Some operations are supported by MySQL as online operations without locking the table.

**v5.0.0-r8 **  (2018)

* Upgraded to support Querydsl v5.0.0
* Added MySQL dialect: `com.querydsl.core.types.dsl.MySQLWithJSONTemplates`, `JsonExpressions`, etc., supporting JSON field operations
* Annotation `@AutoGenerated` supports automatic generation of field content (during update/insert). When using the `populate()` method, it autowrites; when using the `set()` method, you can call the `populateAutoGeneratedColumns()` method to generate.
* Added generation rules for GUID, SnowFlake ID, etc.


**v4.1.1-r4**

* Added simpler logging more suitable for Linux.
* Optimized performance for result set concatenation. Provided additional `RelationalPathBaseEx` for inheritance.
* API additions — introduced a DDL syntax framework (not yet implemented).

**v4.2.1-r1 **  (2017)

1. Log Extensions
2. Addressed the `Connection is not transactional` exception and certain other personalized requirements through this project extension. Further updates will follow as functionalities are improved.