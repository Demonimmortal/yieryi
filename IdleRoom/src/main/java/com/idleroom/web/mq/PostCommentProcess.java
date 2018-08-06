package com.idleroom.web.mq;

import com.idleroom.web.annotation.CacheAfter;
import com.idleroom.web.annotation.CacheLock;
import com.idleroom.web.dao.GoodDao;
import com.idleroom.web.dao.UserDao;
import com.idleroom.web.entity.Comment;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.Message;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.util.MessageBuilder;
import com.idleroom.web.util.UserEntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class PostCommentProcess {

    @Autowired
    private GoodDao goodDao;
    @Autowired
    private UserDao userDao;


    /**
     * 以评论为组id，当 group isEmpty 时表示需要新启一组
     * 虽然看起来整个插入过程复杂，但在消息队列未出错的情况下，查找只会进行1、2次
     * @param id
     * @param com
     * @param group
     * @return
     */
    @CacheLock("good:lock")
    @CacheAfter("good")
    public  GoodEntity postCommentToGood(String id,Comment com,String group){
        GoodEntity g = goodDao.getGoodEntity(id);
        if(g.comment==null)
            g.comment = new ArrayList<>();


        int size = g.comment.size();
        //当为空数组时，直接添加
        //当需要新启一行，且com的时间为最新时间时，after保证必须是大于第一个时间
        if(size==0 || (group.isEmpty()&&com.date.after(g.comment.get(0).get(0).date)) ){
            ArrayList newGroup = new ArrayList<Comment>();
            newGroup.add(com);
            g.comment.add(0,newGroup);
            goodDao.save(g);
            return  g;
        }

        Comment c;
        if(group.isEmpty()){
            //查找最近的
            int i=0;
            for(;i<size;i++){
                c = g.comment.get(i).get(0);
                //如果已存在，则不修改
                if(c.cid.equals(com.cid)){
                    return  null;
                }
                //当查找到更早的回复，就停止,使用before保证对相同时间也在检查范围内
                if(c.date.before(com.date)){
                    break;
                }
            }

            ArrayList newGroup = new ArrayList<Comment>();
            newGroup.add(com);
            g.comment.add(i,newGroup);
            goodDao.save(g);
            return g;
        }

        //对评论进行回复
        for(List<Comment> list:g.comment){
            if(list.get(0).cid.equals(group)){
                int i = list.size()-1;
                for(;i>=0;i--){
                    if(com.cid.equals(list.get(i).cid)){
                        return  null;
                    }
                    //before 保证相同时间也会检查
                    if(list.get(i).date.before(com.date)){
                        break;
                    }
                }
                list.add(i+1,com);
                goodDao.save(g);
                return g;
            }
        }
        //该错误是由于前端提交的逻辑有问题产生的，如果出错放弃处理
        throw new NoSuchElementException(com.toString()+" group :"+group);
    }


    /**
     * 将回复信息添加到被回复人消息里
     * @param id
     * @param com
     * @return
     */

    @CacheLock("user:lock")
    @CacheAfter("user")
    public UserEntity postMessageToReceiver(String id,Comment com){
        UserEntity u = userDao.getUserEntity(id);

        Message msg = MessageBuilder.build(com);

        if(new UserEntityHelper(u).addUserMessage(msg).isChange()){
            return u;
        }else{
            return  null;
        }
    }

    //由于设计上的修改该方法作废
    /**
     * 如果回复将新启一组，那么首先根据日期和cid进行查找，
     * 如果第一次查找就能确定Comment是最新日期，那么就将回复直接插入
     * 如果Comment的日期不是最新日期，那么就更新Comment的日期，保证按时间排序
     * 如果Comment查找到了，那么就跳过
     * @param id
     * @param com
     * @return
     */
//    @CacheLock("good:lock")
//    @CacheAfter("good")
//    public GoodEntity postCommentToGood(String id, Comment com,int group){
//        //验证存在
//        GoodEntity g = goodDao.getGoodEntity(id);
//
//        if(g.comment==null)
//            g.comment=new ArrayList<>();
//
//        //幂等
//        int size = g.comment.size();
//        //当为空数组时，直接添加
//        //当需要新启一行，且com的时间为最新时间时，after保证必须是大于第一个时间
//        if (size==0 || (group<=0||group>size) && com.date.after(g.comment.get(0).get(0).date) ){
//            ArrayList newGroup = new ArrayList<Comment>();
//            newGroup.add(com);
//            g.comment.add(0,newGroup);
//            goodDao.save(g);
//            return  g;
//        }
//
//        Comment c;
//        //当需要新启一行，但回复时间在回复列表最后一个时间之前 或 与最后一个时间相同
//        if(group<=0||group>size){
//            //查找最近的
//            for(int i=0;i<size;i++){
//                c = g.comment.get(i).get(0);
//                //如果已存在，则不修改
//                if(c.cid.equals(com.cid)){
//                    return  null;
//                }
//                //当查找到更早的回复，就停止,使用before保证对相同时间也在检查范围内
//                if(c.date.before(com.date)){
//                    break;
//                }
//            }
//
//            ArrayList newGroup = new ArrayList<Comment>();
//            //修改为最新时间
//            com.date= new Date();
//            newGroup.add(com);
//            g.comment.add(0,newGroup);
//            goodDao.save(g);
//            return g;
//        }
//
//        //当添加到留言组中
//        if(group>0&&group<=size){
//
//            List<Comment> list = g.comment.get(group-1);
//            int i=list.size()-1;
//            for(;i>=0;i--){
//                if(com.cid.equals(list.get(i).cid)){
//                    return  null;
//                }
//                //before 保证相同时间也会被检查
//                if(list.get(i).date.before(com.date)){
//                    break;
//                }
//            }
//            list.add(i+1,com);
//            goodDao.save(g);
//            return g;
//        }
//        throw new UnReachException();
//    }


    //以下内容由于需求更改，作废
    /**
     * 回复我的
     * @param id
     * @param comment
     * @return
     */

//    @CacheLock("user:lock")
//    @CacheAfter("user")
//    public UserEntity postMessageToUserFrom(String id,Comment comment){
//        UserEntity u = userDao.getUserEntity(id);
//
//        if(u.id.equals(comment.to)){
//            if(u.comment==null){
//                u.comment = new UserEntity.CommentCollections();
//            }
//            if(u.comment.from==null){
//                u.comment.from=new ArrayList<>();
//            }
//            if(u.msg==null)
//                u.msg=new ArrayList<>();
//
//
//            //添加到回复我的
//            if(u.comment.from.size()==0){
//                u.comment.from.add(comment);
//            }else{
//                for(int i=0;i<u.comment.from.size();i++){
//                    Comment c = u.comment.from.get(i);
//                    if(c.cid.equals(comment.cid)){
//                        break;
//                    }
//                    //before保证对相同时间进行检查
//                    if(c.date.before(comment.date)){
//                        u.comment.from.add(i,comment);
//                        break;
//                    }
//                }
//            }
//
//            Message msg = new Message();
//            msg.mid = comment.cid;
//            msg.date= comment.date;
//            msg.text =  new StringBuilder().append(" 用户 ").append(comment.from).append(" 在商品 ").append(comment.title).append(" 回复了您一条消息！").toString();
//
//            //通知
//            if(u.msg.size()==0){
//                u.msg.add(msg);
//                u.msgCount++;
//            }else{
//                for(int i=0;i<u.msg.size();i++){
//                    Message m = u.msg.get(i);
//                    if(m.mid.equals(msg.mid)){
//                        break;
//                    }
//                    if(m.date.before(msg.date)){
//                        u.msg.add(i,msg);
//                        u.msgCount++;
//                        break;
//                    }
//                }
//            }
//
//            userDao.save(u);
//            return  u;
//        }
//
//        throw new UnReachException();
//    }
//
//    /**
//     * 我的回复
//     * @param id
//     * @param comment
//     * @return
//     */
//    @CacheLock("user:lock")
//    @CacheAfter("user")
//    public UserEntity postMessageToUserTo(String id,Comment comment){
//        UserEntity u = userDao.getUserEntity(id);
//
//        if(u.id.equals(comment.from)){
//            if(u.comment==null){
//                u.comment=new UserEntity.CommentCollections();
//            }
//            if(u.comment.to==null){
//                u.comment.to=new ArrayList<>();
//            }
//
//            if(u.comment.to.size()==0){
//                u.comment.to.add(comment);
//            }else{
//                for(int i=0;i<u.comment.to.size();i++){
//                    Comment c = u.comment.to.get(i);
//                    if(c.cid.equals(comment.cid)){
//                        break;
//                    }
//                    //before保证对相同时间进行检查
//                    if(c.date.before(comment.date)){
//                        u.comment.to.add(i,comment);
//                        break;
//                    }
//                }
//            }
//
//            userDao.save(u);
//            return  u;
//        }
//
//        throw new UnReachException();
//    }
}
