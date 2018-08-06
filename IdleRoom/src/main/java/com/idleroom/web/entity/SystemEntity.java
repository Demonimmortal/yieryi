package com.idleroom.web.entity;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class SystemEntity implements Serializable {
    public static class STATUS{
        public static final String WAIT = "待审核";
        public static final String CLOSE= "已审核";
        public static final String DELETE="已删除";
    }

    @Id
    public String id; //消息id
    public String target; //举报id 如果是商品就是 gid 用户 uid
    public String who;  //被举报人
    public String type; //举报类型
    public String text; //举报原因
    public String informer; //举报人
    public Date   date;  //时间
    public String status;  //状态
    public String admin; //处理人
}
