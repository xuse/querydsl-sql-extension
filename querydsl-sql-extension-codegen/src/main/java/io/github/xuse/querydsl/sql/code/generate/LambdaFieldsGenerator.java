package io.github.xuse.querydsl.sql.code.generate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.sql.column.AccessibleElement;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.util.StringUtils;

import io.github.xuse.querydsl.sql.code.generate.JavaTypeToPropertyPathMappings.PathGenerator;
import io.github.xuse.querydsl.sql.code.generate.core.ClassImpl;
import io.github.xuse.querydsl.sql.code.generate.core.ClassMetadata;
import io.github.xuse.querydsl.sql.code.generate.core.CompilationUnitBuilder;
import io.github.xuse.querydsl.sql.code.generate.model.OutputDir;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 解析注解类，生成Lambda定义的模型常量。
 * <p>支持两种模式：</p>
 * <ul>
 *   <li>{@code inlineMode = false}（默认）：生成独立的 {@code EntityName_} 文件</li>
 *   <li>{@code inlineMode = true}：解析已有的 entity 源文件，将 static final 常量直接插入其中</li>
 * </ul>
 */
@Slf4j
@Setter
public class LambdaFieldsGenerator {
    private Function<String, String> tableRefNameFunction = LambdaFieldsGenerator::clacTableField;
    private Function<String, String> columnRefNameFunction = (s) -> "_" + s;

    private OutputDir outputDir = OutputDir.DIR_MAIN;

    /**
     * 自定义输出路径。设置后优先于 outputDir。
     */
    private String outputPath;

    /**
     * 是否将常量直接写入已有的entity源文件（inline模式）。
     * 为 false 时生成独立的 EntityName_ 文件（默认行为）。
     */
    private boolean inlineMode = false;

    /**
     * 获取实际使用的输出基础路径
     */
    private String getBasePath() {
        if (outputPath != null) {
            return outputPath.endsWith("/") ? outputPath : outputPath + "/";
        }
        return outputDir.path;
    }

    /**
     * Generate lambda fields for entity class.
     * 
     * @param entityClz entity class
     * @return the file generated or modified.
     */
    public File generate(Class<?> entityClz) {
        log.info("Generating lambda fields for {}", entityClz.getName());
        return generate(entityClz.getPackage().getName(), entityClz);
    }

    /**
     * Generate lambda fields for entity class.
     * 
     * @param pkg       target package name
     * @param entityClz entity class.
     * @return the file generated or modified.
     */
    public File generate(String pkg, Class<?> entityClz) {
        Assert.notNull(entityClz);
        if (inlineMode) {
            return generateInline(pkg, entityClz);
        } else {
            return generateSeparateFile(pkg, entityClz);
        }
    }

    /**
     * 默认模式：生成独立的 EntityName_ 文件
     */
    private File generateSeparateFile(String pkg, Class<?> entityClz) {
        String className = entityClz.getSimpleName() + "_";
        CompilationUnitBuilder cu = CompilationUnitBuilder.create();

        cu.addImport(entityClz);
        cu.setPackageDeclaration(pkg);
        ClassOrInterfaceDeclaration targetClz = cu.addClass(className);

        ClassMetadata entity = new ClassImpl(entityClz);
        addStaticDefinitions(targetClz, entity, cu);

        File file = new File(getBasePath() + pkg.replace('.', '/') + "/" + className + ".java");
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), cu.build().toString().getBytes("UTF-8"));
        } catch (IOException e) {
            throw Exceptions.toRuntime(e);
        }
        return file;
    }

    /**
     * Inline模式：解析已有的entity源文件，将 static final LambdaColumn 常量插入类中。
     * 已存在的同名常量会被跳过，不会重复生成。
     */
    private File generateInline(String pkg, Class<?> entityClz) {
        String entityFileName = entityClz.getSimpleName() + ".java";
        File sourceFile = new File(getBasePath() + pkg.replace('.', '/') + "/" + entityFileName);
        if (!sourceFile.exists()) {
            throw Exceptions.illegalArgument("Source file not found for inline generation: {}", sourceFile.getAbsolutePath());
        }

        JavaParser javaParser = new JavaParser();
        CompilationUnit rawCu;
        try {
            String source = new String(Files.readAllBytes(sourceFile.toPath()), "UTF-8");
            ParseResult<CompilationUnit> result = javaParser.parse(source);
            if (!result.isSuccessful()) {
                throw Exceptions.illegalArgument("Failed to parse source file: {}\nProblems: {}", sourceFile, result.getProblems());
            }
            rawCu = result.getResult().get();
        } catch (IOException e) {
            throw Exceptions.toRuntime(e);
        }

        ClassOrInterfaceDeclaration classDecl = rawCu.getClassByName(entityClz.getSimpleName())
                .orElseThrow(() -> new IllegalStateException(
                        "Class " + entityClz.getSimpleName() + " not found in " + sourceFile));

        // 使用 CompilationUnitBuilder 包装已有的 CompilationUnit，复用 import 和类型生成逻辑
        CompilationUnitBuilder cu = new CompilationUnitBuilder(rawCu);
        ClassMetadata entity = new ClassImpl(entityClz);
        addStaticDefinitionsInline(classDecl, entity, cu);

        try {
            Files.write(sourceFile.toPath(), cu.build().toString().getBytes("UTF-8"));
        } catch (IOException e) {
            throw Exceptions.toRuntime(e);
        }
        log.info("Lambda fields added inline to {}", sourceFile);
        return sourceFile;
    }

    /**
     * 向已有类中添加 static final 常量（inline模式使用）。
     * 如果同名字段已存在则跳过。
     */
    private void addStaticDefinitionsInline(ClassOrInterfaceDeclaration classDecl, ClassMetadata entityClz,
            CompilationUnitBuilder cu) {
        String tableFieldName = tableRefNameFunction.apply(entityClz.getSimpleName());
        ClassOrInterfaceType entityType = cu.createClassType(entityClz);

        // 表引用常量
        if (!hasField(classDecl, tableFieldName)) {
            FieldDeclaration table = classDecl.addField(
                    cu.createType(LambdaTable.class, entityType), tableFieldName,
                    Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL);
            LambdaExpr expr = new LambdaExpr(NodeList.nodeList(),
                    new ExpressionStmt(new ClassExpr(entityType.clone())), true);
            table.getVariables().get(0).setInitializer(expr);
        }

        // 各列引用常量
        for (AccessibleElement field : entityClz.getColumnFields()) {
            String name = field.getName();
            String constantName = columnRefNameFunction.apply(name);
            if (hasField(classDecl, constantName)) {
                continue;
            }
            PathGenerator generator = JavaTypeToPropertyPathMappings.getGenerator(field.getType());
            java.lang.reflect.Type fType = field.getGenericType();
            FieldDeclaration propPath = classDecl.addField(
                    generator.lambdaType(fType, entityType, cu), constantName,
                    Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL);

            MethodReferenceExpr methodRef = new MethodReferenceExpr();
            methodRef.setScope(new TypeExpr(entityType.clone()));
            methodRef.setIdentifier(toGetMethodName(field));
            propPath.getVariable(0).setInitializer(methodRef);
        }
    }

    public void addStaticDefinitions(ClassOrInterfaceDeclaration parent,
            ClassMetadata entityClz, CompilationUnitBuilder cu) {
        String tableFieldName = tableRefNameFunction.apply(entityClz.getSimpleName());
        ClassOrInterfaceType entityType = cu.createClassType(entityClz);
        // 生成表定义
        {
            FieldDeclaration table = parent.addField(cu.createType(LambdaTable.class, entityType), tableFieldName, Keyword.PUBLIC,
                    Keyword.FINAL, Keyword.STATIC);
            LambdaExpr expr = new LambdaExpr(NodeList.nodeList(), new ExpressionStmt(new ClassExpr(entityType)), true);
            table.getVariables().get(0).setInitializer(expr);
        }
        // 各个字段描述
        for (AccessibleElement field : entityClz.getColumnFields()) {
            String name = field.getName();
            PathGenerator generator = JavaTypeToPropertyPathMappings.getGenerator(field.getType());
            java.lang.reflect.Type fType = field.getGenericType();
            FieldDeclaration propPath = parent.addField(generator.lambdaType(fType, entityType, cu), columnRefNameFunction.apply(name),
                    Keyword.PUBLIC, Keyword.FINAL, Keyword.STATIC);

            MethodReferenceExpr expr = new MethodReferenceExpr();
            expr.setScope(new TypeExpr(entityType));
            expr.setIdentifier(toGetMethodName(field));
            propPath.getVariable(0).setInitializer(expr);
        }
    }

    private static boolean hasField(ClassOrInterfaceDeclaration classDecl, String fieldName) {
        for (FieldDeclaration fd : classDecl.getFields()) {
            for (VariableDeclarator vd : fd.getVariables()) {
                if (vd.getNameAsString().equals(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String toGetMethodName(AccessibleElement field) {
        if (field.getType() == boolean.class) {
            return "is" + StringUtils.capitalize(field.getName());
        } else {
            return "get" + StringUtils.capitalize(field.getName());
        }
    }

    public static String clacTableField(String simpleName) {
        return StringUtils.uncapitalize(simpleName);
    }

    public static String calcAlias(String simpleName) {
        StringBuilder sb = new StringBuilder();
        for (char c : simpleName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        if (sb.length() < 3) {
            return simpleName.substring(0, Math.min(3, simpleName.length())).toLowerCase();
        }
        return sb.toString();
    }
}
