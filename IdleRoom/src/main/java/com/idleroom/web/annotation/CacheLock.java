package com.idleroom.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


/**
 * 分布式锁，根据参数列表id 使分布式环境下对相同主键的元素顺序执行
 * 如果超时返回 CacheLockTimeOutException
 *
 * 使用redis实现的分布式锁，该分布式锁通过轮询redis的key实现
 *  redis集群每一个服务器分配一定量的插槽，并且服务器是单线程的具有的线程安全，利用setIfAbsent实现分布式锁
 *
 *  value  主键前缀   默认为lock
 *  key   从参数列表中获取的相同value的key，默认为参数名为id的参数，形成 value:key 这样的主键
 *  expire 过期时间  默认 30   不大于0时持续获取锁直到主动释放，大于0时防止因为系统崩溃而导致迟迟不释放形成死锁
 *  timeOut 轮询锁的时间  默认 10  //如果超时还未获取锁则返回 CacheLockTimeOutException
 *  waitTime 轮询间隔时间 默认 500 单位ms
 *  timeUnit 过期时间单位 默认 秒
 *  freeAfter 释放在函数结束后释放锁，当为false时，不会释放锁，注意锁有效期，可用以解决重复提交问题
 * @author Unrestraint
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheLock {
    String value() default "lock";     //锁前缀
    String key() default "id";         //锁后缀
    long expire() default 60;          //锁时长
    long timeOut() default 10;         //锁轮询时长
    long waitTime() default 500;       //轮询间隔
    TimeUnit timeUnit() default TimeUnit.SECONDS;  //时长单位
    boolean freeAfter() default true;  //是否在函数执行完毕后释放锁
}
