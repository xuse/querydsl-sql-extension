package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.derby.iapi.services.io.ArrayInputStream;
import org.junit.Test;

import lombok.SneakyThrows;

@SuppressWarnings("unused")
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
		
		byte[] data = a.getBytes();
		IOUtils.saveAsFile(temp, StringUtils.toHexString(data,true));
		{
			try(Reader reader=IOUtils.getUTF8Reader(temp)){
				assertArrayEquals(data,IOUtils.readHexString(reader));
			}
		}
		{
			File invalid = new File(temp, "flow");
			RuntimeException e = null;
			try {
				IOUtils.ensureParentFolder(invalid);
			} catch (RuntimeException ex) {
				e = ex;
			}
			assertNotNull(e);
		}
		
		assertTrue(IOUtils.toByteArray(temp).length > 0);
		assertTrue(IOUtils.toByteArray(url).length > 0);
		
		File file=IOUtils.rename(temp,"test1.txt",true);
		assertEquals("test1.txt",file.getName());
		
		try(InputStream in=url.openStream()){
			assertTrue(IOUtils.toByteArray(in,6).length == 6);
		}
		try(InputStream in=url.openStream()){
			data=IOUtils.toByteArray(in);
			IOUtils.saveAsFile(temp, data);
		}
		file=IOUtils.rename(file,temp.getName(),false);
		assertEquals(null, file);
		
		
		File tmp=IOUtils.saveAsTempFile(new ArrayInputStream(data));
		assertTrue(tmp.exists());
		tmp.delete();
		
		{
			file=new File(temp.getParent(),"test1.txt");
			File t=new File(temp.getParent(),"test2.txt");
			boolean b = IOUtils.move(file, t);
			if(b) {
				t.delete();	
			}
		}
		
		assertTrue(IOUtils.toString(url, StandardCharsets.UTF_8).length()>0);
		assertTrue(IOUtils.toString((URL)null, StandardCharsets.UTF_8)==null);
		
		assertEquals(null, IOUtils.toString(null));
		
		sw=new StringWriter();
		try(Reader reader=new StringReader(a)){
			IOUtils.copy(reader, sw, 1024);
		};
		assertEquals(sw.getBuffer().toString(), a);
		
		
		File dir=new File("asb/asv/vfvf");
		IOUtils.ensureParentFolder(dir);
		
		IOUtils.saveAsFile(new File("asb/t1.txt"), a);
		IOUtils.saveAsFile(new File("asb/asv/t1.txt"), a);
		for(File f:IOUtils.listFiles(new File("asb"), "txt")) {
			assertEquals("txt", IOUtils.getExtName(f.getName()));
		};
		for(File f:IOUtils.listFiles(new File("asb"))) {
			assertEquals("txt", IOUtils.getExtName(f.getName()));
		};
		for(File f:IOUtils.listFolders(new File("asb"))) {
			assertTrue(f.isDirectory());
		};
		File root=new File("asb");
		IOUtils.deleteTree(root, false);
		assertTrue(root.exists());
		IOUtils.deleteTree(root, true);
		assertFalse(root.exists());
		
		byte[] ret=IOUtils.serialize(a);
		assertEquals(null, IOUtils.deserialize(null));
		assertEquals(a, IOUtils.deserialize(ret));
		
		IOUtils.ensureParentFolder(path);
		IOUtils.saveAsFile(temp, a);
		

		file=IOUtils.escapeExistFile(new File("newFile.txt"));
		assertEquals("newFile.txt", file.getName());
		
		file=IOUtils.escapeExistFile(temp);
		assertEquals("test(1).txt",file.getName());
		assertEquals("txt",IOUtils.getExtName(temp.getName()));
		assertEquals("",IOUtils.getExtName("ssss"));
		assertEquals("test",IOUtils.removeExt(temp.getName()));
		assertEquals("test",IOUtils.removeExt("test"));
		
		try(BufferedWriter w = IOUtils.getUTF8Writer(temp, true)){
			w.write(a);
			w.newLine();
		};
		try(BufferedWriter w = IOUtils.getWriter(temp, null, true)){
			w.write(a);
			w.newLine();
		};
		
		Object obj;
		IOUtils.saveObject(a, temp);
		try(InputStream in=IOUtils.getInputStream(temp)){
			obj=IOUtils.loadObject(in);
		}
		assertEquals(obj, a);
	}
	
	@Test
	public void testProperties() {
		URL url=this.getClass().getResource("/test.properties");
		Map<String,String> map=IOUtils.loadProperties(url);
		assertEquals(5,map.size());
		
		url=this.getClass().getResource("/test.ini");
		map=IOUtils.loadProperties(url);
		assertEquals(65,map.size());
		
		map=IOUtils.loadProperties(url,true);
		map.forEach((k,v)->System.out.println(k+": "+v));
		assertEquals(116,map.size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertiesException() {
		URL url=this.getClass().getResource("/testerror.properties");
		IOUtils.loadProperties(url);
	}
	
	@Test
	public void testFileReader() throws IOException {
		URL url=this.getClass().getResource("/");
		File file=new File(url.getPath(),"com.github.xuse.querydsl.entity.SeataStateMachineInst.csv");
		try(InputStream in=new FileInputStream(file)){
			byte[] data=IOUtils.toByteArray(in, 2049);
			assertEquals(2049,data.length);
		}
		
		try(InputStream in=new FileInputStream(file)){
			byte[] data=IOUtils.toByteArray(in, 4099);
			assertEquals(4099,data.length);
		}
		
		try(InputStream in=new FileInputStream(file)){
			byte[] data=IOUtils.toByteArray(in, 5096);
			if(file.length()<5096) {
				assertEquals(file.length(),data.length);
			}else {
				assertEquals(5096,data.length);
			}
		}
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
