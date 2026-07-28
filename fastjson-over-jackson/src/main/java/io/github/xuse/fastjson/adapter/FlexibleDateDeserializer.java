package io.github.xuse.fastjson.adapter;

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
 */
public class FlexibleDateDeserializer extends JsonDeserializer<Date> {

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
                    return parseDate(text, "yyyyMMdd");
                }
                break;
            case 10:
                if (text.charAt(4) == '-') {
                    return parseDate(text, "yyyy-MM-dd");
                }
                if (isDigits(text)) {
                    return new Date(Long.parseLong(text) * 1000L);
                }
                break;
            case 12:
                if (isDigits(text)) {
                    return parseDate(text, "yyyyMMddHHmm");
                }
                break;
            case 13:
                if (isDigits(text)) {
                    return new Date(Long.parseLong(text));
                }
                break;
            case 14:
                if (isDigits(text)) {
                    return parseDate(text, "yyyyMMddHHmmss");
                }
                break;
            case 16:
                if (text.charAt(4) == '-') {
                    return parseDate(text, "yyyy-MM-dd HH:mm");
                }
                break;
            case 19:
                if (text.charAt(4) == '-') {
                    if (text.charAt(10) == 'T') {
                        return parseDate(text, "yyyy-MM-dd'T'HH:mm:ss");
                    }
                    return parseDate(text, "yyyy-MM-dd HH:mm:ss");
                }
                break;
            case 23:
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

        if (isDigits(text)) {
            return new Date(Long.parseLong(text));
        }

        if (len > 19 && text.charAt(4) == '-') {
            try {
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
