package com.idleroom.web.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class GoodEntity implements Serializable{
	public static class TARGET{
		public static final String SELL="出售";
		public static final String WANT="求购";
	}
	public static class STATUS{
	    public static final String SELL="出售";
	    public static final String SOLD="出售成功";
	    public static final String OFF="下架"; //正在出售的商品下架
	    public static final String BARGAIN="正在交易";

        //public static final String CANCEL="取消求购";
        //public static final String WANTED="求购成功";
		public static final String WANT="求购";
	    public static final String DELETE="删除";
    }
	@Id
	public String id;
	public String title;
	public String target;
	public String type;
	public boolean _1;
	public boolean _2;
	public float  price;
	public String status;
	public String info;
	public Date   date;
	public String seller;
	public String buyer;
	public int	  click;
	public List<String> pic;
	public List<List<Comment>> comment;
}
