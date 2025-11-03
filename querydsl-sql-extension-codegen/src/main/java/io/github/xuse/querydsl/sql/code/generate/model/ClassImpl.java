package io.github.xuse.querydsl.sql.code.generate.model;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.sql.column.AccessibleElement;
import com.github.xuse.querydsl.sql.column.FieldImpl;
import com.github.xuse.querydsl.util.TypeUtils;
import com.querydsl.sql.Column;

public class ClassImpl implements ClassMetadata {

    private final Class<?> clazz;

    public ClassImpl(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public String getSimpleName() {
        return clazz.getSimpleName();
    }

    @Override
    public List<AccessibleElement> getColumnFields() {
        return TypeUtils.getAllDeclaredFields(clazz).stream()
                .filter(f -> !Modifier.isStatic(f.getModifiers())
                        && (f.getAnnotation(Column.class) != null || f.getAnnotation(ColumnSpec.class) != null))
                .map(f -> new FieldImpl(f)).collect(Collectors.toList());
    }

}
