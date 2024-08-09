package com.github.xuse.querydsl.spring.core.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class AResourceLoader implements ResourceLoader{
	public InputStream getResourceAsStream(String name){
		URL u= getResource(name);
		try {
			return u==null?null:u.openStream();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	protected File toFile(URL url){
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	protected URL toURL(File file){
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	public BufferedReader getResourceAsReader(String name,String charset){
		URL u= getResource(name);
		try {
			return u==null?null:new BufferedReader(new InputStreamReader(u.openStream(), charset));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
