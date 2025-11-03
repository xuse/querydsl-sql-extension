package io.github.xuse.querydsl.sql.code.generate;

import java.lang.reflect.Type;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.lang.Primitives;

import io.github.xuse.querydsl.sql.code.generate.CompilationUnitBuilder.AnnotationBuilder;

public class FieldCratetors {
    static class FieldGenerator{
        protected Class<?> clz;
        protected final String sqlTypeName;
        
        FieldGenerator(String sqlTypeName,Class<?> clz){
            this.sqlTypeName=sqlTypeName;
            this.clz=clz;
        }
        
        public Type getFieldType(ColumnDef c) {
            if(!c.isNullable()) {
                return Primitives.toPrimitiveClass(clz);
            }
            return clz;
        };

        public void setAttribs(AnnotationBuilder<ColumnSpec> anno, ColumnDef c) {
            CompilationUnitBuilder cu=anno.getParent();
            anno.add("name", cu.literal(c.getColumnName()));
            anno.add("type", cu.createFieldAccess(Types.class, sqlTypeName));
            if(!c.isNullable()) {
                anno.add("nullable", cu.literal(false));
            }
            if(c.getColumnDef()!=null && c.getColumnDef().length()>0) {
                anno.add("defaultValue", cu.literal(c.getColumnDef()));
            }
            if(c.isAutoIncrement()) {
                anno.add("autoIncrement", cu.literal(true));
            }
        };
    }
    
    static class ObjectGenerator extends FieldGenerator{
        ObjectGenerator(String sqlTypeName) {
            super(sqlTypeName,Object.class);
        }
    };
    
    static class NumberGenerator extends FieldGenerator{
        private Class<?> clzIfNoDigits;
        
        NumberGenerator(String sqlTypeName,Class<?> clz) {
            this(sqlTypeName,clz,null);
        }
        
        NumberGenerator(String sqlTypeName,Class<?> clz,Class<?> ifNoDigits) {
            super(sqlTypeName,clz);
            this.clzIfNoDigits=ifNoDigits;
        }
        
        @Override
        public Type getFieldType(ColumnDef c) {
            if(clzIfNoDigits!=null) {
                if(c.getDecimalDigit()==0) {
                    return clzIfNoDigits;
                }
            }
            return super.getFieldType(c);
        }

        @Override
        public void setAttribs(AnnotationBuilder<ColumnSpec> anno, ColumnDef c) {
            super.setAttribs(anno, c);
            CompilationUnitBuilder cu=anno.getParent();
            anno.add("size", cu.literal(c.getColumnSize()));
            if(SQLTypeUtils.hasDigits(c.getJdbcType())) {
                anno.add("digits",cu.literal(c.getColumnSize()));
            }
            if(c.getDataType().toUpperCase().contains("UNSIGNED")) {
                anno.add("unsigned", cu.literal(true));
            }
        }
    }
    static class TimeGenerator extends FieldGenerator{
        TimeGenerator(String sqlTypeName,Class<?> clz) {
            super(sqlTypeName,clz);
        }
        @Override
        public void setAttribs(AnnotationBuilder<ColumnSpec> anno, ColumnDef c) {
            super.setAttribs(anno, c);
            CompilationUnitBuilder cu=anno.getParent();
            if(c.getColumnSize()>0) {
                anno.add("size", cu.literal(c.getColumnSize()));
            }
        }
    }
    
    
    
    private static final Map<Integer,FieldGenerator> creators = new HashMap<>();
    
    static {
        creators.put(Types.ARRAY, new ObjectGenerator("ARRAY"));
        creators.put(Types.BIGINT, new NumberGenerator("BIGINT", Long.class));
        creators.put(Types.BINARY, new FieldGenerator("BINARY", byte[].class));
        creators.put(Types.BIT, new FieldGenerator("BIT", Boolean.class));
        creators.put(Types.BLOB, new FieldGenerator("BLOB", Object.class));
        creators.put(Types.BOOLEAN, new FieldGenerator("BOOLEAN",Boolean.class));
        
        creators.put(Types.CHAR, new FieldGenerator("CHAR",String.class));
        creators.put(Types.CLOB, new FieldGenerator("CLOB",String.class));
        creators.put(Types.DATALINK, new ObjectGenerator("DATALINK"));
        creators.put(Types.DATE, new FieldGenerator("DATE", Date.class));
        creators.put(Types.DECIMAL, new NumberGenerator("DECIMAL",Double.class,Long.class));
        creators.put(Types.DISTINCT,  new ObjectGenerator("DISTINCT"));
        creators.put(Types.DOUBLE,  new NumberGenerator("DOUBLE",Double.class));
        
        creators.put(Types.FLOAT, new NumberGenerator("FLOAT",Float.class));
        creators.put(Types.INTEGER, new NumberGenerator("INTEGER",Integer.class));
        creators.put(Types.JAVA_OBJECT,  new ObjectGenerator("JAVA_OBJECT"));
        creators.put(Types.LONGNVARCHAR, new FieldGenerator("LONGNVARCHAR",String.class));
        creators.put(Types.LONGVARBINARY, new FieldGenerator("LONGVARBINARY",Object.class));
        creators.put(Types.LONGVARCHAR, new FieldGenerator("LONGVARCHAR",String.class));
        
        creators.put(Types.NCHAR, new FieldGenerator("NCHAR",String.class));
        creators.put(Types.NCLOB, new FieldGenerator("NCLOB",String.class));
        creators.put(Types.NULL, new FieldGenerator("NULL",Object.class));
        creators.put(Types.NUMERIC, new NumberGenerator("NUMERIC",Double.class,Long.class));
        creators.put(Types.NVARCHAR, new FieldGenerator("NVARCHAR",String.class));
        
        
        creators.put(Types.OTHER, new FieldGenerator("OTHER",Object.class));
        creators.put(Types.REAL, new NumberGenerator("REAL",Double.class,Long.class));
        creators.put(Types.REF, new FieldGenerator("REF",Object.class));
        creators.put(Types.REF_CURSOR, new FieldGenerator("REF_CURSOR",String.class));
        creators.put(Types.ROWID, new FieldGenerator("ROWID",String.class));
        
        
        creators.put(Types.SMALLINT,  new FieldGenerator("SMALLINT",Integer.class));
        creators.put(Types.SQLXML, new FieldGenerator("SQLXML",String.class));
        creators.put(Types.STRUCT,  new FieldGenerator("STRUCT",String.class));
        creators.put(Types.TIME,  new FieldGenerator("TIME",java.sql.Time.class));
        creators.put(Types.TIME_WITH_TIMEZONE, new FieldGenerator("TIME_WITH_TIMEZONE",LocalTime.class));
        creators.put(Types.TIMESTAMP, new FieldGenerator("TIMESTAMP",Date.class));
        creators.put(Types.TIMESTAMP_WITH_TIMEZONE, new FieldGenerator("TIMESTAMP_WITH_TIMEZONE",LocalDateTime.class));
        creators.put(Types.TINYINT, new NumberGenerator("TINYINT",Integer.class));
        creators.put(Types.VARBINARY, new FieldGenerator("VARBINARY",byte[].class));
        creators.put(Types.VARCHAR, new FieldGenerator("VARCHAR",String.class));
        
    }

    public static FieldGenerator getGenerator(int type) {
        FieldGenerator g=creators.get(type);
        if(g!=null) {
            return g;
        }
        throw Exceptions.unsupportedOperation("sqlType:{}",type);
    }
}
