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
                JSONField ann = getFieldAnnotation(beanDesc.getBeanClass(), writer.getName());
                if (ann != null && !ann.serialize()) {
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
                JSONField ann = getFieldAnnotation(beanDesc.getBeanClass(), prop.getInternalName());
                if (ann != null && !ann.deserialize()) {
                    continue;
                }
                result.add(prop);
            }
            return result;
        }
    }

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
