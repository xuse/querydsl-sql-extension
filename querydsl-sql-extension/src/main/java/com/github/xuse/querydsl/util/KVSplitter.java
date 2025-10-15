package com.github.xuse.querydsl.util;

import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;


public class KVSplitter {
    private String str;
    private int begin;
    private int len;
    private final char entrySep;
    private final char keyValueSep;
    private IntUnaryOperator ignoreSpace = (i) -> {
        while (str.charAt(i) == ' ') {
            i++;
        }
        return i;
    };
    private IntUnaryOperator ignorePrevSpace = (i)->{
        while (str.charAt(i - 1) == ' ') {
            i--;
        }
        return i;
    };
    
    private static final IntUnaryOperator KEEP_INDEX = (i) -> i;

    public static KVSplitter on(char entrySep, char kvSep) {
        KVSplitter p = new KVSplitter(entrySep, kvSep);
        return p;
    }

    public KVSplitter split(String text) {
        this.begin = 0;
        this.str = text;
        this.len = str.length();
        return this;
    }
    
    public KVSplitter keepSpace() {
        this.ignoreSpace = KEEP_INDEX;
        this.ignorePrevSpace = KEEP_INDEX;
        return this;
    }

    public Map<String, String> collect(Supplier<Map<String, String>> supplier) {
        Map<String, String> map = supplier.get();
        try {
            String k;
            while ((k = nextKey()) != null) {
                if (k.length() == 0) {
                    continue;
                }
                map.put(k, nextValue());
            }
            return map;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parse map at '" + errorPosition() + "'", e);
        }
    }

    public KVSplitter(char entrySep, char valueSep) {
        this.entrySep = entrySep;
        this.keyValueSep = valueSep;
    }

    private String nextKey() {
        int start = ignoreSpace.applyAsInt(this.begin);
        if (start >= len) {
            return null;
        }
        int i = str.indexOf(keyValueSep, start);
        if (i > -1) {
            begin = i + 1;
            return str.substring(start, i > start ? ignorePrevSpace.applyAsInt(i) : i);
        }
        return null;
    }

    private String nextValue() {
        int start = ignoreSpace.applyAsInt(this.begin);
        if (start >= len) {
            return null;
        }
        int i = str.indexOf(entrySep, start);
        if (i > -1) {
            begin = i + 1;
            return str.substring(start, i > start ? ignorePrevSpace.applyAsInt(i) : i);
        } else {
            return str.substring(start, ignorePrevSpace.applyAsInt(len));
        }
    }

    public String errorPosition() {
        return str.substring(begin);
    }
}