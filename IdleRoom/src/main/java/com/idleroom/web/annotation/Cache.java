package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
/**
 * 自定义缓存注解，当有缓存时，返回缓存的内容不执行方法；没有则执行方法并将结果缓存，如果为null则不缓存（注：与CacheOut,CacheAfter注解相斥）
 *
 * value 主键前缀   默认为cache
 * key   缓存id 将会在redis中形成value:id的主键
 * expire 缓存有效时间 默认 2
 * timeUnit 缓存有效单位 默认 小时
 * @author Unrestraint
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
	String value() default "cache";
	String key() default "id";
	long expire() default 2;
	TimeUnit timeUnit() default TimeUnit.HOURS;
}