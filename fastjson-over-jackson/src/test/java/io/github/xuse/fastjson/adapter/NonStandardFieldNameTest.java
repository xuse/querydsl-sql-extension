package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 验证非 JavaBean 规范字段名的兼容性。
 * <p>
 * 受影响的命名风格：
 * <ul>
 * <li>全大写 + 下划线：{@code TOTAL_BYTES_CNT}、{@code GROUP_TRANSACTIONID}</li>
 * <li>全大写无下划线：{@code ICCID}、{@code MSID}、{@code MSID_TYPE}</li>
 * <li>首字母大写：{@code Response}</li>
 * </ul>
 * 实测 fastjson 与 Jackson 由 getter 推导出的名字都不等于声明名，且两者规则不同：
 * <pre>
 * 声明字段            fastjson          Jackson 默认
 * TOTAL_BYTES_CNT     tOTAL_BYTES_CNT   total_BYTES_CNT
 * ICCID               iCCID             iccid
 * Response            response          response
 * </pre>
 * 本兼容层的目标行为：存在同名字段时以声明名为准（{@code TOTAL_BYTES_CNT}），
 * 同时反序列化忽略大小写，从而仍能读入 fastjson 旧数据里的 {@code tOTAL_BYTES_CNT}。
 */
public class NonStandardFieldNameTest {

    public static class NonStandardBean {
        private long TOTAL_BYTES_CNT;
        private String GROUP_TRANSACTIONID;
        private String ICCID;
        private String MSID;
        private String MSID_TYPE;
        private String Response;

        public long getTOTAL_BYTES_CNT() { return TOTAL_BYTES_CNT; }
        public void setTOTAL_BYTES_CNT(long v) { this.TOTAL_BYTES_CNT = v; }
        public String getGROUP_TRANSACTIONID() { return GROUP_TRANSACTIONID; }
        public void setGROUP_TRANSACTIONID(String v) { this.GROUP_TRANSACTIONID = v; }
        public String getICCID() { return ICCID; }
        public void setICCID(String v) { this.ICCID = v; }
        public String getMSID() { return MSID; }
        public void setMSID(String v) { this.MSID = v; }
        public String getMSID_TYPE() { return MSID_TYPE; }
        public void setMSID_TYPE(String v) { this.MSID_TYPE = v; }
        public String getResponse() { return Response; }
        public void setResponse(String v) { this.Response = v; }
    }

    private static NonStandardBean sample() {
        NonStandardBean b = new NonStandardBean();
        b.setTOTAL_BYTES_CNT(1024L);
        b.setGROUP_TRANSACTIONID("G1");
        b.setICCID("8986");
        b.setMSID("M1");
        b.setMSID_TYPE("T1");
        b.setResponse("ok");
        return b;
    }

    private static final String JSON_TEXT = "{\"TOTAL_BYTES_CNT\":1024,"
            + "\"GROUP_TRANSACTIONID\":\"G1\","
            + "\"ICCID\":\"8986\","
            + "\"MSID\":\"M1\","
            + "\"MSID_TYPE\":\"T1\","
            + "\"Response\":\"ok\"}";

    /** 序列化输出声明名，不做 mangle */
    @Test
    public void testSerialize_keepsDeclaredNames() {
        JSONObject obj = JSON.parseObject(JSON.toJSONString(sample()));

        assertThat(obj.keySet()).containsExactlyInAnyOrder("TOTAL_BYTES_CNT",
                "GROUP_TRANSACTIONID", "ICCID", "MSID", "MSID_TYPE", "Response");
    }

    /** 声明名可反序列化 */
    @Test
    public void testDeserialize_declaredNames() {
        NonStandardBean bean = JSON.parseObject(JSON_TEXT, NonStandardBean.class);

        assertThat(bean.getTOTAL_BYTES_CNT()).isEqualTo(1024L);
        assertThat(bean.getGROUP_TRANSACTIONID()).isEqualTo("G1");
        assertThat(bean.getICCID()).isEqualTo("8986");
        assertThat(bean.getMSID()).isEqualTo("M1");
        assertThat(bean.getMSID_TYPE()).isEqualTo("T1");
        assertThat(bean.getResponse()).isEqualTo("ok");
    }

    /** fastjson 产出的 mangle 名（iCCID / tOTAL_BYTES_CNT）仍能读入，便于迁移旧数据 */
    @Test
    public void testDeserialize_fastjsonMangledNames() {
        String fjText = com.alibaba.fastjson.JSON.toJSONString(sample());

        NonStandardBean bean = JSON.parseObject(fjText, NonStandardBean.class);

        assertThat(bean.getTOTAL_BYTES_CNT()).isEqualTo(1024L);
        assertThat(bean.getICCID()).isEqualTo("8986");
        assertThat(bean.getMSID_TYPE()).isEqualTo("T1");
        assertThat(bean.getResponse()).isEqualTo("ok");
    }

    @Test
    public void testRoundTrip() {
        NonStandardBean bean = JSON.parseObject(JSON.toJSONString(sample()), NonStandardBean.class);

        assertThat(JSON.toJSONString(bean)).isEqualTo(JSON.toJSONString(sample()));
    }

    /** @JSONField 指定的名字优先于声明名 */
    public static class AnnotatedBean {
        @JSONField(name = "iccid")
        private String ICCID;

        public String getICCID() { return ICCID; }
        public void setICCID(String v) { this.ICCID = v; }
    }

    @Test
    public void testAnnotationWins() {
        AnnotatedBean b = new AnnotatedBean();
        b.setICCID("x");

        assertThat(JSON.toJSONString(b)).isEqualTo("{\"iccid\":\"x\"}");
        assertThat(JSON.parseObject("{\"iccid\":\"y\"}", AnnotatedBean.class).getICCID())
                .isEqualTo("y");
    }

    /** 常规命名不受影响 */
    public static class NormalBean {
        private String userName;
        private String url;
        private boolean active;

        public String getUserName() { return userName; }
        public void setUserName(String v) { this.userName = v; }
        public String getUrl() { return url; }
        public void setUrl(String v) { this.url = v; }
        public boolean isActive() { return active; }
        public void setActive(boolean v) { this.active = v; }
    }

    @Test
    public void testNormalNaming_unaffected() {
        NormalBean b = new NormalBean();
        b.setUserName("u");
        b.setUrl("http://x");
        b.setActive(true);

        JSONObject obj = JSON.parseObject(JSON.toJSONString(b));
        assertThat(obj.keySet()).containsExactlyInAnyOrder("userName", "url", "active");
        assertThat(JSON.parseObject(JSON.toJSONString(b), NormalBean.class).getUserName())
                .isEqualTo("u");
    }
}
