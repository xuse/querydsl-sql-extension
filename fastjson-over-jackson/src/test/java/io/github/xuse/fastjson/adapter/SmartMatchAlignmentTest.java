package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 对齐 fastjson {@code JavaBeanDeserializer.smartMatch} 的归一化规则。
 * <p>
 * 依据 fastjson 1.2.83 源码 {@code TypeUtils.fnv1a_64_extract}：归一化时
 * 同时剔除下划线 {@code _} 与连字符 {@code -}，再转小写。
 * 另有 {@code smartMatch} 的 {@code is} 前缀兜底：key 以 {@code is} 开头且
 * 去掉前缀后能匹配到 boolean 字段时命中，非 boolean 字段则不命中。
 */
public class SmartMatchAlignmentTest {

    public static class Bean {
        private String TOTAL_BYTES_CNT;
        private String userName;

        public String getTOTAL_BYTES_CNT() { return TOTAL_BYTES_CNT; }
        public void setTOTAL_BYTES_CNT(String v) { this.TOTAL_BYTES_CNT = v; }
        public String getUserName() { return userName; }
        public void setUserName(String v) { this.userName = v; }
    }

    /** 连字符与下划线等价，双向都能命中 */
    @Test
    public void testHyphenTreatedAsUnderscore() {
        assertThat(JSON.parseObject("{\"TOTAL-BYTES-CNT\":\"V\"}", Bean.class)
                .getTOTAL_BYTES_CNT()).isEqualTo("V");
        assertThat(JSON.parseObject("{\"total-bytes-cnt\":\"V\"}", Bean.class)
                .getTOTAL_BYTES_CNT()).isEqualTo("V");
        assertThat(JSON.parseObject("{\"user-name\":\"V\"}", Bean.class)
                .getUserName()).isEqualTo("V");
        assertThat(JSON.parseObject("{\"USER-NAME\":\"V\"}", Bean.class)
                .getUserName()).isEqualTo("V");
    }

    @Test
    public void testHyphenMatchesFastjson() {
        String[] keys = {"TOTAL-BYTES-CNT", "total-bytes-cnt", "Total-Bytes-Cnt",
                "user-name", "USER-NAME", "user_name", "TOTAL_BYTES_CNT"};
        for (String key : keys) {
            String json = "{\"" + key + "\":\"V\"}";
            Bean fj = com.alibaba.fastjson.JSON.parseObject(json, Bean.class);
            Bean ad = JSON.parseObject(json, Bean.class);

            assertThat("cnt=" + ad.getTOTAL_BYTES_CNT() + ",user=" + ad.getUserName())
                    .as("key=%s", key)
                    .isEqualTo("cnt=" + fj.getTOTAL_BYTES_CNT() + ",user=" + fj.getUserName());
        }
    }

    public static class BoolBean {
        private boolean ACTIVE;
        private Boolean ENABLED_FLAG;
        private String NAME;

        public boolean getACTIVE() { return ACTIVE; }
        public void setACTIVE(boolean v) { this.ACTIVE = v; }
        public Boolean getENABLED_FLAG() { return ENABLED_FLAG; }
        public void setENABLED_FLAG(Boolean v) { this.ENABLED_FLAG = v; }
        public String getNAME() { return NAME; }
        public void setNAME(String v) { this.NAME = v; }
    }

    /** key 带 is 前缀时去掉前缀匹配 boolean 字段 */
    @Test
    public void testIsPrefixMatchesBooleanField() {
        assertThat(JSON.parseObject("{\"isActive\":true}", BoolBean.class).getACTIVE()).isTrue();
        assertThat(JSON.parseObject("{\"isACTIVE\":true}", BoolBean.class).getACTIVE()).isTrue();
        assertThat(JSON.parseObject("{\"isEnabledFlag\":true}", BoolBean.class)
                .getENABLED_FLAG()).isTrue();
    }

    /** 非 boolean 字段不接受 is 前缀，避免误匹配 */
    @Test
    public void testIsPrefixIgnoredForNonBoolean() {
        BoolBean b = JSON.parseObject("{\"isName\":\"V\"}", BoolBean.class);
        assertThat(b.getNAME()).isNull();
    }

    @Test
    public void testIsPrefixMatchesFastjson() {
        String[][] cases = {
                {"isActive", "true"}, {"isACTIVE", "true"}, {"active", "true"},
                {"ACTIVE", "true"}, {"isEnabledFlag", "true"}, {"is_active", "true"},
                {"isName", "\"V\""}, {"isNAME", "\"V\""}, {"NAME", "\"V\""},
        };
        for (String[] c : cases) {
            String json = "{\"" + c[0] + "\":" + c[1] + "}";
            BoolBean fj = com.alibaba.fastjson.JSON.parseObject(json, BoolBean.class);
            BoolBean ad = JSON.parseObject(json, BoolBean.class);

            assertThat(describe(ad)).as("key=%s", c[0]).isEqualTo(describe(fj));
        }
    }

    private static String describe(BoolBean b) {
        return "active=" + b.getACTIVE() + ",enabled=" + b.getENABLED_FLAG()
                + ",name=" + b.getNAME();
    }

    /** is 开头但本身就是字段名的情况，不能被前缀剥离规则破坏 */
    public static class IsNamedBean {
        private boolean isReady;
        private String ISSUE_CODE;

        public boolean getIsReady() { return isReady; }
        public void setIsReady(boolean v) { this.isReady = v; }
        public String getISSUE_CODE() { return ISSUE_CODE; }
        public void setISSUE_CODE(String v) { this.ISSUE_CODE = v; }
    }

    @Test
    public void testFieldNameStartingWithIs() {
        assertThat(JSON.parseObject("{\"isReady\":true}", IsNamedBean.class)
                .getIsReady()).isTrue();
        assertThat(JSON.parseObject("{\"ISSUE_CODE\":\"V\"}", IsNamedBean.class)
                .getISSUE_CODE()).isEqualTo("V");
        assertThat(JSON.parseObject("{\"issueCode\":\"V\"}", IsNamedBean.class)
                .getISSUE_CODE()).isEqualTo("V");
    }
}
