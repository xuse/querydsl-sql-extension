/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.sql;

import java.util.List;

import com.querydsl.core.types.Path;

/**
 * <h2>English:</h2>
 * Extends the parent class to include additional information such as parameter names.
 * <h2>Chinese:</h2>
 * 修改父类，增加参数的名称等信息
 * 
 * @author Joey
 */
public class SQLBindingsAlter extends com.querydsl.sql.SQLBindings {
	private final List<Path<?>> paths;

	public SQLBindingsAlter(String sql, List<Object> bindings, List<Path<?>> paths) {
		super(sql, bindings);
//		if(paths!=null && paths.size()!=bindings.size()) {
//			throw new IllegalStateException();
//		}
		this.paths = paths;
	}

	public List<Path<?>> getPaths() {
		return paths;
	}
}
