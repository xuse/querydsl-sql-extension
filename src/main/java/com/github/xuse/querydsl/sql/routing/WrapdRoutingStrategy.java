package com.github.xuse.querydsl.sql.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.sql.SchemaAndTable;

public class WrapdRoutingStrategy implements RoutingStrategy {
	private final List<RoutingStrategy> chain;

	public WrapdRoutingStrategy() {
		chain = new ArrayList<>();
	}
	
	WrapdRoutingStrategy(List<RoutingStrategy> routingStrategies) {
		 chain = routingStrategies;
	}
	
	@Override
	public SchemaAndTable getOverride(SchemaAndTable schemaAndTable, ConfigurationEx configurationEx) {
		for(RoutingStrategy s:chain) {
			schemaAndTable = s.getOverride(schemaAndTable, configurationEx);			
		}
		return schemaAndTable;
	}
	
	/*
	 * Merge routing strategies, null safe
	 */
	public static RoutingStrategy wrap(RoutingStrategy as,RoutingStrategy bs) {
		boolean a = as == null;
		boolean b = bs == null;
		if (a && b) {
			return RoutingStrategy.DEFAULT;
		} else if (a) {
			return bs;
		} else if (b) {
			return as;
		} else {
			return new WrapdRoutingStrategy(Arrays.asList(as, bs));
		}
	}
	
	
	/*
	 * Merge routing strategies, null safe
	 */
	public static RoutingStrategy wrap(RoutingStrategy...routingStrategies) {
		List<RoutingStrategy> result=new ArrayList<>();
		for(RoutingStrategy s:routingStrategies) {
			if(s!=null) {
				result.add(s);
			}
		}
		if(result.isEmpty()) {
			return RoutingStrategy.DEFAULT;
		}
		if(result.size()==1) {
			return result.get(0);
		}
		return new WrapdRoutingStrategy(result);
	}
}
