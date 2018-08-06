package com.idleroom.web.aspect;

import com.idleroom.web.exception.CacheLockTimeOutException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.exception.UnReachException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 分布式锁，分布式锁的AOP优先级最高
 *
 * @author Unrestraint
 *
 */
@Aspect
@Order(1)
@Component
public class CacheLockAspect {

    public static Logger log = LoggerFactory.getLogger(CacheLockAspect.class);
    @Autowired
    private StringRedisTemplate lockRedisTemplate;

    @Around("@annotation(com.idleroom.web.annotation.CacheLock)")
    public Object aroundCacheLock(ProceedingJoinPoint pjp) throws Throwable{
        CacheLock cacheLock = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(CacheLock.class);

        if(cacheLock.waitTime()<=0||cacheLock.timeOut()<0)
            throw new RuntimeException("轮询间隔时间不能小于等于0 或 轮询时长不能小于0 ！");

        if(cacheLock!=null){
            String cacheKey = CacheKeyGenerator.getCacheKey(pjp,cacheLock.value(),cacheLock.key());

            log.info("尝试获取锁 :"+cacheKey);
            //尝试获取锁
            Boolean success = lockRedisTemplate.opsForValue().setIfAbsent(cacheKey,"1");
            long timeOut=0;
            while( !success && timeOut<cacheLock.timeOut()*1000){
                Thread.sleep(cacheLock.waitTime());
                success = lockRedisTemplate.opsForValue().setIfAbsent(cacheKey,"1");
                timeOut+= cacheLock.waitTime();
            }
            //轮询超时，抛出异常
            if(!success){
                throw new CacheLockTimeOutException();
            }
            log.info("成功获取锁："+cacheKey);
            //当获取到锁后先设置锁的有效时间，expire <=0 时 有效时间为永久
            if(cacheLock.expire()>0)
                lockRedisTemplate.expire(cacheKey,cacheLock.expire(),cacheLock.timeUnit());

            try{
                return pjp.proceed();
            }catch (Throwable e){
                throw e;
            }finally {
                //当函数执行完毕，释放锁
                if(cacheLock.freeAfter()){
                    log.info("释放锁："+cacheKey);
                    lockRedisTemplate.delete(cacheKey);
                }
            }
        }
        throw new UnReachException();
    }
}
