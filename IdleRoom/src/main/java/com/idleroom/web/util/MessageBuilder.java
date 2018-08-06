package com.idleroom.web.util;

import com.idleroom.web.entity.Comment;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.Message;
import com.idleroom.web.entity.UserEntity;

import java.util.Date;

public class MessageBuilder {

    private static  Message build(String mid,Date date,String title,String text){
        Message msg = new Message();
        msg.date = date;
        msg.mid  = mid;
        msg.read = false;
        msg.title= title;
        msg.text = text;
        return msg;
    }

    public static Message build(Comment com){
        Message msg = new Message();
        msg.date  = com.date;
        msg.mid   = com.cid;
        msg.read  = false;
        msg.title = new StringBuilder().append("<a href='/i?id=").append(com.gid).append("'  target=\"_blank\" > ").append(com.fromName).append(" 在 “").append(com.title).append("” 回复了您</a>").toString();
        msg.text  = com.text;
        return msg;
    }

    public static Message buildByHaveGoodToWantUser(String mid , Date date, UserEntity haveUser,GoodEntity good){
        String title = new StringBuilder().append("您的求购 “").append(good.title).append("” 有人响应").toString();
        String text = new StringBuilder().append("名字 ：").append(haveUser.name).append(" 电话 ：").append(haveUser.phone).append(" 邮箱:").append(haveUser.mail).append(" QQ：").append(haveUser.qq).append("wechat: ").append(haveUser.wechat).toString();
        return MessageBuilder.build(mid,date,title,text);
    }

    public static Message buildByHaveGoodToHaveUser(String mid, Date date,UserEntity wantUser,GoodEntity good){
        String title = new StringBuilder().append("“").append(good.title).append("” 的求购者 ").append(wantUser.name).toString();
        String text = new StringBuilder().append("名字 ：").append(wantUser.name).append(" 电话 ：").append(wantUser.phone).append(" 邮箱:").append(wantUser.mail).append(" QQ：").append(wantUser.qq).append("wechat: ").append(wantUser.wechat).toString();
        return MessageBuilder.build(mid,date,title,text);
    }

    public static Message buildByAdminDeleteGoodToSeller(String mid,Date date,GoodEntity good){
        String title = new StringBuilder().append("您的").append(good.target).append(" “").append(good.title).append("” 被管理员").append(good.status).toString();
        String text = new StringBuilder().append("您的 ").append(good.target).append(" ").append(good.title).append(" 被管理员").append(good.status).append(", 请注意内容的合法性！").toString();
        return MessageBuilder.build(mid,date,title,text);
    }

    public static Message buildByAdminDeleteGoodToBuyer(String mid,Date date,GoodEntity good){
        String title = new StringBuilder().append("您正在交易的商品 “").append(good.title).append("” 已被管理员").append(good.status).toString();
        String text = "";
        return MessageBuilder.build(mid,date,title,text);
    }

    public static Message buildByBargainToSeller(String mid,Date date,GoodEntity g,UserEntity buyer){
        String title= new StringBuilder().append(buyer.name).append("想要购买您的 “").append(g.title).append("”").toString();
        String text = new StringBuilder().append("名字 ：").append(buyer.name).append(" 电话 ：").append(buyer.phone).append(" 邮箱:").append(buyer.mail).append(" QQ：").append(buyer.qq).append("wechat: ").append(buyer.wechat).toString();
        return MessageBuilder.build(mid,date,title,text);
    }
    public static Message buildByBargainToBuyer(String mid,Date date,GoodEntity g,UserEntity seller){
        String title= new StringBuilder().append("您购买的 “").append(g.title).append("” 所有者联系方式").toString();
        String text = new StringBuilder().append("名字 ：").append(seller.name).append(" 电话 ：").append(seller.phone).append(" 邮箱:").append(seller.mail).append(" QQ：").append(seller.qq).append(" wechat: ").append(seller.wechat).toString();
        return MessageBuilder.build(mid,date,title,text);
    }

    public static Message buildBySoldToBuyer(String mid,Date date,GoodEntity g){
        String title= new StringBuilder().append("您购买的 “").append(g.title).append("” 卖方已经确认交易成功!").toString();
        String text = "";
        return  MessageBuilder.build(mid,date,title,text);
    }

    public static Message buildBySellToBuyer(String mid,Date date,GoodEntity g){
        String title= new StringBuilder().append("您正在交易的商品 “").append(g.title).append("” 已被卖方取消").toString();
        String text="";
        return MessageBuilder.build(mid,date,title,text);
    }

}
