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
package com.github.xuse.querydsl.sql.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.Templates;
import com.querydsl.core.types.Visitor;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPath;

public final class ToStringVisitor2 implements Visitor<String, Templates> {

	public static final ToStringVisitor2 DEFAULT = new ToStringVisitor2(Collections.singletonMap(PathType.PROPERTY, TemplateFactory.DEFAULT.create("{1s}")));

	private Map<Operator, Template> overrideTemplates;

	private ToStringVisitor2(Map<Operator, Template> overrideTemplates) {
		this.overrideTemplates = overrideTemplates;
	}

	@Override
	public String visit(Constant<?> e, Templates templates) {
		return e.getConstant().toString();
	}

	@Override
	public String visit(FactoryExpression<?> e, Templates templates) {
		final StringBuilder builder = new StringBuilder();
		builder.append("new ").append(e.getType().getSimpleName()).append("(");
		boolean first = true;
		for (Expression<?> arg : e.getArgs()) {
			if (!first) {
				builder.append(", ");
			}
			builder.append(arg.accept(this, templates));
			first = false;
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public String visit(Operation<?> o, Templates templates) {
		final Template template = getTemplate(o.getOperator(), templates);
		if (template != null) {
			final int precedence = templates.getPrecedence(o.getOperator());
			final StringBuilder builder = new StringBuilder();
			for (Template.Element element : template.getElements()) {
				final Object rv = element.convert(o.getArgs());
				if (rv instanceof Expression) {
					if (precedence > -1 && rv instanceof Operation) {
						if (precedence < templates.getPrecedence(((Operation<?>) rv).getOperator())) {
							builder.append("(");
							builder.append(((Expression<?>) rv).accept(this, templates));
							builder.append(")");
							continue;
						}
					}
					builder.append(((Expression<?>) rv).accept(this, templates));
				} else {
					builder.append(rv.toString());
				}
			}
			return builder.toString();
		} else {
			return "unknown operation with operator " + o.getOperator().name() + " and args " + o.getArgs();
		}
	}

	@Override
	public String visit(ParamExpression<?> param, Templates templates) {
		return "{" + param.getName() + "}";
	}

	@Override
	public String visit(Path<?> p, Templates templates) {
		final Path<?> parent = p.getMetadata().getParent();
		final Object elem = p.getMetadata().getElement();
		Template pattern = getTemplate(p.getMetadata().getPathType(),templates);
		if (pattern == null) {
			return "";
		}
		if (parent != null) {
			String columnName = null;
			if (parent instanceof RelationalPath<?>) {
				ColumnMetadata column = ((RelationalPath<?>) parent).getMetadata(p);
				columnName = column.getName();
			}
			final List<?> args = Arrays.asList(parent, columnName == null ? elem : columnName);
			final StringBuilder builder = new StringBuilder();
			for (Template.Element element : pattern.getElements()) {
				Object rv = element.convert(args);
				if (rv instanceof Expression) {
					builder.append(((Expression<?>) rv).accept(this, templates));
				} else {
					builder.append(rv.toString());
				}
			}
			return builder.toString();
		} else {
			return elem.toString();
		}
	}

	private Template getTemplate(Operator op, Templates templates) {
		Template t = overrideTemplates.get(op);
		return t == null ? templates.getTemplate(op) : t;
	}

	@Override
	public String visit(SubQueryExpression<?> expr, Templates templates) {
		return expr.getMetadata().toString();
	}

	@Override
	public String visit(TemplateExpression<?> expr, Templates templates) {
		final StringBuilder builder = new StringBuilder();
		for (Template.Element element : expr.getTemplate().getElements()) {
			Object rv = element.convert(expr.getArgs());
			if (rv instanceof Expression) {
				builder.append(((Expression<?>) rv).accept(this, templates));
			} else {
				builder.append(rv.toString());
			}
		}
		return builder.toString();
	}

}
