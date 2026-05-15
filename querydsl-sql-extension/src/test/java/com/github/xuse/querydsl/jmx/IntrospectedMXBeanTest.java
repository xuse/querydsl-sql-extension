package com.github.xuse.querydsl.jmx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MXBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IntrospectedMXBean}.
 * Covers attribute get/set, operation invoke, MBeanInfo creation, and annotation processing.
 */
class IntrospectedMXBeanTest {

	// --- Test MXBean interface ---
	@MXBean
	public interface SampleMXBean {
		@JMXText(description = "The name property")
		String getName();

		void setName(String name);

		@JMXText(description = "The count property")
		int getCount();

		void setCount(int count);

		@JMXText(description = "Read-only property")
		boolean isActive();

		@JMXText(value = "doReset", description = "Reset operation")
		void reset();

		@JMXText(value = "computeSum", description = "Compute sum of two numbers")
		int compute(@JMXText(value = "a", description = "first number") int a,
				@JMXText(value = "b", description = "second number") int b);
	}

	// --- Test POJO implementing the interface ---
	public static class SampleBean implements SampleMXBean {
		private String name = "default";
		private int count = 0;
		private boolean active = true;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public void reset() {
			this.name = "default";
			this.count = 0;
			this.active = true;
		}

		@Override
		public int compute(int a, int b) {
			return a + b;
		}
	}

	// --- Non-MXBean interface with explicit annotations ---
	public interface AnnotatedBean {
		@JMXAttribute
		String getValue();

		@JMXAttribute
		void setValue(String value);

		@JMXOperation(JMXOperation.Impact.ACTION)
		void doAction();
	}

	public static class AnnotatedBeanImpl implements AnnotatedBean {
		private String value = "init";

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public void doAction() {
			this.value = "acted";
		}
	}

	private IntrospectedMXBean mxBean;
	private SampleBean sampleBean;

	@BeforeEach
	void setUp() {
		sampleBean = new SampleBean();
		mxBean = new IntrospectedMXBean(sampleBean, SampleMXBean.class);
	}

	@Test
	void testGetMBeanInfo() {
		MBeanInfo info = mxBean.getMBeanInfo();
		assertNotNull(info);
		assertNotNull(info.getAttributes());
		assertNotNull(info.getOperations());
		assertTrue(info.getAttributes().length > 0, "Should have attributes");
		assertTrue(info.getOperations().length > 0, "Should have operations");
	}

	@Test
	void testGetAttribute() throws Exception {
		Object name = mxBean.getAttribute("name");
		assertEquals("default", name);

		Object count = mxBean.getAttribute("count");
		assertEquals(0, count);

		Object active = mxBean.getAttribute("active");
		assertEquals(true, active);
	}

	@Test
	void testGetAttribute_notFound() {
		assertThrows(AttributeNotFoundException.class, () -> mxBean.getAttribute("nonExistent"));
	}

	@Test
	void testSetAttribute() throws Exception {
		mxBean.setAttribute(new Attribute("name", "newValue"));
		assertEquals("newValue", sampleBean.getName());

		mxBean.setAttribute(new Attribute("count", 42));
		assertEquals(42, sampleBean.getCount());
	}

	@Test
	void testSetAttribute_notFound() {
		assertThrows(AttributeNotFoundException.class,
				() -> mxBean.setAttribute(new Attribute("nonExistent", "value")));
	}

	@Test
	void testGetAttributes() throws Exception {
		AttributeList list = mxBean.getAttributes(new String[] { "name", "count", "active" });
		assertNotNull(list);
		assertEquals(3, list.size());
	}

	@Test
	void testSetAttributes() throws Exception {
		AttributeList attrs = new AttributeList();
		attrs.add(new Attribute("name", "batch"));
		attrs.add(new Attribute("count", 99));
		AttributeList result = mxBean.setAttributes(attrs);
		assertNotNull(result);
		assertEquals("batch", sampleBean.getName());
		assertEquals(99, sampleBean.getCount());
	}

	@Test
	void testInvokeOperation() throws Exception {
		// invoke reset
		mxBean.invoke("doReset", new Object[0], new String[0]);
		assertEquals("default", sampleBean.getName());

		// invoke compute
		sampleBean.setName("changed");
		Object result = mxBean.invoke("computeSum", new Object[] { 3, 7 }, new String[] { "int", "int" });
		assertEquals(10, result);
	}

	@Test
	void testMBeanInfoDescriptions() {
		MBeanInfo info = mxBean.getMBeanInfo();

		// Check attribute descriptions
		for (MBeanAttributeInfo attr : info.getAttributes()) {
			if ("name".equals(attr.getName())) {
				assertEquals("The name property", attr.getDescription());
			}
		}

		// Check operation descriptions
		for (MBeanOperationInfo op : info.getOperations()) {
			if ("doReset".equals(op.getName())) {
				assertEquals("Reset operation", op.getDescription());
			}
			if ("computeSum".equals(op.getName())) {
				assertEquals("Compute sum of two numbers", op.getDescription());
				// Check parameter info
				assertEquals(2, op.getSignature().length);
				assertEquals("a", op.getSignature()[0].getName());
				assertEquals("first number", op.getSignature()[0].getDescription());
			}
		}
	}

	@Test
	void testAnnotatedBeanWithExplicitAnnotations() throws Exception {
		AnnotatedBeanImpl impl = new AnnotatedBeanImpl();
		IntrospectedMXBean bean = new IntrospectedMXBean(impl, AnnotatedBean.class);

		MBeanInfo info = bean.getMBeanInfo();
		assertNotNull(info);

		// Should have 'value' attribute
		assertEquals("init", bean.getAttribute("value"));

		// Set attribute
		bean.setAttribute(new Attribute("value", "updated"));
		assertEquals("updated", impl.getValue());

		// Invoke operation
		bean.invoke("doAction", new Object[0], new String[0]);
		assertEquals("acted", impl.getValue());
	}

	@Test
	void testRegister() {
		// register should succeed (or return false if already registered)
		boolean result = mxBean.registerWithIdentity("test-" + System.nanoTime());
		assertTrue(result, "First registration should succeed");
	}

	@Test
	void testJMXOperationImpactEnum() {
		// Cover the Impact enum values
		assertEquals(0, JMXOperation.Impact.INFO.impactValue);
		assertEquals(1, JMXOperation.Impact.ACTION.impactValue);
		assertEquals(2, JMXOperation.Impact.ACTION_INFO.impactValue);
		assertEquals(3, JMXOperation.Impact.UNKNOWN.impactValue);
	}
}
