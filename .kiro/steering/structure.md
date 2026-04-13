# Project Structure

## Module Layout

```
querydsl-sql-extension-parent (root POM)
├── querydsl-sql-extension-annotation   # Annotations only (@TableSpec, @ColumnSpec, @CustomType, etc.) — zero dependencies
├── querydsl-sql-extension              # Core module: SQL extensions, DDL, DML, ASM accessors, utilities
├── querydsl-sql-extension-spring       # Spring Framework integration (transactions, GenericRepository)
├── querydsl-sql-extension-datatype     # Additional custom type mappings
├── querydsl-sql-extension-codegen      # Code generation tools (Q-class generation from DB schema)
├── querydsl-sql-r2dbc                  # Reactive (R2DBC) database access
├── querydsl-sql-r2dbc-spring           # R2DBC + Spring transaction support
├── benchmarks                          # JMH-style performance benchmarks (not published)
└── static/                             # Documentation assets (user guide, performance guide, etc.)
```

## Dependency Flow

```
annotation ← extension ← spring
                       ← datatype
                       ← codegen
                       ← r2dbc ← r2dbc-spring
```

The `annotation` module has no dependencies and can be used standalone. All other modules depend on `extension`.

## Core Module Package Layout (`querydsl-sql-extension`)

Base package: `com.github.xuse.querydsl`

| Package | Purpose |
|---|---|
| `asm` | Bundled ASM library for bytecode generation (do not modify directly) |
| `config` | Configuration and initialization settings |
| `init` | Application startup, package scanning, schema sync |
| `jmx` | JMX monitoring beans |
| `lambda` | Lambda-based column/table references (POJO mode) |
| `repository` | GenericRepository and CRUD abstractions |
| `spring` | Spring-specific utilities (in the spring module) |
| `sql` | Core SQL extensions — query factories, DML, DDL, expressions |
| `sql.column` | Column metadata and modeling |
| `sql.dbmeta` | Database metadata retrieval |
| `sql.ddl` | DDL statement generation and execution |
| `sql.dialect` | Database-specific SQL dialect extensions (`SQLTemplatesEx`) |
| `sql.dml` | DML operations (insert, update, delete, batch) |
| `sql.expression` | Custom SQL expressions and functions |
| `sql.extension` | Extension point interfaces |
| `sql.log` | SQL logging (`QueryDSLSQLListener`) |
| `sql.partitions` | Table partition management |
| `sql.routing` | Table name routing / sharding support |
| `sql.support` | Internal support utilities |
| `types` | Custom JDBC type mappings (`@CustomType` implementations) |
| `util` | General-purpose utilities |

## Key Extension Points

- `RelationalPathBaseEx` — extended base class for Q-classes (adds annotation scanning via `scanClassMetadata()`)
- `SQLTemplatesEx` — dialect extension interface for DDL support on new databases
- `ExtensionQueryFactory` — interface for custom facade implementations
- `GenericRepository` — low-code CRUD repository (Spring module)

## Test Structure

Tests mirror the main source layout. Each module has its own `src/test/java` and `src/test/resources`. The core module also has Kotlin tests under `src/test/kotlin`.
