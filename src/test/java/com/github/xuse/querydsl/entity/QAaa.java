package com.github.xuse.querydsl.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;
import java.util.Date;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QAaa is a Querydsl query type for Aaa
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAaa extends com.querydsl.sql.RelationalPathBase<Aaa> {

    private static final long serialVersionUID = -1389588466;

    public static final QAaa aaa = new QAaa("AAA");

    public final DateTimePath<Date> created = createDateTime("created", Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public QAaa(String variable) {
        super(Aaa.class, forVariable(variable), "APP", "AAA");
        addMetadata();
    }

    public QAaa(String variable, String schema, String table) {
        super(Aaa.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAaa(String variable, String schema) {
        super(Aaa.class, forVariable(variable), schema, "AAA");
        addMetadata();
    }

    public QAaa(Path<? extends Aaa> path) {
        super(path.getType(), path.getMetadata(), "APP", "AAA");
        addMetadata();
    }

    public QAaa(PathMetadata metadata) {
        super(Aaa.class, metadata, "APP", "AAA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(created, ColumnMetadata.named("CREATED").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(9));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(64));
    }

}

