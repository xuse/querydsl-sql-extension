package io.github.xuse.fastjson.adapter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * java.time (JSR-310) 类型的 fastjson 兼容编解码，不依赖 jackson-datatype-jsr310。
 * <p>
 * 序列化默认输出 ISO-8601 字符串，与 fastjson 1.x {@code Jdk8DateCodec} 一致：
 * <ul>
 * <li>{@link LocalDate}、{@link LocalTime} 等 → {@code toString()}</li>
 * <li>{@link LocalDateTime} → 按纳秒精度选择 {@code yyyy-MM-dd'T'HH:mm:ss}、
 * {@code yyyy-MM-dd'T'HH:mm:ss.SSS} 或 {@code yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS}</li>
 * </ul>
 * 反序列化保持宽松，与 fastjson 一致地接受多种输入：epoch 毫秒数字、ISO 字符串、
 * 空格分隔的 {@code yyyy-MM-dd HH:mm:ss}、紧凑数字串（{@code yyyyMMdd} 等）
 * 以及带时区偏移的 ISO 串。
 * <p>
 * {@code @JSONField(format=...)} 通过 {@link ContextualSerializer} 与
 * {@link ContextualDeserializer} 生效，序列化与反序列化都会采用该格式。
 * <p>
 * 注意：{@link JSON#setDefaultDateFormat(String)} 只作用于 {@link java.util.Date}，
 * 不改变 java.time 类型的 ISO 默认输出，这与 fastjson 的行为一致。
 */
final class JavaTimeCodec {

    private JavaTimeCodec() {}

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private static final DateTimeFormatter LDT_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter LDT_MILLI = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final DateTimeFormatter LDT_NANO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    /** 注册所有支持的 java.time 类型 */
    static void register(SimpleModule module) {
        add(module, LocalDateTime.class, JavaTimeCodec::parseLocalDateTime, ldt -> ldt);
        add(module, LocalDate.class, JavaTimeCodec::parseLocalDate, LocalDateTime::toLocalDate);
        add(module, LocalTime.class, JavaTimeCodec::parseLocalTime, LocalDateTime::toLocalTime);
        add(module, Instant.class, JavaTimeCodec::parseInstant, ldt -> ldt.atZone(ZONE).toInstant());
        add(module, ZonedDateTime.class, JavaTimeCodec::parseZonedDateTime, ldt -> ldt.atZone(ZONE));
        add(module, OffsetDateTime.class, JavaTimeCodec::parseOffsetDateTime,
                ldt -> ldt.atZone(ZONE).toOffsetDateTime());
    }

    private static <T extends TemporalAccessor> void add(SimpleModule module, Class<T> type,
            BiFunction<String, DateTimeFormatter, T> textParser,
            Function<LocalDateTime, T> millisMapper) {
        module.addSerializer(type, new Ser<>(type, null));
        module.addDeserializer(type, new Deser<>(type, textParser, millisMapper, null));
    }

    // ==================== 默认序列化文本 ====================

    /**
     * fastjson 默认输出形式。{@code LocalDateTime} 按纳秒精度选择 ISO 变体，
     * 其余类型直接使用 {@code toString()}（即 ISO-8601）。
     */
    static String defaultText(TemporalAccessor value) {
        if (value instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) value;
            int nano = ldt.getNano();
            if (nano == 0) {
                return LDT_SEC.format(ldt);
            }
            return nano % 1000000 == 0 ? LDT_MILLI.format(ldt) : LDT_NANO.format(ldt);
        }
        return value.toString();
    }

    /** 从属性上解析 {@code @JSONField(format=...)} / {@code @JsonFormat(pattern=...)} */
    static DateTimeFormatter formatOf(AnnotationIntrospector intr, BeanProperty property) {
        if (property == null || intr == null) {
            return null;
        }
        JsonFormat.Value v = intr.findFormat(property.getMember());
        if (v == null || !v.hasPattern()) {
            return null;
        }
        String pattern = v.getPattern();
        return pattern.isEmpty() ? null : DateTimeFormatter.ofPattern(pattern);
    }

    // ==================== 文本解析 ====================

    static LocalDateTime parseLocalDateTime(String text, DateTimeFormatter explicit) {
        if (explicit != null) {
            // 指定格式可能只含日期部分，此时补 00:00:00
            try {
                return LocalDateTime.parse(text, explicit);
            } catch (DateTimeParseException e) {
                return LocalDate.parse(text, explicit).atStartOfDay();
            }
        }
        if (isDigits(text)) {
            return fromCompactDateTime(text);
        }
        // 空格分隔转为 ISO 的 'T' 分隔
        String iso = normalizeSeparator(text);
        if (iso.length() == 10) {
            return LocalDate.parse(iso).atStartOfDay();
        }
        if (hasOffset(iso)) {
            return OffsetDateTime.parse(iso).atZoneSameInstant(ZONE).toLocalDateTime();
        }
        return LocalDateTime.parse(iso);
    }

    static LocalDate parseLocalDate(String text, DateTimeFormatter explicit) {
        if (explicit != null) {
            return LocalDate.parse(text, explicit);
        }
        if (isDigits(text)) {
            return text.length() == 8 ? LocalDate.parse(text, YYYYMMDD)
                    : fromCompactDateTime(text).toLocalDate();
        }
        String iso = normalizeSeparator(text);
        return iso.length() == 10 ? LocalDate.parse(iso) : parseLocalDateTime(iso, null).toLocalDate();
    }

    static LocalTime parseLocalTime(String text, DateTimeFormatter explicit) {
        if (explicit != null) {
            return LocalTime.parse(text, explicit);
        }
        if (isDigits(text)) {
            if (text.length() == 4) {
                return LocalTime.parse(text, HHMM);
            }
            if (text.length() == 6) {
                return LocalTime.parse(text, HHMMSS);
            }
            return fromCompactDateTime(text).toLocalTime();
        }
        // 完整日期时间串取时间部分，纯时间串直接解析
        String iso = normalizeSeparator(text);
        return iso.indexOf('T') >= 0 ? parseLocalDateTime(iso, null).toLocalTime() : LocalTime.parse(iso);
    }

    static Instant parseInstant(String text, DateTimeFormatter explicit) {
        if (explicit == null && !isDigits(text) && hasOffset(normalizeSeparator(text))) {
            return OffsetDateTime.parse(normalizeSeparator(text)).toInstant();
        }
        return parseLocalDateTime(text, explicit).atZone(ZONE).toInstant();
    }

    static ZonedDateTime parseZonedDateTime(String text, DateTimeFormatter explicit) {
        if (explicit == null && !isDigits(text)) {
            String iso = normalizeSeparator(text);
            if (hasOffset(iso)) {
                return ZonedDateTime.parse(iso);
            }
        }
        return parseLocalDateTime(text, explicit).atZone(ZONE);
    }

    static OffsetDateTime parseOffsetDateTime(String text, DateTimeFormatter explicit) {
        if (explicit == null && !isDigits(text)) {
            String iso = normalizeSeparator(text);
            if (hasOffset(iso)) {
                return OffsetDateTime.parse(iso);
            }
        }
        return parseLocalDateTime(text, explicit).atZone(ZONE).toOffsetDateTime();
    }

    /** 纯数字串：按长度识别 epoch 秒/毫秒或紧凑日期时间 */
    private static LocalDateTime fromCompactDateTime(String text) {
        switch (text.length()) {
            case 8:
                return LocalDate.parse(text, YYYYMMDD).atStartOfDay();
            case 12:
                return LocalDateTime.parse(text, YYYYMMDDHHMM);
            case 14:
                return LocalDateTime.parse(text, YYYYMMDDHHMMSS);
            case 10:
                // epoch 秒
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(text)), ZONE);
            default:
                // 其余按 epoch 毫秒
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), ZONE);
        }
    }

    /** 将 "yyyy-MM-dd HH:mm:ss" 的空格分隔符转为 ISO 的 'T' */
    private static String normalizeSeparator(String text) {
        int space = text.indexOf(' ');
        return space == 10 ? text.substring(0, 10) + 'T' + text.substring(11) : text;
    }

    /** 判断 ISO 串是否带时区偏移（如 +08:00 或结尾 Z） */
    private static boolean hasOffset(String iso) {
        if (iso.endsWith("Z")) {
            return true;
        }
        // 时间部分中出现 +/- 才算偏移，避开日期部分的 '-'
        int t = iso.indexOf('T');
        if (t < 0) {
            return false;
        }
        return iso.indexOf('+', t) > 0 || iso.indexOf('-', t) > 0;
    }

    private static boolean isDigits(String str) {
        if (str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

/**
 * java.time 类型序列化器，支持 {@code @JSONField(format=...)}。
 */
class Ser<T extends TemporalAccessor> extends JsonSerializer<T> implements ContextualSerializer {

    private final Class<T> type;
    private final DateTimeFormatter format;

    Ser(Class<T> type, DateTimeFormatter format) {
        this.type = type;
        this.format = format;
    }

    @Override
    public Class<T> handledType() {
        return type;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(format != null ? format.format(value) : JavaTimeCodec.defaultText(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        DateTimeFormatter f = JavaTimeCodec.formatOf(prov.getAnnotationIntrospector(), property);
        return f == null ? this : new Ser<>(type, f);
    }
}

/**
 * java.time 类型反序列化器，宽松接受数字时间戳与多种字符串格式。
 */
class Deser<T extends TemporalAccessor> extends JsonDeserializer<T> implements ContextualDeserializer {

    private final Class<T> type;
    private final BiFunction<String, DateTimeFormatter, T> textParser;
    private final Function<LocalDateTime, T> millisMapper;
    private final DateTimeFormatter format;

    Deser(Class<T> type, BiFunction<String, DateTimeFormatter, T> textParser,
            Function<LocalDateTime, T> millisMapper, DateTimeFormatter format) {
        this.type = type;
        this.textParser = textParser;
        this.millisMapper = millisMapper;
        this.format = format;
    }

    @Override
    public Class<?> handledType() {
        return type;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return millisMapper.apply(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), ZoneId.systemDefault()));
        }
        String text = p.getText();
        if (text == null) {
            return null;
        }
        text = text.trim();
        if (text.isEmpty() || "null".equals(text)) {
            return null;
        }
        try {
            return textParser.apply(text, format);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new JSONException("Cannot parse " + type.getSimpleName() + ": " + text, e);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        DateTimeFormatter f = JavaTimeCodec.formatOf(ctxt.getAnnotationIntrospector(), property);
        return f == null ? this : new Deser<>(type, textParser, millisMapper, f);
    }
}
