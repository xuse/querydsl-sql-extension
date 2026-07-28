package com.github.xuse.querydsl.datatype;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.xuse.querydsl.datatype.json.FlexibleDateDeserializer;
import com.github.xuse.querydsl.datatype.json.JSONFieldAnnotationIntrospector;
import com.github.xuse.querydsl.datatype.json.JSONFieldCompatModule;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 基于 Jackson 的 JSON 类型映射器，用于将 Java 对象以 JSON 字符串形式存取数据库 VARCHAR 列。
 * <p>
 * 行为说明：
 * <ul>
 *   <li>写入时：将 Java 对象序列化为 JSON 字符串，存入 VARCHAR 列；null 值写入 SQL NULL。</li>
 *   <li>读取时：从 VARCHAR 列读取 JSON 字符串，反序列化为目标类型；空字符串或 NULL 返回 null。</li>
 *   <li>反序列化时忽略 JSON 中未知属性（不会因为数据库中存储的 JSON 多了字段而报错）。</li>
 *   <li>日期字段反序列化时自动适配多种格式（时间戳、yyyy-MM-dd、ISO 8601 等），
 *       由 {@link FlexibleDateDeserializer} 提供支持。</li>
 * </ul>
 *
 * <p>注意：本类默认识别 Jackson 原生注解（如 {@code @JsonProperty}、{@code @JsonIgnore}）。
 * 此外，如果类路径上存在 {@code io.github.xuse.fastjson.adapter.JSONField} 注解（来自 fastjson-over-jackson），
 * 则自动通过反射识别该注解，无需显式引入 fastjson-over-jackson 依赖。
 * 若类路径上不存在该注解，则静默忽略，不影响正常使用。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 通过 Class 构造
 * new JacksonJsonType<>(MyPojo.class)
 *
 * // 通过 Type 构造（支持泛型，如 List<MyPojo>）
 * new JacksonJsonType<>(new TypeReference<List<MyPojo>>(){}.getType())
 * }</pre>
 *
 * @param <T> 目标 Java 类型
 */
public class JacksonJsonType<T> extends AbstractType<T> {

	/**
	 * 全局共享的 ObjectMapper 实例。
	 * <p>
	 * 配置：
	 * <ul>
	 *   <li>FAIL_ON_UNKNOWN_PROPERTIES = false：反序列化时忽略 JSON 中多余的字段</li>
	 *   <li>SerializationInclusion = NON_NULL：序列化时不输出 null 字段，与 fastjson 默认行为一致</li>
	 *   <li>注册 FlexibleDateDeserializer：自动识别多种日期格式进行解析</li>
	 *   <li>条件注册 JSONFieldAnnotationIntrospector：若类路径上存在 @JSONField 注解则自动识别</li>
	 *   <li>条件注册 JSONFieldCompatModule：处理 @JSONField 的 serialize/deserialize 属性过滤</li>
	 * </ul>
	 */
	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		SimpleModule module = new SimpleModule("FlexibleDate");
		module.addDeserializer(Date.class, new FlexibleDateDeserializer());
		MAPPER.registerModule(module);
		// 如果类路径上存在 @JSONField 注解，自动注册对应的 AnnotationIntrospector 和 Module
		if (JSONFieldAnnotationIntrospector.isAvailable()) {
			AnnotationIntrospector defaultAi = MAPPER.getSerializationConfig().getAnnotationIntrospector();
			AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(defaultAi, new JSONFieldAnnotationIntrospector());
			MAPPER.setAnnotationIntrospector(pair);
			MAPPER.registerModule(new JSONFieldCompatModule());
		}
	}

	/** 目标类型的 Class 对象，通过 Class 构造时有值；通过 Type 构造时为 null */
	private final Class<T> clz;

	/** 目标类型（支持泛型），始终有值 */
	private final Type type;

	/**
	 * 使用 Class 构造。适用于非泛型的简单类型。
	 *
	 * @param clz 目标 Java 类
	 */
	public JacksonJsonType(Class<T> clz) {
		super(Types.VARCHAR);
		this.clz = clz;
		this.type = clz;
	}

	/**
	 * 使用 Type 构造。适用于泛型类型（如 {@code List<Foo>}、{@code Map<String, Bar>}）。
	 * <p>
	 * 注意：使用此构造器时 {@link #getReturnedClass()} 返回 null。
	 *
	 * @param clz 目标 Java 泛型类型
	 */
	public JacksonJsonType(Type clz) {
		super(Types.VARCHAR);
		this.clz = null;
		this.type = clz;
	}

	@Override
	public Class<T> getReturnedClass() {
		return clz;
	}

	/**
	 * 从 ResultSet 中读取 JSON 字符串并反序列化为目标对象。
	 * <p>
	 * 如果数据库值为 NULL 或空字符串，返回 null。
	 */
	@Override
	public T getValue(ResultSet rs, int startIndex) throws SQLException {
		String s = rs.getString(startIndex);
		if (StringUtils.isEmpty(s)) {
			return null;
		} else {
			return parse(s);
		}
	}

	/**
	 * 将 JSON 字符串反序列化为目标类型。
	 *
	 * @param s JSON 字符串，非空
	 * @return 反序列化后的对象
	 * @throws IllegalArgumentException 如果 JSON 解析失败
	 */
	private T parse(String s) {
		try {
			if (clz != null) {
				return MAPPER.readValue(s, clz);
			} else {
				JavaType javaType = MAPPER.getTypeFactory().constructType(type);
				return MAPPER.readValue(s, javaType);
			}
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to parse JSON: " + e.getMessage(), e);
		}
	}

	/**
	 * 将目标对象序列化为 JSON 字符串并写入 PreparedStatement。
	 * <p>
	 * 如果 value 为 null，写入 SQL NULL。
	 */
	@Override
	public void setValue(PreparedStatement st, int startIndex, T value) throws SQLException {
		if (value == null) {
			st.setNull(startIndex, Types.VARCHAR);
		} else {
			try {
				st.setString(startIndex, MAPPER.writeValueAsString(value));
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException("Failed to serialize to JSON: " + e.getMessage(), e);
			}
		}
	}
}
