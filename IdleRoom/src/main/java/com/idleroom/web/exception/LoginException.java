package com.idleroom.web.exception;

public class LoginException extends BaseUserException{
	public LoginException(){
		super(1053,"用户名或密码错误！");
	}
}
