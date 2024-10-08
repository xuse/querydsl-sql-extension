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
package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.querydsl.sql.types.AbstractType;

/**
 * {@code IntegerType} maps Integer to Integer on the JDBC level
 *
 * @author tiwe
 *
 */
public class PrimitiveIntegerType extends AbstractType<Integer> {
	

    public PrimitiveIntegerType() {
        super(Types.INTEGER);
    }

    @Override
    public Class<Integer> getReturnedClass() {
        return int.class;
    }

    @Override
    public Integer getValue(ResultSet rs, int startIndex) throws SQLException {
        return rs.getInt(startIndex);
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, Integer value) throws SQLException {
        st.setInt(startIndex, value);
    }

}
