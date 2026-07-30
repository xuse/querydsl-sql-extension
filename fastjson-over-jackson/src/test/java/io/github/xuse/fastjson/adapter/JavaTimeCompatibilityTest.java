package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * java.time (JSR-310) 类型的兼容性验证。
 * <p>
 * 覆盖 {@link JavaTimeCodec} 的序列化默认格式、宽松反序列化输入，
 * 以及 {@code @JSONField(format=...)} 的作用。反序列化用例同时与原生 fastjson
 * 对比，确保接受的输入形式一致。
 */
public class JavaTimeCompatibilityTest {

    public static class TimeBean {
        private LocalDate date;
        private LocalDateTime dateTime;
        private LocalTime time;
        private Instant instant;
        private ZonedDateTime zoned;
        private OffsetDateTime offset;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
        public LocalTime getTime() { return time; }
        public void setTime(LocalTime time) { this.time = time; }
        public Instant getInstant() { return instant; }
        public void setInstant(Instant instant) { this.instant = instant; }
        public ZonedDateTime getZoned() { return zoned; }
        public void setZoned(ZonedDateTime zoned) { this.zoned = zoned; }
        public OffsetDateTime getOffset() { return offset; }
        public void setOffset(OffsetDateTime offset) { this.offset = offset; }
    }

    public static class FormattedBean {
        @JSONField(format = "yyyy/MM/dd")
        private LocalDate date;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dateTime;

        @JSONField(format = "HH:mm")
        private LocalTime time;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
        public LocalTime getTime() { return time; }
        public void setTime(LocalTime time) { this.time = time; }
    }

    // ==================== 序列化默认格式 ====================

    @Test
    public void testSerialize_isoByDefault() {
        TimeBean bean = new TimeBean();
        bean.setDate(LocalDate.of(2024, 1, 2));
        bean.setDateTime(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        bean.setTime(LocalTime.of(3, 4, 5));

        JSONObject obj = JSON.parseObject(JSON.toJSONString(bean));

        assertThat(obj.getString("date")).isEqualTo("2024-01-02");
        assertThat(obj.getString("dateTime")).isEqualTo("2024-01-02T03:04:05");
        assertThat(obj.getString("time")).isEqualTo("03:04:05");
    }

    /** LocalDateTime 按纳秒精度选择 ISO 变体，与 fastjson Jdk8DateCodec 一致 */
    @Test
    public void testSerialize_localDateTimePrecision() {
        assertThat(text(LocalDateTime.of(2024, 1, 2, 3, 4, 5)))
                .isEqualTo("2024-01-02T03:04:05");
        assertThat(text(LocalDateTime.of(2024, 1, 2, 3, 4, 5, 123000000)))
                .isEqualTo("2024-01-02T03:04:05.123");
        assertThat(text(LocalDateTime.of(2024, 1, 2, 3, 4, 5, 123456789)))
                .isEqualTo("2024-01-02T03:04:05.123456789");
    }

    private static String text(LocalDateTime value) {
        TimeBean bean = new TimeBean();
        bean.setDateTime(value);
        return JSON.parseObject(JSON.toJSONString(bean)).getString("dateTime");
    }

    @Test
    public void testSerialize_instantAndZoned() {
        TimeBean bean = new TimeBean();
        bean.setInstant(Instant.ofEpochMilli(1704135845000L));
        bean.setZoned(ZonedDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneId.of("Asia/Shanghai")));
        bean.setOffset(OffsetDateTime.of(2024, 1, 2, 3, 4, 5, 0, java.time.ZoneOffset.ofHours(8)));

        JSONObject obj = JSON.parseObject(JSON.toJSONString(bean));

        assertThat(obj.getString("instant")).isEqualTo("2024-01-01T19:04:05Z");
        assertThat(obj.getString("zoned")).isEqualTo("2024-01-02T03:04:05+08:00[Asia/Shanghai]");
        assertThat(obj.getString("offset")).isEqualTo("2024-01-02T03:04:05+08:00");
    }

    @Test
    public void testSerialize_nullOmitted() {
        assertThat(JSON.toJSONString(new TimeBean())).isEqualTo("{}");
    }

    // ==================== 反序列化：与 fastjson 接受相同输入 ====================

    @Test
    public void testDeserialize_isoFormat() {
        String json = "{\"date\":\"2024-01-02\",\"dateTime\":\"2024-01-02T03:04:05\",\"time\":\"03:04:05\"}";
        assertSameAsFastjson(json, LocalDate.of(2024, 1, 2),
                LocalDateTime.of(2024, 1, 2, 3, 4, 5), LocalTime.of(3, 4, 5));
    }

    @Test
    public void testDeserialize_isoWithMillis() {
        String json = "{\"date\":\"2024-01-02\",\"dateTime\":\"2024-01-02T03:04:05.123\",\"time\":\"03:04:05.123\"}";
        assertSameAsFastjson(json, LocalDate.of(2024, 1, 2),
                LocalDateTime.of(2024, 1, 2, 3, 4, 5, 123000000), LocalTime.of(3, 4, 5, 123000000));
    }

    /** fastjson 接受空格分隔的 "yyyy-MM-dd HH:mm:ss" */
    @Test
    public void testDeserialize_spaceSeparated() {
        String json = "{\"date\":\"2024-01-02\",\"dateTime\":\"2024-01-02 03:04:05\",\"time\":\"03:04:05\"}";
        assertSameAsFastjson(json, LocalDate.of(2024, 1, 2),
                LocalDateTime.of(2024, 1, 2, 3, 4, 5), LocalTime.of(3, 4, 5));
    }

    /** epoch 毫秒数字同样可反序列化为 java.time 类型 */
    @Test
    public void testDeserialize_epochMillis() {
        String json = "{\"date\":1704124800000,\"dateTime\":1704135845000,\"time\":1704135845000}";

        TimeBean adapter = JSON.parseObject(json, TimeBean.class);
        TimeBean fastjson = com.alibaba.fastjson.JSON.parseObject(json, TimeBean.class);

        assertThat(adapter.getDate()).isEqualTo(fastjson.getDate());
        assertThat(adapter.getDateTime()).isEqualTo(fastjson.getDateTime());
        assertThat(adapter.getTime()).isEqualTo(fastjson.getTime());
    }

    /** 紧凑数字串 yyyyMMdd / yyyyMMddHHmmss */
    @Test
    public void testDeserialize_compactDigits() {
        TimeBean bean = JSON.parseObject(
                "{\"date\":\"20240102\",\"dateTime\":\"20240102030405\",\"time\":\"030405\"}",
                TimeBean.class);

        assertThat(bean.getDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(bean.getDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        assertThat(bean.getTime()).isEqualTo(LocalTime.of(3, 4, 5));
    }

    @Test
    public void testDeserialize_withZoneOffset() {
        String json = "{\"dateTime\":\"2024-01-02T03:04:05+08:00\"}";

        TimeBean adapter = JSON.parseObject(json, TimeBean.class);
        TimeBean fastjson = com.alibaba.fastjson.JSON.parseObject(json, TimeBean.class);

        assertThat(adapter.getDateTime()).isEqualTo(fastjson.getDateTime());
    }

    @Test
    public void testDeserialize_dateOnlyIntoDateTime() {
        TimeBean bean = JSON.parseObject("{\"dateTime\":\"2024-01-02\"}", TimeBean.class);
        assertThat(bean.getDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0, 0));
    }

    @Test
    public void testDeserialize_nullAndEmpty() {
        TimeBean bean = JSON.parseObject("{\"date\":null,\"dateTime\":\"\",\"time\":\"null\"}", TimeBean.class);

        assertThat(bean.getDate()).isNull();
        assertThat(bean.getDateTime()).isNull();
        assertThat(bean.getTime()).isNull();
    }

    @Test
    public void testDeserialize_invalidThrowsJSONException() {
        assertThatThrownBy(() -> JSON.parseObject("{\"date\":\"not-a-date\"}", TimeBean.class))
                .isInstanceOf(JSONException.class);
    }

    @Test
    public void testDeserialize_instantAndZoned() {
        String json = "{\"instant\":\"2024-01-01T19:04:05Z\","
                + "\"zoned\":\"2024-01-02T03:04:05+08:00\","
                + "\"offset\":\"2024-01-02T03:04:05+08:00\"}";

        TimeBean bean = JSON.parseObject(json, TimeBean.class);

        assertThat(bean.getInstant()).isEqualTo(Instant.ofEpochMilli(1704135845000L));
        assertThat(bean.getZoned().toInstant()).isEqualTo(Instant.ofEpochMilli(1704135845000L));
        assertThat(bean.getOffset().toInstant()).isEqualTo(Instant.ofEpochMilli(1704135845000L));
    }

    private static void assertSameAsFastjson(String json, LocalDate date, LocalDateTime dateTime, LocalTime time) {
        TimeBean adapter = JSON.parseObject(json, TimeBean.class);
        TimeBean fastjson = com.alibaba.fastjson.JSON.parseObject(json, TimeBean.class);

        assertThat(adapter.getDate()).isEqualTo(date).isEqualTo(fastjson.getDate());
        assertThat(adapter.getDateTime()).isEqualTo(dateTime).isEqualTo(fastjson.getDateTime());
        assertThat(adapter.getTime()).isEqualTo(time).isEqualTo(fastjson.getTime());
    }

    // ==================== @JSONField(format) ====================

    @Test
    public void testFormat_serialize() {
        FormattedBean bean = new FormattedBean();
        bean.setDate(LocalDate.of(2024, 1, 2));
        bean.setDateTime(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        bean.setTime(LocalTime.of(3, 4, 5));

        JSONObject obj = JSON.parseObject(JSON.toJSONString(bean));

        assertThat(obj.getString("date")).isEqualTo("2024/01/02");
        assertThat(obj.getString("dateTime")).isEqualTo("2024-01-02 03:04:05");
        assertThat(obj.getString("time")).isEqualTo("03:04");
    }

    @Test
    public void testFormat_deserialize() {
        FormattedBean bean = JSON.parseObject(
                "{\"date\":\"2024/01/02\",\"dateTime\":\"2024-01-02 03:04:05\",\"time\":\"03:04\"}",
                FormattedBean.class);

        assertThat(bean.getDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(bean.getDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        assertThat(bean.getTime()).isEqualTo(LocalTime.of(3, 4));
    }

    @Test
    public void testFormat_roundTrip() {
        FormattedBean original = new FormattedBean();
        original.setDate(LocalDate.of(2024, 6, 30));
        original.setDateTime(LocalDateTime.of(2024, 6, 30, 12, 30, 0));

        FormattedBean back = JSON.parseObject(JSON.toJSONString(original), FormattedBean.class);

        assertThat(back.getDate()).isEqualTo(original.getDate());
        assertThat(back.getDateTime()).isEqualTo(original.getDateTime());
    }

    /** 仅含日期部分的 format 用于 LocalDateTime 时补 00:00:00 */
    @Test
    public void testFormat_dateOnlyPatternOnDateTime() {
        FormattedBean bean = JSON.parseObject("{\"dateTime\":\"2024-01-02 00:00:00\"}", FormattedBean.class);
        assertThat(bean.getDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
    }

    // ==================== 顶层与集合场景 ====================

    @Test
    public void testTopLevelAndCollection() {
        assertThat(JSON.toJSONString(LocalDate.of(2024, 1, 2))).isEqualTo("\"2024-01-02\"");

        java.util.List<LocalDate> list = JSON.parseArray("[\"2024-01-02\",\"2024-01-03\"]", LocalDate.class);
        assertThat(list).containsExactly(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 3));
    }

    /** setDefaultDateFormat 只影响 java.util.Date，不改变 java.time 的 ISO 默认输出 */
    @Test
    public void testDefaultDateFormatDoesNotAffectJavaTime() {
        String original = JSON.DEFFAULT_DATE_FORMAT;
        try {
            JSON.setDefaultDateFormat("yyyy/MM/dd");

            TimeBean bean = new TimeBean();
            bean.setDate(LocalDate.of(2024, 1, 2));

            assertThat(JSON.parseObject(JSON.toJSONString(bean)).getString("date")).isEqualTo("2024-01-02");
        } finally {
            JSON.setDefaultDateFormat(original);
        }
    }
}
