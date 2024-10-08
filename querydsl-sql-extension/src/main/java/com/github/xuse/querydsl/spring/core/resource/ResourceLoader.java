package com.github.xuse.querydsl.spring.core.resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public interface ResourceLoader {

	URL getResource(String name);

	InputStream getResourceAsStream(String name);

	BufferedReader getResourceAsReader(String name, String charset);

	List<URL> getResources(String name);
}
