package com.idleroom.web.exception;

public class CacheLockTimeOutException extends  BaseSystemException {
    public CacheLockTimeOutException(){
        super(1002,"获取分布式锁超时!");
    }
}
