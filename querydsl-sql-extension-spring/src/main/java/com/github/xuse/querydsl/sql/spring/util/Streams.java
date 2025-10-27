package com.github.xuse.querydsl.sql.spring.util;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams {
    public static <T> Iterable<T> wrap(Iterator<T> iter) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return iter;
            }
        };  
    }

    public static <T> Stream<T> stream(Iterator<T> iter) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, 0), false);
    }
}