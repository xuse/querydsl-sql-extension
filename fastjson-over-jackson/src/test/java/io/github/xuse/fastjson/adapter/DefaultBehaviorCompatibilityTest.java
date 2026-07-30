package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * 验证 fastjson 默认开关行为的对齐情况。
 * <p>
 * 对应 {@code com.alibaba.fastjson.JSON} 静态初始化块中的
 * {@code DEFAULT_PARSER_FEATURE} / {@code DEFAULT_GENERATE_FEATURE}，
 * 以及 {@code com.alibaba.fastjson.util.TypeUtils} 的取值转换语义。
 */
public class DefaultBehaviorCompatibilityTest {

    /** 字段声明顺序刻意打乱，用于验证 SortField */
    public static class OrderBean {
        private String zebra = "z";
        private String apple = "a";
        private String mango = "m";
        private transient String temp = "t";

        public String getZebra() { return zebra; }
        public void setZebra(String v) { this.zebra = v; }
        public String getApple() { return apple; }
        public void setApple(String v) { this.apple = v; }
        public String getMango() { return mango; }
        public void setMango(String v) { this.mango = v; }
        public String getTemp() { return temp; }
        public void setTemp(String v) { this.temp = v; }
    }

    // ==================== SerializerFeature.SortField：刻意不对齐 ====================

    /**
     * fastjson 默认按字段名字典序输出，本兼容层保留 Jackson 的声明顺序。
     * <p>
     * JSON 对象成员无顺序语义，故不强制对齐；此用例固定该决定，
     * 避免日后被误当作缺陷"修复"。
     */
    @Test
    public void testFieldOrder_declarationOrderKept() {
        assertThat(JSON.toJSONString(new OrderBean()))
                .isEqualTo("{\"zebra\":\"z\",\"apple\":\"a\",\"mango\":\"m\"}");
    }

    // ==================== SerializerFeature.SkipTransientField ====================

    /**
     * transient 字段不序列化。Jackson 默认仅在字段无 getter 时跳过，
     * 本用例中的 temp 带有 getter，需靠 PROPAGATE_TRANSIENT_MARKER 才能与 fastjson 一致。
     */
    @Test
    public void testSkipTransientField_evenWithGetter() {
        assertThat(JSON.toJSONString(new OrderBean())).doesNotContain("temp");
        assertThat(com.alibaba.fastjson.JSON.toJSONString(new OrderBean())).doesNotContain("temp");
    }

    // ==================== Feature.UseBigDecimal ====================

    @Test
    public void testUseBigDecimal_floatParsedAsBigDecimal() {
        Object adapter = JSON.parseObject("{\"v\":1.1}").get("v");
        Object fastjson = com.alibaba.fastjson.JSON.parseObject("{\"v\":1.1}").get("v");

        assertThat(adapter).isInstanceOf(BigDecimal.class).isEqualTo(fastjson);
    }

    @Test
    public void testUseBigDecimal_viaParseAndMap() {
        assertThat(JSON.parse("1.1")).isInstanceOf(BigDecimal.class);

        Map<?, ?> map = JSON.parseObject("{\"v\":1.5}", Map.class);
        assertThat(map.get("v")).isInstanceOf(BigDecimal.class);
    }

    @Test
    public void testLargeIntegerParsedAsBigInteger() {
        Object adapter = JSON.parseObject("{\"v\":123456789012345678901}").get("v");
        Object fastjson = com.alibaba.fastjson.JSON.parseObject("{\"v\":123456789012345678901}").get("v");

        assertThat(adapter).isInstanceOf(BigInteger.class).isEqualTo(fastjson);
    }

    @Test
    public void testSmallIntegerStaysInteger() {
        assertThat(JSON.parseObject("{\"v\":123}").get("v")).isInstanceOf(Integer.class);
        assertThat(JSON.parseObject("{\"v\":9999999999}").get("v")).isInstanceOf(Long.class);
    }

    // ==================== 宽松语法 ====================

    @Test
    public void testAllowSingleQuotes() {
        assertThat(JSON.parseObject("{'a':'b'}").getString("a")).isEqualTo("b");
    }

    @Test
    public void testAllowUnquotedFieldNames() {
        assertThat(JSON.parseObject("{a:1}").getIntValue("a")).isEqualTo(1);
    }

    // ==================== 尾部残留内容 ====================

    @Test
    public void testTrailingContentRejected() {
        assertThatThrownBy(() -> JSON.parseObject("{\"a\":1} trailing"))
                .isInstanceOf(JSONException.class);
        assertThatThrownBy(() -> JSON.parseObject("{\"a\":1}{\"b\":2}", Map.class))
                .isInstanceOf(JSONException.class);
    }

    // ==================== 根类型不匹配 ====================

    @Test
    public void testWrongRootTypeThrowsJSONException() {
        assertThatThrownBy(() -> JSON.parseObject("[1,2]"))
                .isInstanceOf(JSONException.class);
        assertThatThrownBy(() -> JSON.parseArray("{\"a\":1}"))
                .isInstanceOf(JSONException.class);
    }

    // ==================== TypeUtils 取值语义：boolean ====================

    @Test
    public void testGetBoolean_fastjsonSemantics() {
        JSONObject obj = JSON.parseObject(
                "{\"n1\":1,\"n0\":0,\"s1\":\"1\",\"s0\":\"0\",\"y\":\"Y\",\"t\":\"T\",\"f\":\"F\",\"no\":\"N\"}");

        assertThat(obj.getBoolean("n1")).isTrue();
        assertThat(obj.getBoolean("n0")).isFalse();
        assertThat(obj.getBoolean("s1")).isTrue();
        assertThat(obj.getBoolean("s0")).isFalse();
        assertThat(obj.getBoolean("y")).isTrue();
        assertThat(obj.getBoolean("t")).isTrue();
        assertThat(obj.getBoolean("f")).isFalse();
        assertThat(obj.getBoolean("no")).isFalse();
    }

    @Test
    public void testGetBoolean_nullSemantics() {
        JSONObject obj = JSON.parseObject("{\"empty\":\"\",\"nullText\":\"null\",\"nil\":null}");

        assertThat(obj.getBoolean("empty")).isNull();
        assertThat(obj.getBoolean("nullText")).isNull();
        assertThat(obj.getBoolean("nil")).isNull();
        assertThat(obj.getBoolean("missing")).isNull();
        // 基本类型版本 null 时取 false
        assertThat(obj.getBooleanValue("missing")).isFalse();
    }

    /** 无法转换时抛异常，而非静默返回 false */
    @Test
    public void testGetBoolean_unconvertibleThrows() {
        assertThatThrownBy(() -> JSON.parseObject("{\"a\":\"abc\"}").getBoolean("a"))
                .isInstanceOf(JSONException.class);
    }

    // ==================== TypeUtils 取值语义：数字 ====================

    @Test
    public void testGetInteger_stringCoercion() {
        JSONObject obj = JSON.parseObject("{\"s\":\"42\",\"withComma\":\"1,234\",\"decimal\":\"3.7\"}");

        assertThat(obj.getInteger("s")).isEqualTo(42);
        assertThat(obj.getInteger("withComma")).isEqualTo(1234);
        assertThat(obj.getInteger("decimal")).isEqualTo(3);
    }

    @Test
    public void testGetInteger_nullSemantics() {
        JSONObject obj = JSON.parseObject("{\"empty\":\"\",\"nullText\":\"NULL\"}");

        assertThat(obj.getInteger("empty")).isNull();
        assertThat(obj.getInteger("nullText")).isNull();
        assertThat(obj.getInteger("missing")).isNull();
        assertThat(obj.getIntValue("missing")).isZero();
    }

    /** 非数字文本抛异常，而非静默返回 0 */
    @Test
    public void testGetInteger_unconvertibleThrows() {
        assertThatThrownBy(() -> JSON.parseObject("{\"a\":\"abc\"}").getInteger("a"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testGetLongAndDoubleAndFloat() {
        JSONObject obj = JSON.parseObject("{\"big\":\"9999999999\",\"d\":\"1.5\",\"f\":\"2.5\"}");

        assertThat(obj.getLong("big")).isEqualTo(9999999999L);
        assertThat(obj.getDouble("d")).isEqualTo(1.5D);
        assertThat(obj.getFloat("f")).isEqualTo(2.5F);
        assertThat(obj.getLongValue("missing")).isZero();
        assertThat(obj.getDoubleValue("missing")).isZero();
        assertThat(obj.getFloatValue("missing")).isZero();
    }

    @Test
    public void testGetBigDecimalAndBigInteger() {
        JSONObject obj = JSON.parseObject("{\"dec\":\"12.34\",\"int\":\"123456789012345678901\"}");

        assertThat(obj.getBigDecimal("dec")).isEqualByComparingTo(new BigDecimal("12.34"));
        assertThat(obj.getBigInteger("int")).isEqualTo(new BigInteger("123456789012345678901"));
        assertThat(obj.getBigDecimal("missing")).isNull();
        assertThat(obj.getBigInteger("missing")).isNull();
    }

    @Test
    public void testBooleanToNumberCoercion() {
        JSONObject obj = JSON.parseObject("{\"t\":true,\"f\":false}");

        assertThat(obj.getInteger("t")).isEqualTo(1);
        assertThat(obj.getInteger("f")).isZero();
        assertThat(obj.getLong("t")).isEqualTo(1L);
    }

    // ==================== JSONArray 同等语义 ====================

    @Test
    public void testJSONArray_coercionGetters() {
        JSONArray arr = JSON.parseArray("[\"42\",\"1\",\"1.5\",\"123456789012345678901\",null]");

        assertThat(arr.getInteger(0)).isEqualTo(42);
        assertThat(arr.getIntValue(0)).isEqualTo(42);
        assertThat(arr.getLong(0)).isEqualTo(42L);
        assertThat(arr.getBoolean(1)).isTrue();
        assertThat(arr.getBooleanValue(1)).isTrue();
        assertThat(arr.getDouble(2)).isEqualTo(1.5D);
        assertThat(arr.getFloat(2)).isEqualTo(1.5F);
        assertThat(arr.getBigDecimal(2)).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(arr.getBigInteger(3)).isEqualTo(new BigInteger("123456789012345678901"));
        assertThat(arr.getInteger(4)).isNull();
        assertThat(arr.getIntValue(4)).isZero();
    }

    @Test
    public void testJSONArray_bigDecimalElement() {
        assertThat(JSON.parseArray("[1.1]").get(0)).isInstanceOf(BigDecimal.class);
    }

    // ==================== 其他已对齐的默认行为 ====================

    @Test
    public void testEnumUsingName() {
        assertThat(JSON.toJSONString(new EnumHolder()))
                .isEqualTo(com.alibaba.fastjson.JSON.toJSONString(new EnumHolder()));
    }

    public enum Color { RED, GREEN }

    public static class EnumHolder {
        private Color color = Color.GREEN;
        public Color getColor() { return color; }
        public void setColor(Color c) { this.color = c; }
    }

    @Test
    public void testMapInsertionOrderPreserved() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("z", 1);
        map.put("a", 2);

        // Map 按插入顺序输出，与 fastjson 默认一致（MapSortField 默认关闭）
        assertThat(JSON.toJSONString(map)).isEqualTo("{\"z\":1,\"a\":2}");
    }

    @Test
    public void testIgnoreNotMatch_unknownFieldsIgnored() {
        OrderBean bean = JSON.parseObject("{\"apple\":\"x\",\"unknown\":1}", OrderBean.class);
        assertThat(bean.getApple()).isEqualTo("x");
    }
}
