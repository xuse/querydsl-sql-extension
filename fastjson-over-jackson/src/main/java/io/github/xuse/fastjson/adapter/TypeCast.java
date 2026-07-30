package io.github.xuse.fastjson.adapter;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 复刻 fastjson {@code com.alibaba.fastjson.util.TypeUtils} 的取值转换语义，
 * 供 {@link JSONObject} / {@link JSONArray} 的 getXxx 方法使用。
 * <p>
 * 与 Jackson {@code JsonNode.asInt()} 等方法的关键差异：Jackson 在无法转换时
 * 静默返回 0/false，而 fastjson 会抛出异常或返回 null。本类保持 fastjson 语义，
 * 避免脏数据被静默吞掉。
 */
final class TypeCast {

    private TypeCast() {}

    /** 空值语义：fastjson 将 ""、"null"、"NULL" 一并视为 null */
    private static boolean isNullText(String s) {
        return s.isEmpty() || "null".equals(s) || "NULL".equals(s);
    }

    private static boolean isAbsent(JsonNode n) {
        return n == null || n.isNull() || n.isMissingNode();
    }

    /** 去掉千分位逗号，与 fastjson castToInt/castToLong 的处理一致 */
    private static String normalizeNumber(String s) {
        return s.indexOf(',') >= 0 ? s.replace(",", "") : s;
    }

    static Integer toInteger(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isNumber()) {
            return n.intValue();
        }
        if (n.isBoolean()) {
            return n.booleanValue() ? 1 : 0;
        }
        String s = n.asText();
        if (isNullText(s)) {
            return null;
        }
        s = normalizeNumber(s);
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            // fastjson 允许 "1.0" 这类带小数的字符串转 int
            return new BigDecimal(s).intValue();
        }
    }

    static Long toLong(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isNumber()) {
            return n.longValue();
        }
        if (n.isBoolean()) {
            return n.booleanValue() ? 1L : 0L;
        }
        String s = n.asText();
        if (isNullText(s)) {
            return null;
        }
        s = normalizeNumber(s);
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            return new BigDecimal(s).longValue();
        }
    }

    static Double toDouble(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isNumber()) {
            return n.doubleValue();
        }
        String s = n.asText();
        if (isNullText(s)) {
            return null;
        }
        return Double.valueOf(normalizeNumber(s));
    }

    static Float toFloat(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isNumber()) {
            return n.floatValue();
        }
        String s = n.asText();
        if (isNullText(s)) {
            return null;
        }
        return Float.valueOf(normalizeNumber(s));
    }

    /**
     * fastjson 的布尔转换：数字 1 为 true，其余数字为 false；
     * 字符串支持 true/1/Y/T 与 false/0/F/N（大小写不敏感）。
     */
    static Boolean toBoolean(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isBoolean()) {
            return n.booleanValue();
        }
        if (n.isNumber()) {
            return n.intValue() == 1;
        }
        String s = n.asText();
        if (isNullText(s)) {
            return null;
        }
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "Y".equalsIgnoreCase(s) || "T".equals(s)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(s) || "0".equals(s) || "F".equalsIgnoreCase(s) || "N".equals(s)) {
            return Boolean.FALSE;
        }
        throw new JSONException("can not cast to boolean, value : " + s);
    }

    static BigDecimal toBigDecimal(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isNumber()) {
            return n.decimalValue();
        }
        String s = n.asText();
        return isNullText(s) ? null : new BigDecimal(normalizeNumber(s));
    }

    static BigInteger toBigInteger(JsonNode n) {
        if (isAbsent(n)) {
            return null;
        }
        if (n.isNumber()) {
            return n.bigIntegerValue();
        }
        String s = n.asText();
        if (isNullText(s)) {
            return null;
        }
        s = normalizeNumber(s);
        try {
            return new BigInteger(s);
        } catch (NumberFormatException e) {
            return new BigDecimal(s).toBigInteger();
        }
    }

    // ==================== 基本类型：null 时取零值 ====================

    static int intValue(JsonNode n) {
        Integer v = toInteger(n);
        return v == null ? 0 : v;
    }

    static long longValue(JsonNode n) {
        Long v = toLong(n);
        return v == null ? 0L : v;
    }

    static double doubleValue(JsonNode n) {
        Double v = toDouble(n);
        return v == null ? 0D : v;
    }

    static float floatValue(JsonNode n) {
        Float v = toFloat(n);
        return v == null ? 0F : v;
    }

    static boolean booleanValue(JsonNode n) {
        Boolean v = toBoolean(n);
        return v != null && v;
    }
}
