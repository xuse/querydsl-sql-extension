package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.TableDataTypes;

/**
 * Unit tests for {@link BeanCodecManager}.
 */
@DisplayName("BeanCodecManager 单元测试")
class BeanCodecManagerTest {

	private final BeanCodecManager manager = BeanCodecManager.getInstance();

	@Nested
	@DisplayName("Registration - Codec 注册与缓存")
	class Registration {

		@Test
		@DisplayName("getCodec returns same instance for same type (all fields)")
		void testSameInstanceReturnedForSameType() {
			BeanCodec codec1 = manager.getCodec(Foo.class);
			BeanCodec codec2 = manager.getCodec(Foo.class);
			assertSame(codec1, codec2, "Same type should return cached codec instance");
		}

		@Test
		@DisplayName("getCodec returns same instance for same type and binding")
		void testSameInstanceReturnedForSameTypeAndBinding() {
			BindingProvider bindings = new SimpleBindingProvider(Arrays.asList("id", "code", "name"));
			BeanCodec codec1 = manager.getCodec(Foo.class, bindings);
			BeanCodec codec2 = manager.getCodec(Foo.class, bindings);
			assertSame(codec1, codec2, "Same type and binding should return cached codec instance");
		}

		@Test
		@DisplayName("getCodec returns different instances for different field lists")
		void testDifferentInstancesForDifferentBindings() {
			BindingProvider bindings1 = new SimpleBindingProvider(Arrays.asList("id", "code"));
			BindingProvider bindings2 = new SimpleBindingProvider(Arrays.asList("id", "name"));
			BeanCodec codec1 = manager.getCodec(Foo.class, bindings1);
			BeanCodec codec2 = manager.getCodec(Foo.class, bindings2);
			assertNotSame(codec1, codec2, "Different bindings should produce different codec instances");
		}

		@Test
		@DisplayName("getCodec returns valid codec that can create instances")
		void testCodecFunctionality() {
			BeanCodec codec = manager.getCodec(Foo.class);
			assertNotNull(codec);
			assertNotNull(codec.getFields());
			assertTrue(codec.getFields().length > 0, "Codec should have fields");
			assertNotNull(codec.getType());
			assertEquals(Foo.class, codec.getType());
		}

		@Test
		@DisplayName("getCodec returns different instances for different types")
		void testDifferentTypesReturnDifferentCodecs() {
			BeanCodec fooCodec = manager.getCodec(Foo.class);
			BeanCodec dataTypesCodec = manager.getCodec(TableDataTypes.class);
			assertNotSame(fooCodec, dataTypesCodec, "Different types should return different codec instances");
		}
	}

	@Nested
	@DisplayName("ConcurrencySafety - 并发安全")
	class ConcurrencySafety {

		@Test
		@DisplayName("concurrent getCodec for same type returns same instance")
		void testConcurrentAccessSameType() throws Exception {
			int threadCount = 12;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			List<Future<BeanCodec>> futures = new ArrayList<>();

			for (int i = 0; i < threadCount; i++) {
				futures.add(executor.submit(() -> {
					startLatch.await();
					return manager.getCodec(TableDataTypes.class);
				}));
			}

			// Release all threads simultaneously
			startLatch.countDown();

			Set<BeanCodec> results = ConcurrentHashMap.newKeySet();
			for (Future<BeanCodec> future : futures) {
				BeanCodec codec = future.get(10, TimeUnit.SECONDS);
				assertNotNull(codec, "Each thread should receive a valid BeanCodec");
				results.add(codec);
			}

			// All threads should get the same cached instance
			assertEquals(1, results.size(), "All threads should receive the same codec instance");

			executor.shutdown();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("concurrent getCodec for different types all succeed")
		void testConcurrentAccessDifferentTypes() throws Exception {
			int threadCount = 12;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			List<Future<BeanCodec>> futures = new ArrayList<>();

			// Half threads request Foo, half request TableDataTypes
			for (int i = 0; i < threadCount; i++) {
				final Class<?> targetClass = (i % 2 == 0) ? Foo.class : TableDataTypes.class;
				futures.add(executor.submit(() -> {
					startLatch.await();
					return manager.getCodec(targetClass);
				}));
			}

			startLatch.countDown();

			for (Future<BeanCodec> future : futures) {
				BeanCodec codec = future.get(10, TimeUnit.SECONDS);
				assertNotNull(codec, "Each thread should receive a valid BeanCodec");
				assertNotNull(codec.getFields());
				assertTrue(codec.getFields().length > 0);
			}

			executor.shutdown();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("concurrent getCodec with different bindings all succeed without exceptions")
		void testConcurrentAccessWithDifferentBindings() throws Exception {
			int threadCount = 12;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			List<Future<BeanCodec>> futures = new ArrayList<>();

			// Each thread uses a slightly different binding
			List<List<String>> bindingVariants = Arrays.asList(
				Arrays.asList("id", "code"),
				Arrays.asList("id", "name"),
				Arrays.asList("id", "code", "name"),
				Arrays.asList("code", "name"),
				Arrays.asList("id", "content"),
				Arrays.asList("id", "code", "content")
			);

			for (int i = 0; i < threadCount; i++) {
				final List<String> fields = bindingVariants.get(i % bindingVariants.size());
				futures.add(executor.submit(() -> {
					startLatch.await();
					return manager.getCodec(Foo.class, new SimpleBindingProvider(fields));
				}));
			}

			startLatch.countDown();

			for (Future<BeanCodec> future : futures) {
				BeanCodec codec = future.get(10, TimeUnit.SECONDS);
				assertNotNull(codec, "Each thread should receive a valid BeanCodec");
			}

			executor.shutdown();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Nested
	@DisplayName("Fallback - ASM 生成失败时的回退行为")
	class Fallback {

		@Test
		@DisplayName("getCodec throws IllegalStateException with class name when generation fails")
		void testFallbackWhenAsmGenerationFails() {
			// When no properties match between the bean and the binding,
			// BeanCodecManager wraps the error in an IllegalStateException containing the class name
			BindingProvider bindings = new SimpleBindingProvider(Arrays.asList("nonExistentField"));
			IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
				manager.getCodec(NoMatchBean.class, bindings);
			});
			assertTrue(ex.getMessage().contains("NoMatchBean"),
				"Exception message should contain the class name, but was: " + ex.getMessage());
		}
	}

	/**
	 * A simple BindingProvider for testing with explicit field names.
	 */
	private static class SimpleBindingProvider implements BindingProvider {
		private final List<String> fieldNames;

		SimpleBindingProvider(List<String> fieldNames) {
			this.fieldNames = fieldNames;
		}

		@Override
		public List<String> fieldNames() {
			return fieldNames;
		}

		@Override
		public int size() {
			return fieldNames.size();
		}

		@Override
		public List<String> names(Map<String, FieldProperty> fieldOrder) {
			return fieldNames;
		}

		@Override
		public Class<?> getType(String name, FieldProperty property) {
			return property != null && property.getField() != null ? property.getField().getType() : Object.class;
		}
	}

	/**
	 * A bean class with no matching properties for the binding, used to trigger
	 * the "no property match" error path.
	 */
	public static class NoMatchBean {
		public NoMatchBean() {}
	}
}
