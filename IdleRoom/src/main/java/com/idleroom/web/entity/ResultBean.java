package com.idleroom.web.entity;
import java.io.Serializable;

import com.idleroom.web.exception.BaseException;
public class ResultBean<T> implements Serializable {
	public static final int SUCCESS = 0;
	public static final int FAIL = 500;
	private int status = SUCCESS;
	private String msg = "success";
	public T data;
	public ResultBean(){}
	
	public ResultBean(T data){
		this.data=data;
	}
	
	public ResultBean(Throwable e){
		this(FAIL,e.getMessage());
	}
	public ResultBean(BaseException e){
		this(e.getCode(),e.getMsg());
	}
	
	public ResultBean(int code,String msg){
		this.status = code;
		this.msg = msg;
	}
	public int getStatus() {
		return this.status;
	}
	public String getMsg() {
		return this.msg;
	}
	public T getData() {
		return this.data;
	}
}
