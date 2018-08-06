package com.idleroom.web.cache;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * session 管理器
 * 注：如果第一次创建session id 时，同时需要设置session内容，
 * 则必须调用create方法获取sessionId，根据sessionId设置内容，因为cookie并不存在在request中，所以首次不使用id存，会发生频繁创建信息
 * bean单例在这里没法改
 */
@Component
public class SessionManager {
    @Autowired
    private RedisTemplate<String, Serializable> template;

    @Value("${session.key}")
    String key;
    @Value("${session.expire}")
    long expire;

    @Autowired(required=false)
    HttpServletRequest request;
    @Autowired(required=false)
    HttpServletResponse response;

    private String createSessionId(){
        return  UUID.randomUUID().toString();
    }

    public String create(){
        String id = createSessionId();
        Cookie cookie = new Cookie("session",id);
        cookie.setPath("/");
        response.addCookie(cookie);
        return id;
    }

    /**
     * 从cookie中获取sessionid
     * @return
     */
    public String getId(){
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(int i=0;i<cookies.length;i++){
                if ("session".equals(cookies[i].getName())){
                    return cookies[i].getValue();
                }
            }
        }
        return create();
    }


    public Object get(String hk){
        return get(getId(),hk);
    }
    public Object get(String id,String hk){
        return template.opsForHash().get(key+":"+id,hk);
    }

    /**
     * <b>创建session id 时，同时需要设置session内容，需使用create获取的id和set（id,hk,o)方法</b>
     * @param hk
     * @param o
     */
    public void set(String hk,Object o){
        set(getId(),hk,o);
    }

    public void set(String id,String hk,Object o){
        template.opsForHash().put(key+":"+id,hk,o);
        template.expire(key+":"+id,expire,TimeUnit.SECONDS);
    }

    public void clean(){
        clean(getId());
    }
    public void clean(String id){
        template.delete(key+":"+id);
    }

    public void remove(String hk){
        remove(getId(),hk);
    }
    public void remove(String id,String hk){
        template.opsForHash().delete(key+":"+id,hk);
    }

}
