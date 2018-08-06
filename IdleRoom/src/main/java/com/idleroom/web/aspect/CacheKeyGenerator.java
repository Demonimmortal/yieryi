package com.idleroom.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 缓存主键生成器 value:key
 *
 * @author Unretraint
 */
public class CacheKeyGenerator {

    public static  String getCacheKey(ProceedingJoinPoint pjp, String value, String key){
        String cacheKey = null;
        String[] argsName = ((MethodSignature)pjp.getSignature()).getParameterNames();
        for(int i=0;i<argsName.length;i++){
            if(argsName[i].equals(key)){
                cacheKey=value+":"+String.valueOf(pjp.getArgs()[i]);
                break;
            }
        }
        if(cacheKey==null)
            throw new RuntimeException("参数列表无参数名:"+key);
        return cacheKey;
    }
}
