package com.idleroom.web.controller;

import com.idleroom.web.cache.SessionManager;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.ResultBean;
import com.idleroom.web.service.GoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.idleroom.web.annotation.NotOnLine;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * 非登录页面
 * 占用URL  / ;/login; /we; /b; /w; /i; /manager;
 * 
 * @author Unrestraint
 *
 */
@Validated
@Controller
@RequestMapping("/")
public class PageController {
	private static final Logger log  = LoggerFactory.getLogger(PageController.class);

//	@Autowired
//    SessionManager session;
//	@GetMapping("/create")
//    @ResponseBody
//    public String test(){
//	    String id=session.createSessionId();
//	    session.begin(id);
//	    session.set(id,"id","中蒙好");
//	    return id;
//    }
//    @GetMapping("/test")
//    @ResponseBody
//    public String test2(){
//	    return (String)session.get("id");
//    }

	@Autowired
	GoodService goodService;
	//主页  该页面最终放在Nginx静态资源路径 url /
	@GetMapping("")
	public String index(){
		return "index";
	}
	//登录、注册界面
	@NotOnLine
	@GetMapping("/login")
	public String login(){
		return "login";
	}
	
	//平台介绍页面
	@GetMapping("/we")
	public String our(){
		return "we";
	}


    //该页面最终放在Nginx静态资源路径 url /b
    @GetMapping("/b")
    public String buy(){
	    return "goods";
    }

    //该页面最终放在Nginx静态资源路径 url /w
    @GetMapping("/w")
    public String want(){
        return "needs";
    }


    /**
     * 商品详情获取
     * @param id
     * @return
     */
    @GetMapping("/i")
    public String detail(){
        return "goodsdetail";
    }
	@GetMapping("/i/{id}")
    @ResponseBody
	public ResultBean<?> infoGet(@PathVariable("id") String id){
		log.info("id:{}",id);
        return  new ResultBean<>(goodService.getGood(id));
	}

	@PostMapping("/i")
    @ResponseBody
    public ResultBean<?> infoPost(@NotEmpty  String id){
        log.info("post id:{}",id);
        GoodEntity g = goodService.getGood(id);
        if(g.status.equals(GoodEntity.STATUS.DELETE))
            throw new NoSuchElementException();
        return  new ResultBean<>(g);
    }


    /**
     * 分页查询
     * @param type   类型 多个类型以 : 间隔 ; 所有类型都查询则为 *
     * @param status 查询的状态，不支持多状态查询
     * @param page  页码 从1开始
     * @param size  分页大小 默认30页
     * @param sort 排序类型 默认降序
     * @return
     */
    @PostMapping("/page")
    @ResponseBody
    public ResultBean<?> getPage(@NotEmpty String type, @NotEmpty  String status, @RequestParam(defaultValue = "") String page,@RequestParam(defaultValue = "") String size,@RequestParam(defaultValue = "date")  String sort){
        log.info("more type page :{} {} {}",type,status,size);
        int p = Pattern.compile("\\d{1,8}").matcher(page).matches() ? Integer.valueOf(page):0;
        if(p<0) p=0;
        int s = Pattern.compile("\\d{1,2}").matcher(size).matches() ? Integer.valueOf(size):30;
        if(!Pattern.compile("date|click").matcher(sort).matches()) sort="date";
        return  new ResultBean<>(goodService.listGood(Arrays.asList(type.split(":")),status,p,s,sort));
    }


    /**
     * 批量查询
     * @param gid 多个id以: 分隔
     * @return
     */
    @PostMapping("/listGood")
    @ResponseBody
    public ResultBean<?> listGood(@NotEmpty String gid){
        return new ResultBean<>(goodService.listGoodById(Arrays.asList(gid.split(":"))));
    }

    /**
     * 关键字查找
     * @param title
     * @param info
     * @param status
     * @param page
     * @return
     */
    @PostMapping("/search")
    @ResponseBody
    public ResultBean<?> get(@NotEmpty String title,@NotEmpty String info,@NotEmpty String status,@RequestParam(defaultValue = "") String page,@RequestParam(defaultValue = "") String size){
        int p = Pattern.compile("\\d{1,8}").matcher(page).matches()?Integer.valueOf(page):0;
        if(p<0) p=0;
        int s = Pattern.compile("\\d{1,2}").matcher(size).matches() ? Integer.valueOf(size):30;
        log.info("search :{} {} {}",title,info,status);
        return new ResultBean<>(goodService.listGoodLike(title,info,status,p,s,"date"));
    }

    /**
     *
     * @return
     */
    @PostMapping("/sample")
    @ResponseBody
    public ResultBean<?> get(@RequestParam(defaultValue = "") String size){
        int s = Pattern.compile("\\d{1,2}").matcher(size).matches() ? Integer.valueOf(size):30;
        return new ResultBean<>(goodService.listGoodBySample("出售",s));
    }

}
