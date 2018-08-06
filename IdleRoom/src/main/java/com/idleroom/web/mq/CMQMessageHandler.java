package com.idleroom.web.mq;

import com.idleroom.web.entity.*;
import com.idleroom.web.exception.UnReachException;
import com.idleroom.web.util.DateUtil;
import com.idleroom.web.util.Json.JSONObject;
import com.idleroom.web.util.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Date;
import java.util.NoSuchElementException;

/**
 * 由于 spring 的bean 函数自调用 AOP不会生效，所以由CMQMessageHandler 协助处理消息
 * @author  Unrestraint
 */
@Component
public class CMQMessageHandler {
    private  static final Logger log = LoggerFactory.getLogger(CMQMessageHandler.class);
    public static class HANDLE{
        public static final String POST_COMMENT="POST_COMMENT"; //发布回复
        public static final String POST_PRODUCT="POST_PRODUCT"; //发布出售
        public static final String POST_WANT="POST_WANT";      //发布求购
       // public static final String DELETE_PRODUCT="DELETE_PRODUCT";  //删除商品
        public static final String HAVE_PRODUCT="HAVE_PRODUCT"; //求购被响应
        public static final  String CHANGE_GOOD_STATUS="CHANGE_GOOD_STATUS";//修改商品状态

        public static final String REPORT_GOOD="REPORT_GOOD";  //举报
        public static final String CHANGE_GOOD_STATUS_ADMIN="CHANGE_GOOD_STATUS_ADMIN";  //管理员下架或删除出售、删除出售其他状态均抛异常
    }

    @Autowired
    ApplicationContext ctx;

    public void messageHandler(JSONObject msg){

        JSONObject data = msg.optJSONObject("data");
        if(data==null){
            log.error("接受消息无数据内容："+msg.toString());
            return;
        }

        try {
            switch (msg.optString("handle",null)){

                case HANDLE.POST_COMMENT:
                    postComment(data); break;
                case HANDLE.POST_PRODUCT:
                    postProduct(data);break;
                case HANDLE.POST_WANT:
                    postWant(data);break;
               // case HANDLE.DELETE_PRODUCT:
               //     deleteProduct(data);break;
                case HANDLE.HAVE_PRODUCT:
                    haveProduct(data);break;
                case HANDLE.REPORT_GOOD:
                    reportGood(data);break;
                case HANDLE.CHANGE_GOOD_STATUS:
                    changGoodStatusByUser(data);break;
                case HANDLE.CHANGE_GOOD_STATUS_ADMIN:
                    changeGoodStatusByAdmin(data);break;
                 default:
                    log.error("接受消息句柄错误:"+msg.toString());
            }
        }catch (NoSuchElementException e){
            log.error("数据错误，未查找到实例："+msg.toString(),e);
        }
    }

    /**
     *发布出售处理
     */
    protected  void postProduct(JSONObject data){
        PostGoodProcess process = ctx.getBean(PostGoodProcess.class);
        process.postProduct(data.getString("uid"),data.getString("gid"));

    }

    /**
     * 发布求购处理
     * @param data
     */
    protected void postWant(JSONObject data){
        PostGoodProcess process = ctx.getBean(PostGoodProcess.class);
        process.postWant(data.getString("uid"),data.getString("gid"));
    }

    /**
     * 卖家或求购者删除商品
     * @param data
     */
//    protected void deleteProduct(JSONObject data){
//        String oldStatus=data.getString("oldStatus");
//        String id=data.getString("id");
//
//        //从列表中移除商品
//        DeleteGoodProcess process = ctx.getBean(DeleteGoodProcess.class);
//        GoodEntity goodEntity = process.get(id);
//        process.deleteFromUser(goodEntity.seller,goodEntity.id,oldStatus);
//
//        if(oldStatus.equals(GoodEntity.STATUS.BARGAIN)){
//
//        }
//    }

    /**
     * 回复处理
     * @param data
     */
    protected void postComment(JSONObject data){
        Comment comment = new Comment();
        comment.cid  = data.getString("cid");
        comment.gid  = data.getString("gid");
        comment.text = data.getString("text");
        comment.title = data.getString("title");
        comment.date = DateUtil.transferStringToDate(data.getString("date"));
        comment.from = data.getString("from");
        comment.to   = data.getString("to");
        comment.fromName = data.getString("fromName");
        comment.toName = data.getString("toName");
        String group = data.getString("group");

        PostCommentProcess process = ctx.getBean(PostCommentProcess.class);
        process.postCommentToGood(comment.gid,comment,group);
        if(!comment.from.equals(comment.to))
            process.postMessageToReceiver(comment.to,comment);
    }

    /**
     * 求购物品 有人响应
     * @param data
     */
    protected void haveProduct(JSONObject data){
        String gid = data.getString("gid");
        String haveId = data.getString("haveId");
        String mid = data.getString("mid");
        Date date = DateUtil.transferStringToDate(data.getString("date"));

        HaveGoodProcess process = ctx.getBean(HaveGoodProcess.class);
        //获取物品信息
        GoodEntity g = process.getGood(gid);

        //检查物品状态
        if(GoodEntity.TARGET.WANT.equals(g.target)&&!GoodEntity.STATUS.DELETE.equals(g.status)){
            //获取双方信息
            UserEntity wantUser = process.getUser(g.seller);
            UserEntity haveUser = process.getUser(haveId);

            //发送至拥有方
            Message msg = MessageBuilder.buildByHaveGoodToHaveUser(mid,date,wantUser,g);
            process.postToUser(haveId,msg);
            //发送至求购放
            msg = MessageBuilder.buildByHaveGoodToWantUser(mid,date,haveUser,g);
            process.postToUser(g.seller,msg);
        }
    }

    /**
     * 举报处理
     * @param data
     */
    public void reportGood(JSONObject data){
        String uid = data.getString("uid");
        String gid = data.getString("gid");
        String text = data.getString("text");
        String mid = data.getString("mid");

        ctx.getBean(ReportProcess.class).reportGood(mid,uid,gid,text);
    }

    /**
     *修改商品状态
     */
    public void changGoodStatusByUser(JSONObject data){
        String oldStatus=data.getString("oldStatus");
        String status=data.getString("status");
        String gid=data.getString("gid");
        String uid=data.getString("uid");
        String mid = data.getString("mid");
        Date date = DateUtil.transferStringToDate(data.getString("date"));

        GoodStatusChangeProcess process = ctx.getBean(GoodStatusChangeProcess.class);
        GoodEntity g = process.get(gid);

        if(!g.status.equals(status)){
            log.info("g.status {} status {}",g.status,status);
            return;
        }
        //重新上架
        if(oldStatus.equals(GoodEntity.STATUS.OFF)&&status.equals(GoodEntity.STATUS.SELL)){
            process.repost(uid,gid);
            return;
        }

        //用户购买
        if(status.equals(GoodEntity.STATUS.BARGAIN)){
            process.bargainToSeller(g.seller,mid,date,g);
            process.bargainToBuyer(g.buyer,mid,date,g);
            //卖方确认交易成功
        }else if (status.equals(GoodEntity.STATUS.SOLD)){
            log.info(status);
            process.soldToSeller(g.seller,g);
            process.soldToBuyer(g.buyer,mid,date,g);
            //卖方取消交易重新变为出售状态
        }else if(status.equals(GoodEntity.STATUS.SELL)){
            log.info(status);
            process.sellToBuyer(data.getString("buyer"),mid,date,g);
        }else if(status.equals(GoodEntity.STATUS.DELETE)){
            log.info(status);
            process.deleteFromUser(g.seller,g.id,oldStatus);
        }else if(status.equals(GoodEntity.STATUS.OFF)){
            log.info(status);
            process.offToSeller(g.seller,g.id);
        }else{
            throw new UnReachException();
        }
    }
    /**
     * 管理员修改商品状态
     * 这里复用了求购物品的处理
     */
    public void changeGoodStatusByAdmin(JSONObject data){
        String mid = data.getString("mid");
        String gid = data.getString("gid");
        String status = data.getString("status");
        String oldStatus = data.getString("oldStatus");
        Date date = DateUtil.transferStringToDate(data.getString("date"));

        HaveGoodProcess process = ctx.getBean(HaveGoodProcess.class);
        GoodEntity g = process.getGood(gid);

        //状态修改为了删除或下架
        if(GoodEntity.STATUS.OFF.equals(g.status)||GoodEntity.STATUS.DELETE.equals(g.status)){
            Message msg = MessageBuilder.buildByAdminDeleteGoodToSeller(mid,date,g);
            process.changGoodStatusByAdminToSeller(g.seller,msg,gid,status);
        }
        //旧状态为正在交易 且 状态修改为了删除或下架
        if(GoodEntity.STATUS.BARGAIN.equals(oldStatus)&&(GoodEntity.STATUS.OFF.equals(g.status)||GoodEntity.STATUS.DELETE.equals(g.status))){
            Message msg = MessageBuilder.buildByAdminDeleteGoodToBuyer(mid,date,g);
            process.changGoodStatusByAdminBuyer(g.buyer,msg,gid);
        }
        //状态为其他不做处理
    }
}
