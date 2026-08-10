package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** 全大写字段名的反序列化验证（JSON 与 Java 类字段名一致） */
public class UpperCaseDeserializeTest {

    /** 私有字段 + 标准 getter/setter */
    public static class AccessorBean {
        private long TOTAL_BYTES_CNT;
        private String ICCID;
        private String MSID_TYPE;
        private String Response;

        public long getTOTAL_BYTES_CNT() { return TOTAL_BYTES_CNT; }
        public void setTOTAL_BYTES_CNT(long v) { this.TOTAL_BYTES_CNT = v; }
        public String getICCID() { return ICCID; }
        public void setICCID(String v) { this.ICCID = v; }
        public String getMSID_TYPE() { return MSID_TYPE; }
        public void setMSID_TYPE(String v) { this.MSID_TYPE = v; }
        public String getResponse() { return Response; }
        public void setResponse(String v) { this.Response = v; }
    }

    /** 公有字段，无 getter/setter */
    public static class PublicFieldBean {
        public long TOTAL_BYTES_CNT;
        public String ICCID;
        public String GROUP_TRANSACTIONID;
    }

    /** 私有字段，无 getter/setter（fastjson 与 Jackson 默认都不可见） */
    public static class PrivateOnlyBean {
        private String ICCID;

        public String peek() { return ICCID; }
    }

    private static final String TEXT = "{\"TOTAL_BYTES_CNT\":1024,\"ICCID\":\"8986\","
            + "\"MSID_TYPE\":\"T1\",\"Response\":\"ok\",\"GROUP_TRANSACTIONID\":\"G1\"}";

    @Test
    public void testAccessorBean() {
        AccessorBean b = JSON.parseObject(TEXT, AccessorBean.class);

        assertThat(b.getTOTAL_BYTES_CNT()).isEqualTo(1024L);
        assertThat(b.getICCID()).isEqualTo("8986");
        assertThat(b.getMSID_TYPE()).isEqualTo("T1");
        assertThat(b.getResponse()).isEqualTo("ok");
    }

    @Test
    public void testPublicFieldBean() {
        PublicFieldBean b = JSON.parseObject(TEXT, PublicFieldBean.class);

        assertThat(b.TOTAL_BYTES_CNT).isEqualTo(1024L);
        assertThat(b.ICCID).isEqualTo("8986");
        assertThat(b.GROUP_TRANSACTIONID).isEqualTo("G1");
    }

    /** 与 fastjson 行为对比：无访问器的私有字段两者都不填充 */
    @Test
    public void testPrivateOnlyBean_sameAsFastjson() {
        String json = "{\"ICCID\":\"8986\"}";

        assertThat(JSON.parseObject(json, PrivateOnlyBean.class).peek())
                .isEqualTo(com.alibaba.fastjson.JSON.parseObject(json, PrivateOnlyBean.class).peek());
    }

    /** JSON 全大写、Java 字段驼峰时也能匹配（大小写不敏感的副作用） */
    public static class CamelBean {
        private String iccid;

        public String getIccid() { return iccid; }
        public void setIccid(String v) { this.iccid = v; }
    }

    @Test
    public void testCaseInsensitiveFallback() {
        assertThat(JSON.parseObject("{\"ICCID\":\"8986\"}", CamelBean.class).getIccid())
                .isEqualTo("8986");
    }
}
