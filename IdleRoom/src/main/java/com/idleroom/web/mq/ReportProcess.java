package com.idleroom.web.mq;

import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.SystemDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.SystemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ReportProcess {

    @Autowired
    SystemDao systemDao;
    @Autowired
    GoodDao   goodDao;


    public void reportGood(String mid , String uid,String gid,String text){

        if(!systemDao.existsById(mid)){
            GoodEntity g = goodDao.getGoodEntity(gid);
            SystemEntity s = new SystemEntity();

            s.id    = mid;
            s.target= gid;
            s.who   = g.seller;
            if(g.target.equals(GoodEntity.STATUS.SELL)){
                s.type = "商品";
            }else{
                s.type = "求购";
            }
            s.text  = text;
            s.informer = uid;
            s.date  = new Date();
            s.status= SystemEntity.STATUS.WAIT;
            systemDao.save(s);
        }
    }
}
