package com.idleroom.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import com.idleroom.web.entity.ResultBean;
import com.idleroom.web.exception.BaseSystemException;
import com.idleroom.web.exception.BaseUserException;

import java.util.NoSuchElementException;

/**
 * 全局异常处理
 * 1.BaseUserException 用户错误直接返回给用户
 * 2.BaseSystemException 系统错误提醒用户，并打印日志 500
 * 3.Exception 系统运行错误打印日志  500
 * 4,NoSuchElementException 数据库资源未找到 404
 * 5.严重错误，该错误手动抛出
 * @author Unrestraint
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     *
     */
    public static void seriousError(Throwable e){
        log.error("系统发生严重错误，需要紧急处理：",e);
    }

    /**
     * 对于该资源如用户、商品根据id未找到
     */
	@ExceptionHandler(NoSuchElementException.class)
    public Object noSuchElementExceptionHandler(HttpServletRequest request,final  NoSuchElementException e){
	    log.info("not found :",e);
	    if(request.getMethod().equals("GET")){
            ModelAndView view =new ModelAndView();
            view.setViewName("error/404.html");
            return view;
        }else{
	        return  new ResultBean<>(404,e.getMessage());
        }
    }
    /**
     * 数据校验出错异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Object constraintViolationExceptionHandler(HttpServletRequest request,final ConstraintViolationException e){
        log.info("validation :",e);
        if(request.getMethod().equals("GET")){
            ModelAndView view =new ModelAndView();
            view.setViewName("error/500.html");
            return view;
        }else{
            return new ResultBean<>(1004,"数据校验未通过！"+e.getMessage());
        }
    }
	/**
	 * @return 所有请求类型均返回json数据
	 */
	@ExceptionHandler(BaseUserException.class)
	public ResultBean<?> userExceptionHandler(final BaseUserException e) {
		log.info("user:",e);
		
        return new ResultBean<>(e);
    }
	/**
	 * @return 对于GET请求 返回error/500.html界面，对于其他请求返回json数据
	 */
	@ExceptionHandler(BaseSystemException.class)
	public Object systemExceptionHandler(HttpServletRequest request,final BaseSystemException e){
		log.error("System:", e);
		
		if(request.getMethod().equals("GET")){
			ModelAndView view =new ModelAndView();
			view.setViewName("error/500.html");
			return view;
		}else{
			return new ResultBean<>(e);
			//return new ResultBean<>(500,"服务器发生错误！");
		}
	}
	/**
	 * @return 对于GET请求 返回error/500.html界面，对于其他请求返回json数据
	 */
	@ExceptionHandler(Exception.class)
	public Object runtimeExceptionHandler(HttpServletRequest request,final Exception e){
		log.error("runtime:",e);
		
		if(request.getMethod().equals("GET")){

			ModelAndView view =new ModelAndView();
			view.setViewName("error/500.html");
			return view;
		}else{
			return new ResultBean<>(500,"服务器发生错误！"+e.getMessage());
		}
	}
}
