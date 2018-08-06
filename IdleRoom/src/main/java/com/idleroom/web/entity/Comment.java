package com.idleroom.web.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 用户回复对象，分布式环境下 id 需要唯一
 * 
 * @author Unrestraint
 *
 */
public class Comment implements Serializable{
	/** 回复唯一ID */
	public String cid;
	/** 商品唯一ID */
	public String gid;
	public String text;
	public String title;
	public Date date;
	public String from;
	public String fromName;
	public String to;
	public String toName;

	@Override
    public String toString(){
	    return new StringBuilder().append("{ cid:").append(cid).append(" gid:").append(gid).append(" text:").append(text).append(" title:").append(title)
                .append(" date:").append(date).append(" from:").append(from).append(" fromName:").append(fromName).append(" to:").append(to).append(" toName:").append(toName).append(" } ").toString();
    }

    public String getCid() {
        return cid;
    }

    public String getGid() {
        return gid;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getToName(){return toName;}

    public String getFromName(){return fromName;}
}
