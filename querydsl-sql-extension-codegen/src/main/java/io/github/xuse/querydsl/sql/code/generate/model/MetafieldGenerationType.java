package io.github.xuse.querydsl.sql.code.generate.model;

/**
 * 指定表/字段引用的生成模式
 */
public enum MetafieldGenerationType {
    /**
     * 不生成，选择此模式后。将只生成POJO类型的实体类
     */
    NONE,
    /**
     * 生成QueryClass，即原生QueryDSL模式。该模式下，在POJO实体之外还会有一个Q开头的查询引用类。
     */
    QCLASS,
    /**
     * 简化的Lambda引用。选择此模式后，在POJO类中，会增加静态常量，作为查询引用。
     */
    LAMBDA
}
