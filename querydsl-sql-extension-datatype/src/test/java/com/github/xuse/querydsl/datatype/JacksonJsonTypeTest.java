package com.github.xuse.querydsl.datatype;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.xuse.fastjson.adapter.JSONField;

/**
 * JacksonJsonType 单元测试。
 * <p>
 * 验证：
 * <ul>
 *   <li>基本序列化/反序列化</li>
 *   <li>null 字段不输出</li>
 *   <li>日期格式自动适配</li>
 *   <li>@JSONField 注解通过反射识别</li>
 * </ul>
 */
public class JacksonJsonTypeTest {

    // ==================== 测试 POJO ====================

    public static class SimplePojo {
        private String name;
        private int age;
        private List<String> tags;

        public SimplePojo() {}
        public SimplePojo(String name, int age, List<String> tags) {
            this.name = name;
            this.age = age;
            this.tags = tags;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class DatePojo {
        private String event;
        private Date eventTime;

        public DatePojo() {}

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public Date getEventTime() { return eventTime; }
        public void setEventTime(Date eventTime) { this.eventTime = eventTime; }
    }

    /** 使用 @JSONField(name=...) 的 POJO */
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

    /** serialize=false / deserialize=false */
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

    /** alternateNames */
    public static class CardResponse {
        @JSONField(name = "iccid", alternateNames = {"ICCID", "icc_id"})
        private String iccid;

        public CardResponse() {}

        public String getIccid() { return iccid; }
        public void setIccid(String iccid) { this.iccid = iccid; }
    }

    // ==================== 基本序列化/反序列化 ====================

    private final JacksonJsonType<SimplePojo> simpleType = new JacksonJsonType<>(SimplePojo.class);

    @Test
    public void testSerializeAndDeserialize() throws SQLException {
        SimplePojo pojo = new SimplePojo("张三", 30, Arrays.asList("a", "b"));

        // serialize
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        simpleType.setValue(ps, 1, pojo);
        Mockito.verify(ps).setString(Mockito.eq(1), Mockito.argThat(json -> {
            assertThat(json).contains("\"name\":\"张三\"");
            assertThat(json).contains("\"age\":30");
            assertThat(json).contains("\"tags\":[\"a\",\"b\"]");
            return true;
        }));
    }

    @Test
    public void testDeserialize() throws SQLException {
        String json = "{\"name\":\"李四\",\"age\":25,\"tags\":[\"x\"]}";

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        SimplePojo result = simpleType.getValue(rs, 1);

        assertThat(result.getName()).isEqualTo("李四");
        assertThat(result.getAge()).isEqualTo(25);
        assertThat(result.getTags()).containsExactly("x");
    }

    @Test
    public void testNullValue() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(null);

        SimplePojo result = simpleType.getValue(rs, 1);
        assertThat(result).isNull();
    }

    @Test
    public void testEmptyString() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn("");

        SimplePojo result = simpleType.getValue(rs, 1);
        assertThat(result).isNull();
    }

    // ==================== null 字段不输出 ====================

    @Test
    public void testNullFieldsOmitted() throws SQLException {
        SimplePojo pojo = new SimplePojo();
        pojo.setName("test");
        // tags is null

        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        simpleType.setValue(ps, 1, pojo);
        Mockito.verify(ps).setString(Mockito.eq(1), Mockito.argThat(json -> {
            assertThat(json).doesNotContain("tags");
            assertThat(json).contains("\"name\":\"test\"");
            return true;
        }));
    }

    // ==================== 日期格式自动适配 ====================

    private final JacksonJsonType<DatePojo> dateType = new JacksonJsonType<>(DatePojo.class);

    @Test
    public void testDateTimestamp() throws SQLException {
        // 毫秒时间戳
        String json = "{\"event\":\"login\",\"eventTime\":1700000000000}";
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        DatePojo result = dateType.getValue(rs, 1);
        assertThat(result.getEventTime()).isEqualTo(new Date(1700000000000L));
    }

    @Test
    public void testDateStringFormat() throws SQLException {
        // yyyy-MM-dd HH:mm:ss 格式
        String json = "{\"event\":\"login\",\"eventTime\":\"2023-11-14 22:13:20\"}";
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        DatePojo result = dateType.getValue(rs, 1);
        assertThat(result.getEventTime()).isNotNull();
        assertThat(result.getEvent()).isEqualTo("login");
    }

    @Test
    public void testDateIsoFormat() throws SQLException {
        // yyyy-MM-dd 格式
        String json = "{\"event\":\"signup\",\"eventTime\":\"2023-11-14\"}";
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        DatePojo result = dateType.getValue(rs, 1);
        assertThat(result.getEventTime()).isNotNull();
    }

    // ==================== @JSONField 注解支持 ====================

    private final JacksonJsonType<DeviceInfo> deviceType = new JacksonJsonType<>(DeviceInfo.class);

    @Test
    public void testJSONField_name_serialize() throws SQLException {
        DeviceInfo info = new DeviceInfo();
        info.setDeviceId("DEV001");
        info.setCardType(5);
        info.setNormalField("hello");

        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        deviceType.setValue(ps, 1, info);
        Mockito.verify(ps).setString(Mockito.eq(1), Mockito.argThat(json -> {
            // @JSONField(name="device_id") 生效
            assertThat(json).contains("\"device_id\":\"DEV001\"");
            assertThat(json).contains("\"card_type\":5");
            assertThat(json).contains("\"normalField\":\"hello\"");
            assertThat(json).doesNotContain("\"deviceId\"");
            return true;
        }));
    }

    @Test
    public void testJSONField_name_deserialize() throws SQLException {
        String json = "{\"device_id\":\"DEV002\",\"card_type\":3,\"normalField\":\"world\"}";

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        DeviceInfo result = deviceType.getValue(rs, 1);
        assertThat(result.getDeviceId()).isEqualTo("DEV002");
        assertThat(result.getCardType()).isEqualTo(3);
        assertThat(result.getNormalField()).isEqualTo("world");
    }

    @Test
    public void testJSONField_serializeFalse() throws SQLException {
        JacksonJsonType<SensitiveData> type = new JacksonJsonType<>(SensitiveData.class);

        SensitiveData data = new SensitiveData();
        data.setPublicInfo("visible");
        data.setSecret("hidden");
        data.setComputedField("calc");

        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        type.setValue(ps, 1, data);
        Mockito.verify(ps).setString(Mockito.eq(1), Mockito.argThat(json -> {
            assertThat(json).contains("\"publicInfo\":\"visible\"");
            // serialize=false 的字段不出现
            assertThat(json).doesNotContain("secret");
            // deserialize=false 不影响序列化
            assertThat(json).contains("\"computedField\":\"calc\"");
            return true;
        }));
    }

    @Test
    public void testJSONField_deserializeFalse() throws SQLException {
        JacksonJsonType<SensitiveData> type = new JacksonJsonType<>(SensitiveData.class);

        String json = "{\"publicInfo\":\"hello\",\"secret\":\"pwd\",\"computedField\":\"should_ignore\"}";
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        SensitiveData result = type.getValue(rs, 1);
        assertThat(result.getPublicInfo()).isEqualTo("hello");
        assertThat(result.getSecret()).isEqualTo("pwd");
        // deserialize=false → 字段保持 null
        assertThat(result.getComputedField()).isNull();
    }

    @Test
    public void testJSONField_alternateNames() throws SQLException {
        JacksonJsonType<CardResponse> type = new JacksonJsonType<>(CardResponse.class);

        // 使用别名 "ICCID"
        String json = "{\"ICCID\":\"89860001\"}";
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        CardResponse result = type.getValue(rs, 1);
        assertThat(result.getIccid()).isEqualTo("89860001");
    }

    @Test
    public void testJSONField_alternateNames2() throws SQLException {
        JacksonJsonType<CardResponse> type = new JacksonJsonType<>(CardResponse.class);

        // 使用另一个别名 "icc_id"
        String json = "{\"icc_id\":\"89860002\"}";
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        CardResponse result = type.getValue(rs, 1);
        assertThat(result.getIccid()).isEqualTo("89860002");
    }

    // ==================== 忽略未知字段 ====================

    @Test
    public void testIgnoreUnknownFields() throws SQLException {
        String json = "{\"name\":\"test\",\"age\":20,\"unknownField\":\"xxx\"}";

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn(json);

        SimplePojo result = simpleType.getValue(rs, 1);
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getAge()).isEqualTo(20);
    }
}
