package com.github.xuse.querydsl.entity;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QSeataStateMachineInst is a Querydsl query type for SeataStateMachineInst
 */
@InitializeData(charset = "utf-8",enable = true,mergeKeys = "id")
public class QSeataStateMachineInst extends RelationalPathBaseEx<SeataStateMachineInst> {

    private static final long serialVersionUID = 384474501;
    
    public final StringPath id = createString("id");

    public static final QSeataStateMachineInst seataStateMachineInst = new QSeataStateMachineInst("seata_state_machine_inst");

    public final StringPath businessKey = createString("businessKey");

    public final StringPath compensationStatus = createString("compensationStatus");

    public final StringPath endParams = createString("endParams");

    public final SimplePath<byte[]> excep = createSimple("excep", byte[].class);

    public final DateTimePath<Date> gmtEnd = createDateTime("gmtEnd", Date.class);

    public final DateTimePath<Date> gmtStarted = createDateTime("gmtStarted", Date.class);

    public final DateTimePath<Date> gmtUpdated = createDateTime("gmtUpdated", Date.class);

    public final NumberPath<Integer> isRunning = createNumber("isRunning", Integer.class);

    public final StringPath machineId = createString("machineId");

    public final StringPath parentId = createString("parentId");

    public final StringPath startParams = createString("startParams");

    public final StringPath status = createString("status");

    public final StringPath tenantId = createString("tenantId");

    public QSeataStateMachineInst(String variable) {
        super(SeataStateMachineInst.class, forVariable(variable), "null", "seata_state_machine_inst");
        addMetadata();
    }

    public QSeataStateMachineInst(String variable, String schema, String table) {
        super(SeataStateMachineInst.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSeataStateMachineInst(String variable, String schema) {
        super(SeataStateMachineInst.class, forVariable(variable), schema, "seata_state_machine_inst");
        addMetadata();
    }

    public QSeataStateMachineInst(Path<? extends SeataStateMachineInst> path) {
        super(path.getType(), path.getMetadata(), "null", "seata_state_machine_inst");
        addMetadata();
    }

    public QSeataStateMachineInst(PathMetadata metadata) {
        super(SeataStateMachineInst.class, metadata, "null", "seata_state_machine_inst");
        addMetadata();
    }

    public void addMetadata() {
    	addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.VARCHAR).withSize(128).notNull());
    	addMetadata(machineId, ColumnMetadata.named("machine_id").withIndex(2).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(businessKey, ColumnMetadata.named("business_key").withIndex(6).ofType(Types.VARCHAR).withSize(128));
        addMetadata(compensationStatus, ColumnMetadata.named("compensation_status").withIndex(12).ofType(Types.VARCHAR).withSize(32));
        addMetadata(endParams, ColumnMetadata.named("end_params").withIndex(10).ofType(Types.LONGVARCHAR).withSize(5000));
        addMetadata(excep, ColumnMetadata.named("excep").withIndex(9).ofType(Types.LONGVARBINARY).withSize(5000));
        addMetadata(gmtEnd, ColumnMetadata.named("gmt_end").withIndex(8).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(gmtStarted, ColumnMetadata.named("gmt_started").withIndex(5).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(gmtUpdated, ColumnMetadata.named("gmt_updated").withIndex(14).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(isRunning, ColumnMetadata.named("is_running").withIndex(13).ofType(Types.TINYINT).withSize(3)).withUnsavePredicate(UnsavedValue.ZeroAndMinus);
        addMetadata(parentId, ColumnMetadata.named("parent_id").withIndex(4).ofType(Types.VARCHAR).withSize(128));
        addMetadata(startParams, ColumnMetadata.named("start_params").withIndex(7).ofType(Types.LONGVARCHAR).withSize(5000));
        addMetadata(status, ColumnMetadata.named("status").withIndex(11).ofType(Types.VARCHAR).withSize(32));
        addMetadata(tenantId, ColumnMetadata.named("tenant_id").withIndex(3).ofType(Types.VARCHAR).withSize(128).notNull());
    }

}

