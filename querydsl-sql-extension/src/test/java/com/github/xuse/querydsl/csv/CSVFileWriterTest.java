package com.github.xuse.querydsl.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.init.csv.CsvFileReader;
import com.github.xuse.querydsl.init.csv.CsvFileWriter;
import com.github.xuse.querydsl.init.csv.EscapeMode;
import com.github.xuse.querydsl.util.IOUtils;

public class CSVFileWriterTest {
	@Test
	public void testCsvFileWriter() throws IOException {
		URL url = this.getClass().getResource("/com.github.xuse.querydsl.entity.SeataStateMachineInst.csv");
		assertNotNull(url);
		String[] headers = null;
		List<String[]> data = new ArrayList<>();
		// Reader File.
		try (CsvFileReader<String[]> reader = CsvFileReader.of(url, StandardCharsets.UTF_8)) {
			if (reader.readHeaders()) {
				headers = reader.getHeaders();
				while (reader.readRecord()) {
					data.add(reader.getValues());
				}
			}
		}
		if (headers != null) {
			// Write to new File
			File tmpFile = new File(System.getProperty("user.dir"), "tmp.csv");
			try (CsvFileWriter<String[]> writer = CsvFileWriter.ofUtf8Writer(tmpFile)) {
				writer.writeRecord(headers);
				for (String[] row : data) {
					writer.writeRecord(row);
				}
			}
			File soruce = new File(url.getFile());
			System.out.println("Length:" + soruce.length() + " : " + tmpFile.length());
			assertEquals(soruce.length(), tmpFile.length());
			tmpFile.delete();
		}

	}
	
	@Test
	public void testStaticParse() throws IOException, URISyntaxException {
		URL url = this.getClass().getResource("/csv_escape.csv");
		Files.lines(Paths.get(url.toURI())).forEach(CsvFileReader::parse);
	}

	@Test
	public void testCsvFileWriter2() throws IOException {
		File tmpFile = new File(System.getProperty("user.dir"), "tmp.csv");

		String[] headers = new String[] { "A", "B" };
		try (CsvFileWriter<String[]> writer = CsvFileWriter.of(IOUtils.getWriter(tmpFile, StandardCharsets.UTF_8, false))) {
			writer.getSettings().escapeMode=EscapeMode.BACKSLASH;
			writer.write(null,false);
			writer.write("##");
			writer.write("############################ ",true);
			writer.writeRecord(headers);
			writer.writeRecord(new String[] { "∑ ‍♂️ ‍♂️ ‍♀️", "☘️ ⭐️ ✨ ⚡️" });
			writer.writeRecord(new String[] { ",", "COMM" });
			writer.writeRecord(new String[] { ",\"", "\"" });
			writer.writeRecord(new String[] { "',\"'", "\"\",, " });
			writer.writeRecord(new String[] { " ', \"'", "\"\" ,, " });
			writer.writeRecord(new String[] { " ', \"'\r\n", "\"\" ,, " });
			writer.writeRecord(new String[] { " ', \"'\r\n", "\"\" ,, " });
			writer.writeRecord(new String[] { " ', \"'\r\n", "\"\" ,\r\n\r\n\r\n\t, " });
			writer.writeRecord(new String[] { " AA', \"'\b", "\"\" ,\f, " });
			writer.write(",\\r\\n",true).writeComment("COMMENT !!!");
			writer.write("\"\",").endRecord();
			writer.write(",\"\"").endRecord();
			writer.write("\\u2134,\\x1234").endRecord();
			writer.write("\\d2134,\\D1234").endRecord();
			writer.write("\\x2134,\\X1234").endRecord();
			writer.write("\\o2134,\\O1234").endRecord();
			writer.write(",,").endRecord();
			writer.write("\rcomment",true).write("\ncomment",true);
			writer.write("\tcomment",true).write(" comment ",true)
			.write("comment\t",true).write("comment ",true).endRecord();;
			writer.write("#comment ",true);
			writer.flush();
		}
		try (CsvFileReader<String[]> reader = CsvFileReader.of(IOUtils.getUTF8Reader(tmpFile))) {
			reader.readHeaders();
		}
		try (CsvFileReader<String[]> reader = CsvFileReader.of(tmpFile, StandardCharsets.UTF_8)) {
			reader.getSettings().escapeMode=EscapeMode.BACKSLASH;
			reader.getSettings().useComments=true;
			reader.skipLine();
			reader.skipRecord();
			reader.getCurrentRecord();
			reader.getHeaderCount();
			reader.getRawRecord();
			while (reader.readRecord()) {
				for(int i=0;i<reader.getColumnCount();i++) {
					reader.getHeader(i);
					reader.isQualified(i);
				}
				System.out.println(Arrays.toString(reader.getValues()));
			}
		}
		tmpFile.delete();

		URL url = this.getClass().getResource("/csv_escape.csv");
		try (CsvFileReader<String[]> reader = CsvFileReader.of(url, StandardCharsets.UTF_8)) {
			reader.getSettings().escapeMode=EscapeMode.BACKSLASH;
			reader.getSettings().useComments=true;
				reader.setHeaders(reader.getHeaders());
				if(reader.readHeaders()) {
					reader.setHeaders(reader.getHeaders());
				}
				reader.get("A");
				reader.get("B");
				reader.get("C");
				reader.get("D");
				assertEquals(reader.get(-1),"");
				assertEquals(reader.get(6),"");
				while (reader.readRecord()) {
					for(int i=0;i<reader.getColumnCount();i++) {
						reader.getHeader(i);
						reader.isQualified(i);
					}
					System.out.println(Arrays.toString(reader.getValues()));
				}
		}
		
		try (CsvFileReader<String[]> reader = CsvFileReader.of(url, StandardCharsets.UTF_8)) {
			reader.getSettings().escapeMode=EscapeMode.BACKSLASH;
			reader.getSettings().useComments=true;
			reader.getSettings().useTextQualifier=false;
				reader.setHeaders(reader.getHeaders());
				if(reader.readHeaders()) {
					reader.setHeaders(reader.getHeaders());
				}
				reader.get("A");
				reader.get("B");
				reader.get("C");
				reader.get("D");
				assertEquals(reader.get(-1),"");
				assertEquals(reader.get(6),"");
				while (reader.readRecord()) {
					System.out.println(Arrays.toString(reader.getValues()));
				}
				
		}
	}
	
	@Test
	@SuppressWarnings("unused")
	public void withExceptions() {
		int count = 0;
		try {
			CsvFileReader<String[]> reader=CsvFileReader.of(null);	
		}catch(IllegalArgumentException e) {
			count++;
		}
		try (CsvFileWriter<String[]> reader=CsvFileWriter.of(null)){
			;	
		}catch(IllegalArgumentException e) {
			count++;
		}
		File tmpFile = new File(System.getProperty("user.dir"), "tmp.csv");
		try {
			CsvFileReader<String[]> reader=CsvFileReader.of(tmpFile,null);	
		}catch(IllegalArgumentException e) {
			count++;
		}
		try (CsvFileWriter<String[]> writer=CsvFileWriter.of(tmpFile, null)){
				
		}catch(IllegalArgumentException e) {
			count++;
		}
		try {
			CsvFileReader<String[]> reader=CsvFileReader.of(tmpFile,StandardCharsets.UTF_8);	
		}catch(IllegalArgumentException e) {
			count++;
		}
		try {
			CsvFileWriter<String[]> writer=CsvFileWriter.of(tmpFile, StandardCharsets.UTF_8);	
			writer.close();
			writer.write("123");
		}catch(IOException e) {
			count++;
		}
		tmpFile=null;
		try {
			CsvFileReader<String[]> reader=CsvFileReader.of(tmpFile,StandardCharsets.UTF_8);	
		}catch(IllegalArgumentException e) {
			count++;
		}
		try (CsvFileWriter<String[]> writer=CsvFileWriter.of(tmpFile, StandardCharsets.UTF_8)){
			
		}catch(IllegalArgumentException e) {
			count++;
		}
		URL url=null;
		try(CsvFileReader<String[]> reader=CsvFileReader.of(url,StandardCharsets.UTF_8)) {
		}catch(IllegalArgumentException e) {
			count++;
		}
		url = this.getClass().getResource("/com.github.xuse.querydsl.entity.SeataStateMachineInst.csv");
		
		try {
			CsvFileReader.parse(null);
		}catch(IllegalArgumentException e) {
			count++;
		}
		System.out.println(count);
		assertEquals(10,count);
	}
}
