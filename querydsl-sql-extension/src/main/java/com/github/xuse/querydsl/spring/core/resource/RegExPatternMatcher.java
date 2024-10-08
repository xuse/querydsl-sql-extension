/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.xuse.querydsl.spring.core.resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExPatternMatcher implements PatternMatcher {

	public boolean match(String pattern, String source) {
		if (pattern == null) {
			throw new IllegalArgumentException("pattern argument cannot be null.");
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(source);
		return m.matches();
	}

	public boolean matchStart(String fullPattern, String source) {
		if (!fullPattern.endsWith(".*")) {
			fullPattern = fullPattern.concat(".*");
		}
		return source.matches(fullPattern);
	}

	public boolean isPattern(String substring) {
		// return Pattern.;
		return true;
	}
}
