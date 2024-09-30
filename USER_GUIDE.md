# 快速入门

## 1 GenericRepository介绍

很多Java程序员习惯为每个表和映射类创建一个Repository对象，封装一个数据访问层。数据访问层可以限制上层业务获得过于灵活的数据库访问能力，每种数据库访问行为都需要在DAO/Repository对象中编码实现，对于复杂的业务可以增强管理能力并提升复用性。

Repository只是QueryDSL的一种扩展Facade API（操作门面）。

> QueryDSL具有最强的SQL模型AST，但其使用者定位为能驾驭各种SQL语法的熟练人员，故提供的API极其强大灵活，这反而为一些不需要复杂操作的厂家或开发者所不喜。然而强大引擎的特点是几乎可以模拟业界所有同类框架的API风格。这里只是选择目前一些主流的风格，做了一些Facade的包装，以满足一些相对传统的开发者的喜好。

GenericRepository中提供了对单表常用的增删该查功能见下表，其他个性化业务逻辑可以在Repository类中自行编码实现。

推荐使用GenericRepository对象的一个重要的理由，是在纯POJO（无Query class）下提供了更友好的API。

本文后续的示例就以无QueryClass的情况展开。

| 方法              | 功能                                                         |      |
| ----------------- | ------------------------------------------------------------ | ---- |
| load              | 按数据库主键获得对象                                         |      |
| loadBatch         | 批量按数据库主键获得对象                                     |      |
| findByExample     | 按示例查询对象检索记录。                                     |      |
| find              | 根据用户自行填写的查询条件检索记录。                         |      |
| insert            | 插入一条数据                                                 |      |
| insertBatch       | 批量插入数据                                                 |      |
| delete(ID)        | 按主键删除记录                                               |      |
| delete            | 根据用户自行填写的查询条件删除记录                           |      |
| deleteByExample   | 按示例对象删除记录。                                         |      |
| update(ID,Object) | 按主键更新对象                                               |      |
| updateByKeys      | 按指定的几个字段作为Where条件，更新该对象中其他的字段。      |      |
| update            | 自行拼装条件进行数据库更新                                   |      |
| count             | 自行拼装条件进行Count查询                                    |      |
| countByExample    | 根据示例对象进行Count查询                                    |      |
| query             | 创建一个通用的查询构建器。                                   |      |
| findByCondition   | 使用@ConditionBean注解创建一个查询参数类。该类的字段上可以用@Condition注解查询运算符，<br />配合完成一些典型WEB分页查询。 |      |

> 多表操作和更多复杂的SQL，可以基于QueryDSL的原生API进行操作，即使用SQLQueryFactory系列的API，这是我看到过的业界最好的QueryBuilder。

## 2 多种API风格

### 准备工作

Step.1 要完成下列示例，可以先创建一个简单的POJO

```java
@Data
@TableSpec(name="test_foo",primaryKeys="id",collate = "utf8mb4_general_ci")
@Comment("测试用表")
public class Foo {
    @ColumnSpec(autoIncrement = true)
	private int id;
    
    @ColumnSpec
   	private String name;
    
    @ColumnSpec
   	private Instant created;
    
   	@ColumnSpec(size=16384)
	private String content;
}
```

Step.2 创建表
你可以自行手动创建。如果确认程序有DDL操作权限，也可以用以下java代码创建数据库表。

```java
SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
metadata.createTable(()->Foo.class).ifExists().execute();
```

Step.3 获得Repository对象

您可以参阅 第3章 用法简述 来获得Repository对象。然后即可体验，下面以一个查询为例介绍不同风格API用法。

### 传统风格

传统风格对复杂查询功能支持功能较弱，比如无法支持Between条件

```java
Foo foo=new Foo();
foo.setName("张三");
foo.setCreated(Instant.now());
repository.findByExample(foo);	
```

### Lambda风格

QueryDSL官方原生API中，要求使用者必须创建一个Q开头的类，称为查询类(query class). 使用查询类的模型可以大幅简化操作的写法，甚至包括各种复杂的函数都可以使用查询类中的API直接写出。这个查询类作用接近于JPA中的元模型(meta model)的用法。

本框架扩展了无查询类(query class)的用法。在不创建查询类的情况下，可以使用Lambda表达式来代替查询类。示例如下：

```java
repository.query()
	.eq(Foo::getName, "张三")
	.between(Foo::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
	.findAndCount();
```

### MyBatis-Plus风格

MyBatis支持Lambda风格用法，区别在于其还是相对传统地将查询分为两个对象—— 一个记录查询条件，一个框架会话 (在Mybatis里常用SqlSession或Mapper。在其他各类框架里有Session、Context、EntityManager等不同叫法)。

```java
	LambdaQueryWrapper<Foo> wrapper=new LambdaQueryWrapper<>();
	wrapper
		.eq(Foo::getName, "张三")
		.between(Foo::getCreated, DateUtils.getInstant(2023, 12, 1), Instant.now())
        .orderByAsc(Foo::getId);
	//返回前10条，同时返回总数
	Pair<Integer, List<Foo>> results=repo.findAndCount(wrapper, 10, 0);
```

### MyBatis-Flex风格

```java
	LambdaColumn<Foo, String> name = Foo::getName;
	LambdaColumn<Foo, Instant> created = Foo::getCreated;
	List<Foo> list= repo.find(q->q.where(name.eq("张三").and(created.between(DateUtils.getInstant(2023, 12, 1), Instant.now()))));	
```

### WEB表单查询风格

````java
	//定义一个与查询表单结构一致的类，通过注解设置各种查询和分页条件。
	@Data
	@ConditionBean
	public static class FooParams{
		@Condition(Ops.STRING_CONTAINS)
		String name;
		
		@Condition(Ops.BETWEEN)
		Date[] created;
	}
	//如同常见查询单表那样：空字符串会自动省略不作为where条件。
	FooParams params=new FooParams();
	params.setName("张三");
	params.setCreated(new Date[] {DateUtils.get(2023, 12, 1), new Date()});
	repo.findByCondition(params);
````



## 3 用法简述

> 本节仅介绍Spring集成场景。

**如在非Spring下使用**

参见此文档 [Without Springframework](static/without_springfrwmework.md)


### 基本

本框架直接依赖querydsl-sql库，主要用法和querydsl一致。但初始化方法与querydsl有所不同。

**依赖(Maven)**

本文档使用Spring集成示例，故依赖 `querydsl-sql-extension-spring` 包，如不集成Spring使用可仅依赖 `querydsl-sql-extension`。

```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-extension-spring</artifactId>
	<version>${querydsl-sql-extension.version}</version>
</dependency>
```

**Spring初始化** (支持Spring的事务管理器)

```java
	@Bean
	public com.github.xuse.querydsl.sql.SQLQueryFactory factory(DataSource ds) {
        return com.github.xuse.querydsl.sql.spring.QueryDSLSqlExtension
            .createSpringQueryFactory(ds, querydslConfiguration());
	}

	private ConfigurationEx querydslConfiguration() {
        //Change to the SQLTemplates of your RDBMS.
		SQLTemplates templates = new MySQLWithJSONTemplates();
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.addListener(new QueryDSLSQLListener());
        configuration.scanPackages("{your entity packages}");
        //Add you confiuration here.
		return configuration;
	}

	//可以使用SPring的事务管理器。对于其他使用同一事务管理其的框架(如Mybatis、JDBC Template等)可以共享事务。
	@Bean
	public PlatformTransactionManager tx(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}
```



### 构造自己的业务Repository

自己编写一个Repository类，将包名加入到Spring自动扫描路径即可。

```java
import org.springframework.stereotype.Repository;
import com.github.xuse.querydsl.entity.MyEntity;
import com.github.xuse.querydsl.repository.GenericRepository;

@Repository
public class MyEntityRpository extends GenericRepository<MyEntity, Integer>{
	//GenericRepository用法参见第2章 - 多种API风格。
	//如果要编写自己的Repository层业务逻辑，可在此处增加方法。
}
```

### 快速获得Repository

可直接通过SQLQueryFactory对象获得Repository

```java
CRUDRepository<Foo, Integer> repository = factory.asRepository(()->Foo.class);
```



### **非Spring下初始化** - 可跳过

> 初次使用本框架的用户，本节可以跳过。

```java
private static SQLQueryFactory factory;
static{
	try {
		DataSource ds = initDataSource();
		factory = new SQLQueryFactory(querydslConfiguration(SQLQueryFactory.calcSQLTemplate(ds.getUrl())),ds);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

public static ConfigurationEx querydslConfiguration(SQLTemplates templates) {
	ConfigurationEx configuration = new ConfigurationEx(templates);
	configuration.setSlowSqlWarnMillis(200);
	configuration.addListener(new QueryDSLSQLListener());
	configuration.addListener(new UpdateDeleteProtectListener());
	configuration.scanPackages("{your entity packages}");
	return configuration;
}
```

如果非Spring下，又希望自己管理事务，可以用SQLQueryFactory的另一个构造，自行管理事务和连接。

### 使用Query Class - 可跳过

> 对于没有使用过QueryDSL、且初次使用本框架的用户，建议使用纯POJO方式。本节可以跳过不看。

本节介绍基于QueryDSL的代码生成工具来使用本框架。

因为当前没有编写从数据库自动生成java代码的功能，所以可以先用官方文档中的 “code generation via maven” 一章中介绍来生成代码，生成的代码还需要修改一下。

**使用Maven插件来生成Query class

 ```xml
	<plugin>
		<groupId>com.querydsl</groupId>
		<artifactId>querydsl-maven-plugin</artifactId>
		<version>5.0.0</version>
		<executions>
			<execution>
				<goals>
					<goal>export</goal>
				</goals>
			</execution>
		</executions>
		<configuration>
			<jdbcDriver>com.mysql.cj.jdbc.Driver</jdbcDriver>
			<jdbcUrl>jdbc:mysql://host:port/database?useUnicode=true</jdbcUrl>
			<jdbcUser>username</jdbcUser>
			<jdbcPassword>password</jdbcPassword>
			<exportBeans>true</exportBeans>
			<packageName>xxx.xxx.dal.domain</packageName>
			<targetFolder>${project.basedir}/src/main/java</targetFolder>
			<tableNamePattern>%</tableNamePattern>
			<beanAddToString>true</beanAddToString>
		</configuration>
	</plugin>
 ```



**微调生成后的代码**

生成的代码还需要修改一下，举例：生成的代码如下——

```java
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa extends com.querydsl.sql.RelationalPathBase<Aaa> {

	private static final long serialVersionUID = -1;
    public static final QAaa aaa = new QAaa("AAA");
    public final NumberPath<Long> cBigint = createNumber("cBigint", Long.class);
```

要使用扩展功能，请将上文的 `com.querydsl.sql.RelationalPathBase` 类替换为 `com.github.xuse.querydsl.sql.RelationalPathBaseEx`。
替换后为：

```java
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa extends com.github.xuse.querydsl.sql.RelationalPathBaseEx<Aaa> {

private static final long serialVersionUID = -124472086;
    public static final QAaa aaa = new QAaa("AAA");
    public final NumberPath<Long> cBigint = createNumber("cBigint", Long.class);
```

替换后，在addMetadata()方法中，可以使用更多的API来定义数据结构和框架行为。

另外如果对生成对象的一些字段类型不满意，也可以自行手工修改。



**建议：使用primitive类型**

primitive类型指java中的byte/short/int/long/float/double/char/boolean等类型。

一些ORM框架不建议用户在Bean定义中使用primitive类型，原因是框架无法根据 `object == null` 这样的运算判断用户是否对该字段进行过赋值。但是使用Integer/Long等包装类型也会带来额外的负担——代码中需要频繁地对字段进行 is null的判断，带来了代码的流畅性和装拆箱带来的性能损失。

> 最佳实践：对于NOT NULL的数据库列，使用primitive类型进行映射。

在一个设计良好的数据库中，大部分数值类列都应当是非空（not null）列。querydsl-sql-extension鼓励用户使用primitive类型作为非空列的映射，在querydsl中由于大量API都是显式指定path进行操作的，因此primitive类型对于是否设置数值不存在歧义。但是依然有部分API，会尝试“自动”地判断是否需要将该字段写入或更新到数据库中，在这种情况下，可以通过设置告知框架哪些值被视为null(未设置)。

```java
@UnsavedValue(UnsavedValue.MinusNumber)   //负数被视为无效数值 (等同于null)
private int dataInt;
```

或者

```java
//零视为无效数值
addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER)).withUnsavePredicate(UnsavedValue.Zero);
```

解决了无效值的问题后，Primitive类型不会带来额外的困扰，另有详细分析文档。
### 使用SQLQueryFactory - 可跳过

> QueryDSL原生风格功能强大且非常灵活，对于初学者来说较难驾驭，仅有类JPA框架使用经验的读者可以先跳过本节。

本节介绍使用QueryDSL语法进行基本的数据库操作。SQLQueryFactory是大部分情况下访问数据库的总入口，以下是简单的操作示例。

**Insert DEMO**

```java
QFoo t1 = QFoo.foo;
Foo a = new Foo();
a.setName("Zhang San");
a.setGender(Gender.FEMALE);
Integer id = factory.insert(t1).populate(a).executeWithKey(Integer.class);
System.out.println("The auto increment value is " + id);	
```

**Select DEMO**

```java
Foo selected = factory.selectFrom(t1).where(t1.id.eq(id)).fetchOne();
```

**Update DEMO**

```java
long count = factory.update(t1)
       .set(t1.created, Expressions.currentTimestamp())
       .set(t1.name, "李四")
       .where(t1.id.eq(1))   
       .execute();
System.out.println(count + " records was updated.");	

//Update DEMO2
Foo b = new Foo();
b.setName("Zhang San");
b.setGender(Gender.FEMALE);
long count = factory.update(t1)
       .populate(b)
       .where(t1.id.eq(1))   
       .execute();


```

**对比更新 Comparison Update DEMO**

*  对比更新下，仅有变化的字段被SET，如果两个对象无区别，将不会写数据库。

```java
Foo oldRecord = factory.selectFrom(t1)
     .where(t1.id.eq(id)).fetchOne();
Foo b = new Foo();
b.setName("Zhang San");
b.setGender(Gender.FEMALE);
factory.update(t1)
 	.populateWithCompare(a, oldRecord)
	 .where(t1.id.eq(id))
	 .execute(); 
```

**Delete DEMO**

```java
factory.delete(t1).where(t1.id.eq(id)).execute();
```

**Complex Selection DEMO**

```java

	QCaAsset t2=QCaAsset.caAsset;
	List<Tuple> tuples=factory.select(t1.name,t1.gender,t2.content,t2.code)
	    .from(t1)
		.innerJoin(t2).on(t1.id.eq(t2.id))
	    .where(t1.name.eq("Zhangsan"))
	    .fetch();
```



更多使用方法参见QueryDSL的文档 http://querydsl.com/static/querydsl/latest/reference/html/ch02s03.html。

querydsl-sql-extension在原生版本的基础上扩展很多用法，具体可参见javadoc。

### 使用Record数据类型 

> 要使用Record特性，您必须使用 JDK 16以上版本。
> 本框架对Record对象的访问作了特殊处理，不依赖JDK 16以上版本，当前框架依然可以在java 8~22版本上使用。

表结构定义注解可以正常使用在record类型上，与正常class稍有区别。

```java
@TableSpec(
	name="record_foo",
	primaryKeys = "id",
	keys = {
		@Key(type = ConstraintType.UNIQUE,path = {"name"}
	)
})
@Comment("The record table mapping.")
public record Foo(
		@ColumnSpec(autoIncrement = true,name = "foo_id",nullable = false,type = Types.BIGINT,unsigned = true)
		@Comment("an autoincreament primary key")
		long id,
		
		@ColumnSpec(name="username",nullable = false,size = 128)
		@Comment("the name of users.")
		String name,
				
		@AutoGenerated(value = GeneratedType.CREATED_TIMESTAMP,overwrite = true)
		@ColumnSpec(name="create_time",nullable = false, defaultValue = "current_timestamp()", type=Types.TIMESTAMP)
		Date created
	){}
```

实际用法和前文一样，对record类型的字段名称引用更加简洁。

```java
	CRUDRepository<Foo, Long> repository = factory.asRepository(()->Foo.class);
	List<Foo> result = repository.query()
			.eq(Foo::name, "Joey")
			.or(
				(query) -> query.lt(Foo::created, new Date())
			).fetch();
```

使用record类型要注意以下特性

* 由于Record对象是不可变对象，所有字段都无法set，所以@AutoGenerated的回写功能会自动失效。（日志中会提示，回写的数值被丢弃，但不会抛出异常）

  

## 4 表结构注解详解

当不创建QueryClass时，所有数据库表特性都需要通过注解来完成。当前支持的注解如下：

### @TableSpec

仅可添加在类上，描述数据库表的特性。

```java
@TableSpec(
schema ="test" 	  //schema名，一般服务不会跨schema访问留空即可。复杂应用在测试环境可以配置重定向规则。
name = "ca_foo",  //定义表名
primaryKeys = "id", //主键字段
collate = "utf8mb4_general_ci", //排序字符集
keys = {
		@Key(path= {"code"},type=ConstraintType.UNIQUE),//UNIQUE索引
		@Key(path= {"content"},type=ConstraintType.FULLTEXT),//全文索引(FULLTEXT)，如数据库不支持会自动跳过
	}
check = {
		@Check("updated >= created")  //约束检查，对于不支持的数据库会跳过
	}
)
public class Entity{    
```

### @ColumnSpec

仅可添加在字段上，描述数据库列的特性。

```java
@ColumnSpec(
    name = "is_disabled", //数据库列名
    type = Types.TINYINT, //数据库列类型（用JDBC类型定义）
    unsigned = true,  //是否为无符号数
    nullable = false,  //是否允许为null
    defaultValue = "0", //缺省值表达式，例如要定义为空字符串需要写成 "''"。
	autoIncrement = false,//是否为自增字段
    size = 5,  //定义字段长度。对于Decimal表示数字的最大长度，对于time和timestamp/datetime类型时表示秒以下的小数位精度
    digits = 0 //使用Decimal类型时定义小数后的位数。
)
private int disabled;
```

### @Comment

添加在类或者字段上。位于类上时表示数据表的注释；位于字段上时表示数据库列的注释。

使用create table或者refresh table功能会将注释写入数据库。对于不支持注释的数据库（如Derby），会自动忽略。

```
@Comment("注释")
```

### @UnsavedValue

添加在字段上，描述字段的无效值（忽略值）范围。一般用于基元（primitive）类型字段.

```
	//设置0或负数为无效值后，插入或更新数据时不会将默认的‘0’写入到数据库中。
	@UnsavedValue(UnsavedValue.ZeroAndMinus)
	private int id;
```

### @HashPartition

仅可添加在类上，指定按字段的Hash值进行分区。（仅限支持表分区的数据库上）

```
@HashPartition(
	type=HashType.KEY, columns = {"asset_name"}, count = 4
)
```

### @RangePartition

仅可添加在类上，按数值范围进行分区。数值可以是数字或日期时间。（仅限支持表分区的数据库上）

```java
@RangePartition(
	columns = "recordTime",  //时间字段
	auto = @AutoTimePartitions(  //按当前时间自动计算分区个数
		unit = Period.DAY,     	//按天分区
		periodsBegin = -2,		//最早的分区是两天前
		periodsEnd = 3,			//最晚的分区是三天后
		createForMaxValue = true,//创建max value分区，三天后的数据写入到该分区中
		columnFormat = ColumnFormat.NUMBER_YMD //如采用时间类的数据库列时可不配置。如果该列是number或varchar等类型时需要指定格式。此示例表示数据库列是年月日构成的一个八位数字。
	)
)
```

### @ListPartition

仅可添加在类上，按枚举值进行分区。（仅限支持表分区的数据库上）

```java
@ListPartition(
	columns="name",		//按name字段的值分区
	value={				//分为p1，p2，p3，p4四个分区，按name的值数据分布在不同分区中
		@Partition(name = "p1",value="'0','1','2','3'"),
		@Partition(name = "p2",value="'8','9','a','b'"),
		@Partition(name = "p3",value="'4','5','6','7'"),
		@Partition(name = "p4",value="'c','d','e','f'"),
	}
)
```

## 5 扩展用法注解

扩展用法是指框架封装后的功能，这些注解对应的能力不依赖数据库DDL。

### @InitializeData

仅可添加在类上。初始化时，使用一个资源文件将初始化数据Merge到表中。

理想情况下，数据库表是一张空表

```
@InitializeData(
	value="table_init_data", //指定资源文件名，资源文件为CSV格式。可以使用com.github.xuse.querydsl.init.InitDataExporter生成文件。
	charset ="UTF-8",		//指定资源文件字符集
	forEmptyTableOnly=false, //为true时，如果数据表中已经有数据，将不进行数据初始化。
	mergeKeys = {"name"}，	//如果数据表已经存在数据默认使用主键进行更新。但对于主键无业务含义时，也可以使用若干业务键进行更新。
	updateNulls = false,	//更新数据时，数据文件中的null值要不要写入数据库。
	sqlFile = “”			//也可以不使用CSV文件而改用一个自行编写sql脚本文件，与CSV方式二选一。
)
```

### @CustomType

仅可添加在字段上。用于在写入数据库前自动进行序列化。读取时自动进行反序列化。

```java
	//将复杂类型以JSON格式存入数据库
	@ColumnSpec(type = Types.VARCHAR, size=1024)
    @CustomType(JSONObjectType.class)
    private CaAsset asserts;

	//将字符串格式化为DateTime类型存入数据库
    @CustomType(value=StringAsDateTimeType.class,parameters = "MM/dd/yyyy HH:mm:ss") 
    private String updateTime;

	//用自定义的数字序号将枚举值写入数据库	
    @CustomType(EnumByCodeType.class)
	private Gender gender;
```

### @AutoGenerated

仅可添加在字段上。在写入数据库前如果未指定数值，会使用自动生成策略生成。时间类字段使用数据库的current_timestamp()函数对字段进行赋值，从记录数据库侧的时间。

```java
	//记录创建时自动赋值，此后数据更新默认不会赋值。
	@AutoGenerated(GeneratedType.CREATED_TIMESTAMP)  
	private Date created;

	//记录创建和更新时自动赋值。
	@AutoGenerated(GeneratedType.UPDATED_TIMESTAMP)  
	private Date updated;

	//记录创建时自动用雪花ID生成器生成ID（雪花ID生成器需要全局进行一次初始化）
	@AutoGenerated(GeneratedType.SNOWFLAKE)  
	private long id;
```

### @ConditionBean详解

 @ConditionBean注解示例，支持设计查询对象。
  该功能主要适用于一些相对固定的查询模式，比如一个支持若干条件进行检索的WEB页面，需求一般是这样的：支持若干条件组合检索，其中名称要支持模糊匹配（Like ?%），日期要支持设置范围，其他条件为精确匹配，上述所有字段可以不输入表示无限制。针对这种场景，可以将条件设计为对应的查询对象，通过@ConditionBean和 @Condition注解标记每个字段的运算操作。

定义ConditionBean如下

```java
@Data
@ConditionBean(limitField = "limit",offsetField = "offset")
public class FooQueryParams {
	@Condition(Ops.STARTS_WITH)
	private String content;
	
	@Condition(Ops.GT) //包含多种操作符。
	private int authType;
	
	@Condition(Ops.EQ)
	private String code;
	
	@Condition(Ops.BETWEEN)
	private Date[] createTime;
	
	private Integer limit;
	
	private Integer offset;
}
```

实际使用

  ````java
  	QFoo t = QFoo.foo;
  	FooQueryParams p = new FooQueryParams();
  	......
  	p.setLimit(100);
  	p.setOffset(2);
  	Pair<Integer, List<Foo>> countAndData = factory.findByCondition(p);
  ````



## 6. 包扫描功能

包扫描功能一般配置在系统初始化时，用于执行自动的数据库操作任务。同时完成一些自定义注解的预加载。

### 自动扫描并初始化数据库

特性：应用启动时扫描指定包下的实体。然后）——

* 自动在数据库中创建表、索引、约束等元素。
* 如果数据库中已有该表，自动修改表结构与Java模型一致。（可以关闭列删除、索引删除等特性细项）
* 如果配置了初始化数据，可以将预设的初始化数据合并到数据表中。
* 在数据库中创建一张初始化配置表，控制今后是否还要对该数据表进行修改或合并数据。

```java
configuration.getScanOptions()
	.allowDrops()    //允许修改已存在的表，允许删除列、索引、约束
	.setCreateMissingTable(true) //允许创建缺失的表
    .setDataInitBehavior(DataInitBehavior.FOR_ALL_TABLE) //为所有扫描到的实体做数据初始化
	.detectPermissions(true)  //先尝试判断当前帐号有无DDL权限，如果没权限后续就不执行DDL了。
	.useDistributedLock(true) //开启后，数据库表结构修改和初始化动作，将会尝试获取分布式锁，详见下文
	.useDataInitTable(true);  //启用初始化配置表，自动在当前创建一个记录初始化行为的表，同时也作为分布式锁使用。多实例并发的情况下建议开启。
    
configuration.scanPackages("com.xxx.xxx");
```

上述特性都有多个可控制开关，以防止误操作DROP了表或列，引发损失。

**禁用DDL**

> 虽然框架会使用Online来修改已有的数据库表， 当仍有相当DDL执行可能锁表，对可用性要求特别高的高负载生产环境，应当关闭启动时执行DDL的特性。

```java
configuration.getScanOptions()
    .disableDDL();
configuration.scanPackages("com.xxx.xxx");
```

**禁用数据初始化**

如果担心自动更新数据功能影响生产环境，可以关闭初始化数据写入，甚至禁止所有数据库初始化行为

```java
//关闭表初始化数据写入功能
configuration.getScanOptions().disableDataInitialize();
configuration.scanPackages("com.xxx.xxx");

//禁止所有自动操作数据库的行为
configuration.getScanOptions().disableAllDatabaseOperation();
configuration.scanPackages("com.xxx.xxx");
```

### 确保仅初始化一次

如您希望初始化动作仅在首次运行时执行，需要启用初始化行为记录表。即前文的 `.useDataInitTable(true);`.

该表以业务表的名称作为主键。如果表已经初始化过了， is_disabled 列值就被更新为1，下次启动后就不会反复初始化同一张表了。

记录表的名称是`querydsl_auto_init_data_log`。开启此功能后，**系统会尝试自动创建这张表**。但如果应用没有DDL权限，可能需要手动创建这张表。建表的SQL语句如下（以MySQL为例）：

```sql
CREATE TABLE querydsl_auto_init_data_log (
  table_name VARCHAR (128) NOT NULL COMMENT 'table name.',
  is_disabled TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 - Data init on this table is disabled. 0 - data init enabled.',
  init_records SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'records saved on the last write process.',
  last_init_time DATETIME NULL,
  last_init_user VARCHAR (128) NULL,
  last_init_result VARCHAR (400) NULL,
  PRIMARY KEY (table_name)
) COMMENT 'informations about data initialize by querydsl-sql-extenstion'
```

### 分布式锁

前面介绍过，为了防止多个实例同时启动扫描并修改一个数据库，可以配置分布式锁。启用后，有以下两种方式实现。

**使用数据库表作为分布式锁**

使用一张数据库表作为分布式锁，这是默认的实现。

如果开启了`useDataInitTable(true)`功能，自动会将对应的数据表作为分布式锁。如果检测到DDL权限，**系统会自动创建这张表。**

**使用其他中间件实现分布式锁(ZK,Redis等)**

在初始化时设置自定义的分布式锁

```
//自行实现分布式锁
public class MyLockProviderimplements DistributedLockProvider{
	public DistributedLock getLock(String lockName, int maxLockMinutes){
		//分布式锁实现
	}
}

//设置自定义的分布式锁
ConfigurationEx configuration = new ConfigurationEx(templates);
configuration.setExternalDistributedLockProvider(new MyLockProvider());
```

## 7. 执行DDL语句

> 实验性功能，个人精力有限目前仅完成了部分数据的方言适配（清单参见 README.md ）。但现有框架基于AST的扩展机制十分强大，适配其他主流数据库问题不大，有兴趣者可自行编写方言进行扩展。

### 表修改示例

参见以下示例

**Drop table and recreate.**

```java
	QFoo t=QFoo.foo;
	SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
	metaFactory.dropTable(t).ifExists(true).execute();
	metaFactory.createTable(t).execute();
```

**Alter table**

```java
	SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
	QFoo t=QFoo.foo;
	metadata.refreshTable(t)
		.removeConstraintOrIndex("unq_${table}_name_version")  //变量${table}会被实际的表名替换
		.addColumn(
				ColumnMetadata.named("new_column").ofType(Types.VARCHAR)
				.withSize(64).notNull(), String.class).defaultValue("")
				.build()
		.dropConstraint(true)
		.execute();
```

**Truncate table**

```java
	QFoo t=QFoo.foo;
	SQLMetadataQueryFactory metaFactory = factory.getMetadataFactory();
	metaFactory.truncate(t).execute();
```

**Add Index**

```java
	QFoo t=QFoo.foo;
	SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
	metadata.refreshTable(QAaa.aaa)
		.createIndex("idx_foo_gender", t.gender)
		.execute();
```

### 分区操作示例

仅限支持数据表分区的数据库，如MySQL、PostgreSQL、Oracle等。

> 目前完成了MySQL的适配。PostgreSQL比较接近，近期可能支持。Oracle看情况。

**创建分区**

```java
SQLMetadataQueryFactory metadata = factory.getMetadataFactory();
metadata.createTable(table).reCreate().execute();

//此处引用表的静态模型（QueryDSL代码生成工具生成）。如果没有该类，用LambdaTable<T>）代替
//示例：LambdaTable<Foo> = ()-> Foo.class;
QPartitionFoo1 table = QPartitionFoo1.partitionFoo1;

metadata.createPartitioning(table)
		.partitionBy(Partitions.byHash(HashType.HASH, "TO_DAYS(created)", 4))
		.execute();
```

**查询表的分区信息**

```java
List<PartitionInfo> list=metadata.getPartitions(table.getSchemaAndTable());
```

**清除分区设置（不删除数据）**

```java
metadata.removePartitioning(t1).execute();
```

**创建按时间范围进行的分区**

```java
metadata.createPartitioning(table).partitionBy(
	Partitions.byRangeColumns(table.created)
		.add("p202401", "'2024-02-01'")
		.add("p202402", "'2024-03-01'").build())
    .execute();
```

**在按上述分区中再追加一个分区**

在按上述分区中再追加一个分区。

注意：由于新分区的范围被老分区所包含，可能已经有数据落在了老分区中。
需要将这些老分区的数据重新组织到新分区下，因此系统会自动使用REORGANIZE PARTITION将落在原先第一个分区内的数据移动到新分区

```java
metadata.addParition(table)
		.add("p20200101", "'2021-01-01'")
		.execute();
```

**删除分区（连同分区内的数据）**

```java
metadata.dropPartition(table)
    .partition("p20200101").execute();
```

### 权限问题

前文的 【自动扫描并初始化数据库】一节中介绍，启动扫描有一个嗅探操作权限的动作。如果嗅探后发现没有CREATE, DROP等权限，那么所有的DDL都不会执行。

### MySQL Online DDL（部分支持）

> MySQL OnLine DDL从5.x开始支持，到8,x完善。需要根据具体的判断控制OnLine  DDL的范围。笔者手头的MySQL环境是5.6的，所以按5.6进行了适配。
>
> 8,x的优化还未进行。

OnLine DDL可以防止DDL执行期间锁导致阻塞,影响用户的 DML 操作,而 Online DDL 指的是在 DDL 期间,允许用户进行 DML 操作。

在MYSQL数据库上执行DDL时会自动使用Online方式执行，尽可能避免对生产环境业务访问的干扰。
示例如下：在对数据表进行修改时，会指定算法和锁，使得DDL执行期间不影响业务。

```sql
ALTER TABLE table1
  CHANGE c_bin c_bin BLOB NULL COMMENT 'test column', ALGORITHM = INPLACE, LOCK = NONE,
  DROP KEY idx_aaa_taskstatus, ALGORITHM = INPLACE, LOCK = NONE,
  ADD KEY idx_aaa_taskstatus (task_status), ALGORITHM = INPLACE, LOCK = SHARED
```

* （此功能的应用并不意味着DDL执行对数据表无影响，24小时的运行的高可用系统还是应当在业务低谷期间执行DDL）
* Online DDL是在MySQL 5.x引入的，8.x中支持更多的Online DDL策略。但目前5.x和8.x的方言还没有区分开，目前仅按5.x做了相对保守的策略。



## 8. 常见问题 / 功能杂项

### 业务层分表兼容机制

> 本框架不提供分库分表功能

但有一种情形，当分表规则和用法较为简单，业务层希望自行封装分表时，需要能根据业务数据动态变化表名。针对这种情形，提供了一个允许业务代码自行调整表名的机制。

```java
	//定义本次操作中的表名后缀
	TableRouting routing=TableRouting.suffix( "2024Q2");

	//在DDL中操作带后缀的表名（删表建表）
	SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
	metadata.dropTable(t2).withRouting(routing).execute();
	metadata.createTable(t2).withRouting(routing).execute();
	
	//在DML中操作带有后缀的表名
	List<Tuple> tuples=factory.select(t2.content,t2.code).from(t2)
	    .withRouting(routing)
	    .where(t2.name.eq("Test"))
	    .fetch();
	
	//如果在一个SQL中有多张表需要调整后缀，参考下例
	TableRouting routing=TableRouting.builder()
		.suffix(t1,"202406")
		.suffix(t2, "2024Q2")
		.build();
```

> 如果分库分表规则较为复杂，建议使用Sharding JDBC/Sharding Sphere等专用框架。

### 动态数据库表模型

> 当数据库表字段是动态定义时，无法用Java类来创建静态的表和字段模型。

1. 定义动态表模型

```java
//定义一个动态的表模型
DynamicRelationlPath table = new DynamicRelationlPath("t1", null, key);
//创建各列的模型
Path<Long> id=table.addColumn(Long.class, ColumnMetadata.named("id").ofType(Types.BIGINT).notNull())
		.with(ColumnFeature.AUTO_INCREMENT).unsigned().comment("主键ID")
		.build();
		
Path<String> name=table.addColumn(String.class,ColumnMetadata.named("name").ofType(Types.VARCHAR)
		.withSize(256).notNull())
		.defaultValue("")
		.build();
		
Path<Integer> status=table.addColumn(Integer.class, ColumnMetadata.named("status")
		.ofType(Types.INTEGER).notNull())
		.build();
			
Path<Date> created=table.addColumn(Date.class,ColumnMetadata.named("create_time")
		.ofType(Types.TIMESTAMP).notNull())
		.withAutoGenerate(GeneratedType.CREATED_TIMESTAMP)
		.build();

//创建主键
table.createPrimaryKey(id);
//创建索引
table.createIndex("idx_table_name_status", name, status);
...
```

2. DDL：建表

```java
DynamicRelationlPath table=getModel("dyn_entity_apple");
factory.getMetadataFactory().createTable(table).ifExists().execute();
```

2. DML：数据访问

```java
DynamicRelationlPath table=getModel("dyn_entity_apple");
Tuple o = table.newTuple(null,"张三",2,null);
//Add
factory.insert(table).populate(o).execute();

//Update
Map<String,Object> bean=new HashMap<>();
bean.put("id", 3);
bean.put("name", "李四");
Tuple u = table.newTuple(bean);
factory.update(table).populate(u, true).execute();
		
//Delete
factory.delete(table).populatePrimaryKey(u).execute();
		
//Query
SimpleExpression<String> name = table.path("name", String.class);
SimpleExpression<Long> id = table.path("id", Long.class);
SimpleExpression<Integer> status = table.path("status", Integer.class);

List<Tuple> tuples=factory.select(id,status).from(table).where(name.eq("张三")).fetch();
```

