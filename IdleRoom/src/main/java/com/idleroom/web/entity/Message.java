package com.idleroom.web.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户消息对象，由系统发给用户，所有对用户的提示均已消息为实体。
 * mid 消息唯一标识，分布式环境下id需要唯一
 * text 消息内容 可为HTML语句
 * date 消息产生的时间
 * 
 * @author Unrestraint
 *
 */
public class Message implements Serializable{
	/** 消息唯一id */
	public String mid;
	public String title;
	public String text;
	public Date date;
	public boolean read;

	@Override
    public  String toString(){
	    return new StringBuilder().append(" { mid:").append(mid).append(" text:").append(text).append(" date:").append(date).append(" } ").toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
