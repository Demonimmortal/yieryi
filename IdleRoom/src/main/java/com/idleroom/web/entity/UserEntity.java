package com.idleroom.web.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
public class UserEntity implements Serializable{

	public static class TYPE{
        public static final String GENERAL = "普通";
        public static final String ADMIN = "管理员";
        public static final String BAN = "封禁";
    }

	@Id
	public String id;
	public String constType;
	public String name;
	public String pwd;
	public String phone;
	public String mail;
	public String qq;
	public String wechat;
	public String sex;
	public String type;
	public int	  credit;//信用
	public String sign;
	public String campus;
	public Date   date;//注册日期
	public String pic;

	public MessageCollections msg;
	public GoodCollections    good;

    /**
     * 用户消息组
     */
    public static class MessageCollections implements Serializable{
	    public List<Message> sys;
	    public int sysCount;
	    public List<Message> user;
	    public int userCount;
    }
	/**
	 * sell 	用户正在出售的列表
	 * sold 	非正在出售的列表 包括 交易成功、下架
	 * want 	用户正在求购的列表
	 * buy 		用户点击购买的列表 
	 * bought 	商品交易成功的列表
	 * like		用户收藏列表
	 * 
	 * @author Unrestraint
	 *
	 */
	public static class GoodCollections implements Serializable{
		public List<String>  sell;
		public List<String>  sold;
		public List<String>  want;
		public List<String>  buy;
		public List<String>  bought;
		public List<String>  like;
	}
}
