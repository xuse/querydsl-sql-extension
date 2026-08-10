package io.github.xuse.fastjson.adapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

/**
 * 反序列化兜底：按 fastjson 的规则模糊匹配属性名。
 * <p>
 * 对齐 fastjson 1.2.83 {@code JavaBeanDeserializer.smartMatch}，它在精确匹配
 * （{@code getFieldDeserializer}）失败后依次尝试两种归一化 hash：
 * <ul>
 * <li>{@code TypeUtils.fnv1a_64_lower}：仅转小写</li>
 * <li>{@code TypeUtils.fnv1a_64_extract}：剔除下划线 {@code _} 与连字符 {@code -} 后转小写</li>
 * </ul>
 * 属性侧的 hash 由 {@code FieldInfo.nameHashCode64} 决定：未标注 {@code @JSONField(name=...)}
 * 的属性用 {@code extract}（剔除分隔符），标注了的用 {@code lower}（保留分隔符）。
 * 本类以 {@code extract} 语义建表，覆盖绝大多数场景：
 * <pre>
 * Java 字段            JSON key           结果
 * TOTAL_BYTES_CNT      totalBytesCnt      命中（key 无分隔符）
 * TOTAL_BYTES_CNT      TOTAL-BYTES-CNT    命中（连字符等价于下划线）
 * userName             user_name          命中（key 多下划线）
 * userName             USER-NAME          命中
 * </pre>
 * 另实现 {@code smartMatch} 的 {@code is} 前缀兜底：key 以 {@code is} 开头时去掉
 * 前缀再匹配，且仅当目标属性为 boolean/Boolean 时才命中（{@code isName} 不会
 * 匹配 String 类型的 {@code NAME}）。
 * <p>
 * 匹配优先级：精确名 &gt; 归一化名（忽略大小写与分隔符）&gt; is 前缀剥离。
 * 只在 Jackson 精确匹配失败后介入，正常报文不会走到这里，无额外开销。
 * <p>
 * 刻意不启用 {@code MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES}：
 * 它会在本类之前抢先做大小写匹配，遇到归一化后重名的属性
 * （同类中既有 {@code user_name} 又有 {@code userName}）时选中的目标与 fastjson 不一致。
 * 大小写不敏感由本类的归一化一并覆盖。
 * <p>
 * 归一化后重名时取哪个属性，1.2.83 源码（按 {@code sortedFieldDeserializers} 覆盖
 * hash 槽位）与实测的 fastjson 2.0.58 结论不同，本类以运行时实测为准，
 * 见 {@code NormalizedDuplicateNameTest}。这是不可靠的边角行为，
 * 建议此类命名冲突用 {@code @JSONField} 显式指定名字，不要依赖模糊匹配。
 */
public class SmartMatchHandler extends DeserializationProblemHandler {

    /** 每个目标类的「归一化名 → 属性」映射 */
    private static final ConcurrentMap<Class<?>, Map<String, SettableBeanProperty>> CACHE =
            new ConcurrentHashMap<>();

    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
            JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName)
            throws IOException {
        // 仅处理已实例化的 bean；基于 @JsonCreator 的构造期未知属性无法在此赋值
        if (!(deserializer instanceof BeanDeserializerBase)
                || beanOrClass == null || beanOrClass instanceof Class) {
            return false;
        }
        Map<String, SettableBeanProperty> lookup =
                lookupFor(beanOrClass.getClass(), (BeanDeserializerBase) deserializer);

        SettableBeanProperty prop = lookup.get(normalize(propertyName));
        // is 前缀兜底：仅 boolean 属性接受，避免 isName 误匹配到 String 的 NAME
        if (prop == null && propertyName.length() > 2
                && (propertyName.charAt(0) == 'i' || propertyName.charAt(0) == 'I')
                && (propertyName.charAt(1) == 's' || propertyName.charAt(1) == 'S')) {
            SettableBeanProperty candidate = lookup.get(normalize(propertyName.substring(2)));
            if (candidate != null && isBoolean(candidate)) {
                prop = candidate;
            }
        }
        if (prop == null) {
            return false;
        }
        // 解析器当前指向属性值，deserializeAndSet 会消费掉该值
        prop.deserializeAndSet(p, ctxt, beanOrClass);
        return true;
    }

    private static boolean isBoolean(SettableBeanProperty prop) {
        Class<?> raw = prop.getType().getRawClass();
        return raw == boolean.class || raw == Boolean.class;
    }

    private static Map<String, SettableBeanProperty> lookupFor(Class<?> beanClass,
            BeanDeserializerBase deserializer) {
        Map<String, SettableBeanProperty> cached = CACHE.get(beanClass);
        if (cached != null) {
            return cached;
        }
        Map<String, SettableBeanProperty> lookup = new HashMap<>();
        for (Iterator<SettableBeanProperty> it = deserializer.properties(); it.hasNext();) {
            SettableBeanProperty prop = it.next();
            String key = normalize(prop.getName());
            if (key.isEmpty()) {
                continue;
            }
            // 归一化后重名时取属性名字典序较小者。此规则由实测对齐（见类注释），
            // 非源码推导：1.2.83 与 2.x 在这个边角上的取值不同
            SettableBeanProperty exists = lookup.get(key);
            if (exists == null || prop.getName().compareTo(exists.getName()) < 0) {
                lookup.put(key, prop);
            }
        }
        CACHE.putIfAbsent(beanClass, lookup);
        return lookup;
    }

    /** 剔除下划线与连字符并转小写，对齐 {@code TypeUtils.fnv1a_64_extract} */
    private static String normalize(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c != '_' && c != '-') {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
