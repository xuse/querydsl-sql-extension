# querydsl-sql-extension

query-dsl-sql-extension is a enhancemant lib based on querydsl-sql module.

本框架是在 [querydsl-sql](https://github.com/querydsl/querydsl) 上的扩展，querydsl-sql的使用手册，可以参阅 http://querydsl.com/static/querydsl/latest/reference/html/ch02s03.html 

本框架是为了更便利，以及提供更高性能为目的对querydsl进行的改进。本框架通过初始化时使用不同的入口类的方式与原生的querydsl用法做出区别，保留原querydsl的更新能力，对原框架无侵入性。

> 注意：不是基于`querydsl-jpa`的，中文网上到处都是querydsl-jpa的资料，将其介绍为弥补JPA不足的查询构建器，搞得好像querydsl是JPA下的一个配件一样。实际上queydsl有十几个模块，针对各类SQL与NO  SQL数据存储都有适配。与JPA模式的比较参见下一节。写这个框架的目的是获得一个轻量便捷的数据库访问层，也可以与MyBatis、Spring JDBC Template等一起使用。

**Getting started**

```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-extension</artifactId>
	<version>5.0.0-r111</version>
</dependency>
```

如需要与Spring framework集成，还需要

```xml
<dependency>
	<groupId>io.github.xuse</groupId>
	<artifactId>querydsl-sql-extension-spring</artifactId>
	<version>5.0.0-r111</version>
</dependency>
```



参见  [使用说明](USER_GUIDE.md) /  See file [User  Guide (Chinese)](USER_GUIDE.md)

### 重要变更

5.0.0-r111版本，将Spring框架支持功能移到 querydsl-sql-extension-spring 模块，相关使用说明已修改，Spring下使用需要添加该依赖包。
Spring下的初始化方法从 `SQLQueryFactory.createSpringQueryFactory()`修改为  `QueryDSLSqlExtension.createSpringQueryFactory()`

## 什么是QueryDSL

[QueryDSL](https://github.com/querydsl/querydsl) 是一个历史悠久的SQL查询构建器。

在Hibernate和JPA大行其道的年代里，开发者很快发现ORM无法表达一些稍微复杂的查询，更不要说带有多个组合查询了。Hibernate的方案是引入HQL，JPA的方案是引入QueryBuilder和NativeQuery，然而这些方案都不完美，逐渐被淘汰。大约在2015左右，我在编写GeeQuery以支持Spring-data的时候，发现Spring-Data引入了一套的新的QueryBuilder，即是QueryDSL。

QueryDSL是针对SQL的词法树抽象进行建模的，这就使得它的查询模型非常丰富，几乎能涵盖查询语言（如SQL）的所有功能。QueryDSL用相同的方法为Lucene、JPA、ES、JDO、Mongo等都进行了查询模型的建模，使得开发者可以在强语法和类型检查的情况下访问持久层。

如果开发者直接使用JSON、SQL、HQL、JPQL等查询语言，只能基于字符串拼接完成。而字符串拼接对于一个长期维护的业务项目是非常危险且繁琐的。开发者无法知道改对了没有，也无法确认要修改的代码位置是否都得到了修改。

QueryDSL提供了友好的查询构建API，接近SQL且符合自然语言习惯。我在试用后就发现使用它编写的业务代码可读性很强且没有冗余代码，它的潜力巨大，可以说是一个理想的数据库Facade。 (这种风格大概在十多年前就有了，近几年很多新框架参照了类似的用法。)

在这个基础上，我将自己使用querydsl中用到的一些功能包装了一下，就有个这个框架。前面已经说过querydsl的查询构建界面(Facade)非常好，所以我针对对象构造与反射等场合作了重写，以期将性能推到极致，就此使用了好多年，功能上逐步接近我对数据库访问工具的理想期望。

### 附：Java常见数据库访问的技术路线

**数据访问层常用路线——JPA路线**

对比下表可发现，该路线对于 单表应用/ 小规模企业应用 的开发者是较为适合的。但是要想精通，需要专门学习框架自身知识体系——HQL / JPQL / QueryBuilder/ QueryDSL-JPA /SpringData-JPA /Hiberbate自身的对象管理和缓存机制 等等。

* 这也是整个JPA路线的问题——技术栈很深，但和数据库设计 / SQL优化等不在一个方向上。
* 抽象的JPQL为了解决兼容所有RDBMS的目的，有很多限制，无法最大化利用数据库的特性（比如 RowId, 窗口分析函数等）。

|      | JPA / Hibernate                                              | Spring Data -JPA                                             | QueryDSL-JPA                                                 |
| ---- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 介绍 | 纯面向对象的数据访问，但实际<br />关系数据库的能力很难用一个虚拟的“对象库”代替。<br />因此需要使用HQL语言完成复杂查询。<br />JPA为了替代HQL，设计了QueryBuilder，但是API也比较复杂，并且JPA还设计了JPQL查询语言。<br />加上各种缓存、投影等复杂操作，入门易精通难 | Srpingframework针对JPA进行的封装（也封装了其他很多持久层框架），保留了JPA的所有功能 | 思路与Spring-Data如出一辙，API更易用。一般搭配搭配Spring Data-JPA使用，该路线的终极解决方案。 |
| 优点 | * 单表操作极其简便  <br />* @OneToOne @OneToMany @ManyToOne等注解，多表关联和延迟加载无需编码<br />* LOB字段延迟加载 | * JPA的所有优点<br />* 基于接口定义的方法名自动生成实现代理<br />* 引入querydsl-jpa作为替代的查询构建器。比JPA规范更符合开发者直觉。 | * 搭配Spring data-JPA使用，具有前者的全部优点<br />* 复杂查询构建方便 |
| 缺点 | 自由度较低<br />性能难控制<br />性能依赖缓存，而在多实例应用中，数据库层级的缓存存在无法刷新问题<br />低层级的缓存在复杂业务中不能代替粗粒度的业务数据缓存，整个缓存架构变得更复杂 | * JPA的所有缺点<br />* 本质上还是基于JPA的，要精通还是要深入学习JPA的所有知识<br /> | * JPA的所有缺点<br />* 三套框架的学习成本，                  |

**数据访问层常用路线——JDBC路线**

对比下表可以发现，虽然是JDBC路线，但querydsl-sql是所有框架中唯一做到以下两点的框架：

* 开发者无需编写 xxQL （HQL / JPQL /SQL），仅需掌握**一套API**即可完成（在不同RDBMS上的）所有数据库操作。
* 开发者无需接触JDBC API，无需自行处理结果映射。

|      | SpringData -JDBC /JDBCTemplate                               | querydsl-sql                                                 | QueryDSL-SQL-extension<br />(本框架)                         |
| ---- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 介绍 | Srpingframework针对JDBC的封装，帮助开发者完成从JDBC到Java字段的简单映射，提升效率。 | 对数据库和SQL特性进行建模，提供了直观、简便的API。<br />内部使用了AST（抽象词法树）来描述针对数据库访问请求建模、 | 在querydsl-sql基础上封装。保留了querydsl-sql的特点。         |
| 优点 | * 基于接口定义的方法名自动生成实现代理                       | * 虽然是JDBC路线，但AST的模型包装了几乎所有SQL操作，开发者无需编写SQL，无需字符串拼接。<br />* 提供了各种数据库到java对象返回结果的映射（单列数据/多表任意列返回/指定列数据等）<br />* 数据库可移植性： 通过SQL AST + SQL方言模版，能做到一套数据库访问模型通吃所有数据库类型。一次编写，所有数据库上可运行。 | * querydsl-sql的所有优点<br />* 性能极致（参见后文）<br />* 单表等简单场合的API操作无需编码实现，解决开发效率问题。<br />* 支持近年来流行的Lambda代替数据表模型，无需生成Quey class. |
| 缺点 | * 封装程度不高，开发者需要熟练掌握SQL和JDBC规范，<br />* 复杂映射需要自行处理JDBC ResultSet对象。<br />* 不提供任何辅助数据库可移植性的特性。 | * 提供的API基于SQL AST模型，有种用java语言编写SQL的感觉，单表等简单操作也要编码实现，效率不够高 | *在本路线框架中没发现缺点。对比JPA类框架，没支持自动级联（@OneToOne @OneToMany等）和延迟加载，但提供了便捷的多表Join query和数据返回映射。 |

**数据访问层常用路线——Mixed路线**

这条路线也是基于JDBC封装的，但都基于MyBatis框架。而不直接使用 JDBC。
进入这条路线基本可以放弃数据库可移植性了。因为MyBatis核心是SQL语句拼接而不是AST，因此最终数据库访问模型是用SQL字符串表达的。不过MB的支持者用了SQLParser之类的词法分析器来试图帮助这类框架提升数据库可移植性，以及一些基于MyBatis上层框架也采用了类似AST的模型来表达复杂查询，尤其是国内朋友各种对MyBatis进行“大修大补”的框架，使得这条路线在今天依然是最为流行的路线。但底层的架构决定了这条路线的上限。要解决数据库移植性问题，和重写一套基本没有区别。

>  当然数据库可移植性不一定要在访问框架层解决，还可以通过JDBC驱动（在驱动中通过词法分析修改SQL的行为）、中间件（直接解析数据库网络通信协议）、多语言数据库（现在大多数云数据库和国产数据库支持语法兼容模式）来解决。所以这可能不是什么大问题。

|      | MyBatis                                                      | MyBatis Flex/Plus等                                          |
| ---- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 介绍 | 很老的框架，基本不更新了。基于JDBC封装，<br />提供了良好的SQL生成引擎和对象映射机制。 | 基于MyBatis的封装。                                          |
| 优点 | * 简便、流行、易学、轻量<br />* 还不错的性能                 | * 简便、流行、易学、轻量<br />* Flex提供了自动生成数据表模型的机制（类似querydsl的Query class，或JPA的元模型）<br />* Plus提供了Lambda代替数据表模型的机制 |
| 缺点 | * 没有提供JDBC原生Batch操作的封装<br />* 几乎没有对数据库可移植性帮助的特性<br />* 手工编写SQL开发效率不高。<br />* 无法使用静态工具（如编译器）对SQL<br />进行编译检查<br /> | * 解决了MyBatis开发效率的问题<br />* 其他MyBatis的缺点       |



### 本框架能和QueryDsl-JPA一起使用吗？

querydsl-jpa默认是使用Hibernate Session或者EntityManager进行操作的，也就是说调用层次在上述框架之上。而QueryDSL-sql是基于DataSource（JDBC）的，也就是俗称的原生SQL查询。两者如果要一起用，也不是不行。但两类操作难以协调到一个事务中，因为两者的Spring事务管理器不一样。

> 如果没有两类框架操作共享事务机制的要求，那就随便了。



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


### 性能优化

对QueryDSL-SQL的性能进行了较大幅度的优化。优化后的性能基本与编写良好的JDBC操作持平。

下面列出一部分，更多性能相关数据，参见 [性能参考 Performance guide](static/performance_tunning.md)

#### 性能对比测试（v5.0.0-r110）

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

  

#### 高性能的秘密

* **无反射访问**：重写了QueryDSL 中JavaBean与JDBC交互部分以提升性能。使用ASM，为每个需要Bean对应的查询字段组合生成了一个访问器，作为反射的一个加速替代。

动态类生成方案，每个一个SQL查询的SELECT 字段组合对应一个访问器，在第一次查询时生成并加载，第二次执行该SQL时性能就相当于硬编码。过程中去除了反射和IF分支，对ResultSet的全部访问均为按index的顺序访问（考虑CPU分支预测），大幅减少内存操作次数。

* **表模型与BeanCodec缓存**：对表和字段模型。以及每个对象的编解码器进行了缓存。

* **其他各种优化：**如栈上操作、尽可能final化、手工内联、对象复用、内存一次分配、用tableswitch代替复杂分支的编程技巧。总体原理基于减少内存拷贝、字节码操作减少、对JIT友好、面向分支预测等一些编程原则。

* **向开发者提供Tunning API：**提供fetchSize, maxRows，queryTimeout等方法，操作大量数据时可根据业务需要做性能和安全的Tuning。

  **Example**：一次查出一百万ID。

  ```java
  List<Integer> list = factory.select(t1.id).from(t1)
  		.setFetchSisze(10000)
  		.setMaxRows(1_000_000)
  		.setQueryTimeout(15)
  		.fetch();
  ```


#### 兼容性

* 使用ASM7的动态类生成，支持JDK 8~ 22，GraalVM（22）。

* 在使用GraalVM native （AOT）模式下，ASM类生成无效，Java代码退化为基于反射的类型访问器，功能可正常使用。
  （在GraalVM编译期间，实质上这部分逻辑也已编译为本地静态代码，无需再使用ASM加速）

### 功能增强

#### 数据库结构建模

完整的数据库结构建模：扩展QueryDSL原生的ColumnMetadata，支持在Java(JDBC)模型中描述全部数据库表的特征——包括default value、unsigned、自增、索引、约束（不含外键）。相关模型可以通过元模型API创建，也可以通过注解来定义。

  Q: 有什么用？
  A: 数据库应用开发者一般有两种用法，一种是先在数据库中设计数据结构，然后通过Bean生成工具生成Java类结构。（称为数据库Schema优先）
  另外一种是软件开发者开发跨多种RDBMS应用的做法，是先建立Java Entity模型，然后通过程序等手段自动创建数据库结构。(称为Metadata优先)。
  QueryDSL仅支持第一种用法。本库满足了Metadata优先的场合，可以通过Java元模型反向更新数据库结构。 同时，这种更新支持基于数据库结构对比的增量更新，支持更新索引、约束、视图、等数据表结构。（不支持：外键、物化视图、索引组织表、函数、触发器、存储过程，由于这些特性很少在跨RDBMS应用中用到，暂无支持的必要。）

#### 纯POJO使用 (无QueryClass)

QueryDSL官方版本中操作数据库需要使用代码生成生成工具生成query class。本框架在该基础上作了一些增强，允许用户不创建QueryClass类，通过纯POJO加上若干注解来替代query class模型。并使用lambda表达式来表示表或列的模型。

该功能主要为降低使用门槛，参见文档USER_GUIDE.md。

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

> 实验性功能，个人精力有限目前仅完成了部分数据库的方言适配。但现有框架基于AST的扩展机制十分强大，适配其他主流数据库问题不大，有兴趣者可自行编写方言进行扩展。

**数据库支持(DDL)**

* MySQL 5.6 and above
* Apache Derby 10.14 and above
* PostgreSQL 10.3 and above

相关说明参见文档 quick_start.md

支持DDL需要编写各个不同数据库的方言，目前整个方言的框架机制有了，但只编写完成了MySQL和Derby。下一个考虑抽空完成PostgresSQL的，剩下的看需要吧。

> 其他数据库可以自行编写SQLTemplatesEx (本框架定义的方言扩展类) 进行扩展 ，如有需求也可以邮件与我讨论。


### MySQL Online DDL

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



### 动态变化的数据库表

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



## 修订记录

**版本编号方式：** 版本号由两部分组成，前一个数字是对应的querydsl库版本号，后一个是此扩展框架的修订号。从1开始向上，每个修订都向下兼容。

```
v{querydsl 版本号} - r(extension version)
```

### v5.0.0-r111

2024-10-01

* 工程拆分，将核心不使用的类拆分到其他工程中。拆分出querydsl-sql-extension-spring，用于Spring下集成。querydsl-sql-extension不再依赖任何Spring Framework和FastJSON。
* 构建期自动执行单元测试并输出报告，此版本发布条件为单元测试行覆盖达到85%。
* 测试代码增加了MySQL Mock驱动，用于单元测试时模拟MySQL数据库行为。
* 对工具类的单元测试，修复几处边界数值下的小错误。
* AlterTableQuery功能增强，支持列修改、更名等操作。

### v5.0.0-r110

2024-09-01

* PostgreSQL DDL支持。基于PostgreSQL 10.3测试。常规DDL操作已经支持完成。
* PostgreSQL表分区功能支持，但PostgreSQL分区机制与MySQL差异很大，目前仅支持创建分区表，添加/删除分区。
  不支持Hash分区，
  不支持为已有的表添加/删除分区配置（机制上无法支持，Postgresql分区表在创建时就已明确是是否分区，之后无法修改）
  不支持重组织分区
  上述由数据库机制差异造成的特性差异，暂无支持计划。
* Postgresql的支持比预想更为复杂，为此重构了Schema获取和DDL部分生成的机制。 主要功能开发完毕，测试中，计划9月初发布。
* 优化BatchInsert下的数据插入性能。尤其对MySQL下的批量写入，默认从JDBC Batch更换为Bulk SQL语句，某些场景下性能提升约65倍。
* QueryDSL Insert Batch功能调整，增加参数一致化处理。
* 数据初始化增加配置，setPrimaryKeys用于控制主键列是否要写入到数据库。

### v5.0.0-r104

2024-08-08

* 支持GraalVM的Native模式使用
* 支持在java 16以上环境使用record类型作为实体。

### v5.0.0-r102

2024-07-24

* 按包整理javadoc，部分类更换包位置。补充重要类的双语javadoc。
* 支持外部扩展自行实现Facade。通过自行实现ExtensionQueryFactory接口，扩展各种查询API。
* 提供GenericRepository类，在Spring下可以继承该类后无需编写任何代码实现常用repository的功能。支持多种API风格。
* 【实验性】支持无Query Class的纯POJO映射使用。使用Bean Class代替QueryClass。使用方法引用Lambda代替模型字段。

### v5.0.0-r101

2024-06-28

* 增加了一个供上层业务调整表名的机制，以支持业务层分表场景。
* 支持MySQL Partition管理。支持RANGE/LIST/HASH/KEY等分区类型，支持分区的创建，调整，删除，重组等操作。
* 补充表约束和索引的缺失接口。部分操作MySQL支持不锁表的Online操作。

### v5.0.0-r100

2024-06-11

* 完成DDL相关表语法支持。部分类名重构调整。
* 版本号100开始，保持三位数。QueryDSL官方版本5.1.0是需要Java 11以上的，目前还不打算跟进支持。 

### v5.0.0-r8

* 升级支持Querydsl v5.0.0
* 添加MySQL方言：com.querydsl.core.types.dsl.MySQLWithJSONTemplates、JsonExpressions等，支持JSON字段操作
* 注解@AutoGenerated，支持字段内容自动生成(在update/insert时)。如使用populate()方法则自动写入，如使用set()方法，可调用populateAutoGeneratedColumns()方法生成。
* 增加GUID、SnowFlake ID等生成规则

### v4.1.1-r4
* 增加更适合于linux下的简洁日志。
* 结果集拼接性能优化。提供额外的RelationalPathBaseEx用于继承。
* API增加——增加DDL语法框架（尚未实现）。

### v4.2.1-r1
1. 日志扩展
2. 关于Connection is not transactional异常修复，还有一些其他方面的个性化要求，因此通过本项目进行扩展。后续随功能更新。

## FAQ

1. 关于SQL语句的换行影响日志查看，在创建SQLTemplate的时候使用com.querydsl.sql.SQLTemplates.Builder.newLineToSingleSpace()方法。

2. 如果我的项目已经用了querydsl-sql，现在集成这个框架，需要将原有的query class全部修改为继承com.github.xuse.querydsl.sql.RelationalPathBaseEx`类吗？

    A: 可以不用。本框架是在querydsl上的一些轻微调整格改进，querydsl的原生用法都不受影响。

## 其他

### 什么是元模型

* 元模型(meta mode)：在QuerDSL中，对每个实体会有一个"Q"开头的class，在querydsl文档中，称为 `Query Class`. 其实这个类和JPA中的元模型差不多，都是用于描述数据结构，并且提供查询API引用的类。（在OpenJPA中，会生成下划线结尾的类，用途是差不多的）。所以本文某些场合也会使用元模型一词，和QueryDSL文档中的 query class是一个东西。
* 本框架自r104后，可支持通过Lambda表达式代替元模型，从而省去`Query Class`，本质上是一种语法糖，但对初学者更加友好。

### 一点感想

这个框架可以说是无心偶得，今后也会佛系维护。

* 开始 (2017)。近日使用QueryDSL-sql作为轻量级数据库操作框架，有手写SQL的畅快，有静态语法检查的安心，还有语法自动完成的高效。
  QueryDSL提供了友好的查询构建API，接近SQL且符合自然语言习惯。我在试用后就发现使用它编写的业务代码可读性很强且没有冗余代码，它的潜力巨大，可以说是一个理想的数据库Facade。

  我在编写GeeQuery的几年中，阅读了几乎所有Java数据库访问框架的代码，当我意识到其他QueryDSL的代码要比用其他任何Facade的代码都要简练自由时，我抛弃了其他已知的框架，包括写了近十年的GeeQuery。在2017我将自己使用queryDSL的一些代码放在一个库里，只有几个类。
  本着最小修改的原则，用这个项目对QueryDSL的监听行为进行扩展，从而可以得到更详细的日志信息。

* 性能演进（2018）
对性能进行了优化。除了代码细节修改外，还增加了基于ASM自动生成动态类来完成字段拼装对象，无反射调用。

* 后来（2019~2023）

  之后就是自己用得非常爽快，期间仅对一些常用的功能进行了小改，使得代码更简洁。如自动时间戳、自动生成GUID等。

* 再后来(2024)
  在使用过程中，阅读源码中发现原作者一开始是想要支持DDL语法的，后来不知道为什么没有再支持了。可能确实使用场景不太多，总归有点小缺憾，由于我早年在别的框架上写过相关的功能，于是花了不少时间在目前的框架上支持了DDL语法。
  另外还有些朋友喜欢无代码自动生成，无Query class的纯POJO用法，为此也作了一些适配来降低使用门槛。

* 今后：保持轻量是维护的宗旨，不会再考虑加入什么乐观锁、词法分析器、分库分表、内置连接池之类的东西，虽然都写过，但是将它们糅合在一起没有必要。

* 如果有使用下来觉得不错的朋友，无需给这个项目Star，请 Star [QueryDSL](https://github.com/querydsl/querydsl) 项目，各位觉得好的设计都来自QueryDSL，我只是为方便自己做了一些微不足道的修改而已。

