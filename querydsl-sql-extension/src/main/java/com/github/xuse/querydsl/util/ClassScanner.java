package com.github.xuse.querydsl.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.spring.core.resource.PathMatchingResourcePatternResolver;
import com.github.xuse.querydsl.spring.core.resource.Resource;
import com.github.xuse.querydsl.spring.core.resource.ResourcePatternResolver;

/**
 * 扫描指定包（包括jar）下的class文件 <br>
 *
 * @author Joey
 */
public class ClassScanner {

	/**
	 * 扫描包
	 *
	 * @param packages packages 基础包
	 * @return List of resources
	 */
	public List<Resource> scan(String... packages) {
		boolean allPackages = packages.length == 0;
		URLClassLoader cl = rootClasspath == null ? null : new URLClassLoader(new URL[] { rootClasspath }, null);
		try {
			if (allPackages) {
				return findResources(cl, null);
			}else {
				Set<Resource> result = new HashSet<Resource>();
				for (String packageName : packages) {
					if (StringUtils.isBlank(packageName)) {
						continue;
					}
					List<Resource> res = findResources(cl, packageName);
					if (packages.length == 1) {
						return res;
					}
					result.addAll(res);
				}
				return new ArrayList<>(result);
			}
		} finally {
			IOUtils.closeQuietly(cl);
		}
	}

	/**
	 * 是否排除内部类
	 */
	private boolean excludeInnerClass = true;

	/**
	 * 限定特定的class path根路径，如果不指定那么就在所有ClassPath下寻找
	 */
	private URL rootClasspath;
	
	/**
	 * set a filter condition.
	 */
	private Predicate<Resource> filter;

	public boolean isExcludeInnerClass() {
		return excludeInnerClass;
	}

	public ClassScanner excludeInnerClass(boolean excludeInnerClass) {
		this.excludeInnerClass = excludeInnerClass;
		return this;
	}

	public ClassScanner rootClasspath(URL rootClasspath) {
		this.rootClasspath = rootClasspath;
		return this;
	}
	
	public ClassScanner filterWithClassReader(Predicate<ClassReader> filter) {
		filterWith((r) -> r.isReadable() && filter.test(r.toClassReader()));
		return this;
	}
	
	public ClassScanner filterWith(Predicate<Resource> filter) {
		this.filter = this.filter==null? filter: this.filter.and(filter);
		return this;
	}
	
	
	public List<Resource> findResources(URLClassLoader cl, String packageName) {
		String prifix = rootClasspath == null ? "classpath*:" : "classpath:";
		String locationPattern = prifix
				+ (StringUtils.isBlank(packageName) ? "**/*.class" : packageName.replace('.', '/') + "/**/*.class");
		ResourcePatternResolver rl = new PathMatchingResourcePatternResolver(cl);
		Resource[] res = rl.getResources(locationPattern);
		if (excludeInnerClass) {
			List<Resource> list = filter(res, new Predicate<Resource>() {
				public boolean test(Resource o) {
					String s = o.getFilename();
					int dollor = s.indexOf('$');
					// 当$位于第一个字符时，不认为是内部类
					return dollor <= 0;
				}
			});
			if (list.size() < res.length) {
				res = list.toArray(new Resource[list.size()]);
			}
		}
		if (filter != null) {
			List<Resource> list = filter(res, filter);
			if (list.size() < res.length) {
				res = list.toArray(new Resource[list.size()]);
			}
		}
		return Arrays.asList(res);
	}

	private <S> List<S> filter(S[] source, Predicate<S> filter) {
		List<S> result = new ArrayList<>(source.length);
		for (S t : source) {
			if (filter.test(t)) {
				result.add(t);
			}
		}
		return result;
	}

}
