package com.github.xuse.querydsl.types;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.sql.types.AbstractType;

/**
 * {@code EnumByCodeType} maps Enum types to their Integer ordinals on the JDBC level
 *
 * @author tiwe
 *
 * @param <T>
 */
public class EnumByCodeType<T extends Enum<T> & CodeEnum<T>> extends AbstractType<T> {
    private final Class<T> type;
    private final Map<Integer,T> index=new HashMap<>();
    
    private static final Logger log=LoggerFactory.getLogger(EnumByCodeType.class);

    public EnumByCodeType(Class<T> type) {
        this(Types.INTEGER, type);
    }

    public EnumByCodeType(int jdbcType, Class<T> type) {
        super(jdbcType);
        this.type = type;
        for(T et: Arrays.asList(type.getEnumConstants())) {
//        	@SuppressWarnings("unchecked")
//			T t=(T)et;
        	index.put(et.getCode(), et);
        }
    }

    @Override
    public Class<T> getReturnedClass() {
        return type;
    }

    @Override
    public T getValue(ResultSet rs, int startIndex) throws SQLException {
        int code = rs.getInt(startIndex);
        return rs.wasNull() ? null : index.get(code);
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, T value) throws SQLException {
        st.setInt(startIndex, value.getCode());
        //log.info("set value for ({})={}",startIndex,value.getCode());
    }
}
