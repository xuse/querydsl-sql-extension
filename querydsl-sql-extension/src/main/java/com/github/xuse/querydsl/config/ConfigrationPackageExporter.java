package com.github.xuse.querydsl.config;

import java.util.function.Supplier;

import com.github.xuse.querydsl.init.TableInitTask;
import com.github.xuse.querydsl.sql.dbmeta.DriverInfo;

public class ConfigrationPackageExporter {
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
