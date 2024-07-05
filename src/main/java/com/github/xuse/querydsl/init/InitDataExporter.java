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
	private File targetDirectory = new File(System.getProperty("user.dir"));
	private Charset charset = StandardCharsets.UTF_8;
	private int maxResults = 10000;
	private boolean writeNullString = false;

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
		this.targetDirectory = target;
	}

	
	/**
	 * 由于CSV文件存储的特性，对于varchar等数据库字段，导出数据无法区分null和""。
	 * 开启此选项后，null值将用特殊字符串表示，从而在数据导入时可以区分null和""。
	 * @return this
	 */
	public InitDataExporter writeNullString() {
		this.writeNullString=true;
		return this;
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
	/**
	 * 导出制定的实体类数据
	 * @param anno
	 * @param modelClass
	 */
	public void export(Class<?> modelClass) {
		try {
			RelationalPathEx<?> obj = null;
			for (Field field : modelClass.getDeclaredFields()) {
				if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == modelClass) {
					obj = (RelationalPathEx<?>)field.get(null);
					break;
				}
			}
			
			InitializeData anno=modelClass.getAnnotation(InitializeData.class);
			if (anno == null) {
				anno = obj.getType().getAnnotation(InitializeData.class);
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
		String fileName=DataInitializer.calcResourceName(anno.value(),meta, session.getConfiguration());
		File file = new File(targetDirectory,fileName);
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
					if(data==null && writeNullString && column.getType()==String.class){
						cw.write(DataInitializer.NULL_STRING_ESCAPE);
					}else {
						cw.write(data);
					}
					
				}
				cw.endRecord();
			}
		} finally {
			cw.close();
		}
		log.info("{} was updated.", file.getAbsolutePath());
	}
	
	private void doExport(ClassReader cl, ClassLoader loader) {
		ClassAnnotationExtracter ae = new ClassAnnotationExtracter();
		cl.accept(ae, ClassReader.SKIP_CODE);
		if (!ae.hasAnnotation(InitializeData.class)) {
			// 无需执行
			return;
		}
		if (ae.hasAnnotation(InitializeData.class)) {
			Class<?> modelClass;
			try {
				modelClass = loader.loadClass(ASMUtils.getJavaClassName(cl));
			} catch (ClassNotFoundException ex) {
				throw Exceptions.illegalState(ex);
			}
			InitializeData data = modelClass.getAnnotation(InitializeData.class);
			if (data != null && !data.enable()) {
				log.info("The annotation @InitializeData on [{}] was disabled.", modelClass.getName());
				return;
			}
			log.info("Starting export data:{}", modelClass.getName());
			export(modelClass);
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

	public InitDataExporter setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}

	public File getTarget() {
		return targetDirectory;
	}

	public InitDataExporter targetDirectory(File target) {
		this.targetDirectory = target;
		return this;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public InitDataExporter setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}
}
