package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 对KV类型的字符串进行高效率解析的工具。
 */
public class KVSplitter {
	private static final IntUnaryOperator KEEP_INDEX = i -> i;
	private static final Predicate<String> ACCEPT_ALL = s -> true;
	private static final Predicate<String> NO_EMPTY = s -> s.length() > 0;
    
    private String str;
    private int begin;
    private int len;
    private final char entrySep;
    private final char keyValueSep;
    
    //Use to ignore chars before the key or value. By default, it ignores spaces.
    private IntUnaryOperator ignorePrevChars = (i) -> {
        while (str.charAt(i) == ' ') {
            i++;
        }
        return i;
    };
    //Use to ignore chars after the key or value. By default, it ignores spaces.
    private IntUnaryOperator ignorePostChars = (i)->{
        while (str.charAt(i - 1) == ' ') {
            i--;
        }
        return i;
    };
    
	private Predicate<String> keyFilter = ACCEPT_ALL;

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
        this.ignorePrevChars = KEEP_INDEX;
        this.ignorePostChars = KEEP_INDEX;
        return this;
    }

    public KVSplitter functionOfHeadChar(IntUnaryOperator func) {
    	this.ignorePrevChars=func;
    	return this;
    }
    
    public KVSplitter functionOfTailChar(IntUnaryOperator func) {
    	this.ignorePostChars=func;
    	return this;
    }
    
    public KVSplitter ignoreEmptyKeys() {
		keyFilter = keyFilter == ACCEPT_ALL ? NO_EMPTY : keyFilter.and(NO_EMPTY);
    	return this;
    }
    
	public KVSplitter keyFilter(Predicate<String> filter) {
		this.keyFilter = filter;
		return this;
	}
	
    public Map<String, String> collect(Supplier<Map<String, String>> supplier) {
        Map<String, String> map = supplier.get();
        try {
            String k;
            while ((k = nextKey()) != null) {
                if (keyFilter.test(k)) {
					map.put(k, nextValue());
				}else {
					//skip create the string object if the value is not used.
					ignoreNextValue();
				}
            }
            return map;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parse map at '" + errorPosition() + "'", e);
        }
    }
    
    /**
     * 支持解析成字符串对列表。可通过 {@link #ignoreEmptyKeys()} 或 {@link #keyFilter(Predicate)} 过滤不需要的key。
     * @return List&lt;Entry&lt;String, String&gt;&gt;
     */
    public List<Entry<String, String>> collect() {
    	List<Entry<String, String>> list = new ArrayList<>();
    	try {
            String k;
            while ((k = nextKey()) != null) {
                if (keyFilter.test(k)) {
					list.add(new Entry<>(k, nextValue()));
				}else {
					//skip create the string object if the value is not used.
					ignoreNextValue();
				}
            }
            return list;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parse map at '" + errorPosition() + "'", e);
        }
    }

    public KVSplitter(char entrySep, char valueSep) {
        this.entrySep = entrySep;
        this.keyValueSep = valueSep;
    }

    private String nextKey() {
        int start = ignorePrevChars.applyAsInt(this.begin);
        if (start >= len) {
            return null;
        }
        int i = str.indexOf(keyValueSep, start);
        if (i > -1) {
            begin = i + 1;
            return str.substring(start, i > start ? ignorePostChars.applyAsInt(i) : i);
        }
        return null;
    }

    private String nextValue() {
        int start = ignorePrevChars.applyAsInt(this.begin);
        if (start >= len) {
            return null;
        }
        int i = str.indexOf(entrySep, start);
        if (i > -1) {
            begin = i + 1;
            return str.substring(start, i > start ? ignorePostChars.applyAsInt(i) : i);
        } else {
            return str.substring(start, ignorePostChars.applyAsInt(len));
        }
    }

    //Fast jump the index to the next key, without creating string objects.
    private void ignoreNextValue() {
        int start = ignorePrevChars.applyAsInt(this.begin);
        if (start >= len) {
            return;
        }
        int i = str.indexOf(entrySep, start);
        if (i > -1) {
            begin = i + 1;
        }
    }
    
    /**
     * @return the error position in the parse processing.
     */
    public String errorPosition() {
        return str.substring(begin);
    }
}