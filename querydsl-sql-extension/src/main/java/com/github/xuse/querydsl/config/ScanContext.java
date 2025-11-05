package com.github.xuse.querydsl.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.github.xuse.querydsl.annotation.dbdef.TableSpec;
import com.github.xuse.querydsl.asm.ASMUtils.ClassAnnotationExtracter;
import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.asm.Opcodes;
import com.github.xuse.querydsl.init.TableInitTask;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.spring.core.resource.Resource;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
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
	private Class<? extends Annotation> matchAnnotation;
	private Class<? extends Annotation> matchWithoutAnnotation;
	private final List<Consumer<RelationalPathEx<?>>> listeners = new ArrayList<>();

	ScanContext(ConfigurationEx parent) {
		this.parent = parent;
	}

	public ScanContext withAnnotation(Class<? extends Annotation> annotation) {
	    this.matchAnnotation=annotation;
	    return this;
	}
	
	public ScanContext withoutAnnotation(Class<? extends Annotation> annotation) {
        this.matchWithoutAnnotation=annotation;
        return this;
    }
	
	public ScanContext addListener(Consumer<RelationalPathEx<?>> listener) {
		this.listeners.add(listener);
		return this;
	}
	
	public ScanContext addListeners(Collection<Consumer<RelationalPathEx<?>>> listeners) {
		this.listeners.addAll(listeners);
		return this;
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
				RelationalPathEx<?> table = PathCache.get(clz, null);
				if(filterWithAnnotation(table)) {
				    log.info("Scan Entity Class:{}", name);
				    scanned(table);    
				}
			} catch (ClassNotFoundException e) {
				log.error("class {} load error.", name, e);
				return;
			}
		};
	}

	private boolean filterWithAnnotation(RelationalPathEx<?> table) {
	    if(this.matchAnnotation!=null) {
	        if(findAnnotation(table,matchAnnotation)==null) {
	            return false;
	        }
	    }
	    if(this.matchWithoutAnnotation!=null) {
	        if(findAnnotation(table,matchWithoutAnnotation)!=null) {
	            return false;
	        }
	    }
        return true;
    }

    private Object findAnnotation(RelationalPathEx<?> table, Class<? extends Annotation> type) {
        Object o=table.getType().getAnnotation(type);
        if(o!=null) {
            return o;
        }
        return table.getClass().getAnnotation(type);
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
					    if(filterWithAnnotation(table)) {
					        log.info("Scan Query Class:[{}.{}]", table.getSchemaName(),table.getTableName());
					        scanned(table);    
					    }
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
		for(Consumer<RelationalPathEx<?>> listener:listeners) {
			try {
				listener.accept(table);
			}catch(Exception e) {
				log.error("Notify entity {} to listener {} raise a error.", table.getTableName(), listener, e);
			}
		}
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
		return SQLTypeUtils.getMetaModel(clz);
	}
	
	public int getCount() {
		return count;
	}

}
