package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 *
 * 当函数运行完毕后对结果进行缓存，如果为null则不缓存（注：与CacheOut,Cache注解相斥）
 *
 *  value  主键前缀   默认为cache
 *  key   从参数列表中获取的相同value的key，默认为参数名为id的参数，形成 value:key 这样的主键
 *  expire 过期时间   默认 2
 *  timeUnit 过期时间单位 默认 小时
 *
 *  @author Unrestraint
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheAfter {
    String value() default "cache";
    String key() default "id";
    long expire() default 2;
    TimeUnit timeUnit() default TimeUnit.HOURS;
}
