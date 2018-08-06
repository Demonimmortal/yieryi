package com.idleroom.web.mq;

import com.idleroom.web.controller.GlobalExceptionHandler;
import com.idleroom.web.util.Json.JSONObject;
import com.idleroom.web.util.cmq.CMQServerException;
import com.idleroom.web.util.cmq.Message;
import com.idleroom.web.util.cmq.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * CMQ 消息队列消费者
 ** 不开一个线程无限轮询，由spring boot 调度，防止线程崩溃积压消息
 * @author Unrestraint
 */

@Component
public class CMQConsumer {
    private  static final Logger log = LoggerFactory.getLogger(CMQConsumer.class);

    @Autowired
    CMQMessageHandler handler;

    @Resource(name = "cmq")
    CMQUtil cmq;

    @Scheduled(fixedDelay=3000)
    public void MessageConsumer(){
        //log.info("轮询消息队列");
        Queue queue = cmq.getQueue();
        Message msg=null;

        try {
            while(true){
                msg = queue.receiveMessage(120);
                //接收消息太快了，数据库还没有存，消息已经消费，这里等待100ms
                Thread.sleep(100);
                JSONObject msgBody = new JSONObject(msg.msgBody);

                //如果有消息，提交给CMQMessageHander类，如果没有消息则退出
                log.info("接受消息内容:"+msgBody.toString());
                handler.messageHandler(msgBody);

                queue.deleteMessage(msg.receiptHandle);
            }

        } catch (Exception e) {
           if(e instanceof CMQServerException){
              if( ((CMQServerException) e).getErrorCode()==7000){
                  return ;
              }else{
                  log.error("CMQ:",e);
              }
           }else{
               GlobalExceptionHandler.seriousError(e);
               try {
                   queue.deleteMessage(msg.receiptHandle);
               } catch (Exception e1) {
                   e1.printStackTrace();
               }
           }
        }finally {

        }

    }
}
