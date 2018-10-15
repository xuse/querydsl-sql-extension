package com.github.xuse.querydsl.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAvsUserAuthority is a Querydsl query type for AvsUserAuthority
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAvsUserAuthority extends com.querydsl.sql.RelationalPathBase<AvsUserAuthority> {

    private static final long serialVersionUID = 1931074253;

    public static final QAvsUserAuthority avsUserAuthority = new QAvsUserAuthority("AVS_USER_AUTHORITY");

    public final StringPath authContent = createString("authContent");

    public final NumberPath<Integer> authType = createNumber("authType", Integer.class);

    public final NumberPath<Integer> channelNo = createNumber("channelNo", Integer.class);

    public final DateTimePath<java.sql.Timestamp> createTime = createDateTime("createTime", java.sql.Timestamp.class);

    public final StringPath devId = createString("devId");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updateTime = createDateTime("updateTime", java.sql.Timestamp.class);

    public final StringPath userId = createString("userId");

    public final com.querydsl.sql.PrimaryKey<AvsUserAuthority> sql181012120126200 = createPrimaryKey(id);

    public QAvsUserAuthority(String variable) {
        super(AvsUserAuthority.class, forVariable(variable), "APP", "AVS_USER_AUTHORITY");
        addMetadata();
    }

    public QAvsUserAuthority(String variable, String schema, String table) {
        super(AvsUserAuthority.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAvsUserAuthority(String variable, String schema) {
        super(AvsUserAuthority.class, forVariable(variable), schema, "AVS_USER_AUTHORITY");
        addMetadata();
    }

    public QAvsUserAuthority(Path<? extends AvsUserAuthority> path) {
        super(path.getType(), path.getMetadata(), "APP", "AVS_USER_AUTHORITY");
        addMetadata();
    }

    public QAvsUserAuthority(PathMetadata metadata) {
        super(AvsUserAuthority.class, metadata, "APP", "AVS_USER_AUTHORITY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authContent, ColumnMetadata.named("AUTH_CONTENT").withIndex(6).ofType(Types.VARCHAR).withSize(256));
        addMetadata(authType, ColumnMetadata.named("AUTH_TYPE").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(channelNo, ColumnMetadata.named("CHANNEL_NO").withIndex(4).ofType(Types.INTEGER).withSize(10));
        addMetadata(createTime, ColumnMetadata.named("CREATE_TIME").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(9).notNull());
        addMetadata(devId, ColumnMetadata.named("DEV_ID").withIndex(3).ofType(Types.VARCHAR).withSize(64));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(updateTime, ColumnMetadata.named("UPDATE_TIME").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(9).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.VARCHAR).withSize(64).notNull());
    }

}

