package com.idleroom.web.mq;

import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.Message;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.UnReachException;
import com.idleroom.web.util.MessageBuilder;
import com.idleroom.web.util.UserEntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 修改商品状态后的追加处理
 */
@Component
public class GoodStatusChangeProcess {
    @Autowired
    private UserDao userDao;
    @Autowired
    private GoodDao goodDao;
    public GoodEntity get(String id){
        return goodDao.getGoodEntity(id);
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity repost(String id,String gid){
        UserEntity u = userDao.getUserEntity(id);
        UserEntityHelper helper = new UserEntityHelper(u);
        if(helper.delSold(gid).addSell(gid).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity bargainToSeller(String id, String mid, Date date, GoodEntity g){
        UserEntity u = userDao.getUserEntity(id);
        UserEntity buyer= userDao.getUserEntity(g.buyer);

        Message msg = MessageBuilder.buildByBargainToSeller(mid,date,g,buyer);

        if(new UserEntityHelper(u).addSysMessage(msg).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity bargainToBuyer(String id,String mid,Date date,GoodEntity g){
        UserEntity u = userDao.getUserEntity(id);
        UserEntity seller = userDao.getUserEntity(g.seller);

        Message msg = MessageBuilder.buildByBargainToBuyer(mid,date,g,seller);
        if(new UserEntityHelper(u).addSysMessage(msg).addBuy(g.id).isChange()){
            userDao.save(u);
            return  u;
        }
        return  null;
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity soldToSeller(String id,GoodEntity g){
        UserEntity u = userDao.getUserEntity(id);
        if(new UserEntityHelper(u).delSell(g.id).addSold(g.id).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }
    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity soldToBuyer(String id,String mid,Date date,GoodEntity g){
        UserEntity u = userDao.getUserEntity(id);
        Message msg = MessageBuilder.buildBySoldToBuyer(mid,date,g);
        if(new UserEntityHelper(u).addSysMessage(msg).delBuy(g.id).addBought(g.id).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity sellToBuyer(String id,String mid,Date date,GoodEntity g){
        UserEntity u = userDao.getUserEntity(id);
        Message msg = MessageBuilder.buildBySellToBuyer(mid,date,g);
        if(new UserEntityHelper(u).delBuy(g.id).addSysMessage(msg).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
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

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity offToSeller(String id,String gid){
        UserEntity u = userDao.getUserEntity(id);

        if(new UserEntityHelper(u).delSell(gid).addSold(gid).isChange()){
            userDao.save(u);
            return u;
        }
        return null;
    }
}
