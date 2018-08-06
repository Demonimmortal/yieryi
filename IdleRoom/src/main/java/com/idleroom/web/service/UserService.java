package com.idleroom.web.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.Email;

import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.exception.BaseUserException;
import com.idleroom.web.exception.UnReachException;
import com.idleroom.web.util.SmsUtil;
import com.idleroom.web.util.UserEntityHelper;
import com.idleroom.web.util.generator.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.util.generator.CodeGenerator;

/**
 * 所有对user表的修改部分必须加分布式锁， 锁前缀为 user:lock
 * 当对象方法内部调用本对象内的方法时，一定要通过 ctx.getBean 获取一个新的bean执行
 * @author  Unrestraint
 */
@Validated
@Service
public class UserService {
	private static Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
    UserDao userDao;
	@Autowired
	ApplicationContext ctx;


	public UserEntity getUser(String id){
		return userDao.getUserEntity(id);
	}


	@Value("${spring.mail.username}")
	private String from;
	/*****************发送验证码*******************/
	public String sendCodeByMail(String mail){
		log.info("send:"+mail);
        JavaMailSender sender = ctx.getBean(JavaMailSender.class);
		MimeMessage message = sender.createMimeMessage();
		String code = CodeGenerator.getCode();
        try {
            //EmailUtil email = ctx.getBean(EmailUtil.class);
            //email.send("易二易验证码","验证码： "+code+"  ;验证码有效期2分钟",mail);
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(from);
            helper.setTo(mail);
            helper.setSubject("易二易验证码");
            helper.setText("验证码： "+code+"  ;验证码有效期2分钟");
            sender.send(message);
            return code;
        } catch (Exception e) {
            log.error("发送邮件：",e);
            throw new BaseUserException(1003,"邮件发送失败！");
        }
	}
	public String sendCodeByPhone(String phone){
		log.info("send:"+phone);
		String code = CodeGenerator.getCode();
        SmsUtil sms = ctx.getBean(SmsUtil.class);
        try {
            sms.sendCode(phone,code);
            return code ;
        } catch (Exception e) {
            log.error("发送短信：",e);
            throw new BaseUserException(1003,"短信发送失败！");
        }
	}



	/**************用户密码登录部分****************/

	public UserEntity logInById(String id,String pwd){
		UserEntity user = userDao.getUserEntity(id);
		return user!=null&&user.pwd!=null&&user.pwd.equals(pwd) ? user:null;
	}

	public UserEntity logInByMail(String mail,String pwd){
		UserEntity user = userDao.findByMail(mail);
		return user!=null&&user.pwd!=null&&user.pwd.equals(pwd) ? user:null;
	}

	public UserEntity logInByPhone(String phone,String pwd){
		UserEntity user = userDao.findByPhone(phone);
		return user!=null&&user.pwd!=null&&user.pwd.equals(pwd) ? user:null;
	}






	/**************用户验证码登陆部分****************/
	public UserEntity logInByMailCode(String mail){
		return userDao.findByMail(mail);
	}
	public UserEntity logInByPhoneCode(String phone){
		return userDao.findByPhone(phone);
	}

    @Resource( name= "userId" )
    IdGenerator userId;
	/***************用户注册部分*******************/
	public UserEntity registerByMail(@Email String mail,String pwd,String name){
		if(userDao.existsByMail(mail)||userDao.existsById(mail)){
			return null;
		}else{
			UserEntity user = new UserEntity();
			user.id=userId.getNextId();
			user.constType="mail";
			user.name= (name==null||name.isEmpty())?("用户"+ user.id):name;
			user.pwd=pwd;
			user.mail=mail;
			user.type=UserEntity.TYPE.GENERAL;
			user.date=new Date();
			user.credit=100;
			user.msg = new UserEntity.MessageCollections();
			user.good = new UserEntity.GoodCollections();
			userDao.save(user);
			return user;
		}
	}
	public UserEntity registerByPhone(String phone,String pwd,String name){
		if(userDao.existsByPhone(phone)||userDao.existsById(phone)){
			return null;
		}else{
			UserEntity user = new UserEntity();
			user.id=userId.getNextId();
			user.constType="phone";
			user.name=(name==null||name.isEmpty())?("用户"+user.id):name;
			user.pwd=pwd;
			user.phone=phone;
			user.type=UserEntity.TYPE.GENERAL;
			user.date=new Date();
			user.credit=100;
            user.msg = new UserEntity.MessageCollections();
            user.good = new UserEntity.GoodCollections();
			userDao.save(user);
			return user;
		}
	}



	/*************修改密码部分*****************/
	@CacheLock("user:lock")
    @CacheAfter("user")
	public UserEntity changePwdById(String id,String pwd){
	    UserEntity u  = userDao.getUserEntity(id);
	    if(u!=null){
	        u.pwd=pwd;
	        userDao.save(u);
        }
        return u;
    }

	public UserEntity changePwdByMail(String mail,String pwd){
		UserEntity user = userDao.findByMail(mail);
		if(user!=null){
			user = ctx.getBean(this.getClass()).changePwdById(user.id,pwd);
		}
		return user;
	}

	public UserEntity changePwdByPhone(String phone,String pwd){
		UserEntity user = userDao.findByPhone(phone);
		if(user!=null){
			user = ctx.getBean(this.getClass()).changePwdById(user.id,pwd);
		}
		return user;
	}

	/**
	 * 当用户 phone 或mail作为id时，该属性不允许修改
	 * @param id
	 * @param name
	 * @param phone
	 * @param mail
	 * @param qq
	 * @param wechat
	 * @param sign
	 * @param pic
	 * @param campus
	 * @return
	 */
	@CacheLock("user:lock")
	@CacheAfter("user")
	public UserEntity changeInfo(String id,String name,String sex,String phone,String mail,String qq,String wechat,String sign,String pic,String campus){
		UserEntity user = userDao.getUserEntity(id);
		if(user!=null){
			if(name!=null)  user.name=name;
			if(phone!=null){
				if("phone".equals(user.constType)&&!phone.equals(user.phone))
					throw new BaseUserException(1060,"phone为用户注册信息，不可修改!");
                if(!"phone".equals(user.constType)){
                    if(!phone.isEmpty()&&userDao.findByPhone(phone)!=null){
                        throw new BaseUserException(1060,"该手机号已有注册用户！");
                    }
                    user.phone=phone;
                }
			}
			if(mail!=null){
				if("mail".equals(user.constType)&&!mail.equals(user.mail))
					throw new BaseUserException(1060,"mail为用户注册信息，不可修改!");
                if(!"mail".equals(user.constType)){
                    if(!phone.isEmpty()&&userDao.findByMail(mail)!=null){
                        throw new BaseUserException(1060,"该邮箱已有注册用户！");
                    }
                    user.mail=mail;
                }
			}
			if(qq!=null)	user.qq=qq;
			if(wechat!=null)user.wechat=wechat;
			if(sign!=null)	user.sign=sign;
			if(pic!=null)	user.pic=pic;
			if(campus!=null)user.campus=campus;
			if(sex!=null) user.sex=sex;
			userDao.save(user);
		}
		return user;
	}

    /**
     * 删除消息
     */
	@CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity deleteMessage(String id,List<String> list,String type){
	    UserEntity u = userDao.getUserEntity(id);

        UserEntityHelper helper  = new UserEntityHelper(u);

        if("sys".equals(type)){
            helper.delSysMessage(list);
        }else if("user".equals(type)){
            helper.delUserMessage(list);
        }else {
            throw  new UnReachException();
        }
        if(helper.isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity deleteBought(String id,String gid){
        UserEntity u = userDao.getUserEntity(id);
	    if(new UserEntityHelper(u).delBought(gid).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }
}
