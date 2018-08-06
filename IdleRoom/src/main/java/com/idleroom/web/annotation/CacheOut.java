package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 移除缓存注解 （注：与Cache、CacheAfter注解相斥）
 *  value  主键前缀   默认为cache
 *  key   从参数列表中获取的相同value的key，默认为参数名为id的参数，形成 value:key 这样的主键
 * @author Unrestraint
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheOut {
	String value() default "cache";
	String key() default "id";
}
