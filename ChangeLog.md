## ChangeLog

**Version Numbering Scheme:**
The version number consists of two parts. 
The first number corresponds to the version of the querydsl library, and the second is the revision number of this extension framework. 

```
v{querydsl 版本号} - r(extension version)
```

**v5.0.0-r130**
2024-11-27
 * New modules for r2dbc and r2dbc-spring transaction
 * Supports DDL on H2DB.
 * Upgerade JUnit to Junit.jupiter

**v5.0.0-r120**

2024-10-01

* Project Split: Separated unused core classes into other projects. Created `querydsl-sql-extension-spring` for Spring integration. `querydsl-sql-extension` no longer depends on any Spring Framework and FastJSON.
* Automated Unit Testing and Reporting: During build time, unit tests are automatically executed and reports are generated. Current line coverage is 65%, with further improvements planned for the next version.
* Added MySQL Mock Driver: Introduced a MySQL mock driver in the test code to simulate MySQL database behavior during unit testing.
* Unit Test Enhancements: Fixed minor errors in utility classes under boundary conditions.
* Enhanced AlterTableQuery: Enhanced capabilities of `AlterTableQuery` to support column modifications, renaming, and other operations.

**v5.0.0-r110**

2024-09-01

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

**v5.0.0-r104**

2024-08-08

* Support for GraalVM Native Mode usage.
* Support for using `record` types as entities in Java 16 and above.

**v5.0.0-r102**

2024-07-24

* Organize Javadoc by packages and change the package location of some classes. Add bilingual Javadocs for important classes.
* Support for external extensions to implement Facades independently. Extend various query APIs by implementing the `ExtensionQueryFactory` interface.
* Provide a `GenericRepository` class, enabling common repository features in Spring without writing any code. Supports multiple API styles.
* Support for POJO mapping without Query Class. Use Bean Class instead of Query Class. Use method reference `Lambda` instead of model fields.

**v5.0.0-r101**

2024-06-28

* Added a mechanism for the upper business layer to adjust table names, supporting scenarios where tables are split at the business layer.
* Support for MySQL Partition management. Supports partition types such as RANGE, LIST, HASH, and KEY, as well as operations like partition creation, adjustment, deletion, and reorganization.
* Supplemented missing interfaces for table constraints and indexes. Some operations are supported by MySQL as online operations without locking the table.


**v5.0.0-r8 **

2018

* Upgraded to support Querydsl v5.0.0
* Added MySQL dialect: `com.querydsl.core.types.dsl.MySQLWithJSONTemplates`, `JsonExpressions`, etc., supporting JSON field operations
* Annotation `@AutoGenerated` supports automatic generation of field content (during update/insert). When using the `populate()` method, it autowrites; when using the `set()` method, you can call the `populateAutoGeneratedColumns()` method to generate.
* Added generation rules for GUID, SnowFlake ID, etc.


**v4.1.1-r4**

* Added simpler logging more suitable for Linux.
* Optimized performance for result set concatenation. Provided additional `RelationalPathBaseEx` for inheritance.
* API additions — introduced a DDL syntax framework (not yet implemented).

**v4.2.1-r1 **

2017

1. Log Extensions
2. Addressed the `Connection is not transactional` exception and certain other personalized requirements through this project extension. Further updates will follow as functionalities are improved.