package com.github.xuse.querydsl.datatype.jmx;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.github.xuse.querydsl.datatype.jmx.management.MBeanRegistration;
import com.github.xuse.querydsl.datatype.jmx.management.ObjectNameBuilder;
import com.github.xuse.querydsl.datatype.util.Invoker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MBeanRegister {

    public static final String JMX_HTTP_PORT = "jmx.http.port";

    static {
        registerHtmlAdaptor();
    }

    public static void registerMBean(Object mbean, String packageName, String name) throws Exception {
        new MBeanRegistration(mbean, new ObjectName(packageName + ":name=" + name)).register();
    }

    public static void register(Object mbean) {
        try {
            ObjectName objectName = new ObjectNameBuilder(mbean).build();
            new MBeanRegistration(mbean, objectName).register();

        } catch (Exception e) {
            log.error("",e);
        }
    }

    public static void unregister(Object mbean) {
        try {
            ObjectName objectName = new ObjectNameBuilder(mbean).build();
            new MBeanRegistration(mbean, objectName).unregister();

        } catch (Exception e) {
        	log.error("",e);
        }
    }

    private static void registerHtmlAdaptor() {
        try {
            String port = System.getProperty(JMX_HTTP_PORT, "8888");
            ObjectName adapterName = new ObjectName("JMX:name=htmladapter,port=" + port);
            Class<?> clz = Invoker.call(()->Class.forName("com.sun.jdmk.comm.HtmlAdaptorServer")).defaultValue(null).invoke();
            if(clz==null) {
            	log.warn("Class com.sun.jdmk.comm.HtmlAdaptorServer not found.");
            	return;
            }
            Constructor<?> constructor = clz.getConstructor(int.class);
            Object jmx = constructor.newInstance(Integer.valueOf(port));
            Method method=clz.getDeclaredMethod("start");
            method.invoke(jmx);
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            mBeanServer.registerMBean(jmx, adapterName);
			log.info("HtmlAdaptorServer Started at " + port);
        } catch (Exception e) {
        	log.error("",e);
        }
    }
}
