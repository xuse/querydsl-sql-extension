package com.github.xuse.querydsl.lambda;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 校验 LambdaColumn 声明类型与字段实际 Java 类型的映射是否一致。
 * <p>
 * 在应用启动期（首次使用 LambdaColumn 时）进行校验，确保用户不会将
 * DateLambdaColumn/DateTimeLambdaColumn/TimeLambdaColumn 用错。
 * </p>
 */
public final class ColumnTypeValidator {

    private ColumnTypeValidator() {
    }

    /**
     * 时间类型到对应 LambdaColumn 接口的映射
     */
    private static final Map<Class<?>, Class<?>> TYPE_TO_COLUMN = new HashMap<>();

    static {
        // Date 类型 → DateLambdaColumn
        TYPE_TO_COLUMN.put(LocalDate.class, DateLambdaColumn.class);
        TYPE_TO_COLUMN.put(java.sql.Date.class, DateLambdaColumn.class);

        // DateTime 类型 → DateTimeLambdaColumn
        TYPE_TO_COLUMN.put(Instant.class, DateTimeLambdaColumn.class);
        TYPE_TO_COLUMN.put(LocalDateTime.class, DateTimeLambdaColumn.class);
        TYPE_TO_COLUMN.put(Date.class, DateTimeLambdaColumn.class);
        TYPE_TO_COLUMN.put(Timestamp.class, DateTimeLambdaColumn.class);
        TYPE_TO_COLUMN.put(ZonedDateTime.class, DateTimeLambdaColumn.class);
        TYPE_TO_COLUMN.put(Calendar.class, DateTimeLambdaColumn.class);

        // Time 类型 → TimeLambdaColumn
        TYPE_TO_COLUMN.put(LocalTime.class, TimeLambdaColumn.class);
        TYPE_TO_COLUMN.put(Time.class, TimeLambdaColumn.class);
    }

    /**
     * 校验 lambda 引用的声明类型是否与字段的 Java 类型匹配。
     *
     * @param func      lambda column 引用
     * @param beanClass 实体类
     * @param fieldName 字段名
     */
    public static void validate(LambdaColumnBase<?, ?> func,String fieldName, Class<?> fieldType) {
        Class<?> declaredColumnInterface = resolveColumnInterface(func);
        if (declaredColumnInterface == null) {
            // 不是时间类型的 LambdaColumn，不校验
            return;
        }
        if (fieldType == null) {
            return;
        }

        Class<?> expectedColumnInterface = TYPE_TO_COLUMN.get(fieldType);
        if (expectedColumnInterface == null) {
            // 字段类型不在已知时间类型映射中，不校验
            return;
        }
        if (declaredColumnInterface != expectedColumnInterface) {
            throw new IllegalStateException(String.format(
                    "LambdaColumn type mismatch on %s: declared as %s, but field type '%s' should use %s. ",
                    fieldName,
                    declaredColumnInterface.getSimpleName(),
                    fieldType.getSimpleName(),
                    expectedColumnInterface.getSimpleName()));
        }
    }

    /**
     * 判断 func 实现了哪个时间相关的 LambdaColumn 接口
     */
    private static Class<?> resolveColumnInterface(LambdaColumnBase<?, ?> func) {
        if (func instanceof DateTimeLambdaColumn) {
            return DateTimeLambdaColumn.class;
        }
        if (func instanceof DateLambdaColumn) {
            return DateLambdaColumn.class;
        }
        if (func instanceof TimeLambdaColumn) {
            return TimeLambdaColumn.class;
        }
        return null;
    }

}
