package com.idleroom.web.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.idleroom.web.cache.SessionManager;
import com.idleroom.web.util.ResultBeanFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.ast.Not;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.idleroom.web.annotation.*;
import com.idleroom.web.entity.ResultBean;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.LoginException;
import com.idleroom.web.exception.UnLoginException;
import com.idleroom.web.exception.UnPermissionException;
import com.idleroom.web.exception.UnReachException;

/**
 * 登录信息会保存在session中，该session存在redis中，构成分布式的session验证
 * 
 * session 中存
 * 	id 		用户id
 *  type  	用户类型
 * 
 * @author Unrestraint
 *
 */
@Aspect
@Component
@Order(1)
public class LoginAspect {

	public static final Logger log = LoggerFactory.getLogger(LoginAspect.class);

	@Autowired(required=false)
	private HttpServletRequest request;
	@Autowired(required=false)
	private HttpServletResponse response;

	@Autowired
	SessionManager session;
	
	@Pointcut("execution(public * com.idleroom.web.controller.*.*(..))&&@annotation(com.idleroom.web.annotation.OnLine)")
	public void OnLine(){}
	//该方法由于和LogIn写在一个切面里，所以不能喝LogIn注解同时使用
	@Pointcut("execution(public * com.idleroom.web.controller.*.*(..))&&@annotation(com.idleroom.web.annotation.NotOnLine)")
	public void NotOnLine(){}
	
	@Pointcut("execution(public * com.idleroom.web.controller.*.*(..))&&@annotation(com.idleroom.web.annotation.LogIn)")
	public void LogIn(){}
	@Pointcut("execution(public * com.idleroom.web.controller.*.*(..))&&@annotation(com.idleroom.web.annotation.LogOut)")
	public void LogOut(){}
	
	/**
	 * 统一在线、权限验证环绕方法增强
	 */
	@Around("OnLine()")
	public Object aroundOnLine(ProceedingJoinPoint pjp) throws Throwable{
		log.info("on line check");
		//获取方法信息
		MethodSignature signature = (MethodSignature)pjp.getSignature();
		Method method= signature.getMethod();
		OnLine online;
		//获取注解
		if((online=method.getAnnotation(OnLine.class))!= null){
			//HttpSession session =request.getSession();
			//判断是否登录
			if(session.get("id")!=null){
				//判断权限是否满足
				if(online.type().isEmpty()||online.type().equals(session.get("type"))){
					//鉴权通过，执行方法
					return pjp.proceed();
				}else{
					//鉴权未通过，get请求直接回主页
					if(request.getMethod().equals("GET")){
						response.sendRedirect("/");
						return null;
					}
					//其他请求，抛出异常
					throw new UnPermissionException();
				}
			}else{

				//未登录，GET请求将访问url重定向，其他请求抛出错误
				if(request.getMethod().equals("GET")){
					response.sendRedirect(online.value()+"?URL="+request.getRequestURI());
					return "login";
				}
				throw new UnLoginException();
			}
		}
		throw new UnReachException();
	}
	/**
	 * 统一强制不在线环绕方法增强，当方法增加该注解时，如果用户在线则GET直接跳到NotOnLine.value地址，POST返回已登录
	 * 
	 */
	@Around("NotOnLine()")
	public Object aroundNotOnLine(ProceedingJoinPoint pjp) throws Throwable{
		//获取方法信息
		log.info("not on line check");
		NotOnLine online = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(NotOnLine.class);
		//获取注解
		if(online!= null){
			//HttpSession session =request.getSession();
			//判断是否登录
			if(session.get("id")!=null){
				//资源重定向
				if(request.getMethod().equals("GET")){
					response.sendRedirect(online.value());
					return null;
				}
				else
					return new ResultBean<>("该用户已登录！");
			}else{
				return pjp.proceed();
			}
		}
		throw new UnReachException();
	}
	
	/**
	 * 登录方法增强,登录方法
	 * 这个方法写的有点问题，说不上来，待更改 2018.6.8
	 * @param pjp
	 * @throws Throwable 
	 */
	@Around("LogIn()")
	public Object aroundLogIn(ProceedingJoinPoint pjp) throws Throwable{
		log.info("开始登录：log in");
		//获取方法信息
		MethodSignature signature = (MethodSignature)pjp.getSignature();
		Method method= signature.getMethod();
		
		if(method.getAnnotation(LogIn.class)!=null){
			ResultBean result = (ResultBean) pjp.proceed();
			//登录失败
			if(result.getData()==null){
				return ResultBeanFactory.get(LoginException.class);//new ResultBean<>(new LoginException());
			//成功登录，设置session
			}else{
				if(result.getData() instanceof UserEntity){
					UserEntity u = (UserEntity) result.getData();
					//HttpSession session = request.getSession();
					session.set("id", u.id);
					session.set("type",u.type);
					log.info("登录成功:id :{} name :{} pwd :{}",u.id,u.name,u.pwd);
					return new ResultBean<>(u.id);
				}else{
					throw new Exception(method.getName()+"返回值不是 UserEntity对象");
				}
			}
		}
		throw new UnReachException();
	}
	
	
	/**
	 * 退出方法增强
	 */
	@Around("LogOut()")
	public Object aroundLogOut(ProceedingJoinPoint pjp) throws Throwable{
		log.info("退出：log out");
		//request.getSession().removeAttribute("id");
		//request.getSession().removeAttribute("type");
		session.clean();
		return pjp.proceed();
	}
}
