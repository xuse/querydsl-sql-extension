package io.github.xuse.fastjson.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 归一化后重名的属性（同类中既有 {@code user_name} 又有 {@code userName}）的匹配行为。
 * <p>
 * fastjson 1.2.83 的做法见 {@code JavaBeanDeserializer.smartMatch}：
 * 它构建 {@code smartMatchHashArrayMapping} 时按 {@code sortedFieldDeserializers}
 * 顺序覆盖同一 hash 槽位，因此重名不会被排除，而是由字段排序（{@code FieldInfo.compareTo}
 * 按字段名字典序）决定最后落到哪个属性上。{@code user_name} 与 {@code userName}
 * 归一化后同为 {@code username}，模糊匹配统一命中 {@code userName}。
 * <p>
 * 精确名匹配发生在模糊匹配之前（{@code getFieldDeserializer}），故 {@code user_name}
 * 与 {@code userName} 作为 key 时各自精确命中，不受影响。
 */
public class NormalizedDuplicateNameTest {

    public static class DupBean {
        private String user_name;
        private String userName;

        public String getUser_name() { return user_name; }
        public void setUser_name(String v) { this.user_name = v; }
        public String getUserName() { return userName; }
        public void setUserName(String v) { this.userName = v; }
    }

    /** 精确名优先，各自命中自己的属性 */
    @Test
    public void testExactNameWins() {
        DupBean b = JSON.parseObject("{\"user_name\":\"A\",\"userName\":\"B\"}", DupBean.class);

        assertThat(b.getUser_name()).isEqualTo("A");
        assertThat(b.getUserName()).isEqualTo("B");
    }

    /** 模糊匹配的归属与 fastjson 一致 */
    @Test
    public void testFuzzyMatchTargetSameAsFastjson() {
        String[] keys = {"user_name", "userName", "username", "USERNAME",
                "USER_NAME", "User_Name", "UserName", "user-name"};

        for (String key : keys) {
            String json = "{\"" + key + "\":\"V\"}";
            DupBean fj = com.alibaba.fastjson.JSON.parseObject(json, DupBean.class);
            DupBean ad = JSON.parseObject(json, DupBean.class);

            assertThat(describe(ad)).as("key=%s", key).isEqualTo(describe(fj));
        }
    }

    private static String describe(DupBean b) {
        return "user_name=" + b.getUser_name() + ",userName=" + b.getUserName();
    }

    /** 字段声明顺序颠倒时归属不变，取决于字典序而非声明序 */
    public static class DupBeanReversed {
        private String userName;
        private String user_name;

        public String getUserName() { return userName; }
        public void setUserName(String v) { this.userName = v; }
        public String getUser_name() { return user_name; }
        public void setUser_name(String v) { this.user_name = v; }
    }

    @Test
    public void testOrderIndependent() {
        String json = "{\"username\":\"V\"}";

        DupBeanReversed fj = com.alibaba.fastjson.JSON.parseObject(json, DupBeanReversed.class);
        DupBeanReversed ad = JSON.parseObject(json, DupBeanReversed.class);

        assertThat("user_name=" + ad.getUser_name() + ",userName=" + ad.getUserName())
                .isEqualTo("user_name=" + fj.getUser_name() + ",userName=" + fj.getUserName());
    }
}
