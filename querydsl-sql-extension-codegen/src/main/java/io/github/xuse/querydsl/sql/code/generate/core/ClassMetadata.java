package io.github.xuse.querydsl.sql.code.generate.core;

import java.util.List;

import com.github.xuse.querydsl.sql.column.AccessibleElement;

public interface ClassMetadata {
    String getName();
    
    String getSimpleName();

    List<AccessibleElement> getColumnFields();

}
