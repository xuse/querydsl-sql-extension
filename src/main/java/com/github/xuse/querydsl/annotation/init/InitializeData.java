package com.github.xuse.querydsl.annotation.init;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于定制数据初始化的行为
 * 
 * @author jiyi
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface InitializeData {
    /**
     * 配置一个资源文件名，记录了所有的初始化记录值 如果不指定，默认使用 classpath:/{classname}+全局扩展名 例如：配置为
     * /class1.txt 。
     */
    String value() default "";

    /**
     * 启用该表的自动初始化特性，可配置成false禁用
     */
    boolean enable() default true;
    
    /**
     * 如果合并不是按主键进行，这里填写表的业务主键（name of java field）
     */
    String[] mergeKeys() default {};

    /**
     * 自增键使用记录中的值，不使用数据库的自增编号。
     * 注意，某些数据库可能不支持此特性。
     */
    boolean manualSequence() default false;

    /**
     * 数据文件的字符集 默认使用全局配置
     */
    String charset() default "UTF-8";

    /**
     * 确认资源文件必需存在，如果资源不存在将抛出异常
     */
    boolean ensureFileExists() default true;

    /**
     * 支持指定一个SQL脚本文件，执行该SQL来更新数据库。 一旦使用该功能，value()的CSV文件功能将失效。
     */
    String sqlFile() default "";
}
