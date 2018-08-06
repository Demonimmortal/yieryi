package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 强制用户是不在线用户
 * value 当用户在线时，跳转URL 默认为/
 * @author Unrestraint
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotOnLine {
	String value() default "/";
}
