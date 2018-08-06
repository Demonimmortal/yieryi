package com.idleroom.web.exception;

/**
 * 非正常访问错误，该错误发生有两种情况：1.程序逻辑出现问题。2.用户直接通过接口方法，但参数错误
 * 该错误如果抛出至前端，则说明程序一定出现bug。
 * 
 * @author Unrestraint
 *
 */
public class UnusualException extends BaseUserException {
	public UnusualException() {
		super(1054, "非正常访问！");
	}
}
