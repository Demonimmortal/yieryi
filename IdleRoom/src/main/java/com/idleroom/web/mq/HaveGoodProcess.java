package com.idleroom.web.mq;

import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.Message;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.util.UserEntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 求购者被响应， 这个处理未来可能增加更多需求，所以将处理放在了CMQMessageHandler中
 * 如增加邮箱发送，更容易完成功能
 * @author  Unrestraint
 *
 * 管理员修改商品状态
 */
@Component
public class HaveGoodProcess {

    @Autowired
    UserDao userDao;
    @Autowired
    GoodDao goodDao;
    public GoodEntity getGood(String id){
        return  goodDao.getGoodEntity(id);
    }
    public UserEntity getUser(String id){
        return  userDao.getUserEntity(id);
    }
    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity postToUser(String id, Message msg){
        UserEntity user = userDao.getUserEntity(id);
        if(new UserEntityHelper(user).addSysMessage(msg).isChange()){
            userDao.save(user);
            return user;
        }else{
            return null;
        }
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity changGoodStatusByAdminToSeller(String id,Message msg,String gid,String status){
        UserEntity user = userDao.getUserEntity(id);
        UserEntityHelper helper = new UserEntityHelper(user).addSysMessage(msg);
        helper.delSell(gid);
        if(status.equals(GoodEntity.STATUS.OFF)){
            helper.addSold(gid);
        }
        if(helper.isChange()){
            userDao.save(user);
            return user;
        }else{
            return null;
        }
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity changGoodStatusByAdminBuyer(String id,Message msg,String gid){
        UserEntity user = userDao.getUserEntity(id);
        UserEntityHelper helper = new UserEntityHelper(user).addSysMessage(msg);
        helper.delBuy(gid);
        if(helper.isChange()){
            userDao.save(user);
            return user;
        }else{
            return null;
        }
    }

}
