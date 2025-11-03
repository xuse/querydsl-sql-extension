package io.github.xuse.querydsl.sql.code.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.xuse.querydsl.annotation.dbdef.Check;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.Key;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.column.AccessibleElement;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.SchemaAndTable;

import io.github.xuse.querydsl.sql.code.generate.CompilationUnitBuilder.AnnotationBuilder;
import io.github.xuse.querydsl.sql.code.generate.FieldCratetors.FieldGenerator;
import io.github.xuse.querydsl.sql.code.generate.util.GenericTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 解析数据库数据。生成对应实体类
 */
@Slf4j
public class DbSchemaGenerator {
    private OutputDir outputDir = OutputDir.DIR_MAIN;
    private String packageName = "io.github.xuse.test";
    private boolean writeTableSchema = false;
    private boolean ignoreColumnCase = false;
    private boolean uselombokData = true;
    private boolean addStaticLambdaColumn = true;

    private final SQLMetadataQueryFactory metadata;
    private Function<String, String> classNameConverter = (s) -> nameApply(s,true);
    private Function<String, String> fieldNameConverter = (s) -> nameApply(s,false);
    private Function<String, String> modelNameConvevrter = (s) -> "_" + s;
    private Function<String, String> remarkProcessor= (s)-> s.trim().replace("\"", "\\\"");

    public static String nameApply(String s, boolean beginUpper) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean toUpper = beginUpper;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                toUpper = true;
                continue;
            }
            if (toUpper) {
                sb.append(Character.toUpperCase(c));
                toUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public DbSchemaGenerator(SQLQueryFactory factory) {
        this.metadata = factory.getMetadataFactory();
    }

    public File generate(String name) {
        TableInfo table = metadata.getTable(new SchemaAndTable(null, name));
        return generateTable(table);
    }
    
    public int generateAll(String databaseName) {
        if(StringUtils.isEmpty(databaseName)) {
            databaseName=metadata.getDatabaseInfo().getNamespace();
        }
        List<File> files=new ArrayList<>();
        List<TableInfo> tables=metadata.listTables(databaseName, null);
        for(TableInfo table: tables) {
            File file = generateTable(table);
            files.add(file);
            log.info("Generate file {}", file.getAbsolutePath());
        }
        return files.size();
    }


    public OutputDir getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(OutputDir outputDir) {
        this.outputDir = outputDir;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isWriteTableSchema() {
        return writeTableSchema;
    }

    public void setWriteTableSchema(boolean writeTableSchema) {
        this.writeTableSchema = writeTableSchema;
    }

    public boolean isIgnoreColumnCase() {
        return ignoreColumnCase;
    }

    public void setIgnoreColumnCase(boolean ignoreColumnCase) {
        this.ignoreColumnCase = ignoreColumnCase;
    }

    public boolean isUselombokData() {
        return uselombokData;
    }

    public void setUselombokData(boolean uselombokData) {
        this.uselombokData = uselombokData;
    }

    public boolean isAddStaticLambdaColumn() {
        return addStaticLambdaColumn;
    }

    public void setAddStaticLambdaColumn(boolean addStaticLambdaColumn) {
        this.addStaticLambdaColumn = addStaticLambdaColumn;
    }

    public Function<String, String> getClassNameConverter() {
        return classNameConverter;
    }

    public void setClassNameConverter(Function<String, String> classNameConverter) {
        this.classNameConverter = classNameConverter;
    }

    public Function<String, String> getFieldNameConverter() {
        return fieldNameConverter;
    }

    public void setFieldNameConverter(Function<String, String> fieldNameConverter) {
        this.fieldNameConverter = fieldNameConverter;
    }

    private File generateTable(TableInfo table) {
        SchemaAndTable key = table.toSchemaTable();
        List<ColumnDef> columns = metadata.getColumns(key);
        Map<String, String> columnToFieldName = createColumnMap(columns);

        String className = classNameConverter.apply(table.getName());
        CompilationUnitBuilder cu = CompilationUnitBuilder.create();

        cu.setPackageDeclaration(packageName);
        ClassOrInterfaceDeclaration targetClz = cu.addClass(className);
 
        // 生成表头注解
        {
            AnnotationBuilder<TableSpec> tableSpec = cu.createAnnotation(TableSpec.class);
            if (writeTableSchema) {
                tableSpec.add("schema", cu.literal(table.getSchema()));
            }
            tableSpec.add("name", cu.literal(table.getName()));
            String collate = table.getAttribute("COLLATE");
            if (StringUtils.isNotEmpty(collate)) {
                tableSpec.add("collate", cu.literal(collate));
            }

            Collection<Constraint> constraints = metadata.getAllIndexAndConstraints(key);
            List<String> pkFields = Collections.emptyList();
            List<AnnotationExpr> indexAnnos = new ArrayList<>();
            List<AnnotationExpr> checkAnnos = new ArrayList<>();

            for (Constraint c : constraints) {
                ConstraintType type = c.getConstraintType();
                if (type.isColumnList()) {
                    if (type == ConstraintType.PRIMARY_KEY) {
                        pkFields = c.getColumnNames().stream().map(n -> columnToFieldName.get(normalizeColumn(n)))
                                .collect(Collectors.toList());
                    } else {
                        indexAnnos.add(createIndexAnnotation(c, cu, columnToFieldName));
                    }
                } else if (type.isCheckClause()) {
                    checkAnnos.add(createCheckAnnotation(c, cu));
                }
            }
            if (!pkFields.isEmpty()) {
                tableSpec.add("primaryKeys", cu.arrayString(pkFields));
            }
            if (indexAnnos != null) {
                tableSpec.add("keys", cu.array(indexAnnos));
            }
            if (checkAnnos != null) {
                tableSpec.add("checks", cu.array(checkAnnos));
            }
            targetClz.addAnnotation(tableSpec.build());

            if (StringUtils.isNotBlank(table.getRemarks())) {
                targetClz.addAnnotation(cu.createAnnotation(Comment.class).add("value", cu.literal(remarkProcessor.apply(table.getRemarks()))).build());
            }

            if (uselombokData) {
                targetClz.addAnnotation(cu.createAnnotation(Data.class).build());
            }
        }
        
    
        List<AccessibleElement> fields=new ArrayList<>();
        // 生成各个字段
        for (ColumnDef c : columns) {
            String fieldName = columnToFieldName.get(normalizeColumn(c.getColumnName()));
            Assert.hasLength(fieldName);
            FieldGenerator fg = FieldCratetors.getGenerator(c.getJdbcType());
            Type fieldType=fg.getFieldType(c);
            FieldDeclaration columnField = targetClz.addField(cu.createType(fieldType), fieldName, Keyword.PRIVATE);

            AnnotationBuilder<ColumnSpec> columnSpec = cu.createAnnotation(ColumnSpec.class);
            fg.setAttribs(columnSpec, c);
            columnField.addAnnotation(columnSpec.build());

            if (!StringUtils.isBlank(c.getRemarks())) {
                AnnotationBuilder<Comment> comment = cu.createAnnotation(Comment.class);
                comment.add("value", cu.literal(remarkProcessor.apply(c.getRemarks())));
                columnField.addAnnotation(comment.build());
            }
            fields.add(new ColumnField(fieldName,fieldType));
        }
        if(addStaticLambdaColumn) {
            ClassOrInterfaceType entityType=new ClassOrInterfaceType(null, className);
            LambdaFieldsGenerator.addStaticDefinitions(targetClz, "_table", entityType, cu, fields, modelNameConvevrter);
        }
        
        // 将AST保存成Java文件
        File file = new File(outputDir.path + packageName.replace('.', '/') + "/" + className + ".java");
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(cu.build().toString());
        } catch (IOException e) {
            throw Exceptions.toRuntime(e);
        }
        return file;
    }
    
    @AllArgsConstructor
    static class ColumnField implements AccessibleElement{
        final String name;
        final Type type;
        @Override
        public <T extends Annotation> T getAnnotation(Class<T> clz) {
            return null;
        }
        @Override
        public Class<?> getType() {
            return GenericTypes.getRawClass(type);
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public void set(Object bean, Object value) {
        }
        @Override
        public Type getGenericType() {
            return type;
        }
    }

    private AnnotationExpr createCheckAnnotation(Constraint check, CompilationUnitBuilder cu) {
        return cu.createAnnotation(Check.class).add("name", cu.literal(check.getName()))
                .add("value", cu.literal(check.getCheckClause().toString())).build();
    }

    private AnnotationExpr createIndexAnnotation(Constraint index, CompilationUnitBuilder cu, Map<String, String> columnToFieldName) {
        AnnotationBuilder<Key> builder = cu.createAnnotation(Key.class);

        builder.add("name", cu.literal(index.getName()));

        ConstraintType type = index.getConstraintType();
        builder.add("type", cu.createFieldAccess(ConstraintType.class, type.name()));

        List<String> paths = index.getColumnNames().stream().map((e) -> columnToFieldName.get(normalizeColumn(e)))
                .collect(Collectors.toList());
        builder.add("path", cu.arrayString(paths));
        builder.add("allowIgnore", cu.literal(false));
        return builder.build();
    }

    private Map<String, String> createColumnMap(List<ColumnDef> columns) {
        Map<String, String> map = new HashMap<>();
        for (ColumnDef def : columns) {
            map.put(normalizeColumn(def.getColumnName()), fieldNameConverter.apply(def.getColumnName()));
        }
        return map;
    }

    private String normalizeColumn(String col) {
        return ignoreColumnCase ? col.toLowerCase() : col;
    }

    public Function<String, String> getModelNameConvevrter() {
        return modelNameConvevrter;
    }

    public void setModelNameConvevrter(Function<String, String> modelNameConvevrter) {
        this.modelNameConvevrter = modelNameConvevrter;
    }
}
