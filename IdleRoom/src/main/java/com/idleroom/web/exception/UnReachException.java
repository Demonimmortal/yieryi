package com.idleroom.web.exception;

public class UnReachException extends BaseSystemException{
	public UnReachException(){
		super(1001,"服务器内部错误: un reach exception!");
	}
}
