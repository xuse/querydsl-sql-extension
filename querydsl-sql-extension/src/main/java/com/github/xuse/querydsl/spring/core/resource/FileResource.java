package com.github.xuse.querydsl.spring.core.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.github.xuse.querydsl.util.Exceptions;

public class FileResource implements Resource {
	protected File file;
	
	public FileResource(File file) {
		this.file=file;
	}


	public File getFile(){
		return file;
	}
	
	private boolean open;
	


	@Override
	public boolean isReadable() {
		return file.exists();
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	public boolean exists() {
		return file.exists();
	}

	public boolean isOpen() {
		return open;
	}

	public URI getURI() throws IOException {
		return file.toURI();
	}

	public long contentLength() throws IOException {
		return file.length();
	}

	public long lastModified() throws IOException {
		return file.lastModified();
	}

	public Resource createRelative(String relativePath) throws IOException {
		return new FileResource(new File(file,relativePath));
	}

	public String getFilename() {
		return file.getName();
	}

	public String getDescription() {
		return "file [" + file.getAbsolutePath() + "]";
	}

	@Override
	public String toString() {
		return getDescription();
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public URL getURL() {
		try {
			return this.file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw Exceptions.toRuntime(e);
		}
	}

}
