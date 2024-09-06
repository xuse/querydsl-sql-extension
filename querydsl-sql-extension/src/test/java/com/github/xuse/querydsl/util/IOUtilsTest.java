package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import lombok.SneakyThrows;

public class IOUtilsTest {

	private String a = "hello!";
	private byte[] data = a.getBytes(StandardCharsets.UTF_8);
	private URL url=this.getClass().getResource("/table_aaa");
	private File temp=new File("test.txt");
	private File path=new File("user.dir");
	

	@SneakyThrows
	@Test
	public void testIOUtils() {
		{
			InputStream in=url.openStream();
			String s=IOUtils.toString(in, StandardCharsets.UTF_8);
			IOUtils.closeQuietly(in);
		}
		
		StringWriter sw=new StringWriter();
		try(Reader reader=new StringReader(a)){
			IOUtils.copy(reader, sw, 0);
		};
		assertEquals(sw.getBuffer().toString(), a);

		ByteArrayOutputStream out=new ByteArrayOutputStream();
		try(InputStream in=url.openStream()){
			IOUtils.copy(in, out, 1024);
		}
		assertTrue(out.toByteArray().length>0);
		
		try(InputStream in=url.openStream()){
			IOUtils.saveAsFile(temp, in);
		}
		assertTrue(temp.exists());
		
		assertTrue(IOUtils.toByteArray(temp).length > 0);
		assertTrue(IOUtils.toByteArray(url).length > 0);
		
		File file=IOUtils.rename(temp,"test1.txt",true);
		assertEquals("test1.txt",file.getName());
		
		try(InputStream in=url.openStream()){
			IOUtils.saveAsFile(temp, in);
		}
		file=IOUtils.rename(file,temp.getName(),false);
		assertEquals(null, file);
		
		file=new File(temp.getParent(),"test1.txt");
		file.delete();
		
		
		assertTrue(IOUtils.toString(url, StandardCharsets.UTF_8).length()>0);
		assertTrue(IOUtils.toString((URL)null, StandardCharsets.UTF_8)==null);
		
		assertEquals(null, IOUtils.toString(null));
		
		sw=new StringWriter();
		try(Reader reader=new StringReader(a)){
			IOUtils.copy(reader, sw, 1024);
		};
		assertEquals(sw.getBuffer().toString(), a);
		
		
		File dir=new File("asb/asv/vfvf");
		if(!dir.exists()){
			dir.mkdirs();	
		}
		IOUtils.deleteTree(dir, false);
		
		byte[] ret=IOUtils.serialize(a);
		assertEquals(a, IOUtils.deserialize(ret));
		
		IOUtils.ensureParentFolder(path);
		IOUtils.saveAsFile(temp, a);
		
		file=IOUtils.escapeExistFile(temp);
		assertEquals("test(1).txt",file.getName());
		
		assertEquals("txt",IOUtils.getExtName(temp.getName()));
		assertEquals("test",IOUtils.removeExt(temp.getName()));
		
		try(BufferedWriter w = IOUtils.getUTF8Writer(temp, true)){
			w.write(a);
			w.newLine();
		};
		
		for(File f:IOUtils.listFiles(path, "txt")) {
			assertEquals("txt", IOUtils.getExtName(f.getName()));
		};
		
		for(File f:IOUtils.listFolders(path)) {
			assertTrue(f.isDirectory());
		};
		
		Object obj;
		IOUtils.saveObject(a, temp);
		try(InputStream in=IOUtils.getInputStream(temp)){
			obj=IOUtils.loadObject(in);
		}
		assertEquals(obj, a);
		
	}
	
	
	
	@Test
	public void testCloseException() {
		AtomicBoolean b=new AtomicBoolean(false);
		IOUtils.closeQuietly(new Closeable() {
			@Override
			public void close() throws IOException {
				b.set(true);
				throw new IOException("throw");
			}
		});
		assertTrue(b.get());
	}
}
