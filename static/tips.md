**Contents**
- [More tips](#more-tips)
  - [more about primitive types](#more-about-primitive-types)


# More tips

这是一些较为复杂的特性和机制解释，一般是作者或源码阅读者才需要看。

## more about primitive types

Using a column which is not null mapping to a primitive java file is safe, but what wIll happen when a null mapping to the primitive field? 

Primitive数值对框架的挑战来自以下方面：

| 场景                                      | 什么情况下会出现问题                                         | 此框架的解决方法                                             |
| ----------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| insert数据                                | 正常set(path,value)，columns()+values()等显式指定操作列的场合不会出现误判。<br />仅有将整个Bean作为插入数据直接赋值的方法会出现歧义，如<br />com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter.populate<br />com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter.populateBatch方法 | 使用UnsavedValue机制，判断出数值不具备业务含义，不需要写入数据库。 |
| update                                    | 正常set(path, value)等显式指定操作列的场合不会出现误判。<br />仅有将整个Bean作为更新数据直接赋值的方法会出现歧义，如<br /><br />com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter.populate<br />方法 | 使用UnsavedValue机制，判断出数值不具备业务含义，不需要写入数据库。 |
| 查询：findByExample. @ConditionBean等场合 | 正常使用where(predicate)显式指定操作列的场合不会出现误判。<br /><br />仅有将一个Bean直接作为查询或删除等条件时，才会出现歧义。 | 使用UnsavedValue机制，判断出数值不具备业务含义，不需要写入数据库。 |
| 查询返回数据                              | 如果数据库中存在null值，写入到java bean时会抛出NullPointerException。 | 一刀切，要求用户不要使用primitive字段去映射not null的数据库列。代码中内置了检查开关，如果用户尝试这样做就会在初始化抛出异常。<br /><br />如果用户非要用primitive类型去映射null数值，也不是不可以，可以使用@CustomType注解，例如@CustomType(PrimitiveIntegerType.class) 这样，也可以防止NPE出现。<br /><br />作者也尝试了其他方法也获得了成功，感觉更为复杂，似乎没有进一步介绍的必要。 |

。










[]: