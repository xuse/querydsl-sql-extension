[TOC]

# querydsl-sql-extension

query-dsl-sql-extension is a enhancemant lib based on querydsl-sql module.

本框架是在 querydsl-sql （https://github.com/querydsl/querydsl）上的扩展，querydsl-sql的使用手册，请参见http://querydsl.com/static/querydsl/latest/reference/html/ch02s03.html。

本框架是为了更便利，以及提供更高性能为目的对querydsl进行的改进。本框架通过初始化时使用不同的入口类的方式与原生的querydsl用法做出区别，保留原querydsl的更新能力，对原框架无侵入性。

## 特性

### 提升使用便利性

* 提供@CustomType注解，支持将复杂的Bean映射到数据库字段中。
  **Example**

  ```java
      //Using a string type to mapping a column of timestamp in database.
      @CustomType(value=StringAsDateTimeType.class,parameters = "MM/dd/yyyy HH:mm:ss")
      private String updateTime;
  
      //The data is encrypted before insert into the database, and be decrypted while load from database.
      @CustomType(AESEncryptedField.class)
      private String phoneNumber;
  
  	//the field value will be serialzed to a json String before insert into the database, and v.v.
      @CustomType(JSONObjectType.class)
      private CaAsset asserts;
  ```

  

* 提供更多Batch Insert、Batch Update、Batch Delete操作。通过一个SQL Statment支持多组操作值，提升操作效率。

* 提供@AutoGenerated注解，用于一些字段的自动维护，如记录创建时间、更新时间、GUID、SnowFlake等数据写入。（该注解和数据库Default value或trigger等任何特性无关，是纯Java侧实现）

  **Example:**

  ```java
  	@AutoGenerated(GeneratedType.UPDATED_TIMESTAMP,writeback = true)
  	private Date updated;
  ```

  

* 提供增强的日志输出。通过com.github.xuse.querydsl.sql.log.QueryDSLSQLListener可以提供三种格式的日志输出格式，包括以下功能：

  * 输出每个SQL和参数，以及执行时间和记录数。如果日志级别到WARN，这部分逻辑可被跳过，以最大限度提升性能。
  * 慢SQL以Error级别输出（慢SQL阈值可设置）
  * Batch模式下，省略N组之后的参数。
  * 生产环境建议使用紧凑格式输出。语句和参数在一行中显示有利于使用grep等命令查询和分析。

  **Example:**

  ```java
  configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
  configuration.setSlowSqlWarnMillis(200);
  ```
  
* 包扫描：前文中已经介绍了几种基于Annotation的功能增强，包扫描可以在应用启动时，分析所有数据库元模型定义，提前校验这些Annotation的正确性，并将其中的一些配置注册到全局上下文。@CustomType要生效需要提前进行包扫描。
  备注：早期版本使用 configuration.registerExType()方法注册实体，比较麻烦，新版本可以使用包扫描替代。


* 基于Annotation定义的元模型：QueryDSL原生的元模型（Metamodel）主要使用API进行定义。扩展提供了一套基于注解的元模型定义方式，效果覆盖原有的元模型和扩展功能。

  **Example**

  ```
  @TableSpec(name="ca_asset",primaryKeyPath={"id"},collate = "utf8mb4_general_ci",
  keys = {
  		@Key(path= {"code"},type=ConstraintType.UNIQUE),
  		@Key(path= {"content"},type=ConstraintType.FULLTEXT),
  	}
  )
  @Comment("comments of this table.")
  public class CaAsset {
  	
  	@ColumnSpec(autoIncrement = true,type = Types.INTEGER,unsigned = true,nullable = false)
  	@Comment("primary key，auto increment.")
  	private int id;
  	
  	@ColumnSpec(type = Types.VARCHAR,size=64,nullable = false,defaultValue = "''")
  	@Comment("The code of asset. unique.")
  	private String code;
  	
  	@ColumnSpec(name="asset_name",size=128,nullable = false)
  	@Comment("The name of the asset.")
  	private String name;
  	
  	@ColumnSpec(size=16384)
  	@Comment("Asset's comments.")
  	private String content;
  ```
  
  通过上方示例，可以定义字段在数据库中的映射方式。对应的，元模型类中的部分代码可以省略，但需要增加一行代码使其自动扫描注解:
  
  **Example**
  
  ```
  public class QCaAsset extends RelationalPathBaseEx<CaAsset> {
  	public static final QCaAsset caAsset = new QCaAsset("foo");
  	
  	public final NumberPath<Integer> id = createNumber("id", int.class);
  
  	public final StringPath code = createString("code");
  	
  	public final StringPath name = createString("name");
  	
  	public final StringPath content = createString("content");
  
  	public QCaAsset(String variable) {
  		super(CaAsset.class, PathMetadataFactory.forVariable(variable), "null", "CA_ASSET");
  		super.scanClassMetadata();   //这行代码使元模型在构造时自动扫描实体上的注解。
  	}
  ```
  
  
  

### 访问安全


* 防止一些误操作：这个其实是官方版本的自定义监听器的正常用法，通过SQL语句监听，阻止一些危险操作。比如下面的拦截器可以防止针对全表（不带Where条件）地执行Update或Delete操作。

  ```java
  configuration.addListener(new UpdateDeleteProtectListener());
  ```

* 防止个别慢SQL拖死整个数据库：可以设置全局的SQL请求超时时间，超时自动取消请求，防止请求长时间占用数据库资源。

  ```
  configuration.setDefaultQueryTimeout(5); //设置SQL最大执行时间为5秒
  ```

  

### 性能优化

* 使用ASM生成的Bean访问器提供反射操作的一个高速替代。一个SQL查询返回组合对应一个访问器。去除了反射和IF分支，对ResultSet的全部访问均为按index的顺序访问，在内存操作效率上做到最高。在内存操作次数较多的场合，性能超过某M 60%以上。
* 一些代码细节上的优化，减少内存拷贝和分配。

### 功能增强

* 完整的数据库结构建模：扩展QueryDSL原生的ColumnMetadata，支持在Java(JDBC)模型中描述全部数据库表的特征——包括default value、unsigned、自增、索引、约束（不含外键）。相关模型可以通过元模型API创建，也可以通过注解来定义。

  Q: 有什么用？
  A: 数据库应用开发者一般有两种用法，一种是先在数据库中设计数据结构，然后通过Bean生成工具生成Java类结构。（称为数据库Schema优先）
  另外一种是软件开发者开发跨多种RDBMS应用的做法，是先建立Java Entity模型，然后通过程序等手段自动创建数据库结构。(称为Metadata优先)。
  QueryDSL仅支持第一种用法。本库满足了Metadata优先的场合，可以通过Java元模型反向更新数据库结构。 同时，这种更新支持基于数据库结构对比的增量更新，支持更新索引、约束、视图、等数据表结构。（不支持：外键、物化视图、索引组织表、函数、触发器、存储过程，由于这些特性很少在跨RDBMS应用中用到，暂无支持的必要。）

* 数据库元数据查询：支持通过API查询数据库中的Schema、表、视图、索引、约束等信息。

* 支持Truncate/Create/Drop/Alter table等常用DDL的Java语法操作。

- @ConditionBean注解，支持设计查询对象。
  该功能主要适用于一些相对固定的查询模式，比如一个支持若干条件进行检索的WEB页面，需求一般是这样的：支持若干条件组合检索，其中名称要支持模糊匹配（Like ?%），日期要支持设置范围，其他条件为精确匹配，上述所有字段可以不输入表示无限制。针对这种场景，可以将条件设计为对应的查询对象，通过@ConditionBean和 @Condition注解标记每个字段的运算操作。

  ````
  	QFoo t = QFoo.foo;
  	FooQueryParams p = new FooQueryParams();
  	......
  	p.setLimit(100);
  	p.setOffset(2);
  	List<Foo> values = factory.selectFrom(t).where(p, t).fetch();
  ````

  

### 未完成特性

* 官方querydsl-maven-plugin提供了一个export的动作，可以用querydsl-sql-codegen库从数据库表结构直接生成java代码。querydsl生成的代码与官方版本稍有不同，理论上也需要一个Mavan插件，但因为变化较小，所以一直以来都是先用官方版本生成java代码后，手工修改替换一下，感觉也不是很麻烦。以后再考虑做成maven插件。
* 支持DDL需要编写各个不同数据库的方言，目前整个方言的框架机制有了，但只编写完成了MySQL和Derby。下一个考虑抽空完成PostgresSQL的，剩下的看需要吧。



## 使用方法

本框架是在 querydsl-sql （https://github.com/querydsl/querydsl）上的扩展，querydsl-sql的使用手册，请参见http://querydsl.com/static/querydsl/latest/reference/html/ch02s03.html。

本框架直接依赖DSL库，主要用法和querydsl一致。但初始化方法与querydsl有所不同。

**依赖(Maven)**

```xml
<dependency>
	<groupId>com.github.xuse.querydsl</groupId>
	<artifactId>querydsl-sql-extension</artifactId>
    <version>${querydsl-extension.version}</version>
</dependency>
```

**Spring初始化** (支持Spring的事务管理器)

```java
	@Bean
	public com.github.xuse.querydsl.sql.SQLQueryFactory factory(DataSource ds) {
        return com.github.xuse.querydsl.sql.SQLQueryFactory
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

	//Supports Spring's DataSource transaction manager
	@Bean
	public PlatformTransactionManager tx(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}
```

**非Spring下初始化**（无事务)

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



**修改自动生成的元模型**

因为当前没有编写从数据库自动生成java代码的功能，所以可以先用官方文档中的 “code generation via maven” 一章中介绍来生成代码，生成的代码如下——

```java
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa extends com.querydsl.sql.RelationalPathBase<Aaa> {

private static final long serialVersionUID = -124472086;
    public static final QAaa aaa = new QAaa("AAA");
    public final NumberPath<Long> cBigint = createNumber("cBigint", Long.class);
```

要使用扩展功能，请将上文的 `com.querydsl.sql.RelationalPathBase` 类替换为 `com.github.xuse.querydsl.sql.RelationalPathBaseEx`。替换后为_

```
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa extends com.github.xuse.querydsl.sql.RelationalPathBaseEx<Aaa> {

private static final long serialVersionUID = -124472086;
    public static final QAaa aaa = new QAaa("AAA");
    public final NumberPath<Long> cBigint = createNumber("cBigint", Long.class);
```

替换后，在addMetadata()方法中，可以使用更多的API来定义数据结构和框架行为。



**API和使用**

用法和原版QueryDSL一样。扩展的用法参见java doc以及单元测试下的案例。



## 修订记录

**版本编号方式：** 版本号由两部分组成，前一个数字是对应的querydsl库版本号，后一个是此扩展框架的修订号。从1开始向上，每个修订都向下兼容。

```
v{querydsl 版本号} - r(extension version)
```

### v5.0.0-r100

2024-06-11

* 完成DDL相关表语法支持。部分类名重构调整。

### v5.0.0-r??

……

省略掉中间约10个小版本. ^o^

### v5.0.0-r8

* 升级支持Querydsl v5.0.0
* 添加MySQL方言：com.querydsl.core.types.dsl.MySQLWithJSONTemplates、JsonExpressions等，支持JSON字段操作
* 注解@AutoGenerated，支持字段内容自动生成(在update/insert时)。如使用populate()方法则自动写入，如使用set()方法，可调用populateAutoGeneratedColumns()方法生成。
* 增加GUID、SnowFlake ID等生成规则

### v5.0.0-r4
* 增加更适合于linux下的简洁日志。
* 结果集拼接性能优化。提供额外的RelationalPathBaseEx用于继承。
* API增加——增加DDL语法框架（尚未实现）。

### v5.0.0-r1
1. 日志扩展
2. 关于Connection is not transactional异常修复，还有一些其他方面的个性化要求，因此通过本项目进行扩展。后续随功能更新。

## FAQ

1. 关于SQL语句的换行影响日志查看，在创建SQLTemplate的时候就使用
 com.querydsl.sql.SQLTemplates.Builder.newLineToSingleSpace()方法来实现。

2. 如果我的项目已经用了querydsl-sql，现在集成这个框架，需要将原有的query class全部修改为继承`com.github.xuse.querydsl.sql.RelationalPathBaseEx`类吗？

    A: 可以不用。本框架是在querydsl上的一些轻微调整格改进，querydsl的原生用法都不受影响。

## 其他

### 什么是元模型

* 元模型(meta mode)：在QuerDSL中，对每个实体会有一个"Q"开头的class，在querydsl文档中，称为 `query classes`. 其实这个类和JPA中的元模型差不多，都是用于描述数据结构，并且提供查询API引用的类。（在OpenJPA中，会生成下划线结尾的类，用途是差不多的）。所以本文某些场合也会使用元模型一词，和QueryDSL文档中的 query class是一个东西。

### 写这个框架的过程

* 开始 (About 2013)
近日使用QueryDSL-sql作为轻量级数据库操作框架，有着手写SQL的畅快，又有静态语法检查的安心，还有语法自动完成的高效。
但是日志功能稍有不满，SQLDetailedListener中不能监听到所有的SQL参数、执行时间、影响记录数等信息。
所以本着最小修改的原则，用这个项目对QueryDSL的监听行为进行扩展，从而可以得到更详细的日志信息。

* 性能演进（One year later）
对性能进行了优化。除了代码细节修改外，还增加了基于ASM自动生成动态类来完成字段拼装对象，无反射调用。

* 后来（Three years later）

  对一些常用的功能进行了增强，使得代码更简洁。如自动时间戳、自动生成GUID等。

* 再后来（Five years later）
在使用过程中，阅读源码中发现原作者一开始是想要支持DDL语法的，后来不知道为什么没有再支持了。可能确实使用场景不太多，总归有点小缺憾，由于我早年在别的框架上写过相关的功能，于是花了不少时间在目前的框架上支持了DDL语法。
