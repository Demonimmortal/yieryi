package com.idleroom.web.exception;

public class UnPermissionException extends BaseUserException {
	public UnPermissionException(){
		super(1502,"拒绝执行，用户权限不够！");
	}
}
