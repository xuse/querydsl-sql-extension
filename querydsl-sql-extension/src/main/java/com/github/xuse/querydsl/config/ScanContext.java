package com.github.xuse.querydsl.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.asm.ASMUtils.ClassAnnotationExtracter;
import com.github.xuse.querydsl.init.TableInitTask;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.spring.core.resource.Resource;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.util.ClassScanner;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.IOUtils;
import com.querydsl.sql.RelationalPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScanContext {
	private final Set<String> scannedEntities = new HashSet<>();
	private final ConfigurationEx parent;
	private int count;

	ScanContext(ConfigurationEx parent) {
		this.parent = parent;
	}

	public void scan(String[] pkgNames) {
		List<Resource> resources = new ClassScanner().scan(pkgNames);
		//第一遍扫描QueryClass
		Set<Resource> startWithQButNotQueryClass = scanQueryClass(resources);
		//第二遍扫描EntityClass
		scanEntityClass(resources,startWithQButNotQueryClass);
		log.info("Scan query class finish, {} relations registered.", count);
	}
	

	private void scanEntityClass(List<Resource> resources, Set<Resource> whitelist) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (Resource resource : resources) {
			if (!resource.isReadable()) {
				continue;
			}
			//已经加载 过了
			if(resource.getFilename().startsWith("Q") && !whitelist.contains(resource)) {
				continue;
			}
			loadEntityModel(resource,cl);
		}
	}

	private void loadEntityModel(Resource resource, ClassLoader cl) {
		byte[] data;
		try (InputStream in = resource.getInputStream()) {
			data = IOUtils.toByteArray(in);
		} catch (IOException e) {
			throw Exceptions.illegalState("Load resource {} error", resource, e);
		}
		ClassReader reader = new ClassReader(data);
		ClassAnnotationExtracter annoExtr=new ClassAnnotationExtracter();
		reader.accept(annoExtr, ClassReader.SKIP_CODE);
		if(annoExtr.hasAnnotation(TableSpec.class)) {
			String name=reader.getClassName().replace('/', '.');
			if(scannedEntities.contains(name)) {
				return;
			}
			Class<?> clz;
			try {
				clz = cl.loadClass(name);
				log.info("Scan Entity Class:{}", name);
				RelationalPathEx<?> table = PathCache.get(clz, null);
				scanned(table);
			} catch (ClassNotFoundException e) {
				log.error("class {} load error.", name, e);
				return;
			}
		};
	}

	private Set<Resource> scanQueryClass(List<Resource> resources) {
		Set<Resource> result=new HashSet<>();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (Resource resource : resources) {
			if (!resource.isReadable()) {
				continue;
			}
			if(resource.getFilename().startsWith("Q")) {
				try {
					RelationalPathEx<?> table=loadQueryClass(resource, cl);
					if (table != null) {
						log.info("Scan Query Class:{}", table.getSchemaAndTable());
						scanned(table);
					}else {
						result.add(resource);
					}
				} catch (Exception e) {
					log.error("Scan error: {}", resource, e);
				}	
			}
		}
		return result;
	}

	private void scanned(RelationalPathEx<?> table) {
		parent.registerRelation(table);
		scannedEntities.add(table.getType().getName());
		
		TableInitTask task = new TableInitTask(table);
		parent.initTasks.offer(task);
		count++;
	}

	private RelationalPathEx<?> loadQueryClass(Resource resource, ClassLoader cl) {
		byte[] data;
		try (InputStream in = resource.getInputStream()) {
			data = IOUtils.toByteArray(in);
		} catch (IOException e) {
			throw Exceptions.illegalState("Load resource {} error", resource, e);
		}
		ClassReader reader = new ClassReader(data);
		if ((reader.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
			// 非公有跳过
			return null;
		}
		String superName = reader.getSuperName();
		if ("com/github/xuse/querydsl/sql/RelationalPathBaseEx".equals(superName)) {
			return (RelationalPathEx<?>) getMetaModel(reader, cl);
		} else if ("com/querydsl/sql/RelationalPathBase".equals(superName)) {
			RelationalPath<?> path = getMetaModel(reader, cl);
			if (path != null) {
				return RelationalPathExImpl.toRelationPathEx(path);
			}
		}
		return null;
	}


	private RelationalPath<?> getMetaModel(ClassReader res, ClassLoader cl) {
		String name = res.getClassName().replace('/', '.');
		Class<?> clz;
		try {
			clz = cl.loadClass(name);
		} catch (ClassNotFoundException e) {
			log.error("class {} load error.", name, e);
			return null;
		}
		for (Field field : clz.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == clz) {
				try {
					RelationalPath<?> obj = (RelationalPath<?>) field.get(null);
					return obj;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("register class {}", name, e);
					throw Exceptions.toRuntime(e);
				}
			}
		}
		return null;
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
