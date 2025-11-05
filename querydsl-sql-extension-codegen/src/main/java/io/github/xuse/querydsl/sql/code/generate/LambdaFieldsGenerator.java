package io.github.xuse.querydsl.sql.code.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Function;

import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
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
 * 解析注解类，生成Lambda定义的模型常量
 */
@Slf4j
@Setter
public class LambdaFieldsGenerator {
    private Function<String, String> tableRefNameFunction = LambdaFieldsGenerator::clacTableField;
    private Function<String, String> columnRefNameFunction = (s) -> "_" + s;

    private OutputDir outputDir = OutputDir.DIR_MAIN;

    /**
     * Generate q class for entity class.
     * 
     * @param entityClz
     * @return the file generated.
     */
    public File generate(Class<?> entityClz) {
        log.info("Generating query class for {}", entityClz.getName());
        return generate(entityClz.getPackage().getName(), entityClz);
    }

    /**
     * Generate q class for entity class.
     * 
     * @param pkg       target package name
     * @param entityClz entity class.
     * @return the file generated.
     */
    public File generate(String pkg, Class<?> entityClz) {
        Assert.notNull(entityClz);
        String className = entityClz.getSimpleName() + "_";
        CompilationUnitBuilder cu = CompilationUnitBuilder.create();

        cu.addImport(entityClz);
        cu.setPackageDeclaration(pkg);
        ClassOrInterfaceDeclaration targetClz = cu.addClass(className);

        ClassMetadata entity=new ClassImpl(entityClz);
        addStaticDefinitions(targetClz, entity, cu);

        // 将AST保存成Java文件
        File file = new File(outputDir.path + pkg.replace('.', '/') + "/" + className + ".java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(cu.build().toString());
        } catch (IOException e) {
            throw Exceptions.toRuntime(e);
        }
        return file;
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
            // 需要的
            String name = field.getName();
            PathGenerator generatgor = JavaTypeToPropertyPathMappings.getGenerator(field.getType());
            java.lang.reflect.Type fType = field.getGenericType();
            FieldDeclaration propPath = parent.addField(generatgor.lambdaType(fType, entityType, cu), columnRefNameFunction.apply(name),
                    Keyword.PUBLIC, Keyword.FINAL, Keyword.STATIC);

            MethodReferenceExpr expr = new MethodReferenceExpr();
            expr.setScope(new TypeExpr(entityType));
            expr.setIdentifier(toGetMethodName(field));
            propPath.getVariable(0).setInitializer(expr);
        }

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
