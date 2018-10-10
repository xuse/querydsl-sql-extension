#QueryDSL的增强


##目的
近日使用QueryDSL-sql作为轻量级数据库操作框架，非常清爽，有着手写SQL的畅快，又有静态语法检查的安心，还有语法自动完成的高效。
但是日志功能稍有不满，SQLDetailedListener中不能监听到所有的SQL参数、执行时间、影响记录数等信息。
对于习惯了GeeQuery详细的日志信息的我来说十分不满。
所以本着最小修改的原则，用这个项目对QueryDSL的监听行为进行扩展，从而可以得到更详细的日志信息。

##关于扩展
通过继承SQLQueryFactory并覆盖其中的方法进行扩展，被重载的类包括——
* com.querydsl.sql.SQLQuery<T>
* com.querydsl.sql.dml.SQLUpdateClause
* com.querydsl.sql.dml.SQLMergeClause
* com.querydsl.sql.dml.SQLInsertClause
* com.querydsl.sql.dml.SQLDeleteClause
从代码看，这些类的被覆盖的方法很多是原作者故意标记为protected的。从原作者的本意看，这些类的方法本身就被设计为希望被修饰和覆盖的。
因此可以认为，这种覆盖是符合作者原本的设计目的的。

##其他
还有一些其他方面的个性化要求，因此通过本项目进行扩展。后续随功能更新。