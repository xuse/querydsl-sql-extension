package com.querydsl.core.types.dsl;

import java.lang.reflect.AnnotatedElement;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.Visitor;

public class PrimitiveBooleanPath extends BooleanExpression implements Path<Boolean> {

    private static final long serialVersionUID = 6590516706769430565L;

    private final PathImpl<Boolean> pathMixin;

     protected PrimitiveBooleanPath(Path<?> parent, String property) {
        this(PathMetadataFactory.forProperty(parent, property));
    }

    protected PrimitiveBooleanPath(PathMetadata metadata) {
        super(ExpressionUtils.path(Boolean.TYPE, metadata));
        this.pathMixin = (PathImpl<Boolean>) mixin;
    }

    protected PrimitiveBooleanPath(String var) {
        this(PathMetadataFactory.forVariable(var));
    }

    @Override
    public final <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public PathMetadata getMetadata() {
        return pathMixin.getMetadata();
    }

    @Override
    public Path<?> getRoot() {
        return pathMixin.getRoot();
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return pathMixin.getAnnotatedElement();
    }

}