package com.github.xuse.querydsl.jmx;

import static java.lang.String.format;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.github.xuse.querydsl.jmx.JMXOperation.Impact;
import com.github.xuse.querydsl.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 将原本的POJO包装成动态JMXBean
 */
@Slf4j
public class IntrospectedMXBean implements DynamicMBean {

    private final Object mbean;

    private final Class<?> mbeanClass;

    private final Map<String, PropertyDescriptor> propertyDescriptors;

    private final Map<String, Method> operationMethods;

    private final MBeanInfo mbeanInfo;
    
    private final boolean isAutomatic;

    /**
     * @param mbean
     *            a POJO MBean, that should be exposed as a{@link DynamicMBean}
     */
    public IntrospectedMXBean(Object mbean, Class<?> mxInterface)  {
        this.mbean = mbean;
        this.mbeanClass = mxInterface == null ? mbean.getClass() : mxInterface;
        isAutomatic = mbeanClass.getAnnotation(MXBean.class)!=null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(mbeanClass);
            propertyDescriptors = createPropertyDescriptors(beanInfo);
            operationMethods = createOperationMethods(beanInfo);
            mbeanInfo = createMbeanInfo(mbeanClass, propertyDescriptors, operationMethods);
        } catch (IntrospectionException | java.beans.IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param attribute
     *            the attribute whose value is requested
     * @return the reflected value of attribute
     */
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        PropertyDescriptor propertyDescriptor = propertyDescriptors.get(attribute);
        if (propertyDescriptor == null) {
            throw new AttributeNotFoundException(attribute);
        }
        Method getter = propertyDescriptor.getReadMethod();
        if (getter == null) {
            throw new AttributeNotFoundException(format("Getter method for attribute %s of %s", attribute, mbeanClass));
        }
        try {
            return getter.invoke(mbean);
        } catch (Exception e) {
            throw new RuntimeException(format("Unable to obtain value of attribute %s of %s", attribute, mbeanClass));
        }
    }

    /**
     * @param attributeNames
     *            the attribute names whose values are requested
     * @return an attribute list describing each of attributeNames
     */
    public AttributeList getAttributes(String[] attributeNames) {
        AttributeList attributes = new AttributeList(attributeNames.length);
        for (String attributeName : attributeNames) {
            try {
                Attribute attribute = new Attribute(attributeName, getAttribute(attributeName));
                attributes.add(attribute);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        return attributes;
    }

    /**
     * @param attribute
     *            the attribute for which to update the value
     */
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {
        String name = attribute.getName();
        PropertyDescriptor propertyDescriptor = propertyDescriptors.get(name);
        if (propertyDescriptor == null) {
            throw new AttributeNotFoundException(name);
        }
        Method setter = propertyDescriptor.getWriteMethod();
        if (setter == null) {
            throw new AttributeNotFoundException(format("setter method for attribute %s of %s", name, mbeanClass));
        }
        Object value = attribute.getValue();
        try {
            if (!setter.isAccessible()) {
                setter.setAccessible(true);
            }
            setter.invoke(mbean, value);
        } catch (IllegalArgumentException e) {
            throw new InvalidAttributeValueException(String.format("attribute %s, value = (%s)%s, expected (%s)", name,
                    value.getClass().getName(), value, setter.getParameterTypes()[0].getName()));
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e, format("attribute %s of %s, value = (%s)%s", name, mbeanClass, value
                    .getClass().getName(), value));
        } catch (InvocationTargetException e) {
            throw new MBeanException(e, format("attribute %s of %s, value = (%s)%s", name, mbeanClass, value.getClass()
                    .getName(), value));
        }
    }

    /**
     * @param attributes
     *            a list of attributes for which to update the value
     */
    public AttributeList setAttributes(AttributeList attributes) {
        for (Object object : attributes) {
            Attribute attribute = (Attribute) object;
            try {
                setAttribute(attribute);
            } catch (Exception e) {
                // Must be a mistake that the signature doesn't allow throwing exceptions
                throw new IllegalArgumentException(e);
            }
        }
        // It seems like an API mistake that we have to return the attributes
        return attributes;
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
        ReflectionException {
        Method method = operationMethods.get(actionName);
        try {
            return method.invoke(mbean, params);
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /**
     * @param mbeanClass
     *            the class that declares properties and operations
     * @param propertyDescriptors
     *            descriptors for all beans that are explicitly or implicitly annotated as
     *            attributes
     * @param mbean
     *            the annotated POJO MBean
     * @return an MBeanInfo created by introspect the {@code mbean}
     * @throws IntrospectionException
     * @throws javax.management.IntrospectionException
     */
    private MBeanInfo createMbeanInfo(Class<?> mbeanClass, Map<String, PropertyDescriptor> propertyDescriptors,
            Map<String, Method> operationMethods) throws IntrospectionException{
        
        String description = description(mbeanClass);
        final MBeanAttributeInfo[] attributeInfo = createAttributeInfo(propertyDescriptors);
        final MBeanConstructorInfo[] constructorInfo = createConstructorInfo();
        final MBeanOperationInfo[] operationInfo = createOperationInfo(operationMethods);
        final MBeanNotificationInfo[] notificationInfo = createNotificationInfo();
        return new MBeanInfo(name(mbeanClass), description, attributeInfo, constructorInfo, operationInfo,
                notificationInfo);
    }

    private static MBeanNotificationInfo[] createNotificationInfo() {
        return new MBeanNotificationInfo[0];
    }

    /**
     * @return The methods that constitute the operations
     */
    private Map<String, Method> createOperationMethods(BeanInfo beanInfo) {
        Set<Method> allAccessors = allAccessors(beanInfo);
        Map<String, Method> operationMethods = new HashMap<String, Method>();
        for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
            Method method = descriptor.getMethod();
            JMXOperation operationAnnotation = method.getAnnotation(JMXOperation.class);
            if (operationAnnotation != null && allAccessors.contains(method)) {
                throw new IllegalArgumentException(String.format("Accessor method %s is annotated as an @%s", method,
                        JMXOperation.class.getName()));
            }
            boolean autoOperation = (isAutomatic && isPublicInstance(method) && !allAccessors.contains(method));
            if (operationAnnotation != null || autoOperation) {
                String key=name(method);
                Method old = operationMethods.put(key, method);
                if (old != null) {
                    throw new IllegalArgumentException(format("Multiple Operation annotations for operation %s with name %s",
                            old.getDeclaringClass(),method.getName()));
                }
            }
        }
        return operationMethods;
    }

    private static boolean isPublicInstance(Method method) {
        int mod = method.getModifiers();
        return Modifier.isPublic(mod) && !Modifier.isStatic(mod);
    }

    private static Set<Method> allAccessors(BeanInfo beanInfo) {
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Set<Method> accessors = new HashSet<Method>(propertyDescriptors.length * 2);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            addNotNull(accessors, propertyDescriptor.getReadMethod());
            addNotNull(accessors, propertyDescriptor.getWriteMethod());
        }
        return accessors;
    }

    public static <T> void addNotNull(Collection<T> collection, T element) {
        if (element != null) {
            collection.add(element);
        }
    }

    /**
     * @return an MBeanOPerationInfo array that describes the {@link JMXOperation} annotated
     *         methods of the operationMethods
     */
    private static MBeanOperationInfo[] createOperationInfo(Map<String, Method> operationMethods){
        MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[operationMethods.size()];
        int operationIndex = 0;
        for (String actionName : sortedKeys(operationMethods)) {
            Method method = operationMethods.get(actionName);
            JMXOperation annotation = method.getAnnotation(JMXOperation.class);
            MBeanParameterInfo[] signature = createParameterInfo(method);
            Impact impact = annotation == null ? Impact.UNKNOWN : annotation.value();
            int impactValue = impact.impactValue;
            String description = description(method);
            MBeanOperationInfo opInfo = new MBeanOperationInfo(actionName, description, signature, method
                    .getReturnType().getName(), impactValue, null);
            operationInfos[operationIndex++] = opInfo;
        }
        return operationInfos;
    }

    protected static MBeanParameterInfo[] createParameterInfo(Method method) {
        MBeanParameterInfo[] parameters = new MBeanParameterInfo[method.getParameterTypes().length];
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            final String pType = method.getParameterTypes()[parameterIndex].getName();
            // locate parameter annotation
            JMXText mBeanDescription = getParameterAnnotation(method, parameterIndex, JMXText.class);
            String text = mBeanDescription==null? null: mBeanDescription.value();
            final String pName = StringUtils.isBlank(text)
                    ? "p" + (parameterIndex + 1)
                    : text; 
            final String pDesc = (mBeanDescription != null) ? mBeanDescription.description() : null;
            parameters[parameterIndex] = new MBeanParameterInfo(pName, pType, pDesc);
        }
        return parameters;
    }

    private static MBeanConstructorInfo[] createConstructorInfo() {
        return new MBeanConstructorInfo[0];
    }

    /**
     * @return all properties where getter or setter is annotated with {@link JMXAttribute}
     */
    private Map<String, PropertyDescriptor> createPropertyDescriptors(BeanInfo beanInfo){
        Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
            JMXAttribute getterAnnotation = getAnnotation(property.getReadMethod(), JMXAttribute.class);
            JMXAttribute setterAnnotation = getAnnotation(property.getWriteMethod(), JMXAttribute.class);
            if (isAutomatic || getterAnnotation != null || setterAnnotation != null) {
                properties.put(name(property), property);
            }
        }
        return properties;
    }

    /**
     * @param propertyDescriptors
     *            property descriptors that are known to have at least one {@link JMXAttribute}
     *            annotation on its getter or setter method
     * @return MBean attributeInfo instances with getter/setter methods and description according to
     *         annotations
     * @throws IntrospectionException
     */
    private MBeanAttributeInfo[] createAttributeInfo(Map<String, PropertyDescriptor> propertyDescriptors) throws IntrospectionException {
        MBeanAttributeInfo[] infos = new MBeanAttributeInfo[propertyDescriptors.size()];
        int i = 0;
        for (String propertyName : sortedKeys(propertyDescriptors)) {
            PropertyDescriptor property = propertyDescriptors.get(propertyName);
            Method readMethod = property.getReadMethod();
            Method writeMethod = property.getWriteMethod();
            boolean readable = isAutomatic || (null != getAnnotation(readMethod, JMXAttribute.class));
            boolean writable = isAutomatic || (null != getAnnotation(writeMethod, JMXAttribute.class));
            JMXText descriptionAnnotation = getFirstAnnotation(property, JMXText.class, readMethod, writeMethod);
            String description = (descriptionAnnotation != null) ? descriptionAnnotation.description() : null;
            MBeanAttributeInfo info = new MBeanAttributeInfo(propertyName, description, readable ? readMethod
                    : null, writable ? writeMethod : null);
            infos[i++] = info;
        }
        return infos;
    }

    @SuppressWarnings("unchecked")
    public final static <E> E firstNotNull(E... all) {
        if (all != null) {
            for (E element : all) {
                if (element != null) {
                    return element;
                }
            }
        }
        throw new NullPointerException("All null arguments");
    }

    private static <T extends Annotation> T getFirstAnnotation(PropertyDescriptor property, Class<T> annotationClass,
            AccessibleObject... entities) {
        for (AccessibleObject entity : entities) {
            if (entity != null) {
                T annotation = entity.getAnnotation(annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * Find an annotation for a parameter on a method.
     * 
     * @param <A>
     *            The annotation.
     * @param method
     *            The method.
     * @param index
     *            The index (0 .. n-1) of the parameter in the parameters list
     * @param annotationClass
     *            The annotation class
     * @return The annotation, or null
     */
    private static <A extends Annotation> A getParameterAnnotation(Method method, int index, Class<A> annotationClass) {
        for (Annotation a : method.getParameterAnnotations()[index]) {
            if (annotationClass.isInstance(a)) {
                return annotationClass.cast(a);
            }
        }
        return null;
    }

    /**
     * Null safe annotation checker
     * 
     * @param <A>
     * @param element
     *            element or null
     * @param annotationClass
     * @return the annotation, if element is not null and the annotation is present. Otherwise null
     */
    private static <A extends Annotation> A getAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        return (element != null) ? element.getAnnotation(annotationClass) : null;
    }

    private static String description(AnnotatedElement element) {
        JMXText annotation = element.getAnnotation(JMXText.class);
        String explicitValue = (annotation != null) ? annotation.description() : null;
        if (explicitValue != null && !explicitValue.isEmpty()) {
            return explicitValue;
        } else {
            return generatedDescription(element);
        }
    }

    private static String name(PropertyDescriptor property) {
        JMXText anno= null;
        Method reader = property.getReadMethod();
        if (reader != null) {
            anno = reader.getAnnotation(JMXText.class);
        }
        if (anno == null) {
            Method writer = property.getWriteMethod();
            if (writer != null) {
                anno = writer.getAnnotation(JMXText.class);
            }
        }
        String text = anno.value();
        if(StringUtils.isBlank(text)) {
            return property.getName();
        }else {
            return text;    
        }
    }
    
    private static String name(Method element) {
        JMXText annotation = element.getAnnotation(JMXText.class);
        String explicitValue = (annotation != null) ? annotation.value() : null;
        if (explicitValue != null && !explicitValue.isEmpty()) {
            return explicitValue;
        } else {
            return element.getName();
        }
    }
    
    private static String name(Class<?> element) {
        JMXText annotation = element.getAnnotation(JMXText.class);
        String explicitValue = (annotation != null) ? annotation.value() : null;
        if (explicitValue != null && !explicitValue.isEmpty()) {
            return explicitValue;
        } else {
            return element.getName();
        }
    }
    
    public boolean register() {
        return registerWithIdentity(null);
    }
    
    public boolean registerWithIdentity(String identity) {
        if (StringUtils.isEmpty(identity)) {
            identity = "Default";
        }
        try {
            ObjectName mxbeanName = new ObjectName(this.mbeanInfo.getClassName() + ":type=" + identity);
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            if(!mbs.isRegistered(mxbeanName)) {
                mbs.registerMBean(this, mxbeanName);
                return true;
            }
        }catch(MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            log.error("MBean [{}:type={}] error",this.mbeanInfo.getClassName(),identity,e);
        }
        return false;
    }

    private static String generatedDescription(AnnotatedElement element) {
        if (element instanceof Method) {
            Method method = (Method) element;
            return method.getName() + "() of " + method.getDeclaringClass().getSimpleName();
        } else if (element instanceof Class) {
            return "class " + ((Class<?>) element).getName();

        }
        return element.toString();
    }

    /**
     * @param map
     * @return a list of the keys in map, sorted
     */
    private static List<String> sortedKeys(Map<String, ?> map) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        return keys;
    }
}
