package com.github.xuse.querydsl.spring.core.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public interface Resource {

	InputStream getInputStream() throws IOException;

	boolean exists();

	boolean isReadable();

	boolean isOpen();

	URL getURL();

	URI getURI() throws IOException;

	File getFile() throws IOException;

	long contentLength() throws IOException;

	long lastModified() throws IOException;

	Resource createRelative(String relativePath) throws IOException;

	String getFilename();

	String getDescription();

	boolean isFile();
}
