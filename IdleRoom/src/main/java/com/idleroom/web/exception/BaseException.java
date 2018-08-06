package com.idleroom.web.exception;

public class BaseException extends RuntimeException{
	protected int code;
	protected String msg;
	public BaseException(int code,String msg){
		super(msg);
		this.code=code;
		this.msg=msg;
	}
	public int getCode(){
		return code;
	}
	public String getMsg(){
		return msg;
	}
}
