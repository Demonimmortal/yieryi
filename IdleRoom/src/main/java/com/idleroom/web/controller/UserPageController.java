package com.idleroom.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;

import com.idleroom.web.cache.SessionManager;
import com.idleroom.web.entity.Comment;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.exception.UnPermissionException;
import com.idleroom.web.service.GoodService;
import com.idleroom.web.service.UserService;
import com.idleroom.web.util.ResultBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.idleroom.web.annotation.OnLine;
import com.idleroom.web.entity.ResultBean;

import java.util.Arrays;


/**
 * 用户登录后可进行的操作
 * 占用URL  /u
 * @author Unrestraint
 *
 */
@Validated
@Controller
@RequestMapping("/u")
public class UserPageController {
	private static final Logger log = LoggerFactory.getLogger(UserPageController.class);

	@Autowired
	GoodService goodService;
    @Autowired
    HttpServletRequest request;

    @Autowired
    SessionManager session;
	//用户个人中心主页
	@OnLine
	@GetMapping
	public String center(){
		UserEntity u = goodService.getUser((String)session.get("id"));
		if(u.good!=null){
		    if(u.good.sell!=null&&u.good.sell.size()>0){
                Iterable<GoodEntity> sell = goodService.listGoodById(u.good.sell);
                request.setAttribute("sell",sell);
            }
            if(u.good.sold!=null&&u.good.sold.size()>0){
                Iterable<GoodEntity> sold = goodService.listGoodById(u.good.sold);
                request.setAttribute("sold",sold);
            }
            if(u.good.want!=null&&u.good.want.size()>0){
                Iterable<GoodEntity> want = goodService.listGoodById(u.good.want);
                request.setAttribute("want",want);
            }
            if(u.good.like!=null&&u.good.like.size()>0){
                Iterable<GoodEntity> like = goodService.listGoodById(u.good.like);
                request.setAttribute("like",like);
            }
            if(u.good.buy!=null&&u.good.buy.size()>0){
                Iterable<GoodEntity> buy = goodService.listGoodById(u.good.buy);
                request.setAttribute("buy",buy);
            }
            if(u.good.bought!=null&&u.good.bought.size()>0){
		        Iterable<GoodEntity> bought=goodService.listGoodById(u.good.bought);
		        request.setAttribute("bought",bought);
            }
        }
        request.setAttribute("user",u);
		return "user";
	}

	//发布二手商品
	@OnLine
	@PostMapping("/postProduct")
	@ResponseBody
	public ResultBean<?> postProduct(@NotEmpty @Size(max=30,min = 1) String title, @NotEmpty @Size(max = 20,min = 2) String type, @RequestParam(value = "price",required = false,defaultValue = "0") @DecimalMin(value = "0",message = "价格不能小于0！") Float price, @NotEmpty @Size(max=200) String info, @RequestParam(value = "pic[]") String[] pic){
		log.info("title:{} type:{} price:{} info:{} ",title,type,price,info);

        String seller = (String)session.get("id");
		GoodEntity goodEntity = goodService.postProduct(title,type,price,info,seller, Arrays.asList(pic));

		return new ResultBean<>(goodEntity);
	}


	//发布求购商品
	@OnLine
	@PostMapping("/postWant")
	@ResponseBody
	public ResultBean<?> postWant(@NotEmpty @Size(max=30,min = 1) String title,@NotEmpty @Size(max = 20,min = 2) String type,@RequestParam(value = "price",required = false,defaultValue = "0") @DecimalMin(value = "0",message = "价格不能小于0！")Float price,@NotEmpty @Size(max=200) String info){
		log.info("title:{} type:{} price:{} info:{} ",title,type,price,info);

		String seller = (String)session.get("id");
		GoodEntity goodEntity = goodService.postWant(title,type,price,info,seller);

		return new ResultBean<>(goodEntity);
	}

    //擦亮,用户不能擦亮非自身商品或已删除商品
    @OnLine
    @PostMapping("/repost")
    @ResponseBody
    public ResultBean<?> repostProduct(@NotEmpty String gid){
        log.info("repost id:{}",gid);
        String uid = (String)session.get("id");
        return new ResultBean<>(goodService.repost(uid,gid));
    }

	//求购物品被响应
    @OnLine
    @PostMapping("/haveGood")
    @ResponseBody
    public ResultBean<?> haveGood(@NotEmpty String gid){
	    GoodEntity g = goodService.getGood(gid);
	    if(g.status.equals(GoodEntity.STATUS.WANT)){
            UserEntity u = goodService.haveGood(gid,g.seller,(String)session.get("id"));
            return  new ResultBean<>(u);
        }else{
            return new ResultBean<>(1062,"该商品已不再求购！");
        }
    }


    //添加收藏，验证商品是否存在
	@OnLine
    @PostMapping("/addLike")
    @ResponseBody
    public ResultBean<?> addLike(@NotEmpty String gid){
        String uid =(String) session.get("id");
        goodService.addLike(uid,gid);
        return ResultBeanFactory.simple();
    }

    /**
     * 删除收藏，不验证商品是否存在
     * @param gid 删除收藏列表 全部删除 为 * 删除多个 以 : 分隔
     */
    @OnLine
    @PostMapping("/delLike")
    @ResponseBody
    public ResultBean<?> delLike(@NotEmpty String gid){
        String uid = (String) session.get("id");
        goodService.delLike(uid,Arrays.asList(gid.split(":")));
        return ResultBeanFactory.simple();
    }


    //修改二手商品状态及用户购买、删除
	@OnLine
	@PostMapping("/changeStatus")
	@ResponseBody
	public ResultBean<?> changeStatus(@NotEmpty String gid,@NotEmpty @Pattern(regexp = GoodEntity.STATUS.BARGAIN+"|"+GoodEntity.STATUS.DELETE+"|"+GoodEntity.STATUS.OFF+"|"+GoodEntity.STATUS.SELL+"|"+GoodEntity.STATUS.SOLD) String status){

		String uid = (String)session.get("id");
        log.info("id:{} status:{} uid:{}",gid,status,uid);
		return new ResultBean<>(goodService.changeStatus(gid,uid,status));
	}


    /**
     * 回复
     * @param gid  商品gid
     * @param to  向谁回复,为空时默认回复商品所有人 ,可以回复自身
     * @param text  内容
     * @param group  回复组， 为空时新启一组，不为空表示以首个回复id为组id
     * @return
     */
	@OnLine
	@PostMapping("/comment")
	@ResponseBody
	public ResultBean<?> comment(@NotEmpty String gid,String to,@NotEmpty String text,@RequestParam(value = "group",required = false,defaultValue = "") String group){
	    String from = (String)session.get("id");
        log.info("from:{} to:{} text:{} group:{}",from,to,text,group);
//        if(from.equals(to)){
//            return new ResultBean<>(1061,"不能回复自身！");
//        }
	    Comment comment = goodService.postComment(gid,from,to,text,group);
		return new ResultBean<>(comment);
	}

    /**
     * 删除回复
     * @param gid 该回复在的商品 id
     * @param cid 该回复的编号
     * @param group 该回复所在的组
     * @return
     */
	@OnLine
    @PostMapping("/deleteComment")
    @ResponseBody
    public ResultBean<?> deleteComment(@NotEmpty String gid,@NotEmpty String cid,@NotEmpty String group){
        String uid =(String) session.get("id");
        log.info("delete Comment :{} {} {}",gid,cid,group);
        goodService.deleteComment(uid,gid,cid,group);
        return ResultBeanFactory.simple();
    }


	//评价
	@OnLine
	@PostMapping("/showComment")
	public String comment(String id,String text){
		return "showComment";
	}
	

}
