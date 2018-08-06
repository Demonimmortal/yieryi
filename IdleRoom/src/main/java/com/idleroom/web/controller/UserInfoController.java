package com.idleroom.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;

import com.idleroom.web.cache.SessionManager;
import com.idleroom.web.exception.*;
import com.idleroom.web.util.ResultBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.idleroom.web.annotation.LogIn;
import com.idleroom.web.annotation.LogOut;
import com.idleroom.web.annotation.OnLine;
import com.idleroom.web.entity.ResultBean;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.service.UserService;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * 用户自身管理
 * 占用URL  /user
 * @author Unrestraint
 *
 */
@Validated
@Controller
@RequestMapping("/user")
public class UserInfoController {
	private static final Logger log = LoggerFactory.getLogger(UserInfoController.class);
	
	@Autowired
	UserService userService;

	@Autowired
	SessionManager session;

	/**
	 * 抽象验证码发送
	 * @param type	mail，phone
	 * @param value
	 */
	public void codeAbstractSend(String type,String value){
		String code;
		switch(type){
			case "mail":
				code = userService.sendCodeByMail(value);break;
			case "phone":
				code = userService.sendCodeByPhone(value);break;
			default :
				throw new UnReachException();
		}
		log.info(type+":"+value+" "+code);

		//key <type:value> value <code:curTime> 当前时间以毫秒计
		session.set(type+":"+value,code+":"+System.currentTimeMillis());
	}
	/**
	 * 抽象验证码验证
	 * @param type	mail，phone
	 * @param value
	 * @param code
	 */
	public void codeAbstractCheck(String type,String value,String code){
		log.info(type+":"+value);
		String codeAndTime = (String) session.get(type+":"+value);
		if(codeAndTime==null){
			//前端不应未发送验证码或倒数120秒结束，就直接验证码登录方式提交
			throw new BaseUserException(1059,"用户信息与验证码不匹配，或未发送验证码!");
		}
		String[] split = codeAndTime.split(":");
		String oldCode = split[0];
		//验证验证码是否过期
		if(System.currentTimeMillis()-Long.valueOf(split[1])>120*1000){
			throw new BaseUserException(1055,"验证码过期!");
		}
		if(!oldCode.equals(code)){
			throw new BaseUserException(1056,"验证码错误!");
		}
		//登录成功删除验证码
		session.remove(type+":"+value);
	}

	/**
	 * 获取验证码
	 * @param type
	 * @param value
	 * @return
	 */
	@PostMapping("/getCode")
	@ResponseBody
	public ResultBean<?> getCode(@NotEmpty @Pattern(regexp = "mail|phone",message="登录类型不合法!") String type, @NotEmpty String value){
		this.codeAbstractSend(type, value);
		return ResultBeanFactory.simple();
	}

	/**
	 * 正常登陆方法
	 * @param type  登陆类型，mail，id，phone
	 * @param value 该类型对应参数
	 * @param pwd   密码
	 * @return
	 */

	@LogIn
	@PostMapping("/logInByPwd")
	@ResponseBody
	public ResultBean<?> logInByPwd(@NotEmpty @Pattern(regexp = "id|mail|phone",message="登录类型不合法!") String type, @NotEmpty String value,@NotEmpty @Size(max=20,min=1)String pwd){
		log.info("type: {} value: {} pwd {} " , type,value,pwd);

		UserEntity u;
		switch(type){
		case "id":
			u = userService.logInById(value, pwd);break;
		case "mail":
			u = userService.logInByMail(value, pwd);break;
		case "phone":
			u = userService.logInByPhone(value, pwd);break;
		default :
			throw new UnReachException();
		}
		//登录失败
		if(u==null){
		    return ResultBeanFactory.simple();
        }
        //封禁用户
        if(UserEntity.TYPE.BAN.equals(u.type)){
		    throw new BannedUserException();
        }
        //登录成功
		return new ResultBean<>(u);
	}
	
	/**
	 * 验证码登录
	 * @param type 登录类型，mail，phone
	 * @param value 该类型的参数
	 * @param code  验证码
	 * @return
	 */
	@LogIn
	@PostMapping("/logInByCode")
	@ResponseBody
	public ResultBean<?> logInByCode(@NotEmpty @Pattern(regexp = "mail|phone",message="登录类型不合法!") String type,@NotEmpty String value,@NotEmpty @Pattern(regexp = "^\\d{6}$", message="验证码不合法!") String code){
		
		log.info("type: {} value: {} code {} " , type,value,code);

        UserEntity u;
		//验证验证码
		this.codeAbstractCheck(type, value, code);
		switch(type){
		case "mail":
            u = userService.logInByMailCode(value);break;
		case "phone":
            u = userService.logInByPhoneCode(value);break;
		default:
			throw new UnReachException();
		}
		//未查找到用户
        if(u==null){
            throw new NoSuchElementException("该用户未注册！");
        }
        //封禁用户
        if(UserEntity.TYPE.BAN.equals(u.type)){
            throw new BannedUserException();
        }
        return new ResultBean<>(u);
	}

	
	@LogOut
	@PostMapping("/logOut")
	@ResponseBody
	public ResultBean<?> logOut(){
		return ResultBeanFactory.simple();
	}
	
	/**
	 * 注册
	 * @param name	用户昵称，非必须
	 * @param type  mail，phone
	 * @param value mail或phone对应的值
	 * @param pwd   密码
	 * @param code  验证码
	 * @return
	 */
	@PostMapping("/register")
	@ResponseBody
	public ResultBean<?> register(@Size(max=20,min=1) String name,@NotEmpty @Pattern(regexp = "mail|phone",message="注册类型不合法!") String type, @NotEmpty String value,@NotEmpty @Size(max=20,min=1) String pwd,@NotEmpty @Pattern(regexp = "^\\d{6}$", message="验证码不合法!") String code){
		log.info("name :{} type:{} value :{} pwd :{} code :{}",name,type,value,pwd,code);
		
		this.codeAbstractCheck(type, value, code);
		UserEntity user;
		switch(type){
		case "mail":
			user = userService.registerByMail(value, pwd,name);break;
		case "phone":
			user = userService.registerByPhone(value, pwd,name);break;
		default:
			throw new UnReachException();
		}
		if(user==null){
			return new ResultBean<>(1057,"注册失败，用户已存在!");
		}else{
			return new ResultBean<>(user.id);
		}
	}
	
	/**
	 * 修改密码
	 * @params type, value, pwd,code
	 */
	@PostMapping("/changePwd")
	@ResponseBody
	public ResultBean<?> changePwd(@NotEmpty @Pattern(regexp = "mail|phone",message="用户类型不合法!") String type, @NotEmpty String value, @NotEmpty @Size(max=20,min=1) String pwd,@NotEmpty  @Pattern(regexp = "^\\d{6}$", message="验证码不合法!") String code){
		log.info("type:{} value :{} pwd :{} code :{}",type,value,pwd,code);

		this.codeAbstractCheck(type, value, code);

		UserEntity user;
		switch(type){
		case "mail": user = userService.changePwdByMail(value, pwd);break;
		case "phone":user = userService.changePwdByPhone(value, pwd);break;
		default:  throw new UnReachException();
		}
        //未查找到用户
        if(user==null){
            throw new NoSuchElementException("该用户未注册！");
        }
		return new ResultBean<>(user.id);
	}
	/**
	 * 修改用户信息
	 * 
	 * @params  name, phone, mail, qq,wechat,sign, pic,campus
	 */
	@OnLine
	@PostMapping("/changeInfo")
	@ResponseBody
	public ResultBean<?> changeInfo(@Size(max=20,min=1)String name, @Pattern(regexp = "男|女")String sex ,@Pattern(regexp = "^\\d{0,15}$")String phone, @Email String mail, @Pattern(regexp = "^\\d{0,15}$")String qq, @Size(max=50) String wechat, @Size(max=100)String sign, @Size(min=1) String pic,  @Size(max=100) String campus){
		
		log.info("name :{} phone : {} mail : {} qq : {} wechat : {} sign : {} pic:{} campus:{}",name,phone,mail,qq,wechat,sign,pic,campus);
		
		String id = (String) session.get("id");
		
		UserEntity u = userService.changeInfo(id, name, sex, phone, mail, qq, wechat, sign, pic, campus);
		return u==null? ResultBeanFactory.get(UnLoginException.class) :new ResultBean<>(u.id);
	}

	/**
	 * 用户删除已购买成功的物品
	 * @param gid
	 * @return
	 */
	@OnLine
	@PostMapping("/delBought")
	@ResponseBody
	public ResultBean<?> delBought(@NotEmpty String gid){
		return new ResultBean<>(userService.deleteBought((String)session.get("id"),gid));
	}
	/**
	 * 删除消息
	 * @param id  id列表，当为 * 时表示删除所有， 多个id以 : 拼接
	 * @param type 删除类型 参数 sys 系统消息， user 用户消息
	 * @return
	 */
	@OnLine
	@PostMapping("/deleteMessage")
	@ResponseBody
	public ResultBean<?> deleteMessage(@NotEmpty String id,@NotEmpty @Pattern(regexp = "sys|user") String type){
	    log.info("delete message :{} {}",id,type);
        userService.deleteMessage((String)session.get("id"), Arrays.asList(id.split(":")),type);
       return ResultBeanFactory.simple();
	}
	/**
	 * 获取用户信息，测试使用，生产环境必须删除
	 * @params id
	 */
	@PostMapping("/user")
	@ResponseBody
	public ResultBean<?> user(@NotEmpty String id){
		log.info(id);
		UserEntity userEntity = userService.getUser(id);
		userEntity.good=null;
		userEntity.msg=null;
		userEntity.pwd=null;
		return new ResultBean<>(userEntity);
	}
}
