package com.idleroom.web.util;

import com.idleroom.web.entity.Message;
import com.idleroom.web.entity.UserEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * UserEntity 帮助类，对UserEntity的一些处理都由该帮助类统一处理
 * @author Unrestraint
 */
public class UserEntityHelper {
    private UserEntity u;
    private boolean change=false;

    public UserEntityHelper(UserEntity u){
        if(u==null)
            throw  new NullPointerException();
        this.u = u;
    }
    public UserEntity build(){
        return  u;
    }

    /**
     * 当UserEntity修改时，isChange返回true
     * @return
     */
    public boolean isChange(){
        return change;
    }

    /**
     * 复位修改记录
     */
    public  void resetChange(){
        change=false;
    }

    private void checkNull(Object o){
        if(o==null){
            throw new NullPointerException("参数不能为 null");
        }
    }
    public UserEntityHelper addUserMessage(Message msg){
        this.checkNull(msg);

        if(u.msg==null){
            u.msg = new UserEntity.MessageCollections();
        }
        if(u.msg.user==null){
            u.msg.user = new ArrayList<>();
        }
        if(u.msg.user.size()==0){
            u.msg.user.add(msg);
            u.msg.userCount++;
            change = true;
        }else{
            int i=0;
            //幂等
            for(;i<u.msg.user.size();i++){
                Message m = u.msg.user.get(i);
                if(m.mid.equals(msg.mid)){
                    return this;
                }
                if(m.date.before(msg.date)){
                    break;
                }
            }
            u.msg.user.add(i,msg);
            u.msg.userCount++;
            change=true;
        }
        return this;
    }


    public UserEntityHelper addSysMessage(Message msg){
        this.checkNull(msg);

        if(u.msg==null){
            u.msg=new UserEntity.MessageCollections();
        }
        if(u.msg.sys==null){
            u.msg.sys = new ArrayList<>();
        }
        if(u.msg.sys.size()==0){
            u.msg.sys.add(msg);
            u.msg.sysCount++;
            change = true;
        }else{
            int i=0;
            //幂等
            for(;i<u.msg.sys.size();i++){
                Message m = u.msg.sys.get(i);
                if(m.mid.equals(msg.mid)){
                    return this;
                }
                if(m.date.before(msg.date)){
                    break;
                }
            }
            u.msg.sys.add(i,msg);
            u.msg.sysCount++;
            change=true;
        }
        return this;
    }


    public UserEntityHelper delUserMessage(List<String> midList){
        this.checkNull(midList);

        if(u.msg==null||u.msg.user==null){
            return this;
        }
        if(midList.get(0).equals("*")){
            u.msg.user.clear();
            u.msg.userCount=0;
            change = true;
            return this;
        }
        Iterator<Message> it = u.msg.user.iterator();
        while(it.hasNext()){
            Message m = it.next();
            if(midList.contains(m.mid)){
                if(m.read==false&&u.msg.userCount>0){
                    u.msg.userCount--;
                }
                it.remove();
                change=true;
            }
        }
        return this;
    }


    public UserEntityHelper delSysMessage(List<String> midList){
        this.checkNull(midList);

        if(u.msg==null||u.msg.sys==null){
            return this;
        }
        if(midList.get(0).equals("*")){
            u.msg.sys.clear();
            u.msg.sysCount=0;
            change = true;
            return this;
        }
        Iterator<Message> it = u.msg.sys.iterator();
        while(it.hasNext()){
            Message m = it.next();
            if(midList.contains(m.mid)){
                if(m.read==false&&u.msg.sysCount>0){
                    u.msg.sysCount--;
                }
                it.remove();
                change=true;
            }
        }
        return this;
    }

    public UserEntityHelper addLike(String gid){
        this.checkNull(gid);

        if(u.good==null)
            u.good = new UserEntity.GoodCollections();
        if(u.good.like==null)
            u.good.like= new ArrayList<>();
        if(!u.good.like.contains(gid)){
            u.good.like.add(0,gid);
            change = true;
        }
        return this;
    }

    /**
     * 批量删除收藏，为 * 时删除所有
     * @param gidList
     * @return
     */
    public UserEntityHelper delLike(List<String> gidList){
        this.checkNull(gidList);

        if(u.good==null||u.good.like==null){
            return this;
        }
        if("*".equals(gidList.get(0))){
            u.good.like = null;
            change=true;
            return this;
        }
        Iterator<String> it = u.good.like.iterator();
        while (it.hasNext()){
            String gid = it.next();
            if(gidList.contains(gid)){
                it.remove();
                change=true;
            }
        }
        return this;
    }

    public UserEntityHelper delSell(String id){
        this.checkNull(id);
        if(u.good==null||u.good.sell==null){
            return this;
        }
        if(u.good.sell.remove(id)){
            change=true;
        }
        return this;
    }

    public UserEntityHelper addSell(String id){
        this.checkNull(id);
        if(u.good==null)
            u.good = new UserEntity.GoodCollections();
        if(u.good.sell==null)
            u.good.sell= new ArrayList<>();
        if(!u.good.sell.contains(id)){
            u.good.sell.add(id);
            change=true;
        }
        return this;
    }

    public UserEntityHelper delSold(String id){
        this.checkNull(id);
        if(u.good==null||u.good.sold==null){
            return this;
        }
        if(u.good.sold.remove(id)){
            change=true;
        }
        return this;
    }

    public UserEntityHelper addSold(String id){
        this.checkNull(id);
        if(u.good==null)
            u.good = new UserEntity.GoodCollections();
        if(u.good.sold==null)
            u.good.sold= new ArrayList<>();
        if(!u.good.sold.contains(id)){
            u.good.sold.add(id);
            change=true;
        }
        return this;
    }

    public UserEntityHelper delWant(String id){
        this.checkNull(id);
        if(u.good==null||u.good.want==null){
            return this;
        }
        if(u.good.want.remove(id)){
            change=true;
        }
        return this;
    }

    public UserEntityHelper addWant(String id){
        this.checkNull(id);
        if(u.good==null)
            u.good = new UserEntity.GoodCollections();
        if(u.good.want==null)
            u.good.want= new ArrayList<>();
        if(!u.good.want.contains(id)){
            u.good.want.add(id);
            change=true;
        }
        return this;
    }

    public UserEntityHelper delBuy(String id){
        this.checkNull(id);
        if(u.good==null||u.good.buy==null){
            return this;
        }
        if(u.good.buy.remove(id)){
            change=true;
        }
        return this;
    }

    public UserEntityHelper addBuy(String id){
        this.checkNull(id);
        if(u.good==null)
            u.good = new UserEntity.GoodCollections();
        if(u.good.buy==null)
            u.good.buy= new ArrayList<>();
        if(!u.good.buy.contains(id)){
            u.good.buy.add(id);
            change=true;
        }
        return this;
    }

    public UserEntityHelper delBought(String id){
        this.checkNull(id);
        if(u.good==null||u.good.bought==null){
            return this;
        }
        if(u.good.bought.remove(id)){
            change=true;
        }
        return this;
    }

    public UserEntityHelper addBought(String id){
        this.checkNull(id);
        if(u.good==null)
            u.good = new UserEntity.GoodCollections();
        if(u.good.bought==null)
            u.good.bought= new ArrayList<>();
        if(!u.good.bought.contains(id)){
            u.good.bought.add(id);
            change=true;
        }
        return this;
    }
}
