package com.idleroom.web.mq;


import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.UnReachException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PostGoodProcess {
    @Autowired
    private UserDao userDao;
    @Autowired
    private GoodDao goodDao;


    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity postProduct(String id,String gid){
        //验证商品存在
        GoodEntity g = goodDao.getGoodEntity(gid);
        //验证用户
        UserEntity u = userDao.getUserEntity(id);

        if(g.seller.equals(u.id)){
            //商品处于出售状态,否则跳过
            if(g.status.equals(GoodEntity.STATUS.SELL)){
                if(u.good==null)
                    u.good = new UserEntity.GoodCollections();
                if(u.good.sell==null)
                    u.good.sell = new ArrayList<>();

                //幂等
                for(String i : u.good.sell){
                    if(i.equals(gid)){
                        return  null;
                    }
                }

                u.good.sell.add(gid);
                userDao.save(u);
            }
            return  u;
        }

        throw new UnReachException();
    }

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity postWant(String id,String gid){
        //验证商品存在
        GoodEntity g = goodDao.getGoodEntity(gid);
        //验证用户
        UserEntity u = userDao.getUserEntity(id);

        if(g.seller.equals(u.id)){
            //商品处于求购状态,否则跳过
            if(g.status.equals(GoodEntity.STATUS.WANT)){
                if(u.good==null)
                    u.good = new UserEntity.GoodCollections();
                if(u.good.want==null)
                    u.good.want = new ArrayList<>();

                //幂等
                for(String i : u.good.want){
                    if(i.equals(gid)){
                        return  null;
                    }
                }

                u.good.want.add(gid);
                userDao.save(u);
            }
            return  u;
        }

        throw new UnReachException();
    }

}
