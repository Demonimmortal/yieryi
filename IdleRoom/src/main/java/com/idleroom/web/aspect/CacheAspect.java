package com.idleroom.web.aspect;

import java.io.Serializable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.idleroom.web.annotation.Cache;
import com.idleroom.web.annotation.CacheOut;
import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.exception.UnReachException;
import org.springframework.stereotype.Component;

/**
 * 
 * 自定义缓存切面,注:三个注解不能同时使用，如果同时使用由于aop相同切面按函数名排序则顺序为@Cache,@CacheAfter,@CacheOut(未测试）
 * 
 * @author Unrestraint
 *
 */
@Aspect
@Component
public class CacheAspect {

    public  static Logger log = LoggerFactory.getLogger(CacheAspect.class);

    @Autowired
	private RedisTemplate<String, Serializable>  template;

    /**
     * 切 Cache 注解
     */
	@Around("@annotation(com.idleroom.web.annotation.Cache)")
	public Object aroundCache(ProceedingJoinPoint pjp) throws Throwable{
		Cache cache = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(Cache.class);

		if(cache!=null){
			String cacheKey = CacheKeyGenerator.getCacheKey(pjp,cache.value(),cache.key());

			log.info("查找缓存："+cacheKey);
			Serializable o;
			//有缓存
			if((o=template.opsForValue().get(cacheKey)) != null){
				return o;
			//无缓存
			}else{
				log.info("查数据库："+cacheKey);
				o = (Serializable) pjp.proceed();
				if(o!=null){
					template.opsForValue().set(cacheKey, o, cache.expire(), cache.timeUnit());
				}
				return o;
			}
		}
		throw new UnReachException();
	}

    /**
     * 切 CacheOut 注解
     */
	@Around("@annotation(com.idleroom.web.annotation.CacheOut)")
	public Object aroundCacheOut(ProceedingJoinPoint pjp) throws Throwable{
		CacheOut cacheOut = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(CacheOut.class);;
		
		if(cacheOut!=null){
			String cacheKey = CacheKeyGenerator.getCacheKey(pjp,cacheOut.value(),cacheOut.key());
			Object o = pjp.proceed();
			template.delete(cacheKey);
			return o;
		}
		throw new UnReachException();
	}

    /**
     * 切 CacheAfter 注解
     */
	@Around("@annotation(com.idleroom.web.annotation.CacheAfter)")
    public Object aroundCacheAfter(ProceedingJoinPoint pjp) throws Throwable{
        CacheAfter cacheAfter = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(CacheAfter.class);

        if(cacheAfter!=null){
            String cacheKey = CacheKeyGenerator.getCacheKey(pjp,cacheAfter.value(),cacheAfter.key());
            Serializable o = (Serializable)pjp.proceed();
            if(o!=null){
                template.opsForValue().set(cacheKey,o,cacheAfter.expire(),cacheAfter.timeUnit());
            }
            return  o;
        }
        throw new UnReachException();
    }

}
