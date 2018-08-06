package com.idleroom.web.mq;

import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.UnReachException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 用户删除good处理
 * @author  Unrestraint
 */
@Deprecated
public class DeleteGoodProcess {

    @Autowired
    GoodDao goodDao;
    @Autowired
    UserDao userDao;

    public GoodEntity get(String id){
        return  goodDao.getGoodEntity(id);
    }
    @CacheLock("good:lock")
    @CacheAfter("good")
    public GoodEntity delete(String id){
        GoodEntity g = goodDao.getGoodEntity(id);
        g.status=GoodEntity.STATUS.DELETE;
        goodDao.save(g);
        return  g;
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity deleteFromUser(String id,String gid,String status){
        UserEntity user = userDao.getUserEntity(id);
        List<String> list;

        switch (status){
            case GoodEntity.STATUS.DELETE:
                return null;
            case GoodEntity.STATUS.SELL:
                list = user.good.sell;break;
            case GoodEntity.STATUS.SOLD:
            case GoodEntity.STATUS.OFF:
                list = user.good.sold;break;
            case GoodEntity.STATUS.WANT:
                list = user.good.want;break;
            //正在交易状态必须先取消才可以删除
            case GoodEntity.STATUS.BARGAIN:
            default:
                throw new UnReachException();
        }

        if(list!=null){
            list.remove(gid);
        }
        userDao.save(user);
        return user;
    }
}
