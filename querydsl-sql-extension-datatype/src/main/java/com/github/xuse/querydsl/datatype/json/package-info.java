/**
 * Jackson JSON 序列化/反序列化支持组件，为 {@link com.github.xuse.querydsl.datatype.JacksonJsonType} 提供底层能力。
 * <p>
 * 本包的设计目标是让 {@code JacksonJsonType} 在行为上尽可能兼容 fastjson 的默认表现，
 * 同时不产生对 fastjson 或 fastjson-over-jackson 的编译期硬依赖。
 *
 * <h3>包含组件</h3>
 * <ul>
 *   <li>{@link com.github.xuse.querydsl.datatype.json.FlexibleDateDeserializer}
 *       — 灵活的 Date 反序列化器，自动识别时间戳、yyyy-MM-dd、ISO 8601 等多种日期格式。</li>
 *   <li>{@link com.github.xuse.querydsl.datatype.json.JSONFieldAnnotationIntrospector}
 *       — 通过反射识别 {@code io.github.xuse.fastjson.adapter.JSONField} 注解，
 *       处理字段名映射（name）、别名（alternateNames）和日期格式（format）。</li>
 *   <li>{@link com.github.xuse.querydsl.datatype.json.JSONFieldCompatModule}
 *       — Jackson Module，通过反射处理 {@code @JSONField} 的 serialize/deserialize 属性，
 *       实现属性级别的序列化/反序列化排除。</li>
 * </ul>
 *
 * <h3>条件生效机制</h3>
 * <p>
 * {@code JSONFieldAnnotationIntrospector} 和 {@code JSONFieldCompatModule} 在类加载时通过
 * {@code Class.forName} 检测 {@code io.github.xuse.fastjson.adapter.JSONField} 是否存在：
 * <ul>
 *   <li>存在 → 自动注册，POJO 上的 {@code @JSONField} 注解生效</li>
 *   <li>不存在 → 静默跳过，不影响正常的 Jackson 注解使用</li>
 * </ul>
 */
package com.github.xuse.querydsl.datatype.json;
