package io.github.xuse.fastjson.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 兼容 fastjson 的 Date 反序列化逻辑。
 * <p>
 * 使用线程安全的 {@link DateTimeFormatter} 替代 SimpleDateFormat。
 */
public class FlexibleDateDeserializer extends JsonDeserializer<Date> {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private static final DateTimeFormatter FMT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FMT_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter FMT_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter FMT_YYYY_MM_DD_HH_MM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_YYYY_MM_DD_T_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter FMT_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_YYYY_MM_DD_T_HH_MM_SS_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final DateTimeFormatter FMT_YYYY_MM_DD_HH_MM_SS_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FMT_ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return new Date(p.getLongValue());
        }

        String text = p.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        text = text.trim();

        if ("null".equals(text)) {
            return null;
        }

        int len = text.length();
        switch (len) {
            case 8:
                if (isDigits(text)) {
                    return toDate(LocalDate.parse(text, FMT_YYYYMMDD).atStartOfDay());
                }
                break;
            case 10:
                if (text.charAt(4) == '-') {
                    return toDate(LocalDate.parse(text, FMT_YYYY_MM_DD).atStartOfDay());
                }
                if (isDigits(text)) {
                    return new Date(Long.parseLong(text) * 1000L);
                }
                break;
            case 12:
                if (isDigits(text)) {
                    return toDate(LocalDateTime.parse(text, FMT_YYYYMMDDHHMM));
                }
                break;
            case 13:
                if (isDigits(text)) {
                    return new Date(Long.parseLong(text));
                }
                break;
            case 14:
                if (isDigits(text)) {
                    return toDate(LocalDateTime.parse(text, FMT_YYYYMMDDHHMMSS));
                }
                break;
            case 16:
                if (text.charAt(4) == '-') {
                    return toDate(LocalDateTime.parse(text, FMT_YYYY_MM_DD_HH_MM));
                }
                break;
            case 19:
                if (text.charAt(4) == '-') {
                    if (text.charAt(10) == 'T') {
                        return toDate(LocalDateTime.parse(text, FMT_YYYY_MM_DD_T_HH_MM_SS));
                    }
                    return toDate(LocalDateTime.parse(text, FMT_YYYY_MM_DD_HH_MM_SS));
                }
                break;
            case 23:
                if (text.charAt(4) == '-') {
                    if (text.charAt(10) == 'T') {
                        return toDate(LocalDateTime.parse(text, FMT_YYYY_MM_DD_T_HH_MM_SS_SSS));
                    }
                    return toDate(LocalDateTime.parse(text, FMT_YYYY_MM_DD_HH_MM_SS_SSS));
                }
                break;
            default:
                break;
        }

        if (isDigits(text)) {
            return new Date(Long.parseLong(text));
        }

        // 尝试带时区偏移的 ISO 格式，如 "2024-01-01T12:00:00+08:00"
        if (len > 19 && text.charAt(4) == '-') {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(text, FMT_ISO_OFFSET);
                return Date.from(zdt.toInstant());
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new JSONException("Cannot parse date: " + text);
    }

    private static boolean isDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return str.length() > 0;
    }

    private static Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(DEFAULT_ZONE).toInstant());
    }
}
