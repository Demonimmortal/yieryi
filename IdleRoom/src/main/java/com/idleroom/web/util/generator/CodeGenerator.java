package com.idleroom.web.util.generator;

/**
 * 验证码生成器
 * 
 * @author Unrestraint
 *
 */
public class CodeGenerator {
	public static String getCode(){
		return String.valueOf(100000+(long)(Math.random()*899999));
	}
}
