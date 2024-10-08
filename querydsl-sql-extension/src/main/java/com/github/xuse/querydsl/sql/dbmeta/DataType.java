package com.github.xuse.querydsl.sql.dbmeta;

import com.querydsl.sql.Column;

import lombok.Data;
import lombok.Generated;

@Generated
@Data
public class DataType {

	@Column("TYPE_NAME")
	private String name;

	@Column("DATA_TYPE")
	private int jdbcType;

	@Column("PRECISION")
	private int precision;

	@Column("LITERAL_PREFIX")
	private String literalPrefix;

	@Column("LITERAL_SUFFIX")
	private String literalSuffix;

	@Column("CREATE_PARAMS")
	private String createParams;

	/**
	 * typeNoNulls - does not allow NULL values ◦ typeNullable - allows NULL values
	 * ◦ typeNullableUnknown - nullability unknown
	 */
	@Column("NULLABLE")
	private int nullable;

	@Column("CASE_SENSITIVE")
	private boolean caseSensitive;

	/**
	 * can you use "WHERE" based on this type: ◦ typePredNone - No support ◦
	 * typePredChar - Only supported with WHERE .. LIKE ◦ typePredBasic - Supported
	 * except for WHERE .. LIKE ◦ typeSearchable - Supported for all WHERE ..
	 */
	@Column("SEARCHABLE")
	private int searchable;

	@Column("UNSIGNED_ATTRIBUTE")
	private boolean unsignedAttribute;

	/**
	 * can it be a money value.
	 */
	@Column("FIXED_PREC_SCALE")
	private boolean fixedPrecScale;

	/**
	 * can it be used for anauto-increment value.
	 */
	@Column("AUTO_INCREMENT")
	private boolean autoIncrement;
	/**
	 * localized version of type name(may be null)
	 */
	@Column("LOCAL_TYPE_NAME")
	private String localTypeName;

	/**
	 * minimum scale supported
	 */
	@Column("MINIMUM_SCALE")
	private int minimumScale;

	/**
	 * maximum scale supported
	 */
	@Column("MAXIMUM_SCALE")
	private int maximumScale;

	/**
	 * unused
	 */
	@Column("SQL_DATA_TYPE")
	private int sql_data_type;
	/**
	 * unused
	 */
	@Column("SQL_DATETIME_SUB")
	private int sqlDatetimeSub;

	/**
	 * usually 2 or 10
	 */
	@Column("NUM_PREC_RADIX")
	private int numPrecRadix;
}
