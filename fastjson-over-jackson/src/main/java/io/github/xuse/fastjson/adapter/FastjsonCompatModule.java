package io.github.xuse.fastjson.adapter;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Jackson Module 提供 fastjson 兼容能力。
 */
public class FastjsonCompatModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public FastjsonCompatModule() {
        super("FastjsonCompat", Version.unknownVersion());
        addDeserializer(Date.class, new FlexibleDateDeserializer());
        setSerializerModifier(new SerializeFilterModifier());
        setDeserializerModifier(new DeserializeFilterModifier());
    }

    private static class SerializeFilterModifier extends BeanSerializerModifier {
        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
            Iterator<BeanPropertyWriter> it = beanProperties.iterator();
            while (it.hasNext()) {
                BeanPropertyWriter writer = it.next();
                // 属性可能已被重命名，优先用内部名（Java 字段名）查找
                Field field = findField(beanDesc.getBeanClass(), writer.getMember().getName());
                if (field == null) {
                    field = findField(beanDesc.getBeanClass(), writer.getName());
                }
                if (!isSerialize(field)) {
                    it.remove();
                }
            }
            return beanProperties;
        }
    }

    private static class DeserializeFilterModifier extends BeanDeserializerModifier {
        @Override
        public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
            List<BeanPropertyDefinition> result = new ArrayList<>(propDefs.size());
            for (BeanPropertyDefinition prop : propDefs) {
                Field field = findField(beanDesc.getBeanClass(), prop.getInternalName());
                if (!isDeserialize(field)) {
                    continue;
                }
                result.add(prop);
            }
            return result;
        }
    }

    /**
     * 判断字段是否参与序列化。自定义 @JSONField 优先，其次是原生 fastjson 注解。
     */
    private static boolean isSerialize(Field field) {
        if (field == null) {
            return true;
        }
        JSONField ann = field.getAnnotation(JSONField.class);
        if (ann != null) {
            return ann.serialize();
        }
        FastjsonFieldAnnotationProxy.FieldValues nv = FastjsonFieldAnnotationProxy.findNativeAnnotation(field);
        return nv == null || nv.serialize;
    }

    /**
     * 判断字段是否参与反序列化。自定义 @JSONField 优先，其次是原生 fastjson 注解。
     */
    private static boolean isDeserialize(Field field) {
        if (field == null) {
            return true;
        }
        JSONField ann = field.getAnnotation(JSONField.class);
        if (ann != null) {
            return ann.deserialize();
        }
        FastjsonFieldAnnotationProxy.FieldValues nv = FastjsonFieldAnnotationProxy.findNativeAnnotation(field);
        return nv == null || nv.deserialize;
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        if (fieldName == null) {
            return null;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
