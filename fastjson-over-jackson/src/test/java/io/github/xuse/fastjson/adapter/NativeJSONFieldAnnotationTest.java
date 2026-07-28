package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * 验证通过反射支持原生 com.alibaba.fastjson.annotation.JSONField 注解。
 * <p>
 * 测试中的 POJO 使用原生 fastjson 的 @JSONField 注解（而非本项目自定义注解），
 * 验证 Jackson 兼容层能正确识别并处理这些注解。
 */
public class NativeJSONFieldAnnotationTest {

    // ==================== 使用原生 @JSONField(name=...) 的 POJO ====================

    public static class NativeNameBean {
        @com.alibaba.fastjson.annotation.JSONField(name = "user_name")
        private String userName;

        @com.alibaba.fastjson.annotation.JSONField(name = "age_value")
        private int age;

        private String noAnnotation;

        public NativeNameBean() {}

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getNoAnnotation() { return noAnnotation; }
        public void setNoAnnotation(String noAnnotation) { this.noAnnotation = noAnnotation; }
    }

    @Test
    public void testNativeName_serialize() {
        NativeNameBean bean = new NativeNameBean();
        bean.setUserName("Alice");
        bean.setAge(25);
        bean.setNoAnnotation("plain");

        String json = JSON.toJSONString(bean);
        JSONObject obj = JSON.parseObject(json);

        assertThat(obj.getString("user_name")).isEqualTo("Alice");
        assertThat(obj.getIntValue("age_value")).isEqualTo(25);
        assertThat(obj.getString("noAnnotation")).isEqualTo("plain");
        // 原始 Java 字段名不应出现
        assertThat(obj.containsKey("userName")).isFalse();
        assertThat(obj.containsKey("age")).isFalse();
    }

    @Test
    public void testNativeName_deserialize() {
        String json = "{\"user_name\":\"Bob\",\"age_value\":30,\"noAnnotation\":\"test\"}";

        NativeNameBean bean = JSON.parseObject(json, NativeNameBean.class);

        assertThat(bean.getUserName()).isEqualTo("Bob");
        assertThat(bean.getAge()).isEqualTo(30);
        assertThat(bean.getNoAnnotation()).isEqualTo("test");
    }

    // ==================== 使用原生 @JSONField(serialize/deserialize) 的 POJO ====================

    public static class NativeVisibilityBean {
        private String visible;

        @com.alibaba.fastjson.annotation.JSONField(serialize = false)
        private String writeOnly;

        @com.alibaba.fastjson.annotation.JSONField(deserialize = false)
        private String readOnly;

        @com.alibaba.fastjson.annotation.JSONField(serialize = false, deserialize = false)
        private String ignored;

        public NativeVisibilityBean() {}

        public String getVisible() { return visible; }
        public void setVisible(String visible) { this.visible = visible; }
        public String getWriteOnly() { return writeOnly; }
        public void setWriteOnly(String writeOnly) { this.writeOnly = writeOnly; }
        public String getReadOnly() { return readOnly; }
        public void setReadOnly(String readOnly) { this.readOnly = readOnly; }
        public String getIgnored() { return ignored; }
        public void setIgnored(String ignored) { this.ignored = ignored; }
    }

    @Test
    public void testNativeSerializeFalse() {
        NativeVisibilityBean bean = new NativeVisibilityBean();
        bean.setVisible("yes");
        bean.setWriteOnly("secret");
        bean.setReadOnly("computed");
        bean.setIgnored("skip");

        String json = JSON.toJSONString(bean);
        JSONObject obj = JSON.parseObject(json);

        assertThat(obj.containsKey("visible")).isTrue();
        assertThat(obj.getString("visible")).isEqualTo("yes");
        // serialize=false 不应出现在序列化结果中
        assertThat(obj.containsKey("writeOnly")).isFalse();
        // deserialize=false 不影响序列化
        assertThat(obj.containsKey("readOnly")).isTrue();
        assertThat(obj.getString("readOnly")).isEqualTo("computed");
        // serialize=false && deserialize=false => 不出现
        assertThat(obj.containsKey("ignored")).isFalse();
    }

    @Test
    public void testNativeDeserializeFalse() {
        String json = "{\"visible\":\"v\",\"writeOnly\":\"w\",\"readOnly\":\"r\",\"ignored\":\"i\"}";

        NativeVisibilityBean bean = JSON.parseObject(json, NativeVisibilityBean.class);

        assertThat(bean.getVisible()).isEqualTo("v");
        // serialize=false 不影响反序列化
        assertThat(bean.getWriteOnly()).isEqualTo("w");
        // deserialize=false 反序列化时忽略
        assertThat(bean.getReadOnly()).isNull();
        // 两者都 false 也不反序列化
        assertThat(bean.getIgnored()).isNull();
    }

    // ==================== 使用原生 @JSONField(format=...) 的 POJO ====================

    public static class NativeDateBean {
        private String name;

        @com.alibaba.fastjson.annotation.JSONField(format = "yyyy-MM-dd")
        private Date birthday;

        public NativeDateBean() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Date getBirthday() { return birthday; }
        public void setBirthday(Date birthday) { this.birthday = birthday; }
    }

    @Test
    public void testNativeFormat_serialize() {
        NativeDateBean bean = new NativeDateBean();
        bean.setName("Charlie");
        bean.setBirthday(new Date(1700000000000L)); // 2023-11-14

        String json = JSON.toJSONString(bean);
        JSONObject obj = JSON.parseObject(json);

        assertThat(obj.getString("name")).isEqualTo("Charlie");
        // format 指定后输出为日期字符串
        assertThat(obj.getString("birthday")).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    public void testNativeFormat_roundTrip() {
        NativeDateBean original = new NativeDateBean();
        original.setName("Dave");
        original.setBirthday(new Date(1700000000000L));

        String json = JSON.toJSONString(original);
        NativeDateBean parsed = JSON.parseObject(json, NativeDateBean.class);

        assertThat(parsed.getName()).isEqualTo("Dave");
        // 由于 format 精度为天，只比较日期部分
        assertThat(parsed.getBirthday()).isNotNull();
    }

    // ==================== 使用原生 @JSONField(alternateNames=...) 的 POJO ====================

    public static class NativeAliasBean {
        @com.alibaba.fastjson.annotation.JSONField(name = "device_id", alternateNames = {"deviceId", "DeviceID"})
        private String deviceId;

        public NativeAliasBean() {}

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    }

    @Test
    public void testNativeAlternateNames_primaryName() {
        String json = "{\"device_id\":\"D001\"}";
        NativeAliasBean bean = JSON.parseObject(json, NativeAliasBean.class);
        assertThat(bean.getDeviceId()).isEqualTo("D001");
    }

    @Test
    public void testNativeAlternateNames_alternate1() {
        String json = "{\"deviceId\":\"D002\"}";
        NativeAliasBean bean = JSON.parseObject(json, NativeAliasBean.class);
        assertThat(bean.getDeviceId()).isEqualTo("D002");
    }

    @Test
    public void testNativeAlternateNames_alternate2() {
        String json = "{\"DeviceID\":\"D003\"}";
        NativeAliasBean bean = JSON.parseObject(json, NativeAliasBean.class);
        assertThat(bean.getDeviceId()).isEqualTo("D003");
    }

    @Test
    public void testNativeAlternateNames_serializeUsesPrimaryName() {
        NativeAliasBean bean = new NativeAliasBean();
        bean.setDeviceId("D004");

        String json = JSON.toJSONString(bean);
        JSONObject obj = JSON.parseObject(json);

        assertThat(obj.containsKey("device_id")).isTrue();
        assertThat(obj.containsKey("deviceId")).isFalse();
        assertThat(obj.containsKey("DeviceID")).isFalse();
    }

    // ==================== name 与 serialize/deserialize 组合 ====================

    /**
     * name 与 serialize/deserialize 组合使用。
     * <p>
     * 属性被重命名后，过滤逻辑不能用外部 JSON 名去查 Java 字段，否则过滤会失效。
     */
    public static class NativeRenamedVisibilityBean {
        @com.alibaba.fastjson.annotation.JSONField(name = "public_info")
        private String publicInfo;

        @com.alibaba.fastjson.annotation.JSONField(name = "secret_key", serialize = false)
        private String secret;

        @com.alibaba.fastjson.annotation.JSONField(name = "computed_value", deserialize = false)
        private String computedField;

        @com.alibaba.fastjson.annotation.JSONField(name = "ignored_all", serialize = false, deserialize = false)
        private String ignored;

        public NativeRenamedVisibilityBean() {}

        public String getPublicInfo() { return publicInfo; }
        public void setPublicInfo(String publicInfo) { this.publicInfo = publicInfo; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getComputedField() { return computedField; }
        public void setComputedField(String computedField) { this.computedField = computedField; }
        public String getIgnored() { return ignored; }
        public void setIgnored(String ignored) { this.ignored = ignored; }
    }

    @Test
    public void testNativeRenamed_serializeFalse() {
        NativeRenamedVisibilityBean bean = new NativeRenamedVisibilityBean();
        bean.setPublicInfo("visible");
        bean.setSecret("pwd123");
        bean.setComputedField("calc");
        bean.setIgnored("skip");

        String json = JSON.toJSONString(bean);
        JSONObject obj = JSON.parseObject(json);

        assertThat(obj.getString("public_info")).isEqualTo("visible");
        // serialize=false：重命名后的名字和原字段名都不应出现
        assertThat(obj.containsKey("secret_key")).isFalse();
        assertThat(obj.containsKey("secret")).isFalse();
        // deserialize=false 不影响序列化
        assertThat(obj.getString("computed_value")).isEqualTo("calc");
        // 两者都 false：完全不出现
        assertThat(obj.containsKey("ignored_all")).isFalse();
        assertThat(obj.containsKey("ignored")).isFalse();
    }

    @Test
    public void testNativeRenamed_deserializeFalse() {
        String json = "{\"public_info\":\"hello\",\"secret_key\":\"pwd\","
                + "\"computed_value\":\"should_ignore\",\"ignored_all\":\"nope\"}";

        NativeRenamedVisibilityBean bean = JSON.parseObject(json, NativeRenamedVisibilityBean.class);

        assertThat(bean.getPublicInfo()).isEqualTo("hello");
        // serialize=false 不影响反序列化
        assertThat(bean.getSecret()).isEqualTo("pwd");
        // deserialize=false => 字段保持 null
        assertThat(bean.getComputedField()).isNull();
        // 两者都 false => 字段保持 null
        assertThat(bean.getIgnored()).isNull();
    }

    // ==================== 自定义注解优先于原生注解 ====================

    public static class MixedAnnotationBean {
        // 自定义注解优先
        @JSONField(name = "custom_name")
        @com.alibaba.fastjson.annotation.JSONField(name = "native_name")
        private String field;

        public MixedAnnotationBean() {}

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
    }

    @Test
    public void testCustomAnnotationTakesPrecedence_serialize() {
        MixedAnnotationBean bean = new MixedAnnotationBean();
        bean.setField("value");

        String json = JSON.toJSONString(bean);
        JSONObject obj = JSON.parseObject(json);

        // 自定义 @JSONField 优先级高
        assertThat(obj.containsKey("custom_name")).isTrue();
        assertThat(obj.containsKey("native_name")).isFalse();
    }

    @Test
    public void testCustomAnnotationTakesPrecedence_deserialize() {
        String json = "{\"custom_name\":\"hello\"}";
        MixedAnnotationBean bean = JSON.parseObject(json, MixedAnnotationBean.class);
        assertThat(bean.getField()).isEqualTo("hello");
    }

    // ==================== 与原生 fastjson 行为等效性验证 ====================

    @Test
    public void testNativeName_equivalenceWithFastjson() {
        NativeNameBean bean = new NativeNameBean();
        bean.setUserName("Test");
        bean.setAge(18);
        bean.setNoAnnotation("ok");

        // 原生 fastjson 序列化
        String fastjsonResult = com.alibaba.fastjson.JSON.toJSONString(bean);
        // Jackson 兼容层序列化
        String jacksonResult = JSON.toJSONString(bean);

        // 反序列化后内容一致
        NativeNameBean fromFj = com.alibaba.fastjson.JSON.parseObject(fastjsonResult, NativeNameBean.class);
        NativeNameBean fromJk = JSON.parseObject(jacksonResult, NativeNameBean.class);

        assertThat(fromJk.getUserName()).isEqualTo(fromFj.getUserName());
        assertThat(fromJk.getAge()).isEqualTo(fromFj.getAge());
        assertThat(fromJk.getNoAnnotation()).isEqualTo(fromFj.getNoAnnotation());
    }

    @Test
    public void testNativeVisibility_equivalenceWithFastjson() {
        NativeVisibilityBean bean = new NativeVisibilityBean();
        bean.setVisible("v");
        bean.setWriteOnly("w");
        bean.setReadOnly("r");
        bean.setIgnored("i");

        String fastjsonResult = com.alibaba.fastjson.JSON.toJSONString(bean);
        String jacksonResult = JSON.toJSONString(bean);

        // 输出实际结果方便诊断
        System.out.println("fastjson output: " + fastjsonResult);
        System.out.println("jackson  output: " + jacksonResult);

        com.alibaba.fastjson.JSONObject fjObj = com.alibaba.fastjson.JSON.parseObject(fastjsonResult);
        JSONObject jkObj = JSON.parseObject(jacksonResult);

        // 两者序列化结果中的字段应该一致
        assertThat(jkObj.containsKey("visible")).isEqualTo(fjObj.containsKey("visible"));
        assertThat(jkObj.containsKey("writeOnly")).isEqualTo(fjObj.containsKey("writeOnly"));
        assertThat(jkObj.containsKey("readOnly")).isEqualTo(fjObj.containsKey("readOnly"));
        assertThat(jkObj.containsKey("ignored")).isEqualTo(fjObj.containsKey("ignored"));
    }
}
