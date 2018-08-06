package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * 强制该方法必须条件为用户在线的注解
 * com.idleroom.web.controller包内有效
 * value  用户类型，进行权限控制，user对象要有该字段，进行权限控制
 * @author Unrestraint
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnLine {
	String value() default "/login";
	String type() default ""; //UserEntity 中的type
}
