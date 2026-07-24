package com.github.xuse.querydsl.util;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.github.xuse.querydsl.sql.code.generate.LambdaFieldsGenerator;
import io.github.xuse.querydsl.sql.code.generate.model.OutputDir;

public class LambdaFieldsGeneratorTest {

    @Test
    public void testInlineMode() {
        LambdaFieldsGenerator generator = new LambdaFieldsGenerator();
        generator.setInlineMode(true);
        generator.setOutputDir(OutputDir.DIR_TEST);
        File result = generator.generate(Foo.class);
        System.out.println("Modified file: " + result.getAbsolutePath());
    }

    @Test
    public void testInlineExtensionModule() {
        LambdaFieldsGenerator generator = new LambdaFieldsGenerator();
        generator.setInlineMode(true);
        generator.setOutputDir(OutputDir.DIR_TEST);
        File result = generator.generate(com.github.xuse.querydsl.util.Foo.class);
        System.out.println("Modified file (re-run on same): " + result.getAbsolutePath());
    }
}
