package com.idleroom.web.util;

import com.idleroom.web.entity.ResultBean;
import com.idleroom.web.exception.BaseException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于享元模式的 ResultBean 工厂
 * @author Unrestraint
 */
public class ResultBeanFactory {
    private  static final ConcurrentHashMap map = new ConcurrentHashMap();
    static{
        map.put(ResultBean.class,new ResultBean<>());
    }

    public static ResultBean simple(){
        return  (ResultBean)map.get(ResultBean.class);
    }

    public static ResultBean get(Class<? extends BaseException> c){
        Object o = map.get(c);
        if(o==null){
            try {
                o =new ResultBean<>(c.newInstance());
                map.put(c,o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return  (ResultBean)o;
    }
}
