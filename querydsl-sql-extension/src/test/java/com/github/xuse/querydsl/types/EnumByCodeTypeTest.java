package com.github.xuse.querydsl.types;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;

/**
 * Unit tests for {@link EnumByCodeType}.
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnumByCodeType unit tests")
class EnumByCodeTypeTest {

	@Mock
	private ResultSet rs;

	@Mock
	private PreparedStatement ps;

	@Nested
	@DisplayName("setValue - writes integer from getCode() to PreparedStatement")
	class SetValueTests {

		private final EnumByCodeType<Gender> genderType = new EnumByCodeType<>(Gender.class);
		private final EnumByCodeType<TaskStatus> statusType = new EnumByCodeType<>(TaskStatus.class);

		@Test
		@DisplayName("setValue writes MALE code (0) to PreparedStatement")
		void setValue_male_writesZero() throws SQLException {
			genderType.setValue(ps, 1, Gender.MALE);
			verify(ps).setInt(1, 0);
		}

		@Test
		@DisplayName("setValue writes FEMALE code (1) to PreparedStatement")
		void setValue_female_writesOne() throws SQLException {
			genderType.setValue(ps, 1, Gender.FEMALE);
			verify(ps).setInt(1, 1);
		}

		@Test
		@DisplayName("setValue writes TaskStatus.RUNNING code (1) to PreparedStatement")
		void setValue_taskRunning_writesOne() throws SQLException {
			statusType.setValue(ps, 2, TaskStatus.RUNNING);
			verify(ps).setInt(2, 1);
		}

		@Test
		@DisplayName("setValue writes TaskStatus.SUCCESS code (3) to PreparedStatement")
		void setValue_taskSuccess_writesThree() throws SQLException {
			statusType.setValue(ps, 1, TaskStatus.SUCCESS);
			verify(ps).setInt(1, 3);
		}
	}

	@Nested
	@DisplayName("getValue - returns enum constant matching integer code")
	class GetValueTests {

		private final EnumByCodeType<Gender> genderType = new EnumByCodeType<>(Gender.class);
		private final EnumByCodeType<TaskStatus> statusType = new EnumByCodeType<>(TaskStatus.class);

		@Test
		@DisplayName("getValue returns MALE for code 0")
		void getValue_codeZero_returnsMale() throws SQLException {
			when(rs.getInt(1)).thenReturn(0);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(Gender.MALE, genderType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns FEMALE for code 1")
		void getValue_codeOne_returnsFemale() throws SQLException {
			when(rs.getInt(1)).thenReturn(1);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(Gender.FEMALE, genderType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns TaskStatus.INIT for code 0")
		void getValue_taskInit() throws SQLException {
			when(rs.getInt(1)).thenReturn(0);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(TaskStatus.INIT, statusType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns TaskStatus.FAIL for code 2")
		void getValue_taskFail() throws SQLException {
			when(rs.getInt(1)).thenReturn(2);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(TaskStatus.FAIL, statusType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns TaskStatus.SUCCESS for code 3")
		void getValue_taskSuccess() throws SQLException {
			when(rs.getInt(1)).thenReturn(3);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(TaskStatus.SUCCESS, statusType.getValue(rs, 1));
		}
	}

	@Nested
	@DisplayName("Null handling - wasNull returns null")
	class NullHandlingTests {

		private final EnumByCodeType<Gender> genderType = new EnumByCodeType<>(Gender.class);
		private final EnumByCodeType<TaskStatus> statusType = new EnumByCodeType<>(TaskStatus.class);

		@Test
		@DisplayName("getValue returns null when wasNull is true (Gender)")
		void getValue_wasNull_returnsNull_gender() throws SQLException {
			when(rs.getInt(1)).thenReturn(0);
			when(rs.wasNull()).thenReturn(true);
			assertNull(genderType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns null when wasNull is true (TaskStatus)")
		void getValue_wasNull_returnsNull_taskStatus() throws SQLException {
			when(rs.getInt(1)).thenReturn(0);
			when(rs.wasNull()).thenReturn(true);
			assertNull(statusType.getValue(rs, 1));
		}
	}

	@Nested
	@DisplayName("Unmatched code - returns null for unknown codes")
	class UnmatchedCodeTests {

		private final EnumByCodeType<Gender> genderType = new EnumByCodeType<>(Gender.class);
		private final EnumByCodeType<TaskStatus> statusType = new EnumByCodeType<>(TaskStatus.class);

		@Test
		@DisplayName("getValue returns null for code 99 (no matching Gender constant)")
		void getValue_unmatchedCode_returnsNull_gender() throws SQLException {
			when(rs.getInt(1)).thenReturn(99);
			when(rs.wasNull()).thenReturn(false);
			assertNull(genderType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns null for negative code (no matching Gender constant)")
		void getValue_negativeCode_returnsNull() throws SQLException {
			when(rs.getInt(1)).thenReturn(-1);
			when(rs.wasNull()).thenReturn(false);
			assertNull(genderType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns null for code 100 (no matching TaskStatus constant)")
		void getValue_unmatchedCode_returnsNull_taskStatus() throws SQLException {
			when(rs.getInt(1)).thenReturn(100);
			when(rs.wasNull()).thenReturn(false);
			assertNull(statusType.getValue(rs, 1));
		}

		@Test
		@DisplayName("getValue returns null for code 4 (just beyond TaskStatus range)")
		void getValue_boundaryCode_returnsNull() throws SQLException {
			when(rs.getInt(1)).thenReturn(4);
			when(rs.wasNull()).thenReturn(false);
			assertNull(statusType.getValue(rs, 1));
		}
	}

	@Nested
	@DisplayName("Construction and metadata")
	class ConstructionTests {

		@Test
		@DisplayName("getReturnedClass returns the enum class")
		void getReturnedClass_returnsEnumClass() {
			EnumByCodeType<Gender> type = new EnumByCodeType<>(Gender.class);
			assertEquals(Gender.class, type.getReturnedClass());
		}

		@Test
		@DisplayName("constructor with custom JDBC type")
		void constructorWithCustomJdbcType() {
			EnumByCodeType<TaskStatus> type = new EnumByCodeType<>(Types.SMALLINT, TaskStatus.class);
			assertEquals(TaskStatus.class, type.getReturnedClass());
		}

		@Test
		@DisplayName("default constructor uses Types.INTEGER")
		void defaultConstructor_usesIntegerType() {
			EnumByCodeType<Gender> type = new EnumByCodeType<>(Gender.class);
			// AbstractType stores the JDBC type; verify via getSQLTypes
			int[] sqlTypes = type.getSQLTypes();
			assertNotNull(sqlTypes);
			assertTrue(sqlTypes.length > 0);
			assertEquals(Types.INTEGER, sqlTypes[0]);
		}

		@Test
		@DisplayName("custom JDBC type is preserved")
		void customJdbcType_isPreserved() {
			EnumByCodeType<TaskStatus> type = new EnumByCodeType<>(Types.SMALLINT, TaskStatus.class);
			int[] sqlTypes = type.getSQLTypes();
			assertNotNull(sqlTypes);
			assertTrue(sqlTypes.length > 0);
			assertEquals(Types.SMALLINT, sqlTypes[0]);
		}
	}
}
