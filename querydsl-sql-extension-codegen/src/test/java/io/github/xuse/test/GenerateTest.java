package io.github.xuse.test;

import java.io.File;
import java.sql.Types;
import java.time.Instant;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.support.SimpleDataSource;
import com.github.xuse.querydsl.util.Foo;
import com.github.xuse.querydsl.util.JefBase64;

import io.github.xuse.querydsl.sql.code.generate.DbSchemaGenerator;
import io.github.xuse.querydsl.sql.code.generate.LambdaFieldsGenerator;
import io.github.xuse.querydsl.sql.code.generate.QCalssGenerator;
import io.github.xuse.querydsl.sql.code.generate.model.MetafieldGenerationType;
import io.github.xuse.querydsl.sql.code.generate.model.OutputDir;

public class GenerateTest {

    static String s1 = "r-o-o-t";
    static String s2 = "ODgtMDctNTktOTg=";
    static String host = "MTAuODYuMTYuMTI=";

    @Test
    public void testGenerateQclz() {
        QCalssGenerator g = new QCalssGenerator();
        g.setOutputDir(OutputDir.DIR_TEST);
        File file = g.generate(Foo.class);
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testGenerateLambda() {
        LambdaFieldsGenerator g = new LambdaFieldsGenerator();
        g.setOutputDir(OutputDir.DIR_TEST);
        File file = g.generate(Foo.class);
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testGenerateEntity() {
        DbSchemaGenerator.from(getDataSource()).output(OutputDir.DIR_TARGET).metafields(MetafieldGenerationType.QCLASS).useLombokAnnotation(true)
                .tableRefNameIs(s -> "_table").generateAll("sim_card");
    }

    @Test
    public void testGenerate2() {
        DbSchemaGenerator.from(getDataSource())
        .output(OutputDir.DIR_TARGET)
        .metafields(MetafieldGenerationType.LAMBDA)
        .useLombokAnnotation(false)
        .registerMapping(Types.TIMESTAMP, Instant.class)
        .tableRefNameIs(s -> "_table")
        .generateTables(null, "sim_card_dev%");
    }

    private DataSource getDataSource() {
        System.setProperty("mysql.user", s1.replace("-", ""));
        System.setProperty("mysql.password", JefBase64.decodeUTF8(s2).replace("-", ""));
        System.setProperty("mysql.host", JefBase64.decodeUTF8(host));

        SimpleDataSource ds = new SimpleDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl("jdbc:mysql://" + System.getProperty("mysql.host") + ":3306/sim_card?useSSL=false");
        ds.setUsername(System.getProperty("mysql.user"));
        ds.setPassword(System.getProperty("mysql.password"));
        return ds;
    }
}
