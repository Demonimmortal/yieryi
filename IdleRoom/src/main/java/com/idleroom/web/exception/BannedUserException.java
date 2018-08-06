package com.idleroom.web.exception;

public class BannedUserException extends BaseUserException {
    public BannedUserException() {
        super(1063, "用户被封禁，禁止登录");
    }
}
