package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户登录注解
 * com.idleroom.web.controller包内有效
 * 登录方法必须返回ResultBean<User>或ResultBean<null>对象
 * @author Unrestraint
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogIn{
}
