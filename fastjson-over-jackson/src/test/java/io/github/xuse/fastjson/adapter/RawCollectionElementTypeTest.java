package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * 裸集合（无泛型参数）成员中，嵌套对象/数组的元素类型。
 * <p>
 * fastjson 会把嵌套结构还原为 {@code JSONObject} / {@code JSONArray}，
 * 因此迁移前的代码常见 {@code (JSONObject) rawList.get(0)} 这类强转。
 * 若兼容层给出 {@code LinkedHashMap} / {@code ArrayList}，
 * 该强转编译期无错、运行时抛 {@code ClassCastException}。
 * <p>
 * 每个用例都先断言原生 fastjson 的行为，再对兼容层作同样断言，
 * 便于直接对比两者差异。
 */
public class RawCollectionElementTypeTest {

    /** 成员刻意不带泛型参数 */
    @SuppressWarnings("rawtypes")
    public static class RawBean {
        private List list;
        private Map map;

        public List getList() { return list; }
        public void setList(List list) { this.list = list; }
        public Map getMap() { return map; }
        public void setMap(Map map) { this.map = map; }
    }

    private static final String JSON_TEXT =
            "{\"list\":[{\"k\":1},[2,3],\"s\",4],\"map\":{\"obj\":{\"k\":1},\"arr\":[2,3]}}";

    // ==================== 原生 fastjson 的行为基线 ====================

    @Test
    public void testFastjson_rawListNestedObjectIsJSONObject() {
        RawBean bean = com.alibaba.fastjson.JSON.parseObject(JSON_TEXT, RawBean.class);

        assertThat(bean.getList().get(0)).isInstanceOf(com.alibaba.fastjson.JSONObject.class);
        assertThat(bean.getList().get(1)).isInstanceOf(com.alibaba.fastjson.JSONArray.class);
        // 标量元素不受影响
        assertThat(bean.getList().get(2)).isInstanceOf(String.class);
        assertThat(bean.getList().get(3)).isInstanceOf(Integer.class);
    }

    @Test
    public void testFastjson_rawMapNestedValuesAreJSONTypes() {
        RawBean bean = com.alibaba.fastjson.JSON.parseObject(JSON_TEXT, RawBean.class);

        assertThat(bean.getMap().get("obj")).isInstanceOf(com.alibaba.fastjson.JSONObject.class);
        assertThat(bean.getMap().get("arr")).isInstanceOf(com.alibaba.fastjson.JSONArray.class);
    }

    /** 迁移前代码里常见的强转用法，在 fastjson 下成立 */
    @Test
    public void testFastjson_castToJSONObjectWorks() {
        RawBean bean = com.alibaba.fastjson.JSON.parseObject(JSON_TEXT, RawBean.class);

        com.alibaba.fastjson.JSONObject nested =
                (com.alibaba.fastjson.JSONObject) bean.getList().get(0);
        assertThat(nested.getIntValue("k")).isEqualTo(1);
    }

    // ==================== 兼容层的对应行为 ====================

    @Test
    public void testAdapter_rawListNestedObjectIsJSONObject() {
        RawBean bean = JSON.parseObject(JSON_TEXT, RawBean.class);

        assertThat(bean.getList().get(0)).isInstanceOf(JSONObject.class);
        assertThat(bean.getList().get(1)).isInstanceOf(JSONArray.class);
        assertThat(bean.getList().get(2)).isInstanceOf(String.class);
        assertThat(bean.getList().get(3)).isInstanceOf(Integer.class);
    }

    @Test
    public void testAdapter_rawMapNestedValuesAreJSONTypes() {
        RawBean bean = JSON.parseObject(JSON_TEXT, RawBean.class);

        assertThat(bean.getMap().get("obj")).isInstanceOf(JSONObject.class);
        assertThat(bean.getMap().get("arr")).isInstanceOf(JSONArray.class);
    }

    /** 迁移后仅改 import，强转应当同样成立 */
    @Test
    public void testAdapter_castToJSONObjectWorks() {
        RawBean bean = JSON.parseObject(JSON_TEXT, RawBean.class);

        JSONObject nested = (JSONObject) bean.getList().get(0);
        assertThat(nested.getIntValue("k")).isEqualTo(1);
    }

    // ==================== 回写序列化 ====================

    /**
     * 解析出的 {@link JSONObject} 作为字段值时须按 JSON 内容输出。
     * <p>
     * 若未注册容器序列化器，Jackson 会把 {@code getInner()} / {@code getInnerMap()} /
     * {@code isEmpty()} 当作 bean 属性，输出
     * {@code {"empty":false,"innerMap":{...},"inner":{...}}}。
     */
    @Test
    public void testReserialize_matchesFastjson() {
        String json = "{\"list\":[{\"k\":1}],\"map\":{\"obj\":{\"k\":1}}}";

        String fastjson = com.alibaba.fastjson.JSON.toJSONString(
                com.alibaba.fastjson.JSON.parseObject(json, RawBean.class));
        String adapter = JSON.toJSONString(JSON.parseObject(json, RawBean.class));

        assertThat(adapter).isEqualTo(fastjson).doesNotContain("innerMap");
    }

    @Test
    public void testReserialize_nestedJSONObjectValue() {
        String json = "{\"k\":[1,{\"n\":2}]}";

        assertThat(JSON.toJSONString(JSON.parseObject(json)))
                .isEqualTo(com.alibaba.fastjson.JSON.toJSONString(
                        com.alibaba.fastjson.JSON.parseObject(json)));
    }

    // ==================== 标量元素不受影响 ====================

    /** 仅容器类型改变；标量仍遵循既有语义（含 UseBigDecimal） */
    @Test
    public void testScalarTypesUnaffected() {
        RawBean bean = JSON.parseObject(
                "{\"list\":[\"s\",1,1.5,true,123456789012345678901]}", RawBean.class);

        assertThat(bean.getList().get(0)).isInstanceOf(String.class);
        assertThat(bean.getList().get(1)).isInstanceOf(Integer.class);
        assertThat(bean.getList().get(2)).isInstanceOf(java.math.BigDecimal.class);
        assertThat(bean.getList().get(3)).isInstanceOf(Boolean.class);
        assertThat(bean.getList().get(4)).isInstanceOf(java.math.BigInteger.class);
    }

    /** Object 类型的成员同样还原为 JSONObject */
    public static class ObjectFieldBean {
        private Object any;
        public Object getAny() { return any; }
        public void setAny(Object any) { this.any = any; }
    }

    @Test
    public void testObjectField_isJSONObject() {
        assertThat(JSON.parseObject("{\"any\":{\"k\":1}}", ObjectFieldBean.class).getAny())
                .isInstanceOf(JSONObject.class);
        assertThat(JSON.parseObject("{\"any\":[1]}", ObjectFieldBean.class).getAny())
                .isInstanceOf(JSONArray.class);
        assertThat(JSON.parseObject("{\"any\":null}", ObjectFieldBean.class).getAny()).isNull();
    }

    // ==================== 带泛型的集合：两者本已一致 ====================

    public static class TypedBean {
        private List<Map<String, Object>> list;
        public List<Map<String, Object>> getList() { return list; }
        public void setList(List<Map<String, Object>> list) { this.list = list; }
    }

    /**
     * 声明了 {@code Map<String,Object>} 时，fastjson 与 Jackson 都按声明类型还原为
     * 普通 Map，不涉及 JSONObject。此用例固定该结论，说明差异仅限裸集合。
     */
    @Test
    public void testTypedCollection_bothUsePlainMap() {
        String json = "{\"list\":[{\"k\":1}]}";

        Map<String, Object> fastjson =
                com.alibaba.fastjson.JSON.parseObject(json, TypedBean.class).getList().get(0);
        Map<String, Object> adapter = JSON.parseObject(json, TypedBean.class).getList().get(0);

        assertThat(fastjson).isInstanceOf(Map.class);
        assertThat(adapter).isInstanceOf(Map.class);
        assertThat(adapter.get("k")).isEqualTo(fastjson.get("k"));
    }
}
