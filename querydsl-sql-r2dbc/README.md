# querydsl-sql-r2dbc

Using r2dbc + querydsl API in reactive programming.


**Table of contents**
- [querydsl-sql-r2dbc](#querydsl-sql-r2dbc)
	- [dependency](#dependency)
	- [Usage](#usage)
	- [Features](#features)

## dependency

```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-r2dbc</artifactId>
	<version>${querydsl-extension.version}</version>
</dependency>
```

## Usage

**Init R2dbc Session**

```java

	R2dbcFactory factory =  new R2dbcFactory(
				getConnectionFactory(),
				new ConfigurationEx(H2Templates.builder().newLineToSingleSpace().build()));
```

**Insert**

```java
	LambdaTable<Foo> table = () -> Foo.class;

	Foo foo = new Foo();
	foo.setCode("A" + StringUtils.randomString());
	foo.setContent("Test");
	foo.setCreated(Instant.now());
	foo.setName("Zhangsan");
	foo.setUpdated(new Date());
	foo.setVolume(100);

	Mono<Long> v = factory.insert(table).prepare(insert -> insert.populate(foo)).execute();
```

**Update**

```java
LambdaTable<Foo> table = () -> Foo.class;

Mono<Long> count = factory.update(table).prepare(update -> 
                    update.set(Foo::getName,"new Name")
    					.where().eq(Foo::getId, 100)
						.or(or->or.eq(Foo::getCode, "TEST"))
    					.build()).execute();
```

**Delete**

```java
LambdaTable<Foo> table = () -> Foo.class;

Mono<Long> deleteCount = factory.delete(table).prepare(delete -> 
               delete.where()
                .eq(Foo::getName, "test")
				.between(Foo::getCreated, Instant.parse("2020-12-03T10:15:30.00Z. "), Instant.now())
				.build()).execute();
```

**Select**

```java
LambdaTable<Foo> table = () -> Foo.class;
{
	//Example 1. fetch List
	Flux<Foo> flux = factory.selectFrom(table).prepare(q -> q
				.where().eq(Foo::getName,"Zhangsan")).fetch();
}
{
	//Example 2. fetch List and count
	R2Fetchable<Foo> fetch = factory.selectFrom(table).prepare(q -> q
				.where().eq(Foo::getName,"Zhangsan"));
	Mono<Long> count = fetch.fetchCount();
	Flux<Foo> flux = fetch.fetch();    
}
{
    //Complex query.
	QUser user = QUser.user;
	QSchool school = QSchool.school;
			
	Flux<Tuple> flux = factory.select(user.id,user.name,school.id,school.name).prepare(q->
		q.from(user).leftJoin(school).on(user.uid.eq(school.code))
		.where(user.name.in("Jhon","Mark","Linda"))
	).fetch();
}
```

**TL;DR**

The Query object provided for the Lambda within the `prepare` method is a native subclass of the `com.querydsl.sql.AbstractSQLQuery<T, Q>`, they are --

* com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter 
* com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter
* com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter
* com.github.xuse.querydsl.sql.SQLQueryAlter<T>
  
1. Building queries on these objects are identical to those on original querydsl-sql-extension.

2. Unable to call `execute` or `fetch` methods on these objects: The methods are implemented using JDBC and There is no JDBC connection available in R2db scenario. Therefore, please call the `fetch` and `execute` methods on class `com.github.xuse.querydsl.r2dbc.R2dbcFactory.R2Fetchable` and `com.github.xuse.querydsl.r2dbc.R2dbcFactory.R2Executeable`.

3. Return values in R2dbc queries are always Flux/Mono objects.

## Features

* Only depends on `r2dbc-spi` and `reactor-core`, without reliance on springframework or spring-data.

* Support for connection pooling with `r2dbc-pool`.

* Does not include transaction control. For transaction control, please use the  [query-sql-r2dbc-spring](../querydsl-sql-r2dbc-spring/) module .

* Batch operations(Insert/Delete/Update) are not supported yet. 

* Does not support DDL operations (Create Table/Alter table and etc..).