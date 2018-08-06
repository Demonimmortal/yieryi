package com.idleroom.web.exception;

public class UnLoginException extends BaseUserException {
	public UnLoginException() {
		super(1051, "用户未登录！");
	}
}
