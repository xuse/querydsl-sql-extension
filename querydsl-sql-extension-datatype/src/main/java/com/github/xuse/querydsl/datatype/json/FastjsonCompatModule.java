package com.github.xuse.querydsl.datatype.json;

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
 * Jackson Module 提供 fastjson 兼容能力：
 * <ul>
 *   <li>Date 灵活反序列化</li>
 *   <li>@JSONField(serialize=false) — 序列化时过滤</li>
 *   <li>@JSONField(deserialize=false) — 反序列化时过滤</li>
 * </ul>
 */
public class FastjsonCompatModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public FastjsonCompatModule() {
        super("FastjsonCompat", Version.unknownVersion());
        addDeserializer(Date.class, new FlexibleDateDeserializer());
        setSerializerModifier(new SerializeFilterModifier());
        setDeserializerModifier(new DeserializeFilterModifier());
    }

    /**
     * 序列化时过滤 @JSONField(serialize=false) 的字段
     */
    private static class SerializeFilterModifier extends BeanSerializerModifier {
        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
            Iterator<BeanPropertyWriter> it = beanProperties.iterator();
            while (it.hasNext()) {
                BeanPropertyWriter writer = it.next();
                JSONField ann = getFieldAnnotation(beanDesc.getBeanClass(), writer.getName());
                if (ann != null && !ann.serialize()) {
                    it.remove();
                }
            }
            return beanProperties;
        }
    }

    /**
     * 反序列化时过滤 @JSONField(deserialize=false) 的字段
     */
    private static class DeserializeFilterModifier extends BeanDeserializerModifier {
        @Override
        public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
            List<BeanPropertyDefinition> result = new ArrayList<>(propDefs.size());
            for (BeanPropertyDefinition prop : propDefs) {
                JSONField ann = getFieldAnnotation(beanDesc.getBeanClass(), prop.getInternalName());
                if (ann != null && !ann.deserialize()) {
                    continue; // 跳过
                }
                result.add(prop);
            }
            return result;
        }
    }

    /**
     * 从类的声明字段上查找 @JSONField 注解
     */
    private static JSONField getFieldAnnotation(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                return field.getAnnotation(JSONField.class);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
