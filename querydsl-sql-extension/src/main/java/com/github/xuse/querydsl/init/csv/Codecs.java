package com.github.xuse.querydsl.init.csv;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.xuse.querydsl.init.TableDataInitializer;
import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.JefBase64;
import com.github.xuse.querydsl.util.StringUtils;

public class Codecs {
    private Codecs() {
    }

    private static final CsvCodec<String> STRING = new CsvCodec<String>() {
        public String toString(String t) {
            return t;
        }

        public String fromString(String s) {
        	if(TableDataInitializer.NULL_STRING_ESCAPE.equals(s)) {
        		return null;
        	}
            return s;
        }
    };
    private static final CsvCodec<Integer> I = new CsvCodec<Integer>() {
        public String toString(Integer t) {
            return String.valueOf(t);
        }

        public Integer fromString(String s) {
            if (s == null || s.length() == 0)
                return 0;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Integer.parseInt(s);
        }
    };
    private static final CsvCodec<Boolean> Z = new CsvCodec<Boolean>() {
        public String toString(Boolean t) {
            return String.valueOf(t);
        }

        public Boolean fromString(String s) {
            if (s == null || s.length() == 0)
                return false;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Boolean.parseBoolean(s);
        }
    };
    private static final CsvCodec<Byte> B = new CsvCodec<Byte>() {
        public String toString(Byte t) {
            return String.valueOf(t);
        }

        public Byte fromString(String s) {
            if (s == null || s.length() == 0)
                return 0;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Byte.parseByte(s);
        }
    };
    private final static CsvCodec<Short> S = new CsvCodec<Short>() {
        public String toString(Short t) {
            return String.valueOf(t);
        }

        public Short fromString(String s) {
            if (s == null || s.length() == 0)
                return 0;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Short.parseShort(s);
        }
    };
    private static final CsvCodec<Double> D = new CsvCodec<Double>() {
        public String toString(Double t) {
            return String.valueOf(t);
        }

        public Double fromString(String s) {
            if (s == null || s.length() == 0)
                return 0D;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Double.parseDouble(s);
        }
    };
    private static final CsvCodec<Long> L = new CsvCodec<Long>() {
        public String toString(Long t) {
            return String.valueOf(t);
        }

        public Long fromString(String s) {
            if (s == null || s.length() == 0)
                return 0L;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Long.parseLong(s);
        }
    };
    private static final CsvCodec<Float> F = new CsvCodec<Float>() {
        public String toString(Float t) {
            return String.valueOf(t);
        }

        public Float fromString(String s) {
            if (s == null || s.length() == 0)
                return 0F;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return Float.parseFloat(s);
        }
    };
    private static final CsvCodec<Character> C = new CsvCodec<Character>() {
        public String toString(Character t) {
            return String.valueOf(t);
        }

        public Character fromString(String s) {
            if (s == null || s.length() == 0)
                return 0;
            if("null".equalsIgnoreCase(s)){
            	return null;
            }
            return s.charAt(0);
        }
    };
    private static final CsvCodec<Date> uDate = new CsvCodec<Date>() {
        public String toString(Date t) {
            if (t == null)
                return "";
            return String.valueOf(t.getTime());
        }

        public Date fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return new Date(Long.parseLong(s));
        }
    };
    private static final CsvCodec<java.sql.Date> sDate = new CsvCodec<java.sql.Date>() {
        public String toString(java.sql.Date t) {
            if (t == null)
                return "";
            return DateFormats.DATE_CS.format(t);
        }

        public java.sql.Date fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return new java.sql.Date(DateFormats.DATE_CS.parse(s).getTime());
        }
    };
    private static final CsvCodec<java.sql.Time> sTime = new CsvCodec<java.sql.Time>() {
        public String toString(java.sql.Time t) {
            if (t == null)
                return "";
            return String.valueOf(t.getTime());
        }

        public java.sql.Time fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return new java.sql.Time(Long.parseLong(s));
        }
    };
    private static final CsvCodec<Timestamp> TIMESTAMP = new CsvCodec<Timestamp>() {
        public String toString(Timestamp t) {
            if (t == null)
                return "";
            return String.valueOf(t.getTime());
        }

        public Timestamp fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return new Timestamp(Long.parseLong(s));
        }
    };
    
    private static final CsvCodec<Instant> sInstant = new CsvCodec<Instant>() {
        public String toString(Instant t) {
            if (t == null)
                return "";
            return String.valueOf(t.toEpochMilli());
        }

        public Instant fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return Instant.ofEpochMilli(Long.parseLong(s));
        }
    };
    private static final CsvCodec<LocalDate> sLocalDate = new CsvCodec<LocalDate>() {
        public String toString(LocalDate t) {
            if (t == null)
                return "";
            return String.valueOf(t.toEpochDay());
        }

        public LocalDate fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return LocalDate.ofEpochDay(Long.parseLong(s));
        }
    };
    private static final CsvCodec<LocalTime> sLocalTime = new CsvCodec<LocalTime>() {
        public String toString(LocalTime t) {
            if (t == null)
                return "";
            return String.valueOf(t.toNanoOfDay());
        }

        public LocalTime fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return LocalTime.ofNanoOfDay(Long.parseLong(s));
        }
    };
    private static final CsvCodec<LocalDateTime> sLocalDateTime = new CsvCodec<LocalDateTime>() {
        public String toString(LocalDateTime t) {
            if (t == null)
                return "";
            return DateFormats.TIME_STAMP_CS.format(t);
        }

        public LocalDateTime fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return LocalDateTime.parse(s,DateFormats.TIME_STAMP_CS.df);
        }
    };
    
    private static final CsvCodec<byte[]> BIN = new CsvCodec<byte[]>() {
        public String toString(byte[] t) {
            if (t == null)
                return "";
            return JefBase64.encode(t);
        }

        public byte[] fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return JefBase64.decode(s);
        }
    };
    
    private static final CsvCodec<Serializable> OTHER = new CsvCodec<Serializable>() {
        public String toString(Serializable t) {
            return JefBase64.encode(IOUtils.serialize(t));
        }

        public Serializable fromString(String s) {
            if (s == null || s.length() == 0)
                return null;
            return (Serializable) IOUtils.deserialize(JefBase64.decode(s));
        }
    };

    private static final Map<Type, CsvCodec<?>> CACHE = new HashMap<Type, CsvCodec<?>>();
    static {
        init();
    }

    private static void init() {
        CACHE.put(String.class, STRING);

        CACHE.put(Integer.class, I);
        CACHE.put(Integer.TYPE, I);

        CACHE.put(Short.class, S);
        CACHE.put(Short.TYPE, S);

        CACHE.put(Long.class, L);
        CACHE.put(Long.TYPE, L);

        CACHE.put(Float.class, F);
        CACHE.put(Float.TYPE, F);

        CACHE.put(Double.class, D);
        CACHE.put(Double.TYPE, D);

        CACHE.put(Character.class, C);
        CACHE.put(Character.TYPE, C);

        CACHE.put(Byte.class, B);
        CACHE.put(Byte.TYPE, B);

        CACHE.put(Boolean.class, Z);
        CACHE.put(Boolean.TYPE, Z);

        CACHE.put(byte[].class, BIN);
        CACHE.put(Date.class, uDate);
        CACHE.put(java.sql.Date.class, sDate);
        CACHE.put(java.sql.Time.class, sTime);
        CACHE.put(java.sql.Timestamp.class, TIMESTAMP);
        CACHE.put(Instant.class, sInstant);
        CACHE.put(LocalDate.class, sLocalDate);
        CACHE.put(LocalTime.class, sLocalTime);
        CACHE.put(LocalDateTime.class, sLocalDateTime);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String toString(Object obj, Type type) {
        if (type instanceof Class<?>) {
            Class<?> clz = (Class<?>) type;
            if (clz.isEnum()) {
                return obj==null? "":((Enum<?>) obj).name();
            }
        }
        CsvCodec codec = CACHE.get(type);
        if (codec == null) {
        	if(obj ==null) {
        		return "";
        	}else if (obj instanceof Serializable) {
                return OTHER.toString((Serializable) obj);
            } else {
                throw new UnsupportedOperationException("Object to String error, type " + type + " was not supported.");
            }
        }
        return codec.toString(obj);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object fromString(String s, Type type) {
        if (type instanceof Class<?>) {
            Class<?> clz = (Class<?>) type;
            if (clz.isEnum()) {
            	if(StringUtils.isEmpty(s)) {
            		return null;
            	}
                return Enum.valueOf((Class<Enum>)clz, s);
            }
        }
        CsvCodec<?> codec = CACHE.get(type);
        if (codec == null) {
            return OTHER.fromString(s);
        }
        return codec.fromString(s);
    }
}
