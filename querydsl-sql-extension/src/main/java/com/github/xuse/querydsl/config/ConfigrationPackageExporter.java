package com.github.xuse.querydsl.config;

import java.util.function.Supplier;

import com.github.xuse.querydsl.init.TableInitTask;
import com.github.xuse.querydsl.sql.dbmeta.DriverInfo;

/**
 * 配置包导出器类
 * 该类提供了一些静态方法用于处理配置信息，例如从配置中获取初始化任务以及计算驱动信息
 */
public class ConfigrationPackageExporter {

	/**
	 * 从配置对象中获取并移除初始化任务
	 * This method retrieves and removes the initialization task from the
	 * configuration object.
	 *
	 * @param configuration 配置对象，包含初始化任务队列 / Configuration object containing the
	 *                      queue of initialization tasks
	 * @return 返回被移除的初始化任务，如果队列为空则返回null / Returns the removed initialization task,
	 *         or null if the queue is empty
	 */
	public static TableInitTask pollFrom(ConfigurationEx configuration) {
		return configuration.initTasks.poll();
	}

	public static DriverInfo computeDriverInfo(ConfigurationEx configuration, Supplier<DriverInfo> s) {
		DriverInfo driverInfo = configuration.driverInfo;
		if (driverInfo == null) {
			return configuration.driverInfo = s.get();
		}
		return driverInfo;
	}
}