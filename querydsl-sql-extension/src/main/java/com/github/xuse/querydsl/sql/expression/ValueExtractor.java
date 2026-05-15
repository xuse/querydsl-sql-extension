package com.github.xuse.querydsl.sql.expression;

/**
 * Abstraction for extracting field values from a bean in a defined column order.
 * <p>
 * Used by batch processing to decouple value extraction from the concrete bean type.
 * Both {@link BeanCodec} (for entity beans) and {@link ConverterWrappedBean}
 * (for DTO beans with @PathBinder) implement this contract.
 *
 * @author Joey
 */
public abstract class ValueExtractor {

	/**
	 * Get the bean type that this extractor handles.
	 *
	 * @return the Class of the bean
	 */
	public abstract Class<?> getType();

	/**
	 * Extract field values from the given bean, ordered by the column sequence
	 * defined at construction time.
	 *
	 * @param bean the bean instance
	 * @return values array aligned with the expected column order
	 */
	public abstract Object[] values(Object bean);

	/**
	 * Check if the given bean is an instance of the type this extractor handles.
	 *
	 * @param bean the bean to check
	 * @return true if this extractor can handle the bean
	 */
	public boolean isInstance(Object bean) {
		return getType().isInstance(bean);
	}
}
