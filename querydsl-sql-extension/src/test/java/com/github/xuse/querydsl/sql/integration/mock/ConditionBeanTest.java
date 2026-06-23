package com.github.xuse.querydsl.sql.integration.mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.AvsAuthParams;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.querydsl.core.QueryResults;

/**
 * Tests for @ConditionBean based queries via CRUDRepository.
 * Covers listByCondition, countByCondition, and various Ops operators.
 */
class ConditionBeanTest extends MockedTestBase {

	private static CRUDRepository<AvsUserAuthority, Integer> repo;

	@BeforeAll
	static void setup() {
		doInit();
		// Create the table if not exists
		factory.getMetadataFactory().createTable(() -> AvsUserAuthority.class).ifExists().execute();
		repo = factory.asRepository(() -> AvsUserAuthority.class);
	}

	@Test
	void testListByCondition_emptyParams() {
		AvsAuthParams params = new AvsAuthParams();
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_stringContainsIC() {
		AvsAuthParams params = new AvsAuthParams();
		params.setAuthContent("test");
		params.setLimit(5);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_greaterThan() {
		AvsAuthParams params = new AvsAuthParams();
		params.setAuthType(1);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_between() {
		AvsAuthParams params = new AvsAuthParams();
		params.setCreateTime(new Date[] { new Date(0), new Date() });
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_betweenList() {
		AvsAuthParams params = new AvsAuthParams();
		params.setCreateTime2(Arrays.asList(new Date(0), new Date()));
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_inList() {
		AvsAuthParams params = new AvsAuthParams();
		params.setIds(Arrays.asList(1, 2, 3));
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_inArray() {
		AvsAuthParams params = new AvsAuthParams();
		params.setIds2(new int[] { 1, 2, 3 });
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_startsWith() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDevIdStartWith("C12");
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_endsWith() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDevIdEndWith("78");
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_like() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDevIdLike("%123%");
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_isNull() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDevIdIsNull(true);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_isNotNull() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDevIdIsNotNull(true);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_multipleComparisons() {
		AvsAuthParams params = new AvsAuthParams();
		params.setAuthTypeGoe(1);
		params.setAuthTypeLt(100);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_stringComparisons() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDevIdGt("A");
		params.setDevIdLoe("Z");
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_withOrder() {
		AvsAuthParams params = new AvsAuthParams();
		params.setOrder("id");
		params.setOrderAsc(true);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_withOrderDesc() {
		AvsAuthParams params = new AvsAuthParams();
		params.setOrder("id");
		params.setOrderAsc(false);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_withOffset() {
		AvsAuthParams params = new AvsAuthParams();
		params.setLimit(5);
		params.setOffset(0);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_withFetchTotal() {
		AvsAuthParams params = new AvsAuthParams();
		params.setLimit(5);
		params.setOffset(0);
		params.setFetchTotal(true);
		QueryResults<AvsUserAuthority> results = repo.listByCondition(params, 5, 0);
		assertNotNull(results);
		assertTrue(results.getTotal() >= 0);
	}

	@Test
	void testCountByCondition() {
		AvsAuthParams params = new AvsAuthParams();
		int count = repo.countByCondition(params);
		assertTrue(count >= 0);
	}

	@Test
	void testCountByCondition_withFilter() {
		AvsAuthParams params = new AvsAuthParams();
		params.setAuthType(0);
		int count = repo.countByCondition(params);
		assertTrue(count >= 0);
	}

	@Test
	void testListByCondition_mixField() {
		AvsAuthParams params = new AvsAuthParams();
		params.setMixField("test");
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_caseType() {
		AvsAuthParams params = new AvsAuthParams();
		params.setCaseType(1);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_caseType2() {
		AvsAuthParams params = new AvsAuthParams();
		params.setCaseType(2);
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_ignoreUnsavedValue() {
		// devId has ignoreUnsavedValue=false, so empty string should still be used as condition
		AvsAuthParams params = AvsAuthParams.builder().devId("").limit(10).build();
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_notConditionBean() {
		// Passing a non-ConditionBean object should throw
		assertThrows(IllegalArgumentException.class, () -> repo.listByCondition("not a condition bean"));
	}

	@Test
	void testListByCondition_dateGt() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDateGt(new Date(0));
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}

	@Test
	void testListByCondition_dateLoe() {
		AvsAuthParams params = new AvsAuthParams();
		params.setDateLoe(new Date());
		params.setLimit(10);
		List<AvsUserAuthority> list = repo.listByCondition(params);
		assertNotNull(list);
	}
}
