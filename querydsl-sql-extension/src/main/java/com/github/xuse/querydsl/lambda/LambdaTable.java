package com.github.xuse.querydsl.lambda;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Visitor;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.SchemaAndTable;

public interface LambdaTable<T> extends Supplier<Class<T>>,RelationalPathEx<T>{
	
	default BeanCodec getBeanCodec(){
		return PathCache.getPath(this, null).getBeanCodec();
	}

	default ColumnMapping getColumnMetadata(Path<?> path){
		return PathCache.getPath(this, null).getColumnMetadata(path);
	}

	default Path<?> getColumn(String name){
		return PathCache.getPath(this, null).getColumn(name);
	}

	default Collection<Constraint> getConstraints(){
		return PathCache.getPath(this, null).getConstraints();
	}

	default Collate getCollate(){
		return PathCache.getPath(this, null).getCollate();
	}

	default String getComment(){
		return PathCache.getPath(this, null).getComment();
	}

	default PartitionBy getPartitionBy(){
		return PathCache.getPath(this, null).getPartitionBy();
	}

	default InitializeData getInitializeData(){
		return PathCache.getPath(this, null).getInitializeData();
	}

	default  SchemaAndTable getSchemaAndTable(){
		return PathCache.getPath(this, null).getSchemaAndTable();
	}

	default String getSchemaName(){
		return PathCache.getPath(this, null).getSchemaName();
	}

	default String getTableName(){
		return PathCache.getPath(this, null).getTableName();
	}

	default List<Path<?>> getColumns(){
		return PathCache.getPath(this, null).getColumns();
	}
	
	default List<ColumnMapping> getAutoColumns(){
		return PathCache.getPath(this, null).getAutoColumns();
	}

    default PrimaryKey<T> getPrimaryKey(){
    	return PathCache.getPath(this, null).getPrimaryKey();
    }

    default Collection<ForeignKey<?>> getForeignKeys(){
    	return PathCache.getPath(this, null).getForeignKeys();
    }

    default Collection<ForeignKey<?>> getInverseForeignKeys(){
    	return PathCache.getPath(this, null).getInverseForeignKeys();
    }

    default ColumnMetadata getMetadata(Path<?> column){
    	return PathCache.getPath(this, null).getMetadata(column);
    }
    
    
    default  Expression<T> getProjection(){
    	return PathCache.getPath(this, null).getProjection();
    }

    default PathMetadata getMetadata(){
    	return PathCache.getPath(this, null).getMetadata();
    }

    default Path<?> getRoot(){
    	return PathCache.getPath(this, null).getRoot();
    }
    
    default <R,C> R accept(Visitor<R,C> v, C context) {
    	return v.visit(PathCache.getPath(this, null), context);
    }
    
    default Class<? extends T> getType(){
    	return PathCache.getPath(this, null).getType();
    }
    
    default AnnotatedElement getAnnotatedElement(){
    	return PathCache.getPath(this, null).getAnnotatedElement();
    }
    
    default RelationalPathExImpl<T> forVariable(String variable){
    	RelationalPathEx<T> result=PathCache.getPath(this, variable);
    	if (result instanceof RelationalPathExImpl) {
			return (RelationalPathExImpl<T>) result;
		}
    	return ((RelationalPathBaseEx<T>)result).clone();
    }
}
