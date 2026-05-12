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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.annotation.query.PathBind;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.TypeUtils;
import com.github.xuse.querydsl.util.Util;
import com.github.xuse.querydsl.util.lang.Primitives;
import com.querydsl.core.types.Expression;

import lombok.extern.slf4j.Slf4j;

/**
 * <h2>English:</h2>
 * An enhanced projection expression that supports mapping query results to a DTO class
 * whose fields may differ from the source table in name or type.
 * <p>
 * This class uses a two-phase BeanCodec approach:
 * <ol>
 *   <li>First phase: obtain the full BeanCodec (cached) via {@link BeanCodecManager#getCodec(Class)}
 *       to collect all DTO field metadata including {@link PathBind} annotations</li>
 *   <li>Second phase: generate a targeted BeanCodec that only covers mapped fields,
 *       preserving DTO default values for unmapped fields</li>
 * </ol>
 * Additionally supports simple type conversions via {@link Function} implementations
 * declared in the {@link PathBind#readConverter()} attribute.
 * </p>
 *
 * <h2>Chinese:</h2>
 * 增强的投影表达式，支持将查询结果映射到字段名或类型与源表不同的 DTO 类。
 * <p>
 * 使用两阶段 BeanCodec 方式：
 * <ol>
 *   <li>第一阶段：通过 {@link BeanCodecManager#getCodec(Class)} 获取全量 BeanCodec（已缓存），
 *       从中读取 DTO 字段元数据及 {@link PathBind} 注解</li>
 *   <li>第二阶段：生成仅覆盖有映射字段的 BeanCodec，未映射字段保留 DTO 默认值</li>
 * </ol>
 * 同时支持通过 {@link PathBind#readConverter()} 声明的 {@link Function} 实现进行简单类型转换。
 * </p>
 *
 * @param <T> the type of target DTO
 * @author Joey
 */
@Slf4j
public class QBeanExWithConverter<T> extends QBeanEx<T> {

	private static final long serialVersionUID = 1L;


	/**
	 * The expressions that participate in SQL generation (getArgs() returns this).
	 * Order is consistent with targetCodec's field order.
	 */
	private List<Expression<?>> queryExpressions;

	/**
	 * Per-position converters aligned with queryExpressions order.
	 * Uses {@code Function.identity()} for fields that need no conversion.
	 */
	@SuppressWarnings("rawtypes")
	private Function[] converters;

	/**
	 * The BeanCodec generated for the mapped subset of DTO fields.
	 * Field order matches queryExpressions order.
	 */
	private BeanCodec targetCodec;

	/**
	 * Create a new QBeanExWithConverter instance.
	 *
	 * @param type     type of the target DTO
	 * @param bindings property name to expression bindings (from table columns)
	 */
	protected QBeanExWithConverter(Class<? extends T> type, Map<String, ? extends Expression<?>> bindings) {
		super(type, bindings);
		buildMapping(type, bindings);
	}

	@Override
	public List<Expression<?>> getArgs() {
		return queryExpressions;
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public T newInstance(Object... a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = converters[i].apply(a[i]);
		}
		return (T) targetCodec.newInstance(a);
	}

	/**
	 * Build the mapping between source table columns and target DTO fields.
	 * Uses the cached full BeanCodec to avoid redundant reflection.
	 */
	@SuppressWarnings("rawtypes")
	private void buildMapping(Class<?> dtoType, Map<String, ? extends Expression<?>> sourceBindings) {
		// Phase 1: Get the full BeanCodec (cached) to read DTO field metadata
		BeanCodec fullCodec = BeanCodecManager.getInstance().getCodec(dtoType);
		Property[] allFields = fullCodec.getFields();

		// Phase 2: For each DTO field, find the matching source expression
		List<Expression<?>> queryExprs = new ArrayList<>();
		List<Function> converterList = new ArrayList<>();
		Map<String, Expression<?>> targetBindings = new LinkedHashMap<>();

		for (Property field : allFields) {
			String fieldName = field.getName();
			// Determine source name: check @PathBind annotation
			PathBind pathBind = field.getAnnotation(PathBind.class);
			String sourceName = (pathBind != null) ? pathBind.value() : fieldName;

			Expression<?> sourceExpr = sourceBindings.get(sourceName);
			if (sourceExpr == null) {
				// No matching column in source, skip (preserve DTO default value)
				continue;
			}
			queryExprs.add(sourceExpr);
			targetBindings.put(fieldName, sourceExpr);

			// Determine converter
			Function converter = resolveConverter(dtoType, field, pathBind, sourceExpr.getType());
			converterList.add(converter);
		}

		if (queryExprs.isEmpty()) {
			throw Exceptions.illegalArgument(
					"No matching fields found between DTO [{}] and source bindings: {}",
					dtoType.getName(), sourceBindings.keySet());
		}

		// Phase 3: Generate the targeted BeanCodec
		this.queryExpressions = Collections.unmodifiableList(queryExprs);
		this.converters = converterList.toArray(new Function[0]);
		this.targetCodec = BeanCodecManager.getInstance().getCodec(dtoType, new DefaultBindingProvider(targetBindings));
	}

	/**
	 * Resolve the converter for a field mapping.
	 */
	@SuppressWarnings("rawtypes")
	private static Function resolveConverter(Class<?> dtoType, Property field, PathBind pathBind, Class<?> sourceType) {
		// If explicit read converter class is specified via @PathBind
		if (pathBind != null) {
			Class<? extends Function> converterClass = pathBind.readConverter();
			if (converterClass != Function.class) {
				return (Function) TypeUtils.newInstance(converterClass);
			}
			// If readConverterRef is specified, look up static field in DTO class
			String ref = pathBind.readConverterRef();
			if (!ref.isEmpty()) {
				return Util.getStaticFunctionField(dtoType, ref, field.getName());
			}
		}
		// Check if type conversion is needed
		Class<?> targetType = field.getType();
		if (sourceType == targetType || isAssignableWithBoxing(sourceType, targetType)) {
			return Function.identity();
		}
		// Try built-in converters
		Function builtin = BuiltinConverters.find(sourceType, targetType);
		if (builtin != null) {
			return builtin;
		}
		log.warn("No converter found for {} -> {} on field [{}]. Direct assignment will be attempted.",
				sourceType.getName(), targetType.getName(), field.getName());
		return Function.identity();
	}

	private static boolean isAssignableWithBoxing(Class<?> source, Class<?> target) {
		if (target.isAssignableFrom(source)) {
			return true;
		}
		// Handle primitive/wrapper compatibility
		Class<?> wrappedTarget = Primitives.toWrapperClass(target);
		Class<?> wrappedSource = Primitives.toWrapperClass(source);
		return wrappedTarget.isAssignableFrom(wrappedSource);
	}
}
