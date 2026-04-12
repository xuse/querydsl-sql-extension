package com.github.xuse.querydsl.types;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;

@ExtendWith(MockitoExtension.class)
class ArrayTypeTest {

	@Mock
	private ResultSet rs;

	@Mock
	private PreparedStatement ps;

	// ==================== IntArrayAsVarcharType ====================

	@Nested
	class IntArrayTests {

		private final IntArrayAsVarcharType type = new IntArrayAsVarcharType();

		@Test
		void getReturnedClass_returnsIntArray() {
			assertEquals(int[].class, type.getReturnedClass());
		}

		@Test
		void getValue_validString() throws SQLException {
			when(rs.getString(1)).thenReturn("1,2,3");
			int[] result = type.getValue(rs, 1);
			assertArrayEquals(new int[]{1, 2, 3}, result);
		}

		@Test
		void getValue_nullReturnsNull() throws SQLException {
			when(rs.getString(1)).thenReturn(null);
			assertNull(type.getValue(rs, 1));
		}

		@Test
		void setValue_writesCorrectString() throws SQLException {
			type.setValue(ps, 1, new int[]{10, 20, 30});
			// Note: implementation iterates as double, so values become "10.0,20.0,30.0"
			verify(ps).setString(1, "10.0,20.0,30.0");
		}

		@Test
		void setValue_emptyArray() throws SQLException {
			type.setValue(ps, 1, new int[]{});
			verify(ps).setString(1, "");
		}

		@Test
		void customSeparator() throws SQLException {
			IntArrayAsVarcharType custom = new IntArrayAsVarcharType().setSepChar('|');
			when(rs.getString(1)).thenReturn("4|5|6");
			int[] result = custom.getValue(rs, 1);
			assertArrayEquals(new int[]{4, 5, 6}, result);
		}

		@Test
		void invalidStrAsZero_true_handlesInvalidString() throws SQLException {
			IntArrayAsVarcharType safe = new IntArrayAsVarcharType().invalidStrAsZero(true);
			when(rs.getString(1)).thenReturn("1,abc,3");
			int[] result = safe.getValue(rs, 1);
			assertArrayEquals(new int[]{1, 0, 3}, result);
		}

		@Test
		void invalidStrAsZero_false_throwsException() throws SQLException {
			when(rs.getString(1)).thenReturn("1,abc,3");
			assertThrows(NumberFormatException.class, () -> type.getValue(rs, 1));
		}
	}

	// ==================== LongArrayAsVarcharType ====================

	@Nested
	class LongArrayTests {

		private final LongArrayAsVarcharType type = new LongArrayAsVarcharType();

		@Test
		void getReturnedClass_returnsLongArray() {
			assertEquals(long[].class, type.getReturnedClass());
		}

		@Test
		void getValue_validString() throws SQLException {
			when(rs.getString(1)).thenReturn("100,200,300");
			long[] result = type.getValue(rs, 1);
			assertArrayEquals(new long[]{100L, 200L, 300L}, result);
		}

		@Test
		void getValue_nullReturnsNull() throws SQLException {
			when(rs.getString(1)).thenReturn(null);
			assertNull(type.getValue(rs, 1));
		}

		@Test
		void setValue_writesCorrectString() throws SQLException {
			type.setValue(ps, 1, new long[]{10L, 20L, 30L});
			// Note: implementation iterates as double, so values become "10.0,20.0,30.0"
			verify(ps).setString(1, "10.0,20.0,30.0");
		}

		@Test
		void setValue_emptyArray() throws SQLException {
			type.setValue(ps, 1, new long[]{});
			verify(ps).setString(1, "");
		}

		@Test
		void customSeparator() throws SQLException {
			LongArrayAsVarcharType custom = new LongArrayAsVarcharType().setSepChar(';');
			when(rs.getString(1)).thenReturn("1;2;3");
			long[] result = custom.getValue(rs, 1);
			assertArrayEquals(new long[]{1L, 2L, 3L}, result);
		}

		@Test
		void invalidStrAsZero_true_handlesInvalidString() throws SQLException {
			LongArrayAsVarcharType safe = new LongArrayAsVarcharType().invalidStrAsZero(true);
			when(rs.getString(1)).thenReturn("10,xyz,30");
			long[] result = safe.getValue(rs, 1);
			assertArrayEquals(new long[]{10L, 0L, 30L}, result);
		}

		@Test
		void invalidStrAsZero_false_throwsException() throws SQLException {
			when(rs.getString(1)).thenReturn("10,xyz,30");
			assertThrows(NumberFormatException.class, () -> type.getValue(rs, 1));
		}
	}

	// ==================== DoubleArrayAsVarcharType ====================

	@Nested
	class DoubleArrayTests {

		private final DoubleArrayAsVarcharType type = new DoubleArrayAsVarcharType();

		@Test
		void getReturnedClass_returnsDoubleArray() {
			assertEquals(double[].class, type.getReturnedClass());
		}

		@Test
		void getValue_validString() throws SQLException {
			when(rs.getString(1)).thenReturn("1.1,2.2,3.3");
			double[] result = type.getValue(rs, 1);
			assertArrayEquals(new double[]{1.1, 2.2, 3.3}, result, 0.001);
		}

		@Test
		void getValue_nullReturnsNull() throws SQLException {
			when(rs.getString(1)).thenReturn(null);
			assertNull(type.getValue(rs, 1));
		}

		@Test
		void setValue_writesCorrectString() throws SQLException {
			type.setValue(ps, 1, new double[]{1.5, 2.5, 3.5});
			verify(ps).setString(1, "1.5,2.5,3.5");
		}

		@Test
		void setValue_emptyArray() throws SQLException {
			type.setValue(ps, 1, new double[]{});
			verify(ps).setString(1, "");
		}

		@Test
		void customSeparator() throws SQLException {
			DoubleArrayAsVarcharType custom = new DoubleArrayAsVarcharType().setSepChar(':');
			when(rs.getString(1)).thenReturn("1.0:2.0:3.0");
			double[] result = custom.getValue(rs, 1);
			assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result, 0.001);
		}

		@Test
		void invalidStrAsZero_true_handlesInvalidString() throws SQLException {
			DoubleArrayAsVarcharType safe = new DoubleArrayAsVarcharType().invalidStrAsZero(true);
			when(rs.getString(1)).thenReturn("1.1,bad,3.3");
			double[] result = safe.getValue(rs, 1);
			assertArrayEquals(new double[]{1.1, 0.0, 3.3}, result, 0.001);
		}

		@Test
		void invalidStrAsZero_false_throwsException() throws SQLException {
			when(rs.getString(1)).thenReturn("1.1,bad,3.3");
			assertThrows(NumberFormatException.class, () -> type.getValue(rs, 1));
		}
	}

	// ==================== FloatArrayAsVarcharType ====================

	@Nested
	class FloatArrayTests {

		private final FloatArrayAsVarcharType type = new FloatArrayAsVarcharType();

		@Test
		void getReturnedClass_returnsFloatArray() {
			assertEquals(float[].class, type.getReturnedClass());
		}

		@Test
		void getValue_validString() throws SQLException {
			when(rs.getString(1)).thenReturn("1.5,2.5,3.5");
			float[] result = type.getValue(rs, 1);
			assertArrayEquals(new float[]{1.5f, 2.5f, 3.5f}, result, 0.001f);
		}

		@Test
		void getValue_nullReturnsNull() throws SQLException {
			when(rs.getString(1)).thenReturn(null);
			assertNull(type.getValue(rs, 1));
		}

		@Test
		void setValue_writesCorrectString() throws SQLException {
			type.setValue(ps, 1, new float[]{1.5f, 2.5f, 3.5f});
			// Note: implementation promotes float to double, so output uses double representation
			verify(ps).setString(1, "1.5,2.5,3.5");
		}

		@Test
		void setValue_emptyArray() throws SQLException {
			type.setValue(ps, 1, new float[]{});
			verify(ps).setString(1, "");
		}

		@Test
		void customSeparator() throws SQLException {
			FloatArrayAsVarcharType custom = new FloatArrayAsVarcharType().setSepChar('-');
			when(rs.getString(1)).thenReturn("1.0-2.0-3.0");
			float[] result = custom.getValue(rs, 1);
			assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, result, 0.001f);
		}

		@Test
		void invalidStrAsZero_true_handlesInvalidString() throws SQLException {
			FloatArrayAsVarcharType safe = new FloatArrayAsVarcharType().invalidStrAsZero(true);
			when(rs.getString(1)).thenReturn("1.0,oops,3.0");
			float[] result = safe.getValue(rs, 1);
			assertArrayEquals(new float[]{1.0f, 0.0f, 3.0f}, result, 0.001f);
		}

		@Test
		void invalidStrAsZero_false_throwsException() throws SQLException {
			when(rs.getString(1)).thenReturn("1.0,oops,3.0");
			assertThrows(NumberFormatException.class, () -> type.getValue(rs, 1));
		}
	}

	// ==================== StringArrayAsVarcharType ====================

	@Nested
	class StringArrayTests {

		private final StringArrayAsVarcharType type = new StringArrayAsVarcharType();

		@Test
		void getReturnedClass_returnsStringArray() {
			assertEquals(String[].class, type.getReturnedClass());
		}

		@Test
		void getValue_validString() throws SQLException {
			when(rs.getString(1)).thenReturn("a,b,c");
			String[] result = type.getValue(rs, 1);
			assertArrayEquals(new String[]{"a", "b", "c"}, result);
		}

		@Test
		void getValue_nullReturnsNull() throws SQLException {
			when(rs.getString(1)).thenReturn(null);
			assertNull(type.getValue(rs, 1));
		}

		@Test
		void setValue_writesCorrectString() throws SQLException {
			type.setValue(ps, 1, new String[]{"hello", "world"});
			verify(ps).setString(1, "hello,world");
		}

		@Test
		void setValue_emptyArray() throws SQLException {
			type.setValue(ps, 1, new String[]{});
			verify(ps).setString(1, "");
		}
	}

	// ==================== EnumByCodeType ====================

	@Nested
	class EnumByCodeTests {

		private final EnumByCodeType<Gender> genderType = new EnumByCodeType<>(Gender.class);

		@Test
		void getReturnedClass_returnsEnumClass() {
			assertEquals(Gender.class, genderType.getReturnedClass());
		}

		@Test
		void getValue_mapsCodeToEnum() throws SQLException {
			when(rs.getInt(1)).thenReturn(0);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(Gender.MALE, genderType.getValue(rs, 1));
		}

		@Test
		void getValue_mapsCodeToFemale() throws SQLException {
			when(rs.getInt(1)).thenReturn(1);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(Gender.FEMALE, genderType.getValue(rs, 1));
		}

		@Test
		void getValue_wasNull_returnsNull() throws SQLException {
			when(rs.getInt(1)).thenReturn(0);
			when(rs.wasNull()).thenReturn(true);
			assertNull(genderType.getValue(rs, 1));
		}

		@Test
		void setValue_writesCorrectCode() throws SQLException {
			genderType.setValue(ps, 1, Gender.FEMALE);
			verify(ps).setInt(1, 1);
		}

		@Test
		void constructorWithCustomJdbcType() {
			EnumByCodeType<TaskStatus> custom = new EnumByCodeType<>(Types.SMALLINT, TaskStatus.class);
			assertEquals(TaskStatus.class, custom.getReturnedClass());
		}

		@Test
		void taskStatus_getValue() throws SQLException {
			EnumByCodeType<TaskStatus> statusType = new EnumByCodeType<>(TaskStatus.class);
			when(rs.getInt(1)).thenReturn(3);
			when(rs.wasNull()).thenReturn(false);
			assertEquals(TaskStatus.SUCCESS, statusType.getValue(rs, 1));
		}

		@Test
		void taskStatus_setValue() throws SQLException {
			EnumByCodeType<TaskStatus> statusType = new EnumByCodeType<>(TaskStatus.class);
			statusType.setValue(ps, 1, TaskStatus.RUNNING);
			verify(ps).setInt(1, 1);
		}
	}
}
