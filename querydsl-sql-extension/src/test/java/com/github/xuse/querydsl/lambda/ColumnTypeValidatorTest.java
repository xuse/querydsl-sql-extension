package com.github.xuse.querydsl.lambda;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;

/**
 * 测试 ColumnTypeValidator 在包扫描时能否正确检测 LambdaColumn 类型错误。
 */
public class ColumnTypeValidatorTest {

    // ===== 正确定义的 Entity =====

    @SuppressWarnings("serial")
    @TableSpec(name = "test_correct")
    public static class CorrectEntity implements Serializable {
        @ColumnSpec
        private LocalDate birthday;
        @ColumnSpec
        private Instant created;
        @ColumnSpec
        private LocalTime alarm;

        public LocalDate getBirthday() { return birthday; }
        public Instant getCreated() { return created; }
        public LocalTime getAlarm() { return alarm; }

        // 正确：LocalDate → DateLambdaColumn
        public static final DateLambdaColumn<CorrectEntity, LocalDate> _birthday = CorrectEntity::getBirthday;
        // 正确：Instant → DateTimeLambdaColumn
        public static final DateTimeLambdaColumn<CorrectEntity, Instant> _created = CorrectEntity::getCreated;
        // 正确：LocalTime → TimeLambdaColumn
        public static final TimeLambdaColumn<CorrectEntity, LocalTime> _alarm = CorrectEntity::getAlarm;
    }

    // ===== 错误定义的 Entity（LocalDate 用了 DateTimeLambdaColumn） =====

    @SuppressWarnings("serial")
    @TableSpec(name = "test_wrong_date")
    public static class WrongDateEntity implements Serializable {
        @ColumnSpec
        private LocalDate birthday;

        public LocalDate getBirthday() { return birthday; }

        // 错误！LocalDate 应该用 DateLambdaColumn，不是 DateTimeLambdaColumn
        public static final DateTimeLambdaColumn<WrongDateEntity, LocalDate> _birthday = WrongDateEntity::getBirthday;
    }

    // ===== 错误定义的 Entity（Instant 用了 DateLambdaColumn） =====

    @SuppressWarnings("serial")
    @TableSpec(name = "test_wrong_datetime")
    public static class WrongDateTimeEntity implements Serializable {
        @ColumnSpec
        private Instant created;

        public Instant getCreated() { return created; }

        // 错误！Instant 应该用 DateTimeLambdaColumn，不是 DateLambdaColumn
        public static final DateLambdaColumn<WrongDateTimeEntity, Instant> _created = WrongDateTimeEntity::getCreated;
    }

    // ===== 错误定义的 Entity（LocalTime 用了 DateTimeLambdaColumn） =====

    @SuppressWarnings("serial")
    @TableSpec(name = "test_wrong_time")
    public static class WrongTimeEntity implements Serializable {
        @ColumnSpec
        private LocalTime alarm;

        public LocalTime getAlarm() { return alarm; }

        // 错误！LocalTime 应该用 TimeLambdaColumn，不是 DateTimeLambdaColumn
        public static final DateTimeLambdaColumn<WrongTimeEntity, LocalTime> _alarm = WrongTimeEntity::getAlarm;
    }

    @Test
    public void testCorrectDefinition() {
        // 正确定义不应该抛异常
        assertDoesNotThrow(() -> RelationalPathExImpl.valueOf(CorrectEntity.class, null));
    }

    @Test
    public void testWrongDate_shouldThrow() {
        // LocalDate 用了 DateTimeLambdaColumn，应该抛异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> RelationalPathExImpl.valueOf(WrongDateEntity.class, null));
        assertEquals(true, ex.getMessage().contains("DateTimeLambdaColumn"));
        assertEquals(true, ex.getMessage().contains("DateLambdaColumn"));
        assertEquals(true, ex.getMessage().contains("LocalDate"));
        System.out.println("Caught expected: " + ex.getMessage());
    }

    @Test
    public void testWrongDateTime_shouldThrow() {
        // Instant 用了 DateLambdaColumn，应该抛异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> RelationalPathExImpl.valueOf(WrongDateTimeEntity.class, null));
        assertEquals(true, ex.getMessage().contains("DateLambdaColumn"));
        assertEquals(true, ex.getMessage().contains("DateTimeLambdaColumn"));
        assertEquals(true, ex.getMessage().contains("Instant"));
        System.out.println("Caught expected: " + ex.getMessage());
    }

    @Test
    public void testWrongTime_shouldThrow() {
        // LocalTime 用了 DateTimeLambdaColumn，应该抛异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> RelationalPathExImpl.valueOf(WrongTimeEntity.class, null));
        assertEquals(true, ex.getMessage().contains("DateTimeLambdaColumn"));
        assertEquals(true, ex.getMessage().contains("TimeLambdaColumn"));
        assertEquals(true, ex.getMessage().contains("LocalTime"));
        System.out.println("Caught expected: " + ex.getMessage());
    }
}
