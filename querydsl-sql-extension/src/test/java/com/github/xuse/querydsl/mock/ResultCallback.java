package com.github.xuse.querydsl.mock;

import java.sql.ResultSet;
import java.util.Map;

public interface ResultCallback {

	ResultSet get(String key, Map<String, Object> map);

}
