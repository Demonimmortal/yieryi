package com.idleroom.web.controller;

import com.idleroom.web.annotation.OnLine;
import com.idleroom.web.cache.SessionManager;
import com.idleroom.web.entity.GoodEntity;
import com.idleroom.web.entity.ResultBean;
import com.idleroom.web.entity.SystemEntity;
import com.idleroom.web.entity.UserEntity;
import com.idleroom.web.service.GoodService;
import com.idleroom.web.service.SystemService;
import com.idleroom.web.util.ResultBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Arrays;

/**
 * 管理员的操作
 * @author  Unrestraint
 */
@Validated
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static  final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    GoodService goodService;
    @Autowired
    SystemService sysService;

    @Autowired
    SessionManager session;

    //管理员界面
    @OnLine(type = UserEntity.TYPE.ADMIN)
    @GetMapping("")
    public String admin(){
        log.info("admin");
        return "admin.html";
    }

    /**
     * 显示用户列表
     */
    @OnLine(type = UserEntity.TYPE.ADMIN)
    @PostMapping("/listUser")
    @ResponseBody
    public ResultBean<?> listUser(@RequestParam(defaultValue = "") String page,@RequestParam(defaultValue = "") String size,@RequestParam(defaultValue = "date")  String sort){
        log.info("list User :{} {} {}",page,size,sort);
        int p = java.util.regex.Pattern.compile("\\d{1,8}").matcher(page).matches() ? Integer.valueOf(page):0;
        if(p<0) p=0;
        int s = java.util.regex.Pattern.compile("\\d{1,2}").matcher(size).matches() ? Integer.valueOf(size):30;
        if(!java.util.regex.Pattern.compile("date").matcher(sort).matches()) sort="date";

        return  new ResultBean<>(sysService.listUser(p,s,sort));
    }

    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    @OnLine(type=UserEntity.TYPE.ADMIN)
    @PostMapping("/getUser")
    @ResponseBody
    public ResultBean<?> getUser(@NotEmpty String id){
        return new ResultBean<>(sysService.getUser(id));
    }

    /**
     * 修改用户类型
     * @param uid
     * @param type
     * @return
     */
    @OnLine(type = UserEntity.TYPE.ADMIN)
    @PostMapping("/changeUserType")
    @ResponseBody
    public ResultBean<?> changeUserType(@NotEmpty String uid,@NotEmpty @Pattern(regexp = UserEntity.TYPE.ADMIN+"|"+UserEntity.TYPE.GENERAL+"|"+UserEntity.TYPE.BAN) String type){
        log.info("change user type :{} {}",uid,type);
        String id = (String)session.get("id");
        if(uid.equals(id)){
            return new ResultBean<>(1052,"修改自身类型不合法！");
        }
        UserEntity u = sysService.changeUserType(uid,type);
        u.msg=null;
        u.good=null;
        return new ResultBean<>(u);
    }


    /**
     * 查询商品列表
     * @param target 求购或出售
     * @param status 状态，多个状态以 :分割 全查询 为 *
     * @param page 分页
     * @param size 大小
     * @param sort 排序
     * @return
     */
    @OnLine(type=UserEntity.TYPE.ADMIN)
    @PostMapping("/listGood")
    @ResponseBody
    public ResultBean<?> listGood(@NotEmpty @Pattern(regexp = GoodEntity.TARGET.WANT+"|"+GoodEntity.TARGET.SELL)String target,@NotEmpty String status, @RequestParam(defaultValue = "") String page, @RequestParam(defaultValue = "") String size, @RequestParam(defaultValue = "date")  String sort){
        log.info("list good :{} {}",target,status);
        int p = java.util.regex.Pattern.compile("\\d{1,8}").matcher(page).matches() ? Integer.valueOf(page):0;
        if(p<0) p=0;
        int s = java.util.regex.Pattern.compile("\\d{1,2}").matcher(size).matches() ? Integer.valueOf(size):30;
        if(!java.util.regex.Pattern.compile("date").matcher(sort).matches()) sort="date";

        return new ResultBean<>(sysService.listGood(target, Arrays.asList(status.split(":")),p,s,sort));
    }

    /**
     * 修改商品状态
     * @param gid
     * @param status 只能为下架或删除
     * @return
     */
    @OnLine(type=UserEntity.TYPE.ADMIN)
    @PostMapping("/changeGoodStatus")
    @ResponseBody
    public ResultBean<?> changeGoodStatus(@NotEmpty String gid,@NotEmpty @Pattern(regexp =GoodEntity.STATUS.OFF +"|"+GoodEntity.STATUS.DELETE)String status){
        GoodEntity g = sysService.changeStatus(gid,status);
        g.comment=null;
        g.pic = null;
        return new ResultBean<>(g);
    }

    /**
     * 举报，为防止用户恶意举报，单个用户每分钟只能举报一次，在service层控制
     * @param gid
     * @param text
     * @return
     */
    @OnLine
    @PostMapping("/report")
    @ResponseBody
    public ResultBean<?> report(@NotEmpty String gid,String text){
        String uid = (String)session.get("id");
        sysService.report(uid,gid,text);
        return ResultBeanFactory.simple();
    }


    /**
     * 处理举报信息
     * @param sid
     * @param status
     * @return
     */
    @OnLine(type=UserEntity.TYPE.ADMIN)
    @PostMapping("/dealReport")
    @ResponseBody
    public ResultBean<?> dealReport(@NotEmpty String sid,@NotEmpty @Pattern(regexp = SystemEntity.STATUS.CLOSE) String status){
        String admin = (String) session.get("id");
        return new ResultBean<>(sysService.dealReport(sid,admin,status));
    }

    /**
     * 删除举报信息
     * @param sid 多个id以:分割
     */
    @OnLine(type=UserEntity.TYPE.ADMIN)
    @PostMapping("/delReport")
    @ResponseBody
    public ResultBean<?> delReport(@NotEmpty String sid){
        return  new ResultBean<>(sysService.delReport(Arrays.asList(sid.split(":"))));
    }

    /**
     * 举报信息查询
     * @param who  被举报人  可为空
     * @param type  举报类型 类型字段 为空表示查询所有，否则则是单个类型
     * @param status  状态 举报处理的状态，为*表示查询所有 多个状态以 : 分割
     * @return
     */
    @OnLine(type=UserEntity.TYPE.ADMIN)
    @PostMapping("/listReport")
    @ResponseBody
    public ResultBean<?> listReport(String who, String type, @RequestParam(defaultValue = "") String status, @RequestParam(defaultValue = "") String page, @RequestParam(defaultValue = "") String size){
        int p = java.util.regex.Pattern.compile("\\d{1,8}").matcher(page).matches() ? Integer.valueOf(page):0;
        if(p<0) p=0;
        int s = java.util.regex.Pattern.compile("\\d{1,2}").matcher(size).matches() ? Integer.valueOf(size):30;

        log.info("list report : {} {} {}",who,type,status);
        return new ResultBean<>(sysService.listReport(who,type,Arrays.asList(status.split(":")),p,s));
    }
}
