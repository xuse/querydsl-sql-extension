package com.github.xuse.querydsl.util;

import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.github.xuse.querydsl.lambda.DateTimeLambdaColumn;
import java.time.Instant;
import com.github.xuse.querydsl.lambda.SimpleLambdaColumn;
import java.util.Date;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import java.util.Map;
import com.github.xuse.querydsl.lambda.BooleanLambdaColumn;

public class Foo_ {

    public final static LambdaTable<Foo> foo = () -> Foo.class;

    public final NumberLambdaColumn<Foo, Long> _id = Foo::getId;

    public final StringLambdaColumn<Foo> _content = Foo::getContent;

    public final DateTimeLambdaColumn<Foo, Instant> _created = Foo::getCreated;

    public final DateTimeLambdaColumn<Foo, Date> _updated = Foo::getUpdated;

    public final NumberLambdaColumn<Foo, Integer> _version = Foo::getVersion;

    public final SimpleLambdaColumn<Foo, Map<String, String>> _ext = Foo::getExt;

    public final BooleanLambdaColumn<Foo> _init = Foo::getInit;
}
