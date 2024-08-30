# 1. 批量写入数据指南（About Batch Insert）

> 您需要了解以下知识点。
>
> 声明：本文中提到的性能差距，仅在作者测试环境得出。实际操作由于网络开销、数据库IO性能等原因，在不同环境会有明显差距，故差距的倍数仅供参考。但倍率会变，几种不同场景下快慢的排名是不变的。

MySQL Batch写入有两种方式。

**方式1，JDBC Batch**

> 要点：由JDBC驱动来控制实现方式，在某些服务端原生支持批操作的数据库上性能极佳。

使用JDBC的Batch功能，由JDBC驱动来实现Batch，JDBC操作示例如下：

```java
String sql="insert into sample columns (id, name) values (?,?)"
try(PreparedStatement stmt = connection.prepareStatement(sql)){
    stmt.setInt(1, 1);
    stmt.setString(2,"Name1");
    stmt.addBatch();
    
    stmt.setInt(1, 2);
    stmt.setString(2,"Name2");
    stmt.addBatch();
    ... 
    stmt.executeBatch(); 
}
```

上述方法由JDBC驱动来负责对应数据库上的批操作实现。
不同数据库驱动对这种实现不一，效率不定。但大部分JDBC驱动都能以最有效的方式完成操作任务。

**方式2，Batch to Bulk**

> 要点 用一句SQL写入多条记录

将多组参数编写成一个SQL语句，JDBC操作示例如下。

```java
//这个SQL准备了10组参数，可以一次性写入10条记录
String sql="insert into sample columns (id, name) values (?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)"
try(PreparedStatement stmt = connection.prepareStatement(sql)){
    stmt.setInt(1, 1);
    stmt.setString(2,"Name1");
   
    stmt.setInt(3, 2);
    stmt.setString(4,"Name2");

    stmt.setInt(5, 3);
    stmt.setString(6,"Name3");

    stmt.setInt(7, 4);
    stmt.setString(8,"Name4");
    ... 
    stmt.execute(); 
} 
```

上述方式起到了一个SQL语句写入10条记录的效果。性能较好 ，但有以下缺点——

* 写入条数不同。SQL语句就不同，需要根据记录数变化SQL语句。对业务实现干扰较大。
* 当一批写入数据量很大时，SQL语句会非常的长，内存计算和占用也比较大，需要对批次做更精准的平衡。

### QueryDSL对上述功能支持

* QueryDSL上述两种方式都支持，并允许开发者自行控制。默认使用JDBC Batch方式。可以通过以下方式切换为Batch to Bulk方式。

  ```java
  SQLInsertClauseAlter insert=db.insert(ts);
  insert.setBatchToBulk(true);
  insert.populateBatch(infos).execute();
  ```

  在不同数据库上，建议操作如下

### Oracle

**请使用默认的Batch方式**

Oracle服务端原生支持基于绑定变量批操作。Oracle会话可以在服务端PGA中缓存SQL语句，完全不需要客户端去拼凑Bulk的长SQL语句（只会浪费性能）。
你可以使用SQLPlus等命令行工具尝试，只需要提交插入单条的SQL语句（使用绑定变量），然后逐个传入多组绑定变量，Oracle即可支持写入。

因此Oracle是Batch与逐个单条写入性能差距最大的数据库之一。Batch模式下性能可以比较单条写入高100倍以上。

### PostgreSQL

**优先考虑默认的Batch方式**

PostgreSQL两种方式下，性能差距不大。

* 实测Batch模式比Bulk模式快10%~15%，但可能随着您的版本或业务场景有所变化。
* PostgreSQL单条SQL语句最大支持65535个参数，如单批变量过多可能无法执行。

两种方式都可以使用，但更推荐使用JDBC Batch方式。

### MySQL

> MySQL是情况最复杂的一种。

**使用方言的默认值：Batch to Bulk**

MySQL服务端并非原生支持多组绑定变量参数，因此Batch模式是由客户端处理的。

在默认情况下，JDBC Batch的性能比逐条插入只快了1倍。而采用Batch to Bulk模式要比逐条插入快约20倍！因此，在MySQL上使用Batch to Bulk方式是比较好的选择，这也是为什么MyBatis的Batch实现很适合MySQL的原因。

为了让没看过这篇文档的开发者得到较好的性能，本框架在MySQL的方言中，指定默认使用Batch to Bulk模式插入数据。

默认情况下，MySQL的批量写入性能可以达到是逐条写入的20倍~30倍吞吐， 数据库特性决定了Batch与逐条插入两者无法达到100倍的以上的性能差距。

**修改JDBC URL参数：&rewriteBatchedStatements=true**

然而MySQL的JDBC驱动有非常丰富的特性，您可以在JDBC URL上增加上述参数，情况会完全改变。上述参数使得JDBC驱动主动重写SQL，执行了类似Batch to Bulk的动作。在这种情况下。使用JDBC Batch反而会更快。

因此，在为MySQL的驱动开启`rewriteBatchedStatements`特性后，将MySQL方言修改为JDBC Batch方式反而能将性能进一步提升15%以上。

当您开启构`rewriteBatchedStatements`， 建议您关闭BatchToBulk功能，即可得到这部分性能提升，方法如下——

```java
Templates templates = MySQLWithJSONTemplates.builder().usingBatchToBulkInDefault(false).build();
```



# X. 附录 性能测试数据

## 测试方式

- 均为单线程测试。会先执行一个50万左右的简单计算循环，使CPU睿频飙高。
- 日志级别到ERROR。
- 测试计时前，相同的SQL语句会先执行一遍，确保路径上的类都事先加载。
- 数据库，环境等均使用相同环境。同一组测试两个框架操作先后时间不超过5分钟。
- 每组用例跑三次，计算平均耗时（单位毫秒）
- MyBatis在SELECT的配置中设置fetchSize="5000"，QueryDSL在查询时设置setFetchSize(5000)

> 数据库访问测试耗时由两大部分构成——
>
> 1. 数据库操作和结果回传耗时 
> 1. 各个框架内部生成SQL，处理结果数据耗时
>
> 在相同环境下，第一部分耗时是基本不变的。不同框架的差异在于第二部分。
> 第一部分在总耗时的中占比可能从50%到95%，因此在不同环境下，得到的数据可能差距很大。

## on MySQL （2024 r110）

* MySQL 5.7.26
* MySQL JDBC Driver 5.1.49
* URL：postgresql://局域网地址:3306/test?useSSL=false
* jvm version=17

| Case                                                 | Mybatis 3.5.9（单位ms）                | querydsl-sql-extension<br /> 5.0.0-r110（单位ms） | A/B     |
| ---------------------------------------------------- | -------------------------------------- | ------------------------------------------------- | ------- |
| 7列的表，15批次，每批10000条。<br />总计15万记录     | 7203, 7438, 7925<br />平均 7522        | 2476, 2711, 2834<br />平均2673.67                 | 281.34% |
| 22列的表，15批次，每批10000条<br />总计15万记录      | 16939, 16870, 16782<br />平均 16863.67 | 5541, 5609, 5538<br />平均 5562.67                | 303.16% |
| 22字段表，查出50记录<br />全部加载到内存中的List内   | 12, 14, 12<br />平均12.667             | 8, 9, 10<br />平均9                               | 140.7%  |
| 22字段表，查出5000记录<br />全部加载到内存中的List内 | 646 , 601, 589<br />平均612            | 79, 75, 82<br />平均78.67                         | 777.90% |
| 22字段表，查出5万记录<br />全部加载到内存中的List内  | 935, 930, 953<br />平均939.33          | 321, 323, 339<br />平均327.67                     | 286.67% |
| 22字段表，查出30万记录<br />全部加载到内存中的List内 | 3340，3343, 3577<br />平均 3420        | 1832, 1925, 2313<br />平均 2023.33                | 169.03% |
| 22字段表，查出1M记录<br />全部加载到内存中的List内   | 8478, 9219, 7468<br />平均 8388.33     | 6571, 7535, 5271<br />平均 6459                   | 129.87% |

> 在5000条查询中出现了近8倍的差距，这还是在加上了网络开销后的数值，非常悬殊。估计可能是FetchSize=5000时，后者一次性从服务端接收了全部结果数据。而前者可能是fetchSize设置无效。在MyBatis中我是这样配置的，按DTD文件里描述的，如果这种方式不对那么请告诉我正确的方式。
>
```xml
<select id="selectRmCamera" resultType="CameraInfo"
	parameterType="string" fetchSize="5000">
 		<![CDATA[
			SELECT *
			FROM CAMERA_INFO
			LIMIT ${value,jdbcType = INTEGER}
		]]>
</select>
```

## on MYSQL (2021 r8)

* MySQL 5.7.26
* MySQL JDBC Driver 5.1.49
* jvm version=1.8.0_172

| Case | Mybatis 3.2.7（单位ms）      | querydsl-sql-extension<br /> 4.2.1-r8（单位ms） |
| ---------------------------------------------------- | ---------------------------- | ----------------------------------------------- |
| 15列的表, 写入12*5000=60000记录                     | 54869, 58057, 56317<br />平均56414 | 51417, 52626, 52187<br />平均52076.67         |
| 15列的表, 写入5*300=1500记录                        | 1738,1815, 1936, <br />平均1829.67 | 1473, 1433, 1522<br />平均1476                  |
| 15列的表, 写入5*10=50记录                           | 122, 110, 114, <br />平均115.33 | 85, 92, 83,  <br />平均86.67                |
| 15字段表, 查出30万记录<br />全部加载到内存中的List内 | 2108, 2094, 2075，<br />平均2092.33 | 1986, 1977, 2008<br />平均1990.33      |
| 15字段表, 查出5000记录<br />全部加载到内存中的List内 | 121,131,135，<br />平均129 | 79, 87, 91, <br />平均85.67                  |
| 15字段表, 查出500记录<br />全部加载到内存中的List内 | 42, 42, 31 <br />平均38.33    | 29, 33, 33, <br />平均31.67                  |
| 15字段表, 查出30记录<br />全部加载到内存中的List内  | 11, 10, 11, <br />平均10.67 | 7, 6, 7, <br />平均6.67                      |



## on PostgreSql （2024）

* PostgreSQL 10.3 3
* PostgreSQL JDBC Driver 42.7.3 3
* URL：postgresql://localhost:5432/test
* jvm version=17

> 这组测试使用本机的数据库，网络开销几乎忽略不计，因此放大了内存访问上的性能差距，所以数据会比较悬殊。

| Case                                                         | Mybatis 3.5.9（单位ms）                              | querydsl-sql-extension<br /> 5.0.0-r110（单位ms） | A/B     |
| ------------------------------------------------------------ | ---------------------------------------------------- | ------------------------------------------------- | ------- |
| 7列的表，15批次，每批10000条。<br />总计15万记录             | 11294, 11124, 11363<br />平均 11260.33               | 3450, 4319, 4078<br />平均 3949                   | 285.14% |
| 22列的表，15批次，每批10000条<br />在MyBatis上参数过多无法完成测试<br />改为50批，每批3000条<br />总计15万记录 | 50批*3000条<br />28529, 27950, 28298<br />平均 28259 | 15批*10000条<br />5352, 5290, 5507<br />平均 5383 | 524.97% |
| 22字段表，查出900K记录<br />全部加载到内存中的List内         | 5517， 5601，5470<br />平均 5529.33                  | 4086, 4038, 4107<br />平均 4077                   | 135.62% |


## Oracle

数据暂缺。


```

```