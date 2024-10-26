package com.github.xuse.querydsl.util;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.collection.CollectionUtils;


public class CollectionUtilsTest {
	private static final String[] PREPEARE= {"a","b","c"};
	private static final List<String> list=Arrays.asList(PREPEARE);
		
	@Test
	public void testCollectionUtils() throws IOException {
		Map<Integer,String> map=CollectionUtils.group(list, String::hashCode);
		assertEquals(PREPEARE.length, map.size());
		
		map=CollectionUtils.group(PREPEARE, String::hashCode);
		assertEquals(PREPEARE.length, map.size());
		
		Map<Integer, List<Character>> charMap=CollectionUtils.bucket(list,String::hashCode , e->e.charAt(0));
		assertEquals(PREPEARE.length, charMap.size());
		
		List<CharSequence> css= CollectionUtils.cast(list);
		assertTrue(css.hashCode()==list.hashCode());
		
		boolean has=CollectionUtils.contains(this.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF"), "");
		assertEquals(false, has);
		
		has=CollectionUtils.contains(list, "a");
		assertEquals(true, has);
		
		has=CollectionUtils.containsAll(list, null);
		assertTrue(has);
		has=CollectionUtils.containsAll(null,list);
		assertFalse(has);
		has=CollectionUtils.containsAll(list, Arrays.asList("a","b"));
		assertTrue(has);
		
		has=CollectionUtils.containsAny(list, null);
		assertTrue(has);
		has=CollectionUtils.containsAny(null, list);
		assertFalse(has);
		has=CollectionUtils.containsAny(list, Arrays.asList("a","x"));
		assertTrue(has);
		
		List<Integer> hashs=CollectionUtils.extract(list, String::hashCode);
		assertEquals(PREPEARE.length, hashs.size());
		
		hashs=CollectionUtils.extract(PREPEARE, String::hashCode);
		assertEquals(PREPEARE.length, hashs.size());
		
		hashs = CollectionUtils.extract(list, e -> e.equals("c") ? null : e.hashCode(), true);
		assertEquals(2, hashs.size());
		
		has=CollectionUtils.fastContains(list, "a");
		assertTrue(has);
		
		List<String> strs=CollectionUtils.filter(list, e->!"a".equals(e));
		assertEquals(2,strs.size());
		
		Map<Integer,String> map2=CollectionUtils.filter(map, (k,v)->!v.equals("a"));
		assertEquals(2,map2.size());
		
		List<String> find=CollectionUtils.find(list, (e)->e.hashCode(), "a".hashCode());
		assertEquals(1,find.size());
		
		String findOne = CollectionUtils.findFirst(list, (e)->e.hashCode()>0);
		assertEquals("a",findOne);
		
		findOne =CollectionUtils.findFirst(list, (e)->e, "b");
		assertEquals("b",findOne);
		
		Map<String,Integer> indexes=CollectionUtils.getIndexMap(list);
		assertEquals(3, indexes.size());
		
		strs = new ArrayList<>(CollectionUtils.union(null, null));
		assertEquals(0, strs.size());
		
		strs = new ArrayList<>(CollectionUtils.union(list, null));
		assertEquals(list.size(), strs.size());
		
		strs = new ArrayList<>(CollectionUtils.union(null, list));
		assertEquals(list.size(), strs.size());
		
		
		strs = new ArrayList<>(CollectionUtils.union(list, Arrays.asList("d","e")));
		assertEquals(5, strs.size());
		
		strs = CollectionUtils.toList(list);
		assertEquals(3, strs.size());
		
		
		Enumeration<Object> enumr=new StringTokenizer("a b c");
		List<Object> objs=CollectionUtils.toList(enumr);
		assertEquals(3, objs.size());
		
		List<String> tested=new ArrayList<>(list);
		CollectionUtils.setElement(tested, 5, "d");
		assertEquals(6, tested.size());
		
		CollectionUtils.refine(tested, (el)->el!=null && !"d".equals(el));
		assertEquals(3, tested.size());
		
		System.out.println(map);
		CollectionUtils.refine(map, (k,v)->k>97);
		assertEquals(2, map.size());
		
		
		assertNotNull(CollectionUtils.nullSafe((Set<String>)null));
		assertNotNull(CollectionUtils.nullSafe((List<String>)null));
		assertNotNull(CollectionUtils.nullSafe((Map<String,String>)null));
		assertNotNull(CollectionUtils.nullSafe(list));
		assertNotNull(CollectionUtils.nullSafe(map));
		
		assertEquals(3,CollectionUtils.length(new Object[3]));
		assertEquals(3,CollectionUtils.length(list));
		
		assertEquals("c",CollectionUtils.last(list));
		
		
		for(Iterator<Object> iter=CollectionUtils.iterator(enumr);iter.hasNext();) {
			iter.next();
		}
		
		assertTrue(CollectionUtils.isNotEmpty(map));
		assertTrue(CollectionUtils.isNotEmpty(list));
		assertFalse(CollectionUtils.isNotEmpty((Map<String,String>)null));
		assertFalse(CollectionUtils.isNotEmpty((List<?>)null));
		assertFalse(CollectionUtils.isEmpty(map));
		assertFalse(CollectionUtils.isEmpty(list));
		assertTrue(CollectionUtils.isEmpty((Map<String,String>)null));
		assertTrue(CollectionUtils.isEmpty((List<?>)null));
		
		Set<Class<?>> set=CollectionUtils.identityHashSet();
		assertNotNull(set);
	}
	
	

}
