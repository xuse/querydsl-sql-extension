package com.github.xuse.querydsl.init;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.github.xuse.querydsl.annotation.init.InitializeData;
import com.github.xuse.querydsl.asm.AnnotationVisitor;
import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.asm.ClassVisitor;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.config.util.ClassScanner;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.util.ASMUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.IOUtils;
import com.querydsl.core.types.Path;

import lombok.extern.slf4j.Slf4j;

/**
 * 将表中的数据导出成为CSV格式。。
 * 
 * @author jiyi
 *
 */
@Slf4j
public class InitDataExporter {
	private SQLQueryFactory session;
	private boolean deleteEmpty;
	private File target = new File(System.getProperty("user.dir"));
	private String extension = ".txt";
	private Charset charset = StandardCharsets.UTF_8;
	private int maxResults = 5000;

	/**
	 * @param session 数据库客户端
	 */
	public InitDataExporter(SQLQueryFactory session) {
		this.session = session;
	}

	/**
	 * 
	 * @param session 数据库客户端
	 * @param target  生成的资源文件路径
	 */
	public InitDataExporter(SQLQueryFactory session, File target) {
		this.session = session;
		this.target = target;
	}

	/**
	 * 导出制定的实体类数据
	 * @param anno
	 * @param clz
	 */
	public void export(Class<?> clz,InitializeData anno) {
		try {
			RelationalPathEx<?> obj = null;
			for (Field field : clz.getDeclaredFields()) {
				if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == clz) {
					obj = (RelationalPathEx<?>)field.get(null);
					break;
				}
			}
			if(obj!=null) {
				export0(obj,anno);
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void export0(RelationalPathEx<?> meta,InitializeData anno) throws SQLException, IOException {
		File file = new File(target, meta.getTableName() + extension);
		List<?> records = session.selectFrom(meta).setFetchSisze(2000).setMaxRows(this.maxResults).fetch();
		if (records.isEmpty()) {
			if (deleteEmpty && file.exists()) {
				file.delete();
			}
			return;
		}

		CsvWriter cw = new CsvWriter(file, charset);
		try {
			List<Path<?>> columns = meta.getColumns();
			List<ColumnMapping> columnMetas=new ArrayList<>(); 
			for (Path<?> path : columns) {
				ColumnMapping column=meta.getColumnMetadata(path);
				cw.write("[" + column.fieldName() + "]");
				columnMetas.add(column);
			}
			cw.endRecord();
			int size=columnMetas.size();
			for (Object record : records) {
				Object[] values=meta.getBeanCodec().values(record);
				for (int i=0;i<size;i++) {
					Object value= values[i];
					ColumnMapping column=columnMetas.get(i);
					String data = Codecs.toString(value, column.getType());
					cw.write(data);
				}
				cw.endRecord();
			}
		} finally {
			cw.close();
		}
		log.info("{} was updated.", file.getAbsolutePath());
	}

	/**
	 * 将数据库中的数据全部作为初始化数据导出到src/main/resources下。
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void exportPackage(String... packageName) throws ClassNotFoundException, IOException {
		// 只有第一个Classpath是要扫描的
		ClassLoader classLoader = this.getClass().getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		for (Resource resource : new ClassScanner().scan(packageName)) {
			if (!resource.isReadable()) {
				continue;
			}
			ClassReader cl = new ClassReader(IOUtils.toByteArray(resource.getInputStream()));
			if ((cl.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
				continue;// 非公有跳过
			}
			if (resource.getFilename().startsWith("Q")) {
				String superName = cl.getSuperName();
				if ("com/github/xuse/querydsl/sql/RelationalPathBaseEx".equals(superName)) {
					try {
						doExport(cl, classLoader);
					} catch (Exception e) {
						log.error("registe for {} error.", resource, e);
					}
				}
			}
		}
	}

	private void doExport(ClassReader cl, ClassLoader loader) {
		ClassAnnotationExtracter ae = new ClassAnnotationExtracter();
		cl.accept(ae, ClassReader.SKIP_CODE);
		if (!ae.hasAnnotation(InitializeData.class)) {
			// 无需执行
			return;
		}
		if (ae.hasAnnotation(InitializeData.class)) {
			Class<?> e;
			try {
				e = loader.loadClass(ASMUtils.getJavaClassName(cl));
			} catch (ClassNotFoundException ex) {
				throw Exceptions.illegalState(ex);
			}
			InitializeData data = e.getAnnotation(InitializeData.class);
			if (data != null && !data.enable()) {
				log.info("The annotation @InitializeData on [{}] was disabled.", e.getName());
				return;
			}
			log.info("Starting export data:{}", e.getName());
			export(e,data);
		}
	}

	static class ClassAnnotationExtracter extends ClassVisitor {
		private Set<String> annotations = new HashSet<String>();

		public ClassAnnotationExtracter() {
			super(Opcodes.ASM7);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			annotations.add(desc);
			return null;
		}

		public boolean hasAnnotation(Class<? extends Annotation> clzName) {
			return annotations.contains(ASMUtils.getDesc(clzName));
		}
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
}
