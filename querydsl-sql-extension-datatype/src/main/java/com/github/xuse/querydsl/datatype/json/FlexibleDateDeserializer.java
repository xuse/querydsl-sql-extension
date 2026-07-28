package com.github.xuse.querydsl.datatype.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 兼容 fastjson 的 Date 反序列化逻辑。
 * <p>
 * fastjson（readMillisFromString）的实际优先级：
 * <ol>
 *   <li>数字 token → 时间戳毫秒</li>
 *   <li>字符串按长度分发（优先于 isNumber 判断）：
 *     <ul>
 *       <li>8位 → yyyyMMdd</li>
 *       <li>10位 → yyyy-MM-dd（或纯数字则当时间戳秒）</li>
 *       <li>12位 → yyyyMMddHHmm</li>
 *       <li>14位 → yyyyMMddHHmmss</li>
 *       <li>16位 → yyyy-MM-dd HH:mm</li>
 *       <li>19位 → yyyy-MM-dd HH:mm:ss</li>
 *     </ul>
 *   </li>
 *   <li>以上都不匹配时，如果是纯数字 → 时间戳毫秒</li>
 * </ol>
 */
public class FlexibleDateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 数字 token → 直接当时间戳毫秒
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

        // 按字符串长度分发（与 fastjson readMillisFromString 一致）
        int len = text.length();
        switch (len) {
            case 8:
                // yyyyMMdd（如果是纯数字且符合日期范围）
                if (isDigits(text)) {
                    return parseDate(text, "yyyyMMdd");
                }
                break;
            case 10:
                // yyyy-MM-dd 或纯数字时间戳秒
                if (text.charAt(4) == '-') {
                    return parseDate(text, "yyyy-MM-dd");
                }
                if (isDigits(text)) {
                    return new Date(Long.parseLong(text) * 1000L);
                }
                break;
            case 12:
                // yyyyMMddHHmm
                if (isDigits(text)) {
                    return parseDate(text, "yyyyMMddHHmm");
                }
                break;
            case 13:
                // 时间戳毫秒（13位数字）
                if (isDigits(text)) {
                    return new Date(Long.parseLong(text));
                }
                break;
            case 14:
                // yyyyMMddHHmmss
                if (isDigits(text)) {
                    return parseDate(text, "yyyyMMddHHmmss");
                }
                break;
            case 16:
                // yyyy-MM-dd HH:mm
                if (text.charAt(4) == '-') {
                    return parseDate(text, "yyyy-MM-dd HH:mm");
                }
                break;
            case 19:
                // yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd'T'HH:mm:ss
                if (text.charAt(4) == '-') {
                    if (text.charAt(10) == 'T') {
                        return parseDate(text, "yyyy-MM-dd'T'HH:mm:ss");
                    }
                    return parseDate(text, "yyyy-MM-dd HH:mm:ss");
                }
                break;
            case 23:
                // yyyy-MM-dd HH:mm:ss.SSS
                if (text.charAt(4) == '-') {
                    if (text.charAt(10) == 'T') {
                        return parseDate(text, "yyyy-MM-dd'T'HH:mm:ss.SSS");
                    }
                    return parseDate(text, "yyyy-MM-dd HH:mm:ss.SSS");
                }
                break;
            default:
                break;
        }

        // fallback：纯数字 → 时间戳毫秒
        if (isDigits(text)) {
            return new Date(Long.parseLong(text));
        }

        // 最后尝试带时区的 ISO 格式
        if (len > 19 && text.charAt(4) == '-') {
            try {
                // yyyy-MM-dd'T'HH:mm:ssXXX 或类似
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                return sdf.parse(text);
            } catch (ParseException ignored) {
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                return sdf.parse(text);
            } catch (ParseException ignored) {
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

    private static Date parseDate(String text, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.parse(text);
        } catch (ParseException e) {
            throw new JSONException("Cannot parse date '" + text + "' with pattern " + pattern, e);
        }
    }
}
