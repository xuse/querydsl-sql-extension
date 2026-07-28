package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 补充优先级为"中"的缺失测试场景。
 */
public class MissingCoverageTest {

    // ==================== parseObject(InputStream, ...) ====================

    @Test
    public void testParseObject_inputStream_type() throws IOException {
        String json = "{\"name\":\"Alice\",\"age\":28}";
        ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> result = JSON.parseObject(is, Map.class);

        assertThat(result.get("name")).isEqualTo("Alice");
        assertThat(result.get("age")).isEqualTo(28);
    }

    @Test
    public void testParseObject_inputStream_class() throws IOException {
        String json = "{\"name\":\"Bob\",\"age\":30}";
        ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        SimpleUser user = JSON.parseObject(is, SimpleUser.class);

        assertThat(user.getName()).isEqualTo("Bob");
        assertThat(user.getAge()).isEqualTo(30);
    }

    @Test
    public void testParseObject_inputStream_withCharset() throws IOException {
        String json = "{\"name\":\"中文\",\"age\":1}";
        ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        SimpleUser user = JSON.parseObject(is, StandardCharsets.UTF_8, SimpleUser.class);

        assertThat(user.getName()).isEqualTo("中文");
        assertThat(user.getAge()).isEqualTo(1);
    }

    @Test
    public void testParseObject_inputStream_null() throws IOException {
        assertThat(JSON.parseObject((java.io.InputStream) null, SimpleUser.class)).isNull();
    }

    // ==================== parseObject(byte[], ...) ====================

    @Test
    public void testParseObject_bytes_class() {
        String json = "{\"name\":\"Charlie\",\"age\":22}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        SimpleUser user = JSON.parseObject(bytes, SimpleUser.class);

        assertThat(user.getName()).isEqualTo("Charlie");
        assertThat(user.getAge()).isEqualTo(22);
    }

    @Test
    public void testParseObject_bytes_type() {
        String json = "{\"name\":\"Dave\",\"age\":35}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        SimpleUser user = JSON.parseObject(bytes, (java.lang.reflect.Type) SimpleUser.class);

        assertThat(user.getName()).isEqualTo("Dave");
        assertThat(user.getAge()).isEqualTo(35);
    }

    @Test
    public void testParseObject_bytes_nullAndEmpty() {
        assertThat(JSON.parseObject((byte[]) null, SimpleUser.class)).isNull();
        assertThat(JSON.parseObject(new byte[0], SimpleUser.class)).isNull();
    }

    // ==================== parseObject(String, Type) 泛型 ====================

    @Test
    public void testParseObject_genericType_mapStringObject() {
        String json = "{\"k1\":\"v1\",\"k2\":123}";
        java.lang.reflect.Type type = new TypeReference<Map<String, Object>>() {}.getType();

        Map<String, Object> map = JSON.parseObject(json, type);

        assertThat(map.get("k1")).isEqualTo("v1");
        assertThat(map.get("k2")).isEqualTo(123);
    }

    @Test
    public void testParseObject_genericType_listOfUser() {
        String json = "[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]";
        java.lang.reflect.Type type = new TypeReference<List<SimpleUser>>() {}.getType();

        List<SimpleUser> list = JSON.parseObject(json, type);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getName()).isEqualTo("A");
        assertThat(list.get(1).getAge()).isEqualTo(2);
    }

    @Test
    public void testParseObject_genericType_nullAndEmpty() {
        java.lang.reflect.Type type = new TypeReference<Map<String, Object>>() {}.getType();
        assertThat(JSON.<Map<String, Object>>parseObject((String)null, type)).isNull();
        assertThat(JSON.<Map<String, Object>>parseObject("", type)).isNull();
    }

    // ==================== parseObject(String, TypeReference) ====================

    @Test
    public void testParseObject_typeReference_map() {
        String json = "{\"x\":1,\"y\":2}";

        Map<String, Integer> map = JSON.parseObject(json, new TypeReference<Map<String, Integer>>() {});

        assertThat(map).containsEntry("x", 1).containsEntry("y", 2);
    }

    @Test
    public void testParseObject_typeReference_listOfString() {
        String json = "[\"hello\",\"world\"]";

        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() {});

        assertThat(list).containsExactly("hello", "world");
    }

    @Test
    public void testParseObject_typeReference_nestedGenerics() {
        String json = "{\"users\":[{\"name\":\"X\",\"age\":10}]}";

        Map<String, List<SimpleUser>> result = JSON.parseObject(json,
                new TypeReference<Map<String, List<SimpleUser>>>() {});

        assertThat(result.get("users")).hasSize(1);
        assertThat(result.get("users").get(0).getName()).isEqualTo("X");
    }

    @Test
    public void testParseObject_typeReference_null() {
        assertThat(JSON.parseObject(null, new TypeReference<Map<String, Object>>() {})).isNull();
    }

    // ==================== JSONArray.add / toJavaList / iterator ====================

    @Test
    public void testJSONArray_add_variousTypes() {
        JSONArray arr = new JSONArray();
        arr.add("text");
        arr.add(42);
        arr.add(3.14);
        arr.add(100L);
        arr.add(true);
        arr.add(null);

        assertThat(arr.size()).isEqualTo(6);
        assertThat(arr.getString(0)).isEqualTo("text");
        assertThat(arr.getIntValue(1)).isEqualTo(42);
        assertThat(arr.getDouble(2)).isEqualTo(3.14);
        assertThat(arr.getLong(3)).isEqualTo(100L);
        assertThat(arr.getBoolean(4)).isTrue();
        assertThat(arr.get(5)).isNull();
    }

    @Test
    public void testJSONArray_add_jsonObject() {
        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("id", 1);
        arr.add(obj);

        assertThat(arr.getJSONObject(0).getIntValue("id")).isEqualTo(1);
    }

    @Test
    public void testJSONArray_add_nestedArray() {
        JSONArray arr = new JSONArray();
        JSONArray inner = new JSONArray();
        inner.add("nested");
        arr.add(inner);

        assertThat(arr.getJSONArray(0).getString(0)).isEqualTo("nested");
    }

    @Test
    public void testJSONArray_toJavaList() {
        String json = "[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]";
        JSONArray arr = JSON.parseArray(json);

        List<SimpleUser> users = arr.toJavaList(SimpleUser.class);

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("A");
        assertThat(users.get(1).getAge()).isEqualTo(2);
    }

    @Test
    public void testJSONArray_toJavaList_primitives() {
        String json = "[1,2,3,4,5]";
        JSONArray arr = JSON.parseArray(json);

        List<Integer> list = arr.toJavaList(Integer.class);

        assertThat(list).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    public void testJSONArray_iterator() {
        String json = "[\"a\",1,true]";
        JSONArray arr = JSON.parseArray(json);

        int count = 0;
        for (Object item : arr) {
            count++;
        }
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void testJSONArray_iterator_collectValues() {
        String json = "[10,20,30]";
        JSONArray arr = JSON.parseArray(json);

        int sum = 0;
        for (Object item : arr) {
            sum += (Integer) item;
        }
        assertThat(sum).isEqualTo(60);
    }

    // ==================== FlexibleDateDeserializer 各格式分支 ====================

    @Test
    public void testDateDeserialize_timestamp_millis() {
        // 13位数字 → 毫秒时间戳
        String json = "{\"date\":1700000000000}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isEqualTo(new Date(1700000000000L));
    }

    @Test
    public void testDateDeserialize_timestamp_seconds_string() {
        // 10位纯数字字符串 → 秒时间戳
        String json = "{\"date\":\"1700000000\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isEqualTo(new Date(1700000000000L));
    }

    @Test
    public void testDateDeserialize_timestamp_millis_string() {
        // 13位纯数字字符串 → 毫秒时间戳
        String json = "{\"date\":\"1700000000000\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isEqualTo(new Date(1700000000000L));
    }

    @Test
    public void testDateDeserialize_yyyyMMdd() {
        String json = "{\"date\":\"20231114\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        // 验证年月日
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.YEAR)).isEqualTo(2023);
        assertThat(cal.get(java.util.Calendar.MONTH)).isEqualTo(java.util.Calendar.NOVEMBER);
        assertThat(cal.get(java.util.Calendar.DAY_OF_MONTH)).isEqualTo(14);
    }

    @Test
    public void testDateDeserialize_yyyy_MM_dd() {
        String json = "{\"date\":\"2023-11-14\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.YEAR)).isEqualTo(2023);
        assertThat(cal.get(java.util.Calendar.MONTH)).isEqualTo(java.util.Calendar.NOVEMBER);
        assertThat(cal.get(java.util.Calendar.DAY_OF_MONTH)).isEqualTo(14);
    }

    @Test
    public void testDateDeserialize_yyyy_MM_dd_HH_mm() {
        String json = "{\"date\":\"2023-11-14 10:30\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.HOUR_OF_DAY)).isEqualTo(10);
        assertThat(cal.get(java.util.Calendar.MINUTE)).isEqualTo(30);
    }

    @Test
    public void testDateDeserialize_yyyy_MM_dd_HH_mm_ss() {
        String json = "{\"date\":\"2023-11-14 10:30:45\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.SECOND)).isEqualTo(45);
    }

    @Test
    public void testDateDeserialize_iso8601_noMillis() {
        // yyyy-MM-dd'T'HH:mm:ss
        String json = "{\"date\":\"2023-11-14T10:30:45\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.HOUR_OF_DAY)).isEqualTo(10);
        assertThat(cal.get(java.util.Calendar.MINUTE)).isEqualTo(30);
        assertThat(cal.get(java.util.Calendar.SECOND)).isEqualTo(45);
    }

    @Test
    public void testDateDeserialize_yyyy_MM_dd_HH_mm_ss_SSS() {
        String json = "{\"date\":\"2023-11-14 10:30:45.123\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
    }

    @Test
    public void testDateDeserialize_iso8601_withMillis() {
        // yyyy-MM-dd'T'HH:mm:ss.SSS
        String json = "{\"date\":\"2023-11-14T10:30:45.123\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
    }

    @Test
    public void testDateDeserialize_iso8601_withTimezone() {
        // yyyy-MM-dd'T'HH:mm:ss+08:00
        String json = "{\"date\":\"2023-11-14T10:30:45+08:00\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
    }

    @Test
    public void testDateDeserialize_iso8601_withMillisAndTimezone() {
        // yyyy-MM-dd'T'HH:mm:ss.SSS+08:00
        String json = "{\"date\":\"2023-11-14T10:30:45.123+08:00\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
    }

    @Test
    public void testDateDeserialize_yyyyMMddHHmm() {
        // 12位纯数字
        String json = "{\"date\":\"202311141030\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.YEAR)).isEqualTo(2023);
        assertThat(cal.get(java.util.Calendar.HOUR_OF_DAY)).isEqualTo(10);
        assertThat(cal.get(java.util.Calendar.MINUTE)).isEqualTo(30);
    }

    @Test
    public void testDateDeserialize_yyyyMMddHHmmss() {
        // 14位纯数字
        String json = "{\"date\":\"20231114103045\"}";
        DateHolder holder = JSON.parseObject(json, DateHolder.class);
        assertThat(holder.getDate()).isNotNull();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(holder.getDate());
        assertThat(cal.get(java.util.Calendar.SECOND)).isEqualTo(45);
    }

    @Test
    public void testDateDeserialize_nullAndEmpty() {
        assertThat(JSON.parseObject("{\"date\":null}", DateHolder.class).getDate()).isNull();
        assertThat(JSON.parseObject("{\"date\":\"\"}", DateHolder.class).getDate()).isNull();
        assertThat(JSON.parseObject("{\"date\":\"null\"}", DateHolder.class).getDate()).isNull();
    }

    // ==================== 辅助 POJO ====================

    public static class SimpleUser {
        private String name;
        private int age;

        public SimpleUser() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class DateHolder {
        private Date date;

        public DateHolder() {}
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }
}
