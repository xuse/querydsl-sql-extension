package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.Data;

/**
 * 验证 Jackson 兼容层与 fastjson 输出的等效性。
 * <p>
 * 每个测试方法同时调用 fastjson 和新兼容层，断言结果一致。
 */
public class JsonCompatibilityTest {

    // ==================== 测试用 POJO ====================

	@Data
    public static class User {
        private String name;
        private int age;
        private Date createTime;
        private LocalDate update;
        private LocalDateTime updateTime;
        private List<String> tags;
        private Address address;

        public User() {}
        public User(String name, int age, Date createTime, List<String> tags, Address address) {
            this.name = name;
            this.age = age;
            this.createTime = createTime;
            this.tags = tags;
            this.address = address;
            this.update=LocalDate.now();
            this.updateTime=LocalDateTime.now();
        }
    }

    public static class Address {
        private String city;
        private String street;

        public Address() {}
        public Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }

    private User buildSampleUser() {
        return new User("张三", 30, new Date(1700000000000L),
                Arrays.asList("iot", "simcard"),
                new Address("杭州", "滨江区"));
    }

    // ==================== toJSONString 等效性 ====================

    @Test
    public void testToJSONString_equivalence() {
        User user = buildSampleUser();

        String fastjsonResult = com.alibaba.fastjson.JSON.toJSONString(user);
        String jacksonResult = JSON.toJSONString(user);

        // 两者反序列化后字段值一致（JSON 字段顺序可能不同，但内容等价）
        User fromFastjson = com.alibaba.fastjson.JSON.parseObject(fastjsonResult, User.class);
        User fromJackson = JSON.parseObject(jacksonResult, User.class);

        assertThat(fromJackson.getName()).isEqualTo(fromFastjson.getName());
        assertThat(fromJackson.getAge()).isEqualTo(fromFastjson.getAge());
        assertThat(fromJackson.getCreateTime()).isEqualTo(fromFastjson.getCreateTime());
        assertThat(fromJackson.getTags()).isEqualTo(fromFastjson.getTags());
        assertThat(fromJackson.getAddress().getCity()).isEqualTo(fromFastjson.getAddress().getCity());
        assertThat(fromJackson.getAddress().getStreet()).isEqualTo(fromFastjson.getAddress().getStreet());
    }

    @Test
    public void testToJSONString_null() {
        String fastjsonResult = com.alibaba.fastjson.JSON.toJSONString(null);
        String jacksonResult = JSON.toJSONString(null);
        assertThat(jacksonResult).isEqualTo(fastjsonResult);
    }

    @Test
    public void testToJSONString_simpleTypes() {
        // 数字
        assertThat(JSON.toJSONString(123)).isEqualTo(com.alibaba.fastjson.JSON.toJSONString(123));
        // 字符串
        assertThat(JSON.toJSONString("hello")).isEqualTo(com.alibaba.fastjson.JSON.toJSONString("hello"));
        // boolean
        assertThat(JSON.toJSONString(true)).isEqualTo(com.alibaba.fastjson.JSON.toJSONString(true));
    }

    // ==================== parseObject 等效性 ====================

    @Test
    public void testParseObject_equivalence() {
        String json = "{\"name\":\"李四\",\"age\":25,\"tags\":[\"a\",\"b\"],\"address\":{\"city\":\"北京\",\"street\":\"朝阳\"}}";

        User fastjsonUser = com.alibaba.fastjson.JSON.parseObject(json, User.class);
        User jacksonUser = JSON.parseObject(json, User.class);

        assertThat(jacksonUser.getName()).isEqualTo(fastjsonUser.getName());
        assertThat(jacksonUser.getAge()).isEqualTo(fastjsonUser.getAge());
        assertThat(jacksonUser.getTags()).isEqualTo(fastjsonUser.getTags());
        assertThat(jacksonUser.getAddress().getCity()).isEqualTo(fastjsonUser.getAddress().getCity());
    }

    @Test
    public void testParseObject_ignoreUnknownFields() {
        // fastjson 默认忽略未知字段，Jackson 兼容层也应该
        String json = "{\"name\":\"王五\",\"age\":20,\"unknownField\":\"xxx\"}";

        User fastjsonUser = com.alibaba.fastjson.JSON.parseObject(json, User.class);
        User jacksonUser = JSON.parseObject(json, User.class);

        assertThat(jacksonUser.getName()).isEqualTo(fastjsonUser.getName());
        assertThat(jacksonUser.getAge()).isEqualTo(fastjsonUser.getAge());
    }

    @Test
    public void testParseObject_nullAndEmpty() {
        assertThat(JSON.parseObject((String)null, User.class)).isNull();
        assertThat(JSON.parseObject("", User.class)).isNull();

        assertThat(com.alibaba.fastjson.JSON.parseObject((String) null, User.class)).isNull();
        assertThat(com.alibaba.fastjson.JSON.parseObject("", User.class)).isNull();
    }

    @Test
    public void testParseObject_toJSONObject() {
        String json = "{\"name\":\"test\",\"value\":123,\"nested\":{\"k\":\"v\"}}";

        com.alibaba.fastjson.JSONObject fastjsonObj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jacksonObj = JSON.parseObject(json);

        assertThat(jacksonObj.getString("name")).isEqualTo(fastjsonObj.getString("name"));
        assertThat(jacksonObj.getIntValue("value")).isEqualTo(fastjsonObj.getIntValue("value"));
        assertThat(jacksonObj.getJSONObject("nested").getString("k"))
                .isEqualTo(fastjsonObj.getJSONObject("nested").getString("k"));
    }

    // ==================== parseArray 等效性 ====================

    @Test
    public void testParseArray_equivalence() {
        String json = "[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}]";

        List<User> fastjsonList = com.alibaba.fastjson.JSON.parseArray(json, User.class);
        List<User> jacksonList = JSON.parseArray(json, User.class);

        assertThat(jacksonList).hasSameSizeAs(fastjsonList);
        for (int i = 0; i < fastjsonList.size(); i++) {
            assertThat(jacksonList.get(i).getName()).isEqualTo(fastjsonList.get(i).getName());
            assertThat(jacksonList.get(i).getAge()).isEqualTo(fastjsonList.get(i).getAge());
        }
    }

    @Test
    public void testParseArray_primitives() {
        String json = "[1,2,3,4,5]";

        List<Integer> fastjsonList = com.alibaba.fastjson.JSON.parseArray(json, Integer.class);
        List<Integer> jacksonList = JSON.parseArray(json, Integer.class);

        assertThat(jacksonList).isEqualTo(fastjsonList);
    }

    @Test
    public void testParseArray_doubles() {
        String json = "[1.1,2.2,3.3]";

        List<Double> fastjsonList = com.alibaba.fastjson.JSON.parseArray(json, Double.class);
        List<Double> jacksonList = JSON.parseArray(json, Double.class);

        assertThat(jacksonList).isEqualTo(fastjsonList);
    }

    @Test
    public void testParseArray_nullAndEmpty() {
        assertThat(JSON.parseArray(null, Integer.class)).isNull();
        assertThat(JSON.parseArray("", Integer.class)).isNull();
    }

    // ==================== parse 等效性 ====================

    @Test
    public void testParse_object() {
        String json = "{\"key\":\"value\"}";

        Object fastjsonResult = com.alibaba.fastjson.JSON.parse(json);
        Object jacksonResult = JSON.parse(json);

        // 两者都返回 JSONObject-like 对象
        assertThat(jacksonResult).isInstanceOf(JSONObject.class);
        assertThat(((JSONObject) jacksonResult).getString("key")).isEqualTo("value");
    }

    @Test
    public void testParse_array() {
        String json = "[1,2,3]";

        Object fastjsonResult = com.alibaba.fastjson.JSON.parse(json);
        Object jacksonResult = JSON.parse(json);

        assertThat(jacksonResult).isInstanceOf(JSONArray.class);
        assertThat(((JSONArray) jacksonResult).size()).isEqualTo(3);
    }

    @Test
    public void testParse_null() {
        assertThat(JSON.parse(null)).isNull();
        assertThat(JSON.parse("")).isNull();
    }

    // ==================== toJavaObject 等效性 ====================

    @Test
    public void testToJavaObject_equivalence() {
        String json = "{\"name\":\"test\",\"age\":18,\"address\":{\"city\":\"深圳\",\"street\":\"南山\"}}";

        com.alibaba.fastjson.JSONObject fastjsonObj = com.alibaba.fastjson.JSON.parseObject(json);
        User fastjsonUser = com.alibaba.fastjson.JSON.toJavaObject(fastjsonObj, User.class);

        JSONObject jacksonObj = JSON.parseObject(json);
        User jacksonUser = JSON.toJavaObject(jacksonObj, User.class);

        assertThat(jacksonUser.getName()).isEqualTo(fastjsonUser.getName());
        assertThat(jacksonUser.getAge()).isEqualTo(fastjsonUser.getAge());
        assertThat(jacksonUser.getAddress().getCity()).isEqualTo(fastjsonUser.getAddress().getCity());
    }

    // ==================== JSONObject 方法等效性 ====================

    @Test
    public void testJSONObject_getString() {
        String json = "{\"s\":\"hello\",\"n\":123,\"b\":true,\"nil\":null}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getString("s")).isEqualTo(fj.getString("s"));
        assertThat(jk.getString("nil")).isEqualTo(fj.getString("nil"));
    }

    @Test
    public void testJSONObject_getInteger() {
        String json = "{\"num\":42,\"str\":\"99\"}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getInteger("num")).isEqualTo(fj.getInteger("num"));
        assertThat(jk.getIntValue("num")).isEqualTo(fj.getIntValue("num"));
    }

    @Test
    public void testJSONObject_getLong() {
        String json = "{\"big\":9999999999}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getLong("big")).isEqualTo(fj.getLong("big"));
        assertThat(jk.getLongValue("big")).isEqualTo(fj.getLongValue("big"));
    }

    @Test
    public void testJSONObject_getBoolean() {
        String json = "{\"flag\":true,\"off\":false}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getBoolean("flag")).isEqualTo(fj.getBoolean("flag"));
        assertThat(jk.getBooleanValue("off")).isEqualTo(fj.getBooleanValue("off"));
    }

    @Test
    public void testJSONObject_getJSONObject() {
        String json = "{\"inner\":{\"x\":1,\"y\":2}}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getJSONObject("inner").getIntValue("x"))
                .isEqualTo(fj.getJSONObject("inner").getIntValue("x"));
        assertThat(jk.getJSONObject("inner").getIntValue("y"))
                .isEqualTo(fj.getJSONObject("inner").getIntValue("y"));
    }

    @Test
    public void testJSONObject_getJSONArray() {
        String json = "{\"arr\":[10,20,30]}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getJSONArray("arr").size()).isEqualTo(fj.getJSONArray("arr").size());
        assertThat(jk.getJSONArray("arr").getIntValue(0)).isEqualTo(fj.getJSONArray("arr").getIntValue(0));
        assertThat(jk.getJSONArray("arr").getIntValue(2)).isEqualTo(fj.getJSONArray("arr").getIntValue(2));
    }

    @Test
    public void testJSONObject_containsKey() {
        String json = "{\"exist\":1}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.containsKey("exist")).isEqualTo(fj.containsKey("exist"));
        assertThat(jk.containsKey("notExist")).isEqualTo(fj.containsKey("notExist"));
    }

    @Test
    public void testJSONObject_put_and_remove() {
        JSONObject jk = new JSONObject();
        com.alibaba.fastjson.JSONObject fj = new com.alibaba.fastjson.JSONObject();

        jk.put("a", "hello");
        fj.put("a", "hello");
        jk.put("b", 123);
        fj.put("b", 123);

        assertThat(jk.getString("a")).isEqualTo(fj.getString("a"));
        assertThat(jk.getIntValue("b")).isEqualTo(fj.getIntValue("b"));

        jk.remove("a");
        fj.remove("a");
        assertThat(jk.containsKey("a")).isEqualTo(fj.containsKey("a"));
    }

    @Test
    public void testJSONObject_size() {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.size()).isEqualTo(fj.size());
    }

    @Test
    public void testJSONObject_parseObject_static() {
        String json = "{\"k\":\"v\"}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSONObject.parseObject(json);
        JSONObject jk = JSONObject.parseObject(json);

        assertThat(jk.getString("k")).isEqualTo(fj.getString("k"));
    }

    @Test
    public void testJSONObject_toJSONString_static() {
        User user = buildSampleUser();

        String fjResult = com.alibaba.fastjson.JSONObject.toJSONString(user);
        String jkResult = JSONObject.toJSONString(user);

        // 反序列化验证内容等价
        User fromFj = com.alibaba.fastjson.JSON.parseObject(fjResult, User.class);
        User fromJk = JSON.parseObject(jkResult, User.class);

        assertThat(fromJk.getName()).isEqualTo(fromFj.getName());
        assertThat(fromJk.getAge()).isEqualTo(fromFj.getAge());
    }

    // ==================== JSONArray 方法等效性 ====================

    @Test
    public void testJSONArray_get() {
        String json = "[\"a\",1,true,null]";

        com.alibaba.fastjson.JSONArray fj = com.alibaba.fastjson.JSON.parseArray(json);
        JSONArray jk = JSON.parseArray(json);

        assertThat(jk.size()).isEqualTo(fj.size());
        assertThat(jk.getString(0)).isEqualTo(fj.getString(0));
        assertThat(jk.getIntValue(1)).isEqualTo(fj.getIntValue(1));
        assertThat(jk.getBoolean(2)).isEqualTo(fj.getBoolean(2));
    }

    @Test
    public void testJSONArray_getJSONObject() {
        String json = "[{\"id\":1},{\"id\":2}]";

        com.alibaba.fastjson.JSONArray fj = com.alibaba.fastjson.JSON.parseArray(json);
        JSONArray jk = JSON.parseArray(json);

        assertThat(jk.getJSONObject(0).getIntValue("id"))
                .isEqualTo(fj.getJSONObject(0).getIntValue("id"));
        assertThat(jk.getJSONObject(1).getIntValue("id"))
                .isEqualTo(fj.getJSONObject(1).getIntValue("id"));
    }

    @Test
    public void testJSONArray_getLongValue() {
        String json = "[100,200,9999999999]";

        com.alibaba.fastjson.JSONArray fj = com.alibaba.fastjson.JSON.parseArray(json);
        JSONArray jk = JSON.parseArray(json);

        assertThat(jk.getLongValue(2)).isEqualTo(fj.getLongValue(2));
    }

    // ==================== 边界场景 ====================

    @Test
    public void testDeepNesting() {
        String json = "{\"l1\":{\"l2\":{\"l3\":{\"value\":\"deep\"}}}}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        String fjDeep = fj.getJSONObject("l1").getJSONObject("l2").getJSONObject("l3").getString("value");
        String jkDeep = jk.getJSONObject("l1").getJSONObject("l2").getJSONObject("l3").getString("value");

        assertThat(jkDeep).isEqualTo(fjDeep);
    }

    @Test
    public void testSpecialCharacters() {
        String json = "{\"msg\":\"中文测试\\n换行\\t制表符\\\"\"}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.getString("msg")).isEqualTo(fj.getString("msg"));
    }

    @Test
    public void testEmptyObject() {
        String json = "{}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        assertThat(jk.size()).isEqualTo(fj.size());
        assertThat(jk.isEmpty()).isTrue();
    }

    @Test
    public void testEmptyArray() {
        String json = "[]";

        List<User> fjList = com.alibaba.fastjson.JSON.parseArray(json, User.class);
        List<User> jkList = JSON.parseArray(json, User.class);

        assertThat(jkList).hasSameSizeAs(fjList);
        assertThat(jkList).isEmpty();
    }

    @Test
    public void testInvalidJson_throwsException() {
        String badJson = "not a json";

        assertThatThrownBy(() -> JSON.parseObject(badJson, User.class))
                .isInstanceOf(JSONException.class);
    }

    @Test
    public void testBigDecimalParsing() {
        String json = "{\"amount\":99.99}";

        com.alibaba.fastjson.JSONObject fj = com.alibaba.fastjson.JSON.parseObject(json);
        JSONObject jk = JSON.parseObject(json);

        // 比较 double 值
        assertThat(jk.getDouble("amount")).isEqualTo(fj.getDouble("amount"));
    }

    // ==================== 实际业务场景模拟 ====================

    @Test
    public void testRealWorldPattern_serializeAndDeserialize() {
        // 模拟项目中最常见的用法: serialize -> store/transfer -> deserialize
        User original = buildSampleUser();

        // Jackson 兼容层序列化
        String jacksonJson = JSON.toJSONString(original);
        // fastjson 反序列化（模拟接收方还在用 fastjson）
        User fromFastjson = com.alibaba.fastjson.JSON.parseObject(jacksonJson, User.class);

        assertThat(fromFastjson.getName()).isEqualTo(original.getName());
        assertThat(fromFastjson.getAge()).isEqualTo(original.getAge());
        assertThat(fromFastjson.getTags()).isEqualTo(original.getTags());

        // 反方向：fastjson 序列化，Jackson 兼容层反序列化
        String fastjsonJson = com.alibaba.fastjson.JSON.toJSONString(original);
        User fromJackson = JSON.parseObject(fastjsonJson, User.class);

        assertThat(fromJackson.getName()).isEqualTo(original.getName());
        assertThat(fromJackson.getAge()).isEqualTo(original.getAge());
        assertThat(fromJackson.getTags()).isEqualTo(original.getTags());
    }

    @Test
    public void testRealWorldPattern_parseAndNavigate() {
        // 模拟项目中 parse JSON 响应然后 getJSONObject().getString() 的链式调用模式
        String responseJson = "{\"code\":0,\"data\":{\"iccid\":\"89860123456789\",\"status\":\"ACTIVE\",\"usage\":{\"total\":1024,\"used\":512}}}";

        com.alibaba.fastjson.JSONObject fjResp = com.alibaba.fastjson.JSON.parseObject(responseJson);
        JSONObject jkResp = JSON.parseObject(responseJson);

        // 链式导航
        assertThat(jkResp.getIntValue("code")).isEqualTo(fjResp.getIntValue("code"));
        assertThat(jkResp.getJSONObject("data").getString("iccid"))
                .isEqualTo(fjResp.getJSONObject("data").getString("iccid"));
        assertThat(jkResp.getJSONObject("data").getString("status"))
                .isEqualTo(fjResp.getJSONObject("data").getString("status"));
        assertThat(jkResp.getJSONObject("data").getJSONObject("usage").getIntValue("total"))
                .isEqualTo(fjResp.getJSONObject("data").getJSONObject("usage").getIntValue("total"));
    }

    // ==================== @JSONField 注解等效性 ====================

    /** 使用自定义 @JSONField(name=...) 的 POJO */
    public static class DeviceInfo {
        @JSONField(name = "device_id")
        private String deviceId;

        @JSONField(name = "card_type")
        private int cardType;

        private String normalField;

        public DeviceInfo() {}

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public int getCardType() { return cardType; }
        public void setCardType(int cardType) { this.cardType = cardType; }
        public String getNormalField() { return normalField; }
        public void setNormalField(String normalField) { this.normalField = normalField; }
    }

    /** 使用 @JSONField(alternateNames=...) 的 POJO */
    public static class CardResponse {
        @JSONField(name = "iccid", alternateNames = {"ICCID", "icc_id"})
        private String iccid;

        @JSONField(name = "status", alternateNames = {"card_status", "cardStatus"})
        private String status;

        public CardResponse() {}

        public String getIccid() { return iccid; }
        public void setIccid(String iccid) { this.iccid = iccid; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Test
    public void testJSONField_name_serialize() {
        DeviceInfo info = new DeviceInfo();
        info.setDeviceId("DEV001");
        info.setCardType(5);
        info.setNormalField("hello");

        String json = JSON.toJSONString(info);
        JSONObject obj = JSON.parseObject(json);

        // @JSONField(name="device_id") 序列化时使用指定名称
        assertThat(obj.getString("device_id")).isEqualTo("DEV001");
        assertThat(obj.getIntValue("card_type")).isEqualTo(5);
        // 无注解字段使用原字段名
        assertThat(obj.getString("normalField")).isEqualTo("hello");
    }

    @Test
    public void testJSONField_name_deserialize() {
        String json = "{\"device_id\":\"DEV002\",\"card_type\":3,\"normalField\":\"world\"}";

        DeviceInfo info = JSON.parseObject(json, DeviceInfo.class);

        assertThat(info.getDeviceId()).isEqualTo("DEV002");
        assertThat(info.getCardType()).isEqualTo(3);
        assertThat(info.getNormalField()).isEqualTo("world");
    }

    @Test
    public void testJSONField_alternateNames_primaryName() {
        // 使用主名称
        String json = "{\"iccid\":\"89860001\",\"status\":\"ACTIVE\"}";

        CardResponse resp = JSON.parseObject(json, CardResponse.class);

        assertThat(resp.getIccid()).isEqualTo("89860001");
        assertThat(resp.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    public void testJSONField_alternateNames_alternate1() {
        // 使用别名 ICCID、card_status
        String json = "{\"ICCID\":\"89860002\",\"card_status\":\"DEACTIVATED\"}";

        CardResponse resp = JSON.parseObject(json, CardResponse.class);

        assertThat(resp.getIccid()).isEqualTo("89860002");
        assertThat(resp.getStatus()).isEqualTo("DEACTIVATED");
    }

    @Test
    public void testJSONField_alternateNames_alternate2() {
        // 使用另一组别名 icc_id、cardStatus
        String json = "{\"icc_id\":\"89860003\",\"cardStatus\":\"SUSPENDED\"}";

        CardResponse resp = JSON.parseObject(json, CardResponse.class);

        assertThat(resp.getIccid()).isEqualTo("89860003");
        assertThat(resp.getStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    public void testJSONField_alternateNames_serialize_usesPrimaryName() {
        // 序列化时始终使用主名称，不使用别名
        CardResponse resp = new CardResponse();
        resp.setIccid("89860004");
        resp.setStatus("ACTIVE");

        String json = JSON.toJSONString(resp);
        JSONObject obj = JSON.parseObject(json);

        assertThat(obj.containsKey("iccid")).isTrue();
        assertThat(obj.containsKey("ICCID")).isFalse();
        assertThat(obj.containsKey("icc_id")).isFalse();
        assertThat(obj.getString("status")).isEqualTo("ACTIVE");
    }

    @Test
    public void testJSONField_coexistsWithJsonAlias() {
        // 验证 Jackson 原生注解不受影响（如果有类同时使用 @JsonAlias）
        // 这里直接验证无 @JSONField 的普通 POJO 仍正常工作
        String json = "{\"name\":\"test\",\"age\":20}";
        User user = JSON.parseObject(json, User.class);
        assertThat(user.getName()).isEqualTo("test");
        assertThat(user.getAge()).isEqualTo(20);
    }

    // ==================== @JSONField serialize/deserialize/format ====================

    /** serialize=false 的字段不出现在序列化结果中 */
    public static class SensitiveData {
        private String publicInfo;

        @JSONField(serialize = false)
        private String secret;

        @JSONField(deserialize = false)
        private String computedField;

        public SensitiveData() {}

        public String getPublicInfo() { return publicInfo; }
        public void setPublicInfo(String publicInfo) { this.publicInfo = publicInfo; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getComputedField() { return computedField; }
        public void setComputedField(String computedField) { this.computedField = computedField; }
    }

    @Test
    public void testJSONField_serializeFalse() {
        SensitiveData data = new SensitiveData();
        data.setPublicInfo("visible");
        data.setSecret("hidden_password");
        data.setComputedField("calc_value");

        String json = JSON.toJSONString(data);
        JSONObject obj = JSON.parseObject(json);

        // serialize=false → 不出现在 JSON 中
        assertThat(obj.containsKey("publicInfo")).isTrue();
        assertThat(obj.getString("publicInfo")).isEqualTo("visible");
        assertThat(obj.containsKey("secret")).isFalse();
        // deserialize=false 不影响序列化
        assertThat(obj.containsKey("computedField")).isTrue();
    }

    @Test
    public void testJSONField_deserializeFalse() {
        // deserialize=false → 即使 JSON 中有该字段，反序列化时也忽略
        String json = "{\"publicInfo\":\"hello\",\"secret\":\"pwd123\",\"computedField\":\"should_ignore\"}";

        SensitiveData data = JSON.parseObject(json, SensitiveData.class);

        assertThat(data.getPublicInfo()).isEqualTo("hello");
        // serialize=false 不影响反序列化
        assertThat(data.getSecret()).isEqualTo("pwd123");
        // deserialize=false → 字段保持 null
        assertThat(data.getComputedField()).isNull();
    }

    /** format 控制日期字段的序列化/反序列化格式 */
    public static class EventLog {
        private String event;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date eventTime;

        public EventLog() {}

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public Date getEventTime() { return eventTime; }
        public void setEventTime(Date eventTime) { this.eventTime = eventTime; }
    }

    @Test
    public void testJSONField_format_serialize() {
        EventLog log = new EventLog();
        log.setEvent("login");
        log.setEventTime(new Date(1700000000000L));

        String json = JSON.toJSONString(log);
        JSONObject obj = JSON.parseObject(json);

        // format 指定后，序列化为字符串而非时间戳
        String timeStr = obj.getString("eventTime");
        assertThat(timeStr).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    public void testJSONField_format_deserialize() {
        // 先序列化得到格式化的时间字符串，再反序列化回来验证往返一致
        EventLog original = new EventLog();
        original.setEvent("logout");
        original.setEventTime(new Date(1700000000000L));

        String json = JSON.toJSONString(original);
        EventLog parsed = JSON.parseObject(json, EventLog.class);

        assertThat(parsed.getEvent()).isEqualTo("logout");
        assertThat(parsed.getEventTime()).isEqualTo(original.getEventTime());
    }
}
