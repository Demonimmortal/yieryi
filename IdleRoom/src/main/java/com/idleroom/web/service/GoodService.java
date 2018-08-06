package com.idleroom.web.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.UnPermissionException;
import com.idleroom.web.exception.UnReachException;
import com.idleroom.web.exception.UnusualException;
import com.idleroom.web.mq.CMQMessageHandler;
import com.idleroom.web.mq.CMQUtil;
import com.idleroom.web.util.Json.JSONObject;
import com.idleroom.web.util.UserEntityHelper;
import com.idleroom.web.util.generator.IdGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.entity.Comment;
import com.idleroom.web.entity.GoodEntity;

import javax.annotation.Resource;

/**
 * 所有对good表的修改部分必须加分布式锁， 锁前缀为 good:lock
 * 当对象方法内部调用本对象内的方法时，一定要通过 ctx.getBean 获取一个新的bean执行
 *
 * 尽可能的先发消息队列，再请求数据库，数据库崩了还有消息队列的数据能够盘活，消息队列崩了就有点麻烦了
 * @author  Unrestraint
 */
@Service
public class GoodService {
    private static Logger log = LoggerFactory.getLogger(GoodService.class);
	@Autowired
	GoodDao goodDao;
	@Autowired
    UserDao userDao;
	@Autowired
	ApplicationContext ctx;
	@Autowired
    CMQUtil cmq;

    @Resource(name = "msgId")
    IdGenerator msgId;

    @Resource(name = "goodId")
    IdGenerator goodId;

    /**
     * 当未查找到数据抛出 NoSuchElementException 异常
     * @param id 商品id
     */
	public GoodEntity getGood(String id){
		return goodDao.getGoodEntity(id);
	}
	public UserEntity getUser(String id){
	    return userDao.getUserEntity(id);
    }

	public Object listGoodLike(String title,String info,String status,int page,int size,String sortType){

	    return goodDao.findByStatusEqualsAndTitleLikeOrInfoLike(status,title,info,PageRequest.of(page,size, Sort.by(sortType).descending()));
    }

    public Object listGoodBySample(String status,int size){
        MongoTemplate template = ctx.getBean(MongoTemplate.class);
        Aggregation agg = Aggregation.newAggregation(Aggregation.match(Criteria.where("status").is(status)),Aggregation.sample(size)
                ,Aggregation.project("title","target","type","price","status","info","date","seller","click","pic"));

        return template.aggregate(agg,template.getCollectionName(GoodEntity.class),GoodEntity.class).getMappedResults();
    }

    public Iterable<GoodEntity> listGoodById(List<String> id){
	    Iterable<GoodEntity> it=goodDao.findAllById(id);
	    it.forEach(e->{
	        e.comment=null;
        });
	    return it;
    }
    /**
     * 分页 查询
     */
	public Page<GoodEntity> listGood(List types,String status,int page,int size,String sortType){
		if(types.size()==1 && "*".equals(types.get(0))){
            return goodDao.findByStatusEquals(status, PageRequest.of(page,size, Sort.by(sortType).descending()));
        }
        return goodDao.findAllByTypeInAndStatusEquals(types, status,PageRequest.of(page,size, Sort.by(sortType).descending()));
    }

    /**
     * 发布商品
     */
	public GoodEntity postProduct(String title,String type,float price,String info,String seller,List pic ){
	    GoodEntity good = new GoodEntity();
	    good.id     = goodId.getNextId();
	    good.title  = title;
	    good.target = GoodEntity.TARGET.SELL;
	    good.type   = type;
	    good.price  = price;
        good.status = GoodEntity.STATUS.SELL;
        good.info   = info;
        good.date   = new Date();
        good.seller = seller;
        good.click  = 0;
        good.pic    = pic;
        good._1=false;
        good._2=false;

        //发送至消息队列，这里处理的时候先判断good是否存在
        cmq.sendMessage( new JSONObject().put("gid",good.id).put("uid",good.seller) , CMQMessageHandler.HANDLE.POST_PRODUCT);

        goodDao.save(good);

		return good;
	}

    /**
     * 发布求购
     */
	public GoodEntity postWant(String title,String type,float price,String info,String seller){
        GoodEntity good = new GoodEntity();
        good.id     = goodId.getNextId();
        good.title  = title;
        good.type   = type;
        good.target = GoodEntity.TARGET.WANT;
        good.price  = price;
        good.status = GoodEntity.STATUS.WANT;
        good.info   = info;
        good.date   = new Date();
        good.seller = seller;
        good._1=false;
        good._2=false;
        //发送至消息队列，这里处理的时候先判断good是否存在
        cmq.sendMessage( new JSONObject().put("gid",good.id).put("uid",good.seller) , CMQMessageHandler.HANDLE.POST_WANT);

        goodDao.save(good);
        return good;
	}


	@CacheLock(value = "good:lock",key = "gid")
    @CacheAfter(value= "good",key="gid")
	public GoodEntity repost(String uid,String gid){
        GoodEntity g = goodDao.getGoodEntity(gid);
        if(g.seller.equals(uid)&&!GoodEntity.STATUS.DELETE.equals(g.status)){
            g.date = new Date();
            goodDao.save(g);
            return g;
        }
        throw new UnPermissionException();
    }


    /**
     * 正在求购的物品被别人点击拥有，即响应
     * @param gid
     * @param wantId
     * @param haveId
     * @return
     */
	public UserEntity haveGood(String gid,String wantId,String haveId){
	    UserEntity u = userDao.getUserEntity(wantId);

	    cmq.sendMessage(new JSONObject().put("gid",gid).put("haveId",haveId).put("mid",msgId.getNextId()).put("date",new Date()),CMQMessageHandler.HANDLE.HAVE_PRODUCT);

        u.pwd = null;
        u.msg = null;
        u.good = null;
	    return u;
    }


//    /**
//     * 卖家或求购者删除商品
//     * @param id
//     */
//    @CacheLock("good:lock")
//    @CacheAfter("good")
//    public GoodEntity deleteGood(String id){
//        GoodEntity g = goodDao.getGoodEntity(id);
//        if(g.status.equals(GoodEntity.STATUS.DELETE)){
//            return null;
//        }
//        cmq.sendMessage(new JSONObject().put("oldStatus",g.status).put("id",id),CMQMessageHandler.HANDLE.DELETE_PRODUCT);
//
//        g.status = GoodEntity.STATUS.DELETE;
//        goodDao.save(g);
//        return  g;
//    }
    /**
     *  修改商品状态
     * @param id    商品id
     * @param user  修改人
     * @param status 欲修改状态为
     * @return
     */
    @CacheLock("good:lock")
    @CacheAfter("good")
    public GoodEntity changeStatus(String id,String user,String status){
        GoodEntity g = goodDao.getGoodEntity(id);
        String oldStatus=g.status;
        if(oldStatus.equals(status)){
            return null;
        }
        if(g.status.equals(GoodEntity.STATUS.DELETE)){
            throw new UnusualException();
        }
        String buyer = "";
        //出售物品状态变化
        if(GoodEntity.TARGET.SELL.equals(g.target))
         switch (status){
            case GoodEntity.STATUS.DELETE: //用户删除商品
                if(!g.seller.equals(user)||g.status.equals(GoodEntity.STATUS.BARGAIN)){//正在交易的物品不允许删除
                    throw new UnusualException();
                }
                break;
            case GoodEntity.STATUS.OFF://用户下架商品
                if(!g.seller.equals(user)||g.status.equals(GoodEntity.STATUS.BARGAIN)){//正在交易的物品不允许下架
                    throw new UnusualException();
                }
                break;
            case GoodEntity.STATUS.SELL://修改为出售状态即用户取消交易
                if(!g.seller.equals(user)||!g.status.equals(GoodEntity.STATUS.BARGAIN)&&!g.status.equals(GoodEntity.STATUS.OFF)){//必须为卖方，且商品状态为正在交易,或下架状态
                    throw new UnusualException();
                }
                buyer=g.buyer;
                g.buyer= null;
                break;
            case GoodEntity.STATUS.BARGAIN://修改为正在交易状态
                if(g.seller.equals(user)||!g.status.equals(GoodEntity.STATUS.SELL)){//必须为不同用户且商品状态为出售
                    throw new UnusualException();
                }
                g.buyer=user;
                buyer=user;
                break;
            case GoodEntity.STATUS.SOLD://修改为交易成功状态
                if(!g.seller.equals(user)||!g.status.equals(GoodEntity.STATUS.BARGAIN)){//必须由出售者更改且商品状态为正在交易
                    throw new UnusualException();
                }
                break;
            default:
                throw new UnReachException();
        }
            //求购状态变化
        else if(GoodEntity.TARGET.WANT.equals(g.target)&&!GoodEntity.STATUS.WANT.equals(oldStatus)){
            throw  new UnReachException();
        }
        cmq.sendMessage(new JSONObject().put("oldStatus",g.status).put("status",status).put("gid",id).put("uid",user).put("mid",msgId.getNextId()).put("date",new Date()).put("buyer",buyer),CMQMessageHandler.HANDLE.CHANGE_GOOD_STATUS);

        g.status=status;
        goodDao.save(g);
        return g;
    }


    //收藏,验证商品是否存在
    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity addLike(String id, String gid){
        //验证商品存在
        goodDao.getGoodEntity(gid);
        UserEntity user = userDao.getUserEntity(id);
        if(new UserEntityHelper(user).addLike(gid).isChange()){
            userDao.save(user);
        }
        return user;
    }
    //删除收藏，不验证商品是否存在
    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity delLike(String id,List<String> gidList){
        UserEntity user = userDao.getUserEntity(id);
        if(new UserEntityHelper(user).delLike(gidList).isChange()){
            userDao.save(user);
        }
        return user;
    }


    /**
     * 在商品下回复
     * @param id   商品id
     * @param from 回复人
     * @param to   被回复人
     * @param text 回复内容
     * @param group 放置的回复组 从1计数 范围外则新启一组
     * @return
     */
	public Comment postComment(String id,String from,String to,String text,String group){
	    //查找商品

        GoodEntity goodEntity = goodDao.getGoodEntity(id);
        if(to==null||to.isEmpty()){
            to = goodEntity.seller;
        }
        UserEntity uFrom = userDao.getUserEntity(from);
        UserEntity uTo = userDao.getUserEntity(to);
	    Comment comment = new Comment();
	    comment.cid  = msgId.getNextId();
	    comment.gid  = goodEntity.id;
	    comment.text = text;
	    comment.title= goodEntity.title;
	    comment.date = new Date();
	    comment.from = from;
        comment.to   = to;

        comment.fromName = uFrom.name;
	    comment.toName = uTo.name;
	    //发送到消息队列
        cmq.sendMessage( new JSONObject(comment).put("group",group) , CMQMessageHandler.HANDLE.POST_COMMENT);

        return comment;
	}

    /**
     * 删除回复，普通用户
     * @param uid 删除人
     * @param gid 商品id
     * @param cid 回复id
     */
    @CacheLock(value = "good:lock",key="gid")
	@CacheAfter(value = "good",key="gid")
	public GoodEntity deleteComment(String uid,String gid,String cid,String group){
		GoodEntity g = goodDao.getGoodEntity(gid);

		if(g.comment!= null &&g.comment.size()>0){
		    //查找组
            for(List<Comment> list:g.comment){
                if(list.get(0).cid.equals(group)){

                    Iterator<Comment> it = list.iterator();
                    //查找回复cid
                    while(it.hasNext()){
                        Comment c= it.next();
                        if( c.from.equals(uid) && c.cid.equals(cid) ){
                            c.text="";
                            goodDao.save(g);
                            return g;
                        }else{
                            throw new UnPermissionException();
                        }
                    }
                }
            }
		}
		throw new UnReachException();
    }
}
