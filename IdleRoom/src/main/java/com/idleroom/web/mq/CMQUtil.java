package com.idleroom.web.mq;

import com.idleroom.web.util.Json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.idleroom.web.util.cmq.Account;
import com.idleroom.web.util.cmq.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * CMQ消息队列
 * <b>注：发送实体类对象使用反射获取public的get方法所以必须有get方法，否则必须自己实现转换操作</b>
 * <b>生成JSONObject进行传输</b>
 * @author Unrestraint
 */
@Component
@Configuration
@ConfigurationProperties(prefix="cmq")
public class CMQUtil {
    private static final Logger log = LoggerFactory.getLogger(CMQUtil.class);
	 String id;
	 String key;
	 String point;
	 String queueName;
	 Account account ;

     public void setId(String id) {
        this.id = id;
    }
     public void setKey(String key) {
        this.key = key;
    }
     public void setPoint(String point) {
        this.point = point;
    }
     public void setQueueName(String queueName){
        this.queueName=queueName;
    }
     public Queue getQueue(){
		 return account.getQueue(queueName);
	 }

    /**
     * 注: 实体类必须有get方法
     * 注：发送错误时抛 RuntimeException 由 GlobalExceptionHandler 捕获
     * @param HANDLE  CMQMessageHandler.HANDLE 类的静态属性
     * @param o  需要发送的JSONObject对象
     * @return
     */
	 public String sendMessage(Object o,String HANDLE){
         JSONObject data=null;
         try {
             //转换为JSONObject对象
             if(o instanceof JSONObject){
                 data = (JSONObject)o;
             }else{
                 data = new JSONObject(o);
             }
             //
             JSONObject jsonObject = new JSONObject();
             jsonObject.put("handle",HANDLE);
             jsonObject.put("data",data);
             log.info("发送消息内容"+jsonObject.toString());

             return this.getQueue().sendMessage(jsonObject.toString());
         } catch (Exception e) {
             if(data!=null)
                log.error(data.toString());
             log.error(HANDLE);
             throw new RuntimeException(e);
         }
     }

	 @Bean(name = "cmq")
	 public CMQUtil get(CMQUtil cmq){
         log.info("创建消息队列bean ：{} {} {}",cmq.point,cmq.id,cmq.key);
         cmq.account = new Account(point,id,key);
         return cmq;
     }

}
