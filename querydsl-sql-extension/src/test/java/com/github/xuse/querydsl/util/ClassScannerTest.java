package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.spring.core.resource.Resource;

/**
 * Unit tests for {@link ClassScanner}.
 * Validates Requirement 9.9.
 */
@DisplayName("ClassScanner tests")
class ClassScannerTest {

	/**
	 * Test scanning a known package returns expected class resources.
	 * We scan the "com.github.xuse.querydsl.util" package which we know contains
	 * classes like StringUtils, IOUtils, ClassScanner itself, etc.
	 * Validates: Requirement 9.9
	 */
	@Test
	@DisplayName("Scanning known package returns expected class resources")
	void testScanKnownPackageReturnsExpectedResources() {
		ClassScanner scanner = new ClassScanner();
		scanner.excludeInnerClass(true);

		List<Resource> resources = scanner.scan("com.github.xuse.querydsl.util");

		assertNotNull(resources, "Scan result should not be null");
		// The util package has many classes (StringUtils, IOUtils, ClassScanner, Threads, etc.)
		assertTrue(resources.size() >= 3,
				"Should find at least 3 classes in com.github.xuse.querydsl.util, found: " + resources.size());

		// Verify that known classes are found
		boolean foundStringUtils = false;
		boolean foundClassScanner = false;
		boolean foundIOUtils = false;
		for (Resource r : resources) {
			String filename = r.getFilename();
			assertNotNull(filename, "Resource filename should not be null");
			if (filename.equals("StringUtils.class")) {
				foundStringUtils = true;
			}
			if (filename.equals("ClassScanner.class")) {
				foundClassScanner = true;
			}
			if (filename.equals("IOUtils.class")) {
				foundIOUtils = true;
			}
		}
		assertTrue(foundStringUtils, "Should find StringUtils.class in scan results");
		assertTrue(foundClassScanner, "Should find ClassScanner.class in scan results");
		assertTrue(foundIOUtils, "Should find IOUtils.class in scan results");
	}

	/**
	 * Test excludeInnerClass=true excludes inner classes (classes with $ in filename).
	 * Validates: Requirement 9.9
	 */
	@Test
	@DisplayName("excludeInnerClass=true excludes inner classes")
	void testExcludeInnerClassTrue() {
		ClassScanner scanner = new ClassScanner();
		scanner.excludeInnerClass(true);

		// Scan a package known to have inner classes (e.g., ExecutorServiceEx has PoolExecutor inner interface)
		List<Resource> resources = scanner.scan("com.github.xuse.querydsl.util");

		assertNotNull(resources, "Scan result should not be null");
		assertFalse(resources.isEmpty(), "Should find classes");

		// Verify no inner classes (filenames containing $) are present
		for (Resource r : resources) {
			String filename = r.getFilename();
			assertNotNull(filename, "Resource filename should not be null");
			// Inner classes have $ after position 0 (position 0 is allowed per ClassScanner logic)
			int dollarIndex = filename.indexOf('$');
			assertTrue(dollarIndex <= 0,
					"Inner class should be excluded when excludeInnerClass=true, found: " + filename);
		}
	}

	/**
	 * Test excludeInnerClass=false includes inner classes.
	 * Validates: Requirement 9.9
	 */
	@Test
	@DisplayName("excludeInnerClass=false includes inner classes")
	void testExcludeInnerClassFalse() {
		ClassScanner scanner = new ClassScanner();
		scanner.excludeInnerClass(false);

		// Scan a package known to have inner classes
		List<Resource> resources = scanner.scan("com.github.xuse.querydsl.util");

		assertNotNull(resources, "Scan result should not be null");
		assertFalse(resources.isEmpty(), "Should find classes");

		// With excludeInnerClass=false, we should find at least one inner class
		// (e.g., ExecutorServiceEx$PoolExecutor, Threads$ExecutorServiceExImpl, etc.)
		boolean foundInnerClass = false;
		for (Resource r : resources) {
			String filename = r.getFilename();
			if (filename != null && filename.indexOf('$') > 0) {
				foundInnerClass = true;
				break;
			}
		}
		assertTrue(foundInnerClass,
				"Should find at least one inner class when excludeInnerClass=false");
	}

	/**
	 * Test scanning with excludeInnerClass=true returns fewer results than with false.
	 */
	@Test
	@DisplayName("excludeInnerClass=true returns fewer results than false")
	void testExcludeInnerClassReducesResults() {
		ClassScanner scannerWithExclude = new ClassScanner();
		scannerWithExclude.excludeInnerClass(true);

		ClassScanner scannerWithoutExclude = new ClassScanner();
		scannerWithoutExclude.excludeInnerClass(false);

		List<Resource> withExclude = scannerWithExclude.scan("com.github.xuse.querydsl.util");
		List<Resource> withoutExclude = scannerWithoutExclude.scan("com.github.xuse.querydsl.util");

		assertTrue(withoutExclude.size() > withExclude.size(),
				"Scan without inner class exclusion should return more results. " +
						"With exclusion: " + withExclude.size() + ", without: " + withoutExclude.size());
	}
}
