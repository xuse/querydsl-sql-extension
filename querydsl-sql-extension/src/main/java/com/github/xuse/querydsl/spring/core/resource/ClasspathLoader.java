package com.github.xuse.querydsl.spring.core.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClasspathLoader extends AResourceLoader {

	private boolean directoryOnly;

	private List<ClassLoader> loaders;

	public ClasspathLoader() {
		this(false);
	}

	public ClasspathLoader(boolean directoryOnly) {
		this.directoryOnly = directoryOnly;
		setDefaultClassLoader();
	}

	public ClasspathLoader(boolean directoryOnly, ClassLoader... loaders) {
		this.directoryOnly = directoryOnly;
		if (loaders.length > 0) {
			this.loaders = Arrays.asList(loaders);
		} else {
			setDefaultClassLoader();
		}
	}

	public URL getResource(String name) {
		if (name.startsWith("/"))
			name = name.substring(1);
		if (directoryOnly)
			name = "./" + name;
		if (loaders != null && loaders.size() > 0) {
			for (ClassLoader loader : loaders) {
				URL res = loader.getResource(name);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	public List<URL> getResources(String name) {
		if (name.startsWith("/"))
			name = name.substring(1);
		if (directoryOnly)
			name = "./" + name;
		Set<URL> result = new LinkedHashSet<URL>();
		try {
			if (loaders != null && loaders.size() > 0) {
				for (ClassLoader loader : loaders) {
					for (Enumeration<URL> e = loader.getResources(name); e.hasMoreElements(); ) {
						result.add(e.nextElement());
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return new ArrayList<URL>(result);
	}

	public void setSystemClassLoader() {
		this.loaders = Arrays.asList(ClassLoader.getSystemClassLoader());
	}

	public void setClassLoaders(ClassLoader... loader) {
		this.loaders = Arrays.asList(loader);
	}

	public void setDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			cl = ClasspathLoader.class.getClassLoader();
		}
		this.loaders = Arrays.asList(cl);
	}

	public boolean isDirectoryOnly() {
		return directoryOnly;
	}

	public void setDirectoryOnly(boolean directoryOnly) {
		this.directoryOnly = directoryOnly;
	}
}
