# Querydsl-sql-extension

**querydsl-sql 扩展，提供了高于原版和所有同类框架的性能。**
本框架是在 Querydsl-sql](https://github.com/querydsl/querydsl) 上的扩展，querydsl-sql的使用手册，可以参阅官方文档 [Querying SQL](http://querydsl.com/static/querydsl/latest/reference/html/ch02s03.html)

[English](README.md)|[中文](README_cn.md) 

**目录/ Table of Contents**

- [Querydsl-sql-extension](#querydsl-sql-extension)
  - [简介](#简介)
  - [特性介绍](#特性介绍)
    - [极致性能](#极致性能)
    - [提升使用便利性](#提升使用便利性)
    - [访问安全](#访问安全)
    - [自动创建数据库结构](#自动创建数据库结构)
    - [纯POJO使用](#纯pojo使用)
    - [R2dbc数据源使用(响应式数据)](#r2dbc数据源使用响应式数据)
    - [其他功能增强](#其他功能增强)
      - [多种风格的低代码API](#多种风格的低代码api)
      - [数据库结构访问与修改(DDL)](#数据库结构访问与修改ddl)
      - [Record对象作为数据表映射](#record对象作为数据表映射)
    - [运行环境与适用范围](#运行环境与适用范围)
  - [实验性功能](#实验性功能)
    - [Partition管理（已支持MySQL、PostgreSQL）](#partition管理已支持mysqlpostgresql)
    - [DDL Support](#ddl-support)
    - [MySQL Online DDL](#mysql-online-ddl)
  - [FAQ](#faq)
  - [其他](#其他)
    - [什么是元模型/Query Class](#什么是元模型query-class)
    - [Language](#language)


## 简介

**Querydsl是什么? 为什么选择？**
介绍:  [为什么选择](static/why_querydsl.md)

注意：本框架不基于`querydsl-jpa`，是基于`querydsl-sql`的扩展。与JPA模式的比较参见[Why QueryDSL](static/why_querydsl.md)。
如果要在querydsl-jpa的项目中集成本模块的，可以参见下文 (与 Querydsl-JPA 一起使用)

**变更日志**
[ChangeLogs](static/changelog_cn.md)

**手册 / Manuals**
参见  [使用说明](static/user_guide_cn.md)

**引用 / Repository**

```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-extension</artifactId>
	<version>5.0.0-r120</version>
</dependency>
```

如需要与Spring framework集成，还需要
```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-extension-spring</artifactId>
	<version>5.0.0-r120</version>
</dependency>
```

**与 Querydsl-JPA 一起使用**
要一起使用，需要解决两个问题。

1. Querydsl-sql中自动生成的表模型（Query class）继承 `com.querydsl.sql.RelationalPath`，但是JPA中的Query class继承 `com.querydsl.core.types.EntityPath` 两者并不一致。
   这个问题可以通过 `querydsl-entityql` 的框架解决（https://github.com/eXsio/querydsl-entityql) ，该框架主要作用之一就是将querydsl-jpa的模型转换为querydsl-sql模型。实测该方法可以正常工作。
2. 事务管理器(Transaction Manager)问题。
   querydsl-jpa默认是使用Hibernate Session或者EntityManager进行操作的，也就是说调用层次在上述框架之上。而QueryDSL-sql是基于DataSource（JDBC）的，也就是俗称的原生SQL查询。如果您在一个业务交易中使用了两个框架，事务器问题需要您自行处理解决。



## 特性介绍

### 极致性能

对QueryDSL-SQL的性能进行了较大幅度的优化。优化后的性能基本与编写良好的JDBC操作持平。

下面列出一部分，更多性能相关数据，参见 [性能参考 Performance guide](static/performance_tunning.md)

**性能对比测试（v5.0.0-r110）**

* MySQL 5.7.26
* MySQL JDBC Driver 5.1.49
* URL：postgresql://局域网地址:3306/test?useSSL=false
* jvm version=17

| Case                                                     | Mybatis 3.5.9（单位ms）                | querydsl-sql-extension<br /> 5.0.0-r110（单位ms） | A/B     |
| -------------------------------------------------------- | -------------------------------------- | ------------------------------------------------- | ------- |
| 7列的表插入数据，15批次，每批10000条。<br />总计15万记录 | 7203, 7438, 7925<br />平均 7522        | 2476, 2711, 2834<br />平均2673.67                 | 281.34% |
| 22列的表插入数据，15批次，每批10000条<br />总计15万记录  | 16939, 16870, 16782<br />平均 16863.67 | 5541, 5609, 5538<br />平均 5562.67                | 303.16% |
| 22字段表，查出50记录<br />全部加载到内存中的List内       | 12, 14, 12<br />平均12.667             | 8, 9, 10<br />平均9                               | 140.7%  |
| 22字段表，查出5000记录<br />全部加载到内存中的List内     | 646 , 601, 589<br />平均612            | 79, 75, 82<br />平均78.67                         | 777.90% |
| 22字段表，查出5万记录<br />全部加载到内存中的List内      | 935, 930, 953<br />平均939.33          | 321, 323, 339<br />平均327.67                     | 286.67% |
| 22字段表，查出30万记录<br />全部加载到内存中的List内     | 3340，3343, 3577<br />平均 3420        | 1832, 1925, 2313<br />平均 2023.33                | 169.03% |
| 22字段表，查出1M记录<br />全部加载到内存中的List内       | 8478, 9219, 7468<br />平均 8388.33     | 6571, 7535, 5271<br />平均 6459                   | 129.87% |

备注：

* 均为单线程测试。会先执行一个50万左右的简单计算循环，使CPU睿频飙高。

* 日志级别到ERROR。

* 测试计时前，相同的SQL语句会先执行一遍，确保路径上的类都事先加载。

* 数据库，环境等均使用相同环境。同一组测试两个框架操作先后时间不超过5分钟。

* 每组用例跑三次，计算平均耗时（单位毫秒）

* MyBatis在SELECT的配置中设置fetchSize="5000"，QueryDSL在查询时设置setFetchSize(5000)

**高性能的秘密**

* **无反射访问**：重写了QueryDSL 中JavaBean与JDBC交互部分以提升性能。使用ASM，为每个需要Bean对应的查询字段组合生成了一个访问器，作为反射的一个加速替代。

动态类生成方案，每个一个SQL查询的SELECT 字段组合对应一个访问器，在第一次查询时生成并加载，第二次执行该SQL时性能就相当于硬编码。过程中去除了反射和IF分支，对ResultSet的全部访问均为按index的顺序访问（考虑CPU分支预测），大幅减少内存操作次数。

* **表模型与BeanCodec缓存**：对表和字段模型。以及每个对象的编解码器进行了缓存。

* **面向字节码 / JIT友好**： 如栈上操作、尽可能final化、手工内联、对象复用、内存一次分配、用tableswitch代替复杂分支的编程技巧。总体原理基于减少内存拷贝、字节码操作减少、对JIT友好、面向分支预测等一些编程原则。

* **向开发者提供Tunning API**： 提供fetchSize, maxRows，queryTimeout等方法，操作大量数据时可根据业务需要做性能和安全的Tuning。

  **Example**：一次查出一百万ID。

  ```java
  List<Integer> list = factory.select(t1.id).from(t1)
  		.setFetchSisze(10000)
  		.setMaxRows(1_000_000)
  		.setQueryTimeout(15)
  		.fetch();
  ```

**兼容性**

* 使用ASM7的动态类生成，支持JDK 8~ 22，GraalVM（22）。

* 在使用GraalVM native （AOT）模式下，ASM类生成无效，Java代码退化为基于反射的类型访问器，功能可正常使用。
  （在GraalVM编译期间，实质上这部分逻辑也已编译为本地静态代码，无需再使用ASM加速）

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


* 提供增强的日志输出。通过com.github.xuse.querydsl.sql.log.QueryDSLSQLListener可以提供三种格式的日志输出格式
  **Example:**

  ```java
  /*
   *  FORMAT_COMPACT  适合大型生产环境的紧凑格式
   *  FORMAT_FULL 长的字符串会完整输出，SQL和参数之间会换行。 
   *  FORMAT_DEBUG 详细的信息输出，有换行便于阅读，适合开发环境观察语句和逻辑。
   */
    configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
    configuration.setSlowSqlWarnMillis(200);
  ```
  * 输出每个SQL和参数，以及执行时间和记录数。如果日志级别到WARN，这部分逻辑可被跳过，以最大限度提升性能。
  * 慢SQL以Error级别输出（慢SQL阈值可设置）
  * Batch模式下，省略N组之后的参数。
  * 生产环境建议使用紧凑格式输出。语句和参数在一行中显示有利于使用grep等命令查询和分析。
  
* 包扫描：前文中已经介绍了几种基于Annotation的功能增强，包扫描可以在应用启动时，分析所有数据库元模型定义，提前校验这些Annotation的正确性，并将其中的一些配置注册到全局上下文。@CustomType要生效需要提前进行包扫描。


* 基于Annotation定义的元模型：QueryDSL原生的元模型（Metamodel）主要使用API进行定义。扩展提供了一套基于注解的元模型定义方式，效果覆盖原有的元模型和扩展功能。

  **Example**

  ```java
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

  ```java
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

  ```java
  configuration.setDefaultQueryTimeout(5); //设置SQL最大执行时间为5秒
  ```

### 自动创建数据库结构

完整的数据库结构建模：扩展QueryDSL原生的ColumnMetadata，支持在Java(JDBC)模型中描述全部数据库表的特征——包括default value、unsigned、自增、索引、约束（不含外键）。相关模型可以通过元模型API创建，也可以通过注解来定义。

  Q: 这功能有什么用？

  A: 数据库应用开发者一般有两种用法，一种是先在数据库中设计数据结构，然后通过Bean生成工具生成Java类结构。（称为数据库Schema优先）
  另外一种是软件开发者开发跨多种RDBMS应用的做法，是先建立Java Entity模型，然后通过程序等手段自动创建数据库结构。(称为Metadata优先)。 典型地，Hibernate就支持启动时自动创建数据库结构；本框架也支持这一用法。

QueryDSL思路是数据库Schema优先，本框架扩展支持了Metadata优先的场合，可以通过Java元模型反向更新数据库结构。 同时，这种更新支持基于数据库结构对比的增量更新，支持更新索引、约束、视图、等数据表结构。

> 不支持：外键、物化视图、索引组织表、自定义函数、触发器、存储过程，由于这些特性很少在跨RDBMS应用中用到，暂无支持的必要。

### 纯POJO使用

QueryDSL官方版本中操作数据库需要使用代码生成生成工具生成query class。本框架在该基础上作了一些增强，允许用户不创建QueryClass类，通过纯POJO加上若干注解来替代query class模型。并使用lambda表达式来表示表或列的模型。

该功能主要为降低使用门槛，参见文档`static/USER_GUIDE.md`。

### R2dbc数据源使用(响应式数据)

R2DBC (Reactive Relational Database Connectivity) 是一种为关系型数据库设计的异步非阻塞编程规范，可以替代传统 JDBC 的阻塞编程模式，支持响应式流处理（Reactive Streams）。
目前较流行的R2dbc框架有spring-data-r2dbc。使用本框架可以代替Spring-data-r2dbc（当然也可以同时使用），配合Spring web-flux可以实现完全无阻塞的流式数据处理。

参见  [querydsl-sql-r2dbc](querydsl-sql-r2dbc/) 和 [querydsl-sql-r2dbc的Spring事务](querydsl-sql-r2dbc-spring) 用法。

### 其他功能增强

#### 多种风格的低代码API

有朋友和我反馈说QueryDSL的操作风格就像用java API在编写SQL语句，虽然功能很强大，但是对于SQL初学者他们还是更习惯于类似JPA、Hibernate、Mybatis Mapper等工具提供的一个支持简单对象操作的Repository。
所以也将一些常用操作封装到了GenericRepository类，针对这些低代码需求者，简化日常CRUD操作。

> 以QueryDSL的强大的Query AST机制封装这样一个工具没有任何难度，过去在我看来是这业务层做的事情。
>很多朋友却更喜欢那种风格的API，后来我意识到并不是所有开发者都关注底层的AST和数据库衔接这些问题，他们更关注数据访问facade层，希望用更少的代码完成常用业务功能。所以包装了GenericRepository，以支持一些更偏向“传统习惯”的用法。  

#### 数据库结构访问与修改(DDL)

* 支持通过API查询数据库中的Schema、表、视图、索引、约束等信息。

* 支持Truncate/Create/Drop/Alter table/ Partition等常用DDL的Java语法操作。

#### Record对象作为数据表映射

java 16开始支持 Record特性(**@jls** 8.10 Record Types)， 支持该类对象作为数据表映射。代替传统的POJO实体Bean。详见文档quick start.md。

> 要使用Record特性，您必须使用JDK 16以上版本。
> 本框架对Record对象的访问作了特殊处理，不依赖JDK 16以上版本，当前框架编译后依然可以在java 8上使用。



### 运行环境与适用范围

**Java环境支持**

* Open JDK / Oracle  JDK 8~22 
* Oracle GraalVM 17~22
* GraalVM Native (AOT)

未测试不确定

* Andriod (ASM功能会自动禁用，但其他特性未进行验证)

**数据库支持(DML)**

保持QueryDSL原版能力，包含以下数据库

* MySQL v5.x ~ v8.x
* CUBRID
* DB2 10.1.2  and above
* Apache Derby 10.14  and above
* Firebird
* H2
* HSQLDB
* Oracle 10g  and above
* PostgreSQL 9.1 and above
* Teradata
* SQL Server 2005, 2008, 2012
* SQLite 

> 其他数据库可以自行编写SQLTemplates进行扩展 。

## 实验性功能

> 实验性功能是根据特定使用场景或建议增加的一些新特性，用于体验和建议收集。

### Partition管理（已支持MySQL、PostgreSQL）

> 示例为在MySQL上的功能。
>
> PostgreSQL分区机制与MySQL差异很大，目前仅支持创建分区表，添加/删除分区。在PostgreSQL上由于数据库机制差异造成的特性差异，暂无支持计划。

分区特性支持表

| 功能                 | MySQL | PostgreSQL | Oracle   | Etc.. |
| -------------------- | ----- | ---------- | -------- | ----- |
| 创建分区表           | Yes   | Yes        | Planning |       |
| 查询已有表分区信息   | Yes   | Yes        | Planning |       |
| 将已有表设置为分区表 | Yes   | X          | Planning |       |
| 分区表清除分区设置   | Yes   | X          | Planning |       |
| 创建HASH分区         | Yes   | X          | Planning |       |
| 创建RANGE分区        | Yes   | Yes        | Planning |       |
| 创建LIST分区         | Yes   | Yes        | Planning |       |
| DROP分区             | Yes   | Yes        | Planning |       |
| 重新组织分区         | Yes   | X          | Planning |       |
| 调整HASH分区个数     | Yes   | X          | Planning |       |


代码示例

```java
SQLMetadataQueryFactory metadata=factory.getMetadataFactory();
metadata.createTable(t1).reCreate().execute();

QPartitionFoo1 t1 = QPartitionFoo1.partitionFoo1;
//创建分区
metadata.createPartitioning(t1)
		.partitionBy(Partitions.byHash(HashType.HASH, "TO_DAYS(created)", 4))
		.execute();

//查询表分区信息
List<PartitionInfo> list=metadata.getPartitions(t1.getSchemaAndTable());

//清除分区设置（不删除数据）
metadata.removePartitioning(t1).execute();

//创建按时间范围进行的分区。如果是MySQL上，开始日期可不指定。
metadata.createPartitioning(t1).partitionBy(
	Partitions.byRangeColumns(t1.created)
		.add("p202401", "'1970-12-01'","'2024-02-01'")
		.add("p202402", "'2024-02-01'","'2024-03-01'").build())
    .execute();

//在按上述分区中再追加一个分区，
//该操作会自动使用REORGANIZE PARTITION将落在原先第一个分区内符合条件的数据移动到新分区
metadata.addParition(t1)
		.add("p20200101", "'2021-01-01'")
		.execute();

//删除分区（连同分区内的数据）
metadata.dropPartition(t1)
    .partition("p20200101").execute();
```

### DDL Support

> 实验性功能，支持DDL需要编写各个不同数据库的方言，个人精力有限目前仅完成了部分数据库的方言适配。但现有框架基于AST的扩展机制十分强大，适配其他主流数据库问题不大，有兴趣者可自行编写方言进行扩展。
>
> 以下是计划中的2024年内支持的数据库：Oracle，H2

**数据库支持(DDL)**

* MySQL v5.6 and above
* Apache Derby v10.14 and above
* PostgreSQL v10.3 and above
* H2 v2.3.232

相关说明参见文档 quick_start.md

> 其他数据库可以自行编写SQLTemplatesEx (本框架定义的方言扩展类) 进行扩展 ，如有需求也可以邮件与我讨论。


### MySQL Online DDL
([Contents](# 目录/ Contents))

OnLine DDL可以防止DDL执行期间锁导致阻塞,影响用户的 DML 操作,而 Online DDL 指的是在 DDL 期间,允许用户进行 DML 操作。

在MYSQL数据库上执行DDL时会自动使用Online方式执行，尽可能避免对生产环境业务访问的干扰。
示例如下：在对数据表进行修改时，会指定算法和锁，使得DDL执行期间不影响业务。

```sql
ALTER TABLE table1
  CHANGE c_bin c_bin BLOB NULL COMMENT 'test column', ALGORITHM = INPLACE, LOCK = NONE,
  DROP KEY idx_aaa_taskstatus, ALGORITHM = INPLACE, LOCK = NONE,
  ADD KEY idx_aaa_taskstatus (task_status), ALGORITHM = INPLACE, LOCK = SHARED
```

* 此功能的应用并不意味着DDL执行对数据表无影响，24小时的运行的高可用系统还是应当在业务低谷期间执行DDL
* Online DDL是在MySQL 5.x引入的，8.x中支持更多的Online DDL策略。但目前5.x和8.x的方言还没有区分开，目前仅按5.x做了相对保守的策略。

## FAQ

1. 关于SQL语句的换行影响日志查看，在创建SQLTemplate的时候使用com.querydsl.sql.SQLTemplates.Builder.newLineToSingleSpace()方法。

2. 如果我的项目已经用了querydsl-sql，现在集成这个框架，需要将原有的query class全部修改为继承com.github.xuse.querydsl.sql.RelationalPathBaseEx`类吗？

    A: 可以不用。本框架是在querydsl上的一些轻微调整格改进，querydsl的原生用法都不受影响。

## 其他

### 什么是元模型/Query Class

* 元模型(meta mode)：在QuerDSL中，对每个实体会有一个"Q"开头的class，在querydsl文档中，称为 `Query Class`. 其实这个类和JPA中的元模型用途差不多，都是用于描述数据结构，并且提供查询API引用的类。（在OpenJPA中，会生成下划线结尾的类，用途是差不多的）。所以本文某些场合也会使用元模型一词，和QueryDSL文档中的 query class是一个东西。
* 本框架自r104后，可支持通过Lambda表达式代替元模型，从而省去`Query Class`，本质上是一种语法糖，但对初学者更加友好。
* Lambda在表示数据表时是单例的，无法构造出自表关联查询时。
  （类似这个SQL语句，就是一个自表关联查询 `SELECT t.* FROM tree_node t LEFT JOIN tree_node parent ON t.pid = parent.id`）
  r110版本进一步扩展API，可以使用Lambda表达式构建出自表查询请求。

### Language

目前框架使用的语言情况

* 文档 / Documentation：中文
* Javadoc:  中英文双语 / Chinese English bilingual
* 日志和异常信息  / Log and exception messages: 英文/English only