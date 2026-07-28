package com.github.xuse.querydsl.datatype;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.alibaba.fastjson.JSON;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 扩展类型：基于 fastjson 进行 JSON 序列化与反序列化的数据库类型映射。
 * <p>
 * 本类为原 {@code JSONObjectType} 的重命名，功能不变。使用 {@code com.alibaba.fastjson} 作为 JSON 引擎。
 *
 * <h3>迁移到 {@link JacksonJsonType} 的说明</h3>
 * <p>已对齐的行为（可直接替换，无需改动业务代码）：
 * <ul>
 *   <li>Date 序列化：两者默认都输出毫秒时间戳</li>
 *   <li>Date 反序列化：{@link JacksonJsonType} 已注册 FlexibleDateDeserializer，
 *       支持时间戳、yyyy-MM-dd、yyyy-MM-dd HH:mm:ss 等多种格式自动适配</li>
 *   <li>null 字段处理：两者默认都不输出值为 null 的字段</li>
 *   <li>未知字段容忍：两者反序列化时都忽略 JSON 中多余的字段</li>
 * </ul>
 *
 * <p>需要用户自行迁移的部分：
 * <ul>
 *   <li>{@code @JSONField} 注解：{@link JacksonJsonType} 可通过反射自动识别
 *       {@code io.github.xuse.fastjson.adapter.JSONField} 注解（无需硬依赖 fastjson-over-jackson）。
 *       如果 POJO 上使用的是 {@code com.alibaba.fastjson.annotation.JSONField}（真实 fastjson 注解），
 *       则需替换为 {@code io.github.xuse.fastjson.adapter.JSONField} 或 Jackson 对应注解
 *       （{@code @JsonProperty}、{@code @JsonIgnore} 等）。</li>
 * </ul>
 *
 * @author Administrator
 * @param <T> type of target
 * @see JacksonJsonType
 * @see com.github.xuse.querydsl.datatype.json
 */
public class FastJsonObjectType<T> extends AbstractType<T> {

	/**
	 * 使用Class构造
	 * @param clz clz
	 */
	public FastJsonObjectType(Class<T> clz) {
		super(Types.VARCHAR);
		this.clz = clz;
		this.type = clz;
	}

	/**
	 * 使用Type构造
	 * @param clz clz
	 */
	public FastJsonObjectType(Type clz) {
		super(Types.VARCHAR);
		this.clz = null;
		this.type = clz;
	}

	private final Class<T> clz;

	private final Type type;

	@Override
	public Class<T> getReturnedClass() {
		return clz;
	}

	@Override
	public T getValue(ResultSet rs, int startIndex) throws SQLException {
		String s = rs.getString(startIndex);
		if (StringUtils.isEmpty(s)) {
			return null;
		} else {
			return parse(s);
		}
	}

	private T parse(String s) {
		return clz == null ? JSON.parseObject(s, type) : JSON.parseObject(s, clz);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, T value) throws SQLException {
		if (value == null) {
			st.setNull(startIndex, Types.VARCHAR);
		} else {
			st.setString(startIndex, JSON.toJSONString(value));
		}
	}
}
