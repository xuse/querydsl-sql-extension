package io.github.xuse.fastjson.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Fastjson API 兼容层 — 基于 Jackson 实现。
 * <p>
 * 迁移方式：将 import com.alibaba.fastjson.JSON 替换为 import io.github.xuse.fastjson.adapter.JSON
 */
public final class JSON {
    /** 全局共享 ObjectMapper（线程安全，不可修改配置） */
    private static final ObjectMapper MAPPER = createMapper();

    /** 按日期格式缓存 ObjectWriter，避免每次调用 MAPPER.copy() 的开销 */
    private static final ConcurrentMap<String, ObjectWriter> DATE_FORMAT_WRITERS = new ConcurrentHashMap<>();

    /**
     * 兼容 fastjson 的 DEFFAULT_DATE_FORMAT 字段。
     * <p>
     * 字段名 "DEFFAULT" 为 fastjson 原始拼写（历史遗留），此处刻意保持一致以确保源码级兼容。
     * <p>
     * 行为差异说明：在原版 fastjson 中，直接赋值 {@code JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd"}
     * 即可全局生效。本兼容层由于底层 ObjectMapper 在类加载时已创建完毕，直接赋值不会触发格式切换。
     * 请使用 {@link #setDefaultDateFormat(String)} 方法来修改全局日期格式，该方法会同步更新
     * 内部 ObjectMapper 的 DateFormat 配置。
     */
    public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private JSON() {}

    /**
     * 设置全局默认日期格式，同步更新内部 ObjectMapper 的 DateFormat。
     * <p>
     * 兼容 fastjson 中 {@code JSON.DEFFAULT_DATE_FORMAT = "..."} 的全局配置行为。
     * 调用后，序列化时 Date 类型将使用指定格式输出字符串（而非时间戳）。
     * <p>
     * 示例：
     * <pre>
     * // fastjson 原始写法（本兼容层中直接赋值无效）：
     * // JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd";
     *
     * // 兼容层正确写法：
     * JSON.setDefaultDateFormat("yyyy-MM-dd");
     * </pre>
     *
     * @param dateFormat 日期格式字符串，如 "yyyy-MM-dd HH:mm:ss"
     */
    public static void setDefaultDateFormat(String dateFormat) {
        DEFFAULT_DATE_FORMAT = dateFormat;
        MAPPER.setDateFormat(new SimpleDateFormat(dateFormat));
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * 构建与 fastjson 默认行为等价的 ObjectMapper。
     * <p>
     * 对齐的 fastjson 默认开关（见 {@code com.alibaba.fastjson.JSON} 静态初始化块）：
     * <ul>
     * <li>{@code SkipTransientField} → transient 字段不序列化（含带 getter 的场景）</li>
     * <li>{@code UseBigDecimal} → 浮点数解析为 BigDecimal</li>
     * <li>{@code AllowSingleQuotes} / {@code AllowUnQuotedFieldNames} → 宽松语法</li>
     * <li>{@code IgnoreNotMatch} → 忽略未知字段</li>
     * <li>{@code WriteEnumUsingName} → 枚举按名称输出（与 Jackson 默认一致）</li>
     * <li>尾部残留无效字符时报错（fastjson 报错，Jackson 默认静默忽略）</li>
     * </ul>
     * 刻意不对齐的行为：
     * <ul>
     * <li>{@code SortField}：fastjson 默认按字段名字典序输出属性。JSON 对象成员
     * 本身无顺序语义，此处保留 Jackson 的声明顺序输出</li>
     * <li>非 String 类型的 Map key：fastjson 输出不加引号（非合规 JSON），
     * 此处保留 Jackson 的加引号行为</li>
     * </ul>
     */
    private static ObjectMapper createMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                // fastjson 默认 SkipTransientField：transient 字段不参与序列化。
                // Jackson 仅在无 getter 时跳过，开启此开关后带 getter 的 transient 字段同样忽略
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                // fastjson 默认 AllowSingleQuotes / AllowUnQuotedFieldNames：宽松语法
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                .build();

        // fastjson 默认 IgnoreNotMatch：忽略 JSON 中多出的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // fastjson 默认 UseBigDecimal：浮点数解析为 BigDecimal 而非 Double
        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        // fastjson 解析完整字符串时不允许尾部残留内容，Jackson 默认会静默忽略
        mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);

        mapper.registerModule(new FastjsonCompatModule());
        AnnotationIntrospector defaultAi = mapper.getSerializationConfig().getAnnotationIntrospector();
        AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(defaultAi, new FastjsonAnnotationIntrospector());
        mapper.setAnnotationIntrospector(pair);
        return mapper;
    }

    /** 获取内部 ObjectMapper，供高级场景使用 */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    // ==================== toJSONString ====================

    public static String toJSONString(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize failed", e);
        }
    }

    public static String toJSONString(Object object, boolean prettyFormat) {
        if (object == null) {
            return "null";
        }
        try {
            if (prettyFormat) {
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            }
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize failed", e);
        }
    }

    public static String toJSONStringWithDateFormat(Object object, String dateFormat) {
        if (object == null) {
            return "null";
        }
        try {
            ObjectWriter writer = DATE_FORMAT_WRITERS.computeIfAbsent(dateFormat,
                    fmt -> MAPPER.writer(new SimpleDateFormat(fmt)));
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize failed", e);
        }
    }

    // ==================== parseObject ====================

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, clazz);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize failed: " + truncate(text), e);
        }
    }

    public static <T> T parseObject(String text, Type type) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, MAPPER.getTypeFactory().constructType(type));
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize failed: " + truncate(text), e);
        }
    }

    /**
     * 泛型反序列化。
     * <p>用法: JSON.parseObject(text, new TypeReference&lt;Map&lt;String, Object&gt;&gt;() {})
     */
    public static <T> T parseObject(String text, TypeReference<T> typeRef) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, typeRef);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize failed: " + truncate(text), e);
        }
    }

    /** 解析为 JSONObject（兼容 fastjson 的 JSON.parseObject(str) 无 class 参数的用法） */
    public static JSONObject parseObject(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(text);
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                throw new JSONException("Not a JSON object: " + truncate(text));
            }
            return new JSONObject((ObjectNode) node);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize to JSONObject failed: " + truncate(text), e);
        }
    }

    // ==================== parseArray ====================

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize array failed: " + truncate(text), e);
        }
    }

    public static JSONArray parseArray(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(text);
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isArray()) {
                throw new JSONException("Not a JSON array: " + truncate(text));
            }
            return new JSONArray((ArrayNode) node);
        } catch (JsonProcessingException e) {
            throw new JSONException("Deserialize to JSONArray failed: " + truncate(text), e);
        }
    }

    // ==================== parse ====================

    /** 兼容 fastjson 的 JSON.parse(str) — 返回 JSONObject 或 JSONArray 或基本类型 */
    public static Object parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            // 数值类型的映射与 JSONObject.get 保持一致（浮点数 → BigDecimal）
            return JSONArray.nodeToValue(MAPPER.readTree(text));
        } catch (JsonProcessingException e) {
            throw new JSONException("Parse failed: " + truncate(text), e);
        }
    }

    // ==================== toJavaObject ====================

    /** 兼容 fastjson 的 JSON.toJavaObject(jsonObject, Class) */
    public static <T> T toJavaObject(JSONObject jsonObject, Class<T> clazz) {
        if (jsonObject == null) {
            return null;
        }
        try {
            return MAPPER.treeToValue(jsonObject.getInner(), clazz);
        } catch (JsonProcessingException e) {
            throw new JSONException("Convert JSONObject to JavaObject failed", e);
        }
    }

    // ==================== parseObject (InputStream) ====================

    /**
     * 从 InputStream 反序列化为 JavaBean（默认 UTF-8 编码）。
     * <p>
     * 兼容 fastjson 的 JSON.parseObject(InputStream, Type)
     */
    public static <T> T parseObject(InputStream is, Type type) throws IOException {
        if (is == null) {
            return null;
        }
        return MAPPER.readValue(is, MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * 从 InputStream 反序列化为 JavaBean，指定字符集。
     * <p>
     * 兼容 fastjson 的 JSON.parseObject(InputStream, Charset, Type)
     */
    public static <T> T parseObject(InputStream is, Charset charset, Type type) throws IOException {
        if (is == null) {
            return null;
        }
        Reader reader = new InputStreamReader(is, charset);
        return MAPPER.readValue(reader, MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * 从 InputStream 反序列化为指定 Class 的 JavaBean（默认 UTF-8 编码）。
     */
    public static <T> T parseObject(InputStream is, Class<T> clazz) throws IOException {
        if (is == null) {
            return null;
        }
        return MAPPER.readValue(is, clazz);
    }

    // ==================== parseObject (byte[]) ====================

    /**
     * 从 UTF-8 编码的 byte[] 反序列化为 JavaBean。
     * <p>
     * 兼容 fastjson 的 JSON.parseObject(byte[], Type)
     */
    public static <T> T parseObject(byte[] jsonBytes, Type type) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonBytes, MAPPER.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new JSONException("Deserialize from bytes failed", e);
        }
    }

    /**
     * 从 UTF-8 编码的 byte[] 反序列化为指定 Class 的 JavaBean。
     */
    public static <T> T parseObject(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            throw new JSONException("Deserialize from bytes failed", e);
        }
    }

    // ==================== toJSONBytes ====================

    /**
     * 将 Java 对象序列化为 JSON 格式的 UTF-8 byte[]。
     * <p>
     * 兼容 fastjson 的 JSON.toJSONBytes(Object)
     */
    public static byte[] toJSONBytes(Object object) {
        if (object == null) {
            return "null".getBytes();
        }
        try {
            return MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new JSONException("Serialize to bytes failed", e);
        }
    }

    // ==================== writeJSONString (OutputStream / Writer) ====================

    /**
     * 将 Java 对象序列化为 JSON 字符串，按 UTF-8 编码写入 OutputStream。
     * <p>
     * 兼容 fastjson 的 JSON.writeJSONString(OutputStream, Object)
     *
     * @return 写入的字节数
     */
    public static int writeJSONString(OutputStream os, Object object) throws IOException {
        if (object == null) {
            byte[] nullBytes = "null".getBytes();
            os.write(nullBytes);
            return nullBytes.length;
        }
        byte[] bytes = MAPPER.writeValueAsBytes(object);
        os.write(bytes);
        return bytes.length;
    }

    /**
     * 将 Java 对象序列化为 JSON 字符串，写入 Writer。
     * <p>
     * 兼容 fastjson 的 JSON.writeJSONString(Writer, Object)
     */
    public static void writeJSONString(Writer writer, Object object) throws IOException {
        if (object == null) {
            writer.write("null");
            return;
        }
        MAPPER.writeValue(writer, object);
    }

    // ==================== 内部工具 ====================

    private static String truncate(String text) {
        if (text == null) return "null";
        return text.length() > 64 ? text.substring(0, 64) + "..." : text;
    }
}
