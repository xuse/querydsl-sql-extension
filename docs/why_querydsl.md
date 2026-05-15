# Why QueryDSL?

**Contents**
- [What is QueryDSL](#what-is-querydsl)
- [Why This Framework](#why-this-framework)
- [Why QueryDSL Can Replace JPA](#why-querydsl-can-replace-jpa)
- [Project History](#project-history)
- [Appendix: Java Database Access Approaches Compared](#appendix-java-database-access-approaches-compared)

---

## What is QueryDSL

[QueryDSL](https://github.com/querydsl/querydsl) is a long-established query builder modeled on the SQL abstract syntax tree.

In the heyday of Hibernate and JPA, developers quickly found that ORM could not express moderately complex queries. Hibernate introduced HQL; JPA introduced QueryBuilder and NativeQuery — but none of these solutions were ideal. Around 2015, while adapting GeeQuery for Spring-Data, I discovered that Spring-Data had adopted QueryDSL as its new query builder.

Core strengths of QueryDSL:

- **Expressive**: Models the SQL syntax tree, covering nearly all SQL capabilities. It also provides unified query modeling for Lucene, JPA, Elasticsearch, JDO, MongoDB, and more.
- **Type-safe**: Compile-time checking of query syntax and field references. In contrast, using JSON, SQL, HQL, or JPQL directly means string concatenation — dangerous and tedious for long-lived projects. Developers cannot be sure their changes are correct, nor that all affected locations have been updated.
- **Developer-friendly**: API style close to natural SQL, producing highly readable code with no boilerplate.

After trying QueryDSL I realized its enormous potential — it is essentially an ideal database facade. I rewrote the object construction and reflection layers to push performance to the limit, and that became this framework.

## Why This Framework

### Inherited from QueryDSL

- One of the most developer-friendly Query APIs available; many JPA users are already familiar with it.
- Extremely comprehensive SQL feature coverage — the most flexible framework short of writing raw SQL.
- Compile-time type checking keeps maintenance costs under control. Unlike the xxQL approach, the Query API approach inherently provides type safety.
- Good cross-database portability support.

### Added by This Framework

- **Peak performance**: ASM-generated field accessors eliminate reflection overhead. Batch operations can be 3–8× faster than MyBatis.
- **Single-table CRUD**: GenericRepository wraps common operations — development efficiency on par with JPA for simple cases.
- **DDL management**: Schema analysis, diff, and auto-update. Via package scanning, provides Hibernate-like startup schema maintenance. DDL capabilities are also exposed as APIs for developer use.
- **Pure POJO mode**: No Q-class generation required; use lambda expressions for column references, lowering the barrier to entry.
- **Practical features**: Business-level table routing/sharding, table initialization data import — completely decoupling business code from SQL statements.

### Other Considerations

- Provides raw Connection access for scenarios that genuinely require JDBC/SQL.
- The internet industry has largely abandoned JPA in favor of JDBC and MyBatis (see appendix), losing type safety and compile-time checks in the process. A key contribution of this framework is restoring JPA-like development efficiency and compile-time checking for these developers.
- Cross-database portability is also provided. While the internet industry generally undervalues this, who can guarantee they won't need to deliver on-premise ToB solutions in the future?
- If you choose the Query API approach, this framework is likely the best option available.

## Why QueryDSL Can Replace JPA

Most developers who know QueryDSL learned it through JPA and see it merely as a supplement to JPA queries. Here I explain why it can serve as a complete replacement.

I myself transitioned from the JPA approach, roughly through these steps:

1. **MyBatis**: In an 8-person team with a dedicated DBA on a two-year maintenance project, we frequently optimized database structures and queries. Maintaining scattered, multi-table SQL fragments modified by multiple people — without compiler or type-safety checks — consumed enormous team effort.
2. **JPA with JPQL/QueryBuilder** (gradually moving from mainstream implementations to my own JPA implementation, GeeQuery).
3. **Spring-Data**: Discovered and adopted QueryDSL as the query builder. Business code became highly readable, and the compile-time checking problem was elegantly solved.
4. **The realization**: querydsl-jpa already handles complex queries. What does JPA actually provide beyond that? Single-table CRUD, cascade, and lazy loading. Wrapping single-table CRUD is trivial, and cascade/lazy loading aren't strictly necessary — so why keep JPA?

Here is a feature-by-feature analysis:

**Single-table operations (save/merge/load/update)**

GenericRepository provides equivalent APIs — no need to manually assemble query objects. → **Replaceable**

**Complex multi-table queries**

These already use QueryDSL syntax in JPA; nothing changes after switching. → **Replaceable**

**NativeQuery (extremely complex analytical SQL)**

This framework does not provide NativeQuery. Use Spring-JDBC instead — it is more widely adopted, handles basic object mapping, and shares the same transaction with this framework. → **Solved via complementary approach**

**LOB lazy loading**

In practice, whether business logic needs LOB fields is almost always known before executing the query. Lazy loading turns one database round-trip into two — not a good trade-off. JPA needs this feature because it cannot precisely specify SELECT columns. QueryDSL can flexibly specify exactly which columns to query — fetch what you need, skip what you don't. This "feature" is really compensation for a JPA limitation. → **Replaced by specifying columns explicitly**

**Cascade (@OneToOne/@OneToMany/@ManyToOne/@ManyToMany)**

A good productivity feature, but supporting cascade significantly increases performance risk. Common in enterprise and small projects; virtually unused in the internet industry. This framework targets performance-sensitive large projects. May be added via SPI extension in the future. → **Future consideration**

**L1/L2 Cache**

This framework does not implement object caching, for these reasons:

- The design philosophy is lightweight, easy to understand, and highly observable. The framework should expose database capabilities to developers, not add intermediate layers.
- In distributed applications, multi-instance concurrent access is common; database-level caching complicates things.
- Where the database does not provide repeatable reads, the framework should not alter database behavior.
- Modern caching frameworks abound (Spring Cache + Caffeine/Redis, etc.). Coarse-grained business-level caching is far more effective than database-level caching. The framework should not interfere with architects' caching decisions.

→ **Use application-level caching instead**

## Project History

This framework was something of a happy accident, and will continue to be maintained at a relaxed pace.

- **2017**: Once the idea of replacing JPA took hold, I acted immediately — removed JPA, kept only the querydsl-sql module. Extended QueryDSL's listener mechanism for more detailed logging.
- **2018**: Performance evolution. Optimized away reflection by introducing ASM-generated dynamic classes for field assembly. The framework was officially born.
- **2019–2023**: Personal use phase. Made small refinements to common features for cleaner code — auto-timestamps, auto-generated GUIDs, etc.
- **2024**: While reading the source code, I noticed the original author had initially intended to support DDL syntax but abandoned it. Having written similar functionality for GeeQuery years earlier, I invested significant time adding DDL support. Also added the Q-class-free pure POJO mode to lower the barrier to entry.
- **Going forward**: Staying lightweight is the guiding principle. No plans to add optimistic locking, SQL parsers, sharding, or built-in connection pools — I've written all of these before, but bundling them together serves no purpose.

> If you find this framework useful, please Star [QueryDSL](https://github.com/querydsl/querydsl) rather than this project. The good design comes from QueryDSL; I merely added some modest enhancements.

## Appendix: Java Database Access Approaches Compared

### JPA Approach

This approach suits single-table applications and small enterprise projects. However, mastery requires deep study of the framework's own knowledge system — HQL/JPQL/QueryBuilder/QueryDSL-JPA/SpringData-JPA/Hibernate's object management and caching mechanisms.

The fundamental issue: the technology stack runs deep, but in a direction orthogonal to database design and SQL optimization. JPQL imposes many restrictions for RDBMS compatibility, preventing full use of database-specific features (e.g., RowId, window/analytic functions).

| Aspect | JPA/Hibernate | Spring Data JPA | QueryDSL-JPA |
|---|---|---|---|
| Description | Pure object-oriented data access. Requires HQL/JPQL/QueryBuilder for complex queries. Easy to start, hard to master | Spring's wrapper around JPA, retaining all JPA features | Used with Spring Data JPA; the ultimate solution on this route |
| Pros | Simple single-table ops; cascade/lazy loading with no code | All JPA pros + method-name auto-proxy + QueryDSL as query builder | All of the above + convenient complex query building |
| Cons | Low flexibility; performance depends on cache; multi-instance cache invalidation issues | Still JPA underneath; high mastery cost | All JPA cons + learning cost of three frameworks |

### JDBC Approach

| Aspect | Spring JDBC/JDBCTemplate | querydsl-sql | querydsl-sql-extension (this framework) |
|---|---|---|---|
| Description | Spring's JDBC wrapper; helps with simple mapping | Models database and SQL features; uses AST internally to describe database access requests | Built on querydsl-sql, retaining all its characteristics |
| Pros | Method-name auto-proxy | No SQL writing needed; no string concatenation; various result mappings; cross-DB portability via AST + dialect templates | All querydsl-sql pros + peak performance + single-table API + Lambda mode |
| Cons | Low abstraction; requires SQL and JDBC knowledge; manual ResultSet handling; no portability support | Even simple operations require coding; not efficient enough | No cascade/lazy loading yet (mitigated by convenient multi-table Join Query and result mapping) |

querydsl-sql is the only JDBC-based framework that achieves both:
1. Developers never write any xxQL (HQL/JPQL/SQL) — a single API covers virtually all database operations across different RDBMS.
2. Developers never touch the JDBC API or handle result mapping manually.

### MyBatis Approach

This approach is built on MyBatis rather than using JDBC directly. Choosing this route essentially means giving up database portability — MyBatis's core is SQL string concatenation, not AST, so the final database access model is expressed as SQL strings. While tools like SQLParser attempt to improve portability, and various upper-layer frameworks adopt AST-like models for complex queries, the underlying architecture determines this route's ceiling.

> Database portability doesn't have to be solved at the access framework level. It can also be addressed via JDBC drivers (SQL rewriting through lexical analysis), middleware (parsing database network protocols), or polyglot databases (syntax compatibility modes).

| Aspect | MyBatis | MyBatis Plus/Flex |
|---|---|---|
| Pros | Simple, popular, easy to learn, lightweight, decent performance | Adds Query API and code generation (similar to Q-classes or Lambda mode), improving dev efficiency |
| Cons | No DB portability; hand-written SQL is inefficient; no compile-time checks | Still requires some SQL fragments; other MyBatis drawbacks remain |

### The Ideal SQL Persistence Framework

JPA has both outstanding strengths and glaring weaknesses:
- Strengths: High development efficiency, type safety, compile-time Query API checking
- Weaknesses: Poor performance, high learning cost, heavyweight, structural complexity that hinders optimization and troubleshooting

The ideal = JPA's development efficiency and type safety + lightweight implementation + closeness to native database (SQL and JDBC).

The popularity of MyBatis Plus/Flex and similar frameworks stems from their API-level convergence of these two routes' strengths. This framework shares the same goal — built on QueryDSL, its API is nearly identical to what JPA developers already use, while maintaining a lightweight implementation and peak performance.
