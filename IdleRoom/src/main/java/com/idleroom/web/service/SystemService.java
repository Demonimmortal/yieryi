package com.idleroom.web.service;


import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.SystemDao;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.SystemEntity;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.BaseUserException;
import com.idleroom.web.mq.CMQMessageHandler;
import com.idleroom.web.mq.CMQUtil;
import com.idleroom.web.util.Json.JSONObject;
import com.idleroom.web.util.generator.IdGenerator;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 占用锁前缀 report:lock
 * @author  Unrestraint
 */
@Service
public class SystemService {
    private static final Logger log = LoggerFactory.getLogger(SystemService.class);
    @Autowired
    SystemDao systemDao;
    @Autowired
    CMQUtil cmq;
    @Autowired
    IdGenerator msgId;
    @Autowired
    UserDao userDao;
    @Autowired
    GoodDao goodDao;


    public Page<UserEntity> listUser(int page,int size,String sortType){
        Page<UserEntity> p =  userDao.findAll(PageRequest.of(page,size, Sort.by(sortType).descending()));
        p.forEach(e->{
            e.msg=null;
            e.good=null;
        });
        return p;
    }

    public UserEntity getUser(String id){
        UserEntity  u = userDao.getUserEntity(id);
        u.msg=null;
        u.good = null;
        return u;
    }
    /**
     * 修改用户类型
     * @param id
     * @param type
     * @return
     */
    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity changeUserType(String id,String type){
        UserEntity u = userDao.getUserEntity(id);
        u.type=type;
        userDao.save(u);
        return  u;
    }

    public Page<GoodEntity> listGood(String target,Iterable it,int page,int size,String sortType){
        Page<GoodEntity>  p = goodDao.findAllByStatusInAndTargetEquals(it,target,PageRequest.of(page,size, Sort.by(sortType).descending()));
        p.forEach(e->{
            e.comment=null;
            e.pic=null;
        });
        return p;
    }
    /**
     *  修改商品状态
     * @param id    商品id
     * @param status 欲修改状态为
     * @return
     */
    @CacheLock("good:lock")
    @CacheAfter("good")
    public GoodEntity changeStatus(String id, String status){
        GoodEntity g = goodDao.getGoodEntity(id);
        //已修改状态不再重复
        if(g.status.equals(status)){
            return  g;
        }

        cmq.sendMessage(new JSONObject().put("mid",msgId.getNextId()).put("gid",id).put("status",status).put("oldStatus",g.status).put("date",new Date()),CMQMessageHandler.HANDLE.CHANGE_GOOD_STATUS_ADMIN);

        g.status=status;
        goodDao.save(g);
        return g;
    }

    /**
     * 举报
     * @param id
     * @param gid
     * @param text
     */
    @CacheLock(value = "report:lock",timeOut = 0,freeAfter = false)
    public void report(String id,String gid,String text){
        log.info("report :{} {} {} ",id,gid,text);
        cmq.sendMessage(new JSONObject().put("uid",id).put("gid",gid).put("text",text).put("mid",msgId.getNextId()),CMQMessageHandler.HANDLE.REPORT_GOOD);
    }


    @CacheLock("sys:lock")
    public SystemEntity dealReport(String id,String admin,String status){
        SystemEntity s = systemDao.getSystemEntity(id);
        if(s.status.equals(status)){
            throw new BaseUserException(1064,"状态已修改!");
        }
        s.status = status;
        s.admin = admin;
        systemDao.save(s);
        return s;
    }

    @Resource
    MongoTemplate mongoTemplate;

    public UpdateResult delReport(List<String> id){
        Query query = new Query(Criteria.where("_id").in(id));
        Update update = Update.update("status",SystemEntity.STATUS.DELETE);
        return  mongoTemplate.updateMulti(query,update,SystemEntity.class);
    }


    public Page<SystemEntity> listReport(String who, String type, List<String> status, int page, int size){

        SystemEntity s  = new SystemEntity();
        ExampleMatcher matcher = ExampleMatcher.matching();
        if(who!=null&&!who.isEmpty()){
            s.who = who;
            matcher.withMatcher("who",ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if(type!=null&&!type.isEmpty()){
            s.type = type;
            matcher.withMatcher("type",ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if(status==null||"*".equals(status.get(0))){
            return systemDao.findAll(Example.of(s,matcher),PageRequest.of(page,size,Sort.by("date").descending()));
        }

        return systemDao.findAllByStatusIn(status,Example.of(s,matcher),PageRequest.of(page,size,Sort.by("date").descending()));
//        return  systemDao.findAll(Example.of(s,matcher),PageRequest.of(page,size,Sort.by("date").descending()));
    }
}
