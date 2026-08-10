package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 反序列化时忽略下划线差异，对齐 fastjson 的模糊匹配。
 * <p>
 * fastjson 比对属性名时会把两边的下划线剔除后再忽略大小写比较，
 * 因此下划线位置不一致的 key 双向都能命中：
 * <pre>
 * Java 字段            JSON key          fastjson
 * TOTAL_BYTES_CNT      totalBytesCnt     命中（key 无下划线）
 * TOTAL_BYTES_CNT      TOTALBYTESCNT     命中
 * userName             user_name         命中（key 多下划线）
 * userName             USER_NAME         命中
 * </pre>
 */
public class UnderscoreInsensitiveMatchTest {

    public static class Bean {
        private String TOTAL_BYTES_CNT;
        private String GROUP_TRANSACTIONID;
        private String MSID_TYPE;
        private String userName;

        public String getTOTAL_BYTES_CNT() { return TOTAL_BYTES_CNT; }
        public void setTOTAL_BYTES_CNT(String v) { this.TOTAL_BYTES_CNT = v; }
        public String getGROUP_TRANSACTIONID() { return GROUP_TRANSACTIONID; }
        public void setGROUP_TRANSACTIONID(String v) { this.GROUP_TRANSACTIONID = v; }
        public String getMSID_TYPE() { return MSID_TYPE; }
        public void setMSID_TYPE(String v) { this.MSID_TYPE = v; }
        public String getUserName() { return userName; }
        public void setUserName(String v) { this.userName = v; }
    }

    /** Java 字段带下划线，JSON key 无下划线 */
    @Test
    public void testUnderscoredField_matchesKeyWithoutUnderscore() {
        Bean b = JSON.parseObject("{\"totalBytesCnt\":\"V\"}", Bean.class);
        assertThat(b.getTOTAL_BYTES_CNT()).isEqualTo("V");

        b = JSON.parseObject("{\"TOTALBYTESCNT\":\"V\"}", Bean.class);
        assertThat(b.getTOTAL_BYTES_CNT()).isEqualTo("V");

        b = JSON.parseObject("{\"msidType\":\"V\"}", Bean.class);
        assertThat(b.getMSID_TYPE()).isEqualTo("V");
    }

    /** Java 字段无下划线，JSON key 带下划线 */
    @Test
    public void testCamelField_matchesKeyWithUnderscore() {
        Bean b = JSON.parseObject("{\"user_name\":\"V\"}", Bean.class);
        assertThat(b.getUserName()).isEqualTo("V");

        b = JSON.parseObject("{\"USER_NAME\":\"V\"}", Bean.class);
        assertThat(b.getUserName()).isEqualTo("V");
    }

    /** 与 fastjson 逐 key 对齐 */
    @Test
    public void testMatchesFastjson() {
        String[] keys = {"TOTAL_BYTES_CNT", "tOTAL_BYTES_CNT", "total_BYTES_CNT",
                "total_bytes_cnt", "Total_Bytes_Cnt", "TOTALBYTESCNT", "totalBytesCnt",
                "GROUP_TRANSACTIONID", "group_transactionid", "groupTransactionid",
                "MSID_TYPE", "msid_type", "msidType", "MSIDTYPE",
                "userName", "username", "USERNAME", "user_name", "USER_NAME"};

        for (String key : keys) {
            String json = "{\"" + key + "\":\"V\"}";
            Bean fj = com.alibaba.fastjson.JSON.parseObject(json, Bean.class);
            Bean ad = JSON.parseObject(json, Bean.class);

            assertThat(describe(ad)).as("key=%s", key).isEqualTo(describe(fj));
        }
    }

    private static String describe(Bean b) {
        return "cnt=" + b.getTOTAL_BYTES_CNT() + ",grp=" + b.getGROUP_TRANSACTIONID()
                + ",type=" + b.getMSID_TYPE() + ",user=" + b.getUserName();
    }

    /** 精确匹配优先，不因模糊匹配被抢走 */
    public static class PriorityBean {
        private String user_name;
        private String userName;

        public String getUser_name() { return user_name; }
        public void setUser_name(String v) { this.user_name = v; }
        public String getUserName() { return userName; }
        public void setUserName(String v) { this.userName = v; }
    }

    @Test
    public void testExactMatchWins() {
        PriorityBean b = JSON.parseObject("{\"user_name\":\"A\",\"userName\":\"B\"}",
                PriorityBean.class);

        assertThat(b.getUser_name()).isEqualTo("A");
        assertThat(b.getUserName()).isEqualTo("B");
    }

    /** 真正不存在的字段仍旧静默忽略，不误匹配 */
    @Test
    public void testUnrelatedKeyStillIgnored() {
        Bean b = JSON.parseObject("{\"somethingElse\":\"V\",\"userName\":\"U\"}", Bean.class);

        assertThat(b.getUserName()).isEqualTo("U");
        assertThat(b.getTOTAL_BYTES_CNT()).isNull();
        assertThat(b.getMSID_TYPE()).isNull();
    }

    /** 嵌套对象与集合元素同样生效 */
    public static class Outer {
        private Bean DATA_ITEM;
        private java.util.List<Bean> ITEM_LIST;

        public Bean getDATA_ITEM() { return DATA_ITEM; }
        public void setDATA_ITEM(Bean v) { this.DATA_ITEM = v; }
        public java.util.List<Bean> getITEM_LIST() { return ITEM_LIST; }
        public void setITEM_LIST(java.util.List<Bean> v) { this.ITEM_LIST = v; }
    }

    @Test
    public void testNestedAndCollection() {
        String json = "{\"dataItem\":{\"totalBytesCnt\":\"A\"},"
                + "\"itemList\":[{\"user_name\":\"B\"}]}";

        Outer o = JSON.parseObject(json, Outer.class);

        assertThat(o.getDATA_ITEM().getTOTAL_BYTES_CNT()).isEqualTo("A");
        assertThat(o.getITEM_LIST().get(0).getUserName()).isEqualTo("B");
    }

    /** 非 String 类型走同一路径，转换逻辑不能丢 */
    public static class TypedBean {
        private long TOTAL_BYTES_CNT;
        private boolean IS_ACTIVE;
        private java.util.Date CREATE_TIME;

        public long getTOTAL_BYTES_CNT() { return TOTAL_BYTES_CNT; }
        public void setTOTAL_BYTES_CNT(long v) { this.TOTAL_BYTES_CNT = v; }
        public boolean getIS_ACTIVE() { return IS_ACTIVE; }
        public void setIS_ACTIVE(boolean v) { this.IS_ACTIVE = v; }
        public java.util.Date getCREATE_TIME() { return CREATE_TIME; }
        public void setCREATE_TIME(java.util.Date v) { this.CREATE_TIME = v; }
    }

    @Test
    public void testNonStringTypes() {
        String json = "{\"totalBytesCnt\":1024,\"isActive\":true,"
                + "\"createTime\":\"2024-01-15 10:30:00\"}";

        TypedBean b = JSON.parseObject(json, TypedBean.class);

        assertThat(b.getTOTAL_BYTES_CNT()).isEqualTo(1024L);
        assertThat(b.getIS_ACTIVE()).isTrue();
        assertThat(b.getCREATE_TIME()).isNotNull();
    }
}
