package com.github.xuse.querydsl.config.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.github.xuse.querydsl.util.StringUtils;



/**
 * 扫描指定包（包括jar）下的class文件 <br>
 * 
 * @author jiyi
 */
public class ClassScanner {
    /**
     * 是否排除内部类
     */
    private boolean excludeInnerClass = true;

    /**
     * class path根路径，如果不指定那么就在所有ClassPath下寻找
     * 
     * @return
     */
    private URL rootClasspath;

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

    /**
     * 扫描包
     * 
     * @param basePackage
     *            基础包
     * @param recursive
     *            是否递归搜索子包
     * @return Set
     */
    public List<Resource> scan(String[] packages) {
     // 这里设置父classloader为null
        URLClassLoader cl = rootClasspath == null ? null : new URLClassLoader(new URL[] { rootClasspath }, null);

        boolean allPackages = packages.length == 0;
        String prifix = rootClasspath == null ? "classpath*:" : "classpath:";
        if (allPackages) {
            return findResources(cl, prifix + "**/*.class", excludeInnerClass);
        }
        Set<Resource> result = new HashSet<Resource>();
        for (String packageName : packages) {
            if (StringUtils.isBlank(packageName)) {
                continue;
            }
            String keystr = prifix + packageName.replace('.', '/') + "/**/*.class";
            List<Resource> res = findResources(cl, keystr, excludeInnerClass);
            if (packages.length == 1) {
                return res;
            }
            result.addAll(res);
        }
        try {
			cl.close();
		} catch (IOException e) {
		}
        return new ArrayList<>(result);
    }

    /**
	 * 查找符合Pattern的所有资源
	 * @param locationPattern
	 * @return
	 * @throws IOException
	 */
	public static List<Resource> findResources(ClassLoader cl,String locationPattern,boolean excludeInnerClass) {
		ResourcePatternResolver rl= new PathMatchingResourcePatternResolver(cl);
		try {
		    Resource[] res= rl.getResources(locationPattern);
		    if(excludeInnerClass){
		        List<Resource> list=filter(res, new Predicate<Resource>(){
                    public boolean test(Resource o) {
                        String s=o.getFilename();
                        int dollor=s.indexOf('$');
                        return dollor<=0; //当$位于第一个字符时，不认为是内部类
                    }
		        });
		        res=list.toArray(new Resource[list.size()]);
		    }
			return Arrays.asList(res);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 进行数组元素过滤
	 * 
	 * @param source
	 * @param filter
	 * @return
	 */
	public static <T> List<T> filter(T[] source, Predicate<T> filter) {
		if (source == null)
			return Collections.emptyList();
		if (filter == null)
			return Arrays.asList(source);
		List<T> result = new ArrayList<T>(source.length);
		for (T t : source) {
			if (filter.test(t)) {
				result.add(t);
			}
		}
		return result;
	}
}
