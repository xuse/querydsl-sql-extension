/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.util.FastHashtable;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.util.ArrayUtils;
import com.querydsl.core.util.CollectionUtils;
import com.querydsl.sql.RelationalPath;

/**
 * Expression used to project a list of beans
 *
 * @author Origin author luis(QueryDSL), revised by Joey.
 * 
 */
public class QAliasBeansContinuous extends FactoryExpressionBase<AliasMapBeans> {

    private static final long serialVersionUID = -4411839816134215923L;

    private final Map<RelationalPath<?>, QBeanEx<?>> qBeans;

    private final List<Expression<?>> expressions;

    public QAliasBeansContinuous(RelationalPath<?>... beanPaths) {
    	this(Arrays.asList(beanPaths));
    }
    
    public QAliasBeansContinuous(List<RelationalPath<?>> beanPaths) {
        super(AliasMapBeans.class);
        try {
            final List<Expression<?>> listBuilder = new ArrayList<>();
            final Map<RelationalPath<?>, QBeanEx<?>> mapBuilder = new HashMap<>();
            for (RelationalPath<?> path : beanPaths) {
            	List<Path<?>> paths=path.getColumns();
                Map<String, Expression<?>> bindings = new FastHashtable<Expression<?>>(paths.size());
                for (Path<?> column : paths) {
                    bindings.put(column.getMetadata().getName(), column);
                    listBuilder.add(column);
                }
                mapBuilder.put(path, ProjectionsAlter.bean(path.getType(), bindings));
            }
            expressions = CollectionUtils.unmodifiableList(listBuilder);
            qBeans = Collections.unmodifiableMap(mapBuilder);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public List<Expression<?>> getArgs() {
        return expressions;
    }

    @Override
    public AliasMapBeans newInstance(Object... args) {
        int offset = 0;
        Map<String, Object> beans = new HashMap<>();
        for (Map.Entry<RelationalPath<?>, QBeanEx<?>> entry : qBeans.entrySet()) {
            RelationalPath<?> path = entry.getKey();
            QBeanEx<?> qBean = entry.getValue();
            int argsSize = qBean.getArgs().size();
            Object[] subArgs = ArrayUtils.subarray(args, offset, offset + argsSize);
            beans.put(path.getMetadata().getName(), qBean.newInstance(subArgs));
            offset += argsSize;
        }
        return new AliasMapBeans(beans);
    }

}