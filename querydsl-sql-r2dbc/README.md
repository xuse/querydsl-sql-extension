# querydsl-sql-r2dbc





在响应式编程中使用 r2dbc + querydsl的API。

## 示例：

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

**简单来说**



在 `prepare` 方法中，Lambda表达式中提供的Query对象就是querydsl原生的Query对象子类，它们是——

* com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter

* com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter

* com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter

* com.github.xuse.querydsl.sql.SQLQueryAlter<T>

  

1. 用法与原生querydsl相同的方法构造查询：在这个对象上构建查询，其方法和在querydsl-sql-extension上完全相同。

2. 不可以调用`execute``fetch`等方法：上述类的执行方法，底层是调用JDBC实现的，在R2db场合下没有JDBC连接。因此在prepare方法执行完毕后，需要调用R2db框架下的fetch, execute等方法执行R2dbc查询。

3. R2dbc的请求，返回值总是Flux/Mono对象。









依赖

```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-r2dbc</artifactId>
	<version>5.0.0-r130</version>
</dependency>
```



测试支持：

* 



特性 

* 最小依赖，仅依赖 `r2dbc-spi` `reactor-core` 不依赖springframework,  spring-data。

* 支持连接池 r2dbc-pool.

* 此模块不包含事务控制。事务控制可使用  [query-sql-r2dbc-spring 模块](../querydsl-sql-r2dbc-spring/)  。

  

