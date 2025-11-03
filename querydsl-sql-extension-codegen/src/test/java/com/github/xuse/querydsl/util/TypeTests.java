package com.github.xuse.querydsl.util;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.support.SimpleDataSource;
import com.querydsl.sql.SQLTemplates;

import io.github.xuse.querydsl.sql.code.generate.DbSchemaGenerator;
import io.github.xuse.querydsl.sql.code.generate.LambdaFieldsGenerator;
import io.github.xuse.querydsl.sql.code.generate.OutputDir;
import io.github.xuse.querydsl.sql.code.generate.QCalssGenerator;
import lombok.SneakyThrows;

public class TypeTests {
    JavaParser p = new JavaParser();

    @Test
    public void testParserAst() {
        ParseResult<ClassOrInterfaceType> result = p.parseClassOrInterfaceType("String<List<Aaa<? super Object>[]>>");
        if (result.isSuccessful()) {
            ClassOrInterfaceType value = result.getResult().get();
            print(value, 0);
        } else {
            System.err.println(result.getProblems());
        }
    }

    @Test
    public void testClassAST() {
        URL url = this.getClass().getResource("/Foo.txt");
        printResource(url);
    }

    @Test
    public void testClassAST2() {
        URL url = this.getClass().getResource("/DbTaskQueue.txt");
        printResource(url);
    }

    @SneakyThrows
    private void printResource(URL url) {
        ParseResult<CompilationUnit> result = p.parse(url.openStream());
        if (result.isSuccessful()) {
            CompilationUnit value = result.getResult().get();
            print(value, 0);
            value.accept(new GenericVisitorAdapter<Void, Void>() {

                @Override
                public Void visit(LambdaExpr n, Void arg) {
                    return super.visit(n, arg);
                }

                @Override
                public Void visit(VariableDeclarator n, Void arg) {
                    return super.visit(n, arg);
                }

            }, null);
        } else {
            System.err.println(result.getProblems());
        }
    }

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

    static String s1 = "r-o-o-t";
    static String s2 = "ODgtMDctNTktOTg=";
    static String host = "MTAuODYuMTYuMTI=";

    @Test
    public void testGenerateEntity() {

        System.setProperty("mysql.user", s1.replace("-", ""));
        System.setProperty("mysql.password", JefBase64.decodeUTF8(s2).replace("-", ""));
        System.setProperty("mysql.host", JefBase64.decodeUTF8(host));

        SimpleDataSource ds = new SimpleDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl("jdbc:mysql://" + System.getProperty("mysql.host") + ":3306/sim_card?useSSL=false");
        ds.setUsername(System.getProperty("mysql.user"));
        ds.setPassword(System.getProperty("mysql.password"));

        System.out.println(ds);
        SQLQueryFactory factory = new SQLQueryFactory(querydslConfiguration(SQLQueryFactory.calcSQLTemplate(ds.getUrl())), ds);

        DbSchemaGenerator g = new DbSchemaGenerator(factory);
        g.setOutputDir(OutputDir.DIR_TEST);

//        File file = g.generate("open_simcard_config");
//        System.out.println(file.getAbsolutePath());
        int size=g.generateAll(null);
        System.out.println("生成文件"+size+"个。");
    }

    private void print(Node value, int i) {
        System.out.print(StringUtils.repeat("  ", i));
        System.out.print(" - ");
        String nodeClass = value.getClass().getSimpleName();
        String v;
        if (value instanceof NodeWithIdentifier) {
            v = ((NodeWithIdentifier<?>) value).getIdentifier();
        } else {
            v = value.toString();
        }
        System.out.println(nodeClass + " -- " + v.replace("\r\n", ""));
        for (Node node : value.getChildNodes()) {
            if (node instanceof MethodDeclaration) {
            } else {
                print(node, i + 1);
            }
        }
    }

    public static ConfigurationEx querydslConfiguration(SQLTemplates templates) {
        ConfigurationEx configuration = new ConfigurationEx(templates);
        configuration.setSlowSqlWarnMillis(4000);
        configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
        configuration.allowTableDropAndCreate();
        // 如果使用了自定义映射，需要提前注册，或者扫描指定包
        configuration.getScanOptions().setCreateMissingTable(true).allowDrops().setDataInitBehavior(DataInitBehavior.NONE);
        return configuration;
    }
}
