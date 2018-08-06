/**
 * @author Unrestraint
 */
var $;
var laytpl;
var form;
function show(val){
    $("#framebody").attr("src",val);
};

//JavaScript代码区域
layui.use(['element','layer','laytpl','layedit','form'], function(){
    layui.layer;
    var element = layui.element;
    $ = layui.jquery;
    laytpl  = layui.laytpl;
    form = layui.form;

    $.post = function (url,data,callback,error) {
        $.ajax({
            url:url,
            data:data,
            type:"POST",
            cache:false,
            error:function (err) {
                console.log(err);
                if(error) error(err);
            },
            success:function(data){
                console.log(data);
                if(data.status==0){
                    if(callback) callback(data.data);
                }else{
                    console.log(data);
                    switch (data.status){
                        case 404: layer.msg("未查找到详细信息！", {icon: 5});break;
                        default: layer.msg(data.msg, {icon: 5});
                    }
                    if(error)error(data);
                }
            }
        });
    };

    ready();
});

function ready() {
    //listUser(0);
}

//退出
function logOut(){
    $.post("/user/logOut");
}

function renderContent(template){
    $("#content").html(template.innerHTML);
}
//列出用户表
function listUser(url,template,page,size){
    list(url,template,{},page,size);
}
function listGood(){
    list("/admin/listGood",goodInfo_template,{status:"出售:正在交易:下架:出售成功",target:"出售"});
}
function listWant(){
    list("/admin/listGood",goodInfo_template,{status:"求购",target:"求购"});
}
function listOpenReport() {
    renderContent(report_manager);
    list("/admin/listReport",reportInfo_template,{status:"待审核"});
}
function listCloseReport() {
    renderContent(report_manager);
    list("/admin/listReport",reportInfo_template,{status:"已审核"});

}

//加载事件
function  renderEvent(template){
    switch (template){
        case userInfo_template:renderUser();break;
        case goodInfo_template:renderGood();break;
        case reportInfo_template:renderReport();break;
    }
}

function list(url,template,param,page,size){
    if(!page){
        page=0;
    }
    if(!size){
        size = 30;
    }
    param.page = page;
    param.size = size;
    console.log(param);
    //请求数据
    $.post(url,param,function (data) {

        //渲染内容部分
        var html="";
        data.content.forEach(function(data){
            laytpl(template.innerHTML).render(data,function(item){
                html += item;
            })
        });

        $("#info").html(html);
        renderEvent(template);

        //渲染分页
        layui.use(['laypage', 'layer'], function(){
            var laypage = layui.laypage,layer = layui.layer;
            laypage.render({
                elem: 'page'
                ,count:data.totalElements
                ,limit: size
                ,curr:data.number+1
                ,jump:function (obj,first) {
                    if(!first){
                        list(url,template,param,obj.curr-1,size);
                    }
                }
            });
        });

    });
}

//根据id查询单个信息
function get(url,template){
    var id = $("#id").val();
    if(id&&id!=""){

        $.post(url,{id:id},function (data) {

            $("#page").remove();
            laytpl(template.innerHTML).render(data, function (item) {
                $("#info").html(item);
            });

            renderEvent(template);
        }
        ,function (err) {
        });
    }
}


/**
 * 用户管理部分
 * @author Unrestraint
 */
//加载事件
function  renderUser() {
    //封禁
    $("_closure").unbind();
    $(".closure").click(function(){

        var tr  = $(this).parents("tr");
        var id = tr.attr("id");

        open({
            content:'确定封禁 '+ id +' 账号吗？'
            ,url:   "/admin/changeUserType"
            ,param:{uid:id,type:"封禁"}
            ,msg:   '已封禁用户 '+id
            ,tr:    tr
            ,template:userInfo_template
        });
    });


    //解封
    $("._closure").unbind();
    $("._closure").click(function () {

        var tr  = $(this).parents("tr");
        var id = tr.attr("id");

        open({
            content:'确定解封 '+ id +' 账号吗？'
            ,url:   "/admin/changeUserType"
            ,param:{uid:id,type:"普通"}
            ,msg:   '已解封用户 '+id
            ,tr:    tr
            ,template:userInfo_template
        });
    });


    //修改
    $(".update").unbind();
    $(".update").click(function(){

        //获取该行数据
        var tr = $(this).parents("tr");
        var data  = new Object();
        data.id =tr.attr("id");
        var td = tr.find("td");
        data.name = td.eq(1).text();
        data.pwd  = td.eq(2).text();
        data.phone= td.eq(3).text();
        data.mail = td.eq(4).text();
        data.wechat= td.eq(5).text();
        data.qq   = td.eq(6).text();
        data.type = td.eq(7).text();

        var param = new Object();
        param.type = data.type;
        param.uid  = data.id;

        laytpl(change_info.innerHTML).render(data, function (item) {

            open({
                content:item
                ,url:   "/admin/changeUserType"
                ,param: param
                ,msg:   '已修改用户 '+data.id +" 的信息"
                ,tr:    tr
                ,template:userInfo_template
            });

            form.render();
            form.on('radio', function(data){
                param.type = data.value;
            });
        });
    });

}


/**
 * 二手商品管理部分
 * @author Unrestraint
 */
//加载时间
function renderGood() {

    //删除
    $(".delete").unbind();
    $(".delete").on("click",function(){

        var tr = $(this).parents("tr");
        var id = tr.attr("id");

        open({
            content:'确定删除 '+ id +' 吗？'
            ,url:   "/admin/changeGoodStatus"
            ,param: {gid:id,status:"删除"}
            ,msg:   '已修改 '+id+' 状态为删除！'
            ,tr:    tr
        });
    });

    //下架
    $(".off").unbind();
    $(".off").on("click",function () {

        var tr = $(this).parents("tr");
        var id = tr.attr("id");

        open({
            content:'确定下架 ' + id + ' 吗？'
            ,url:   "/admin/changeGoodStatus"
            ,param: {gid: id, status: "下架"}
            ,msg:   '已修改 ' + id + ' 状态为下架！'
            ,tr:    tr
            ,template:goodInfo_template
        });

    });
}

/**
 * 举报信息管理部分
 * @author Unrestraint
 */
function  renderReport() {
    $(".uncheck").unbind();
    $(".uncheck").on("click",function () {

        var tr = $(this).parents("tr");
        var id = tr.attr("id");

        open({
            content:'确定该条举报信息已审核吗？'
            ,url:   "/admin/dealReport"
            ,param: {sid: id, status: "已审核"}
            ,msg:   '已审核 ' + id + ' ！'
            ,tr:    tr
           // ,template:reportInfo_template
        });
    });
}
//全选
function checkAll() {
    var all = document.getElementById('all');
    var one = document.getElementsByClassName("check-box");
    if(all.checked==true){
        for(var i=0;i<one.length;i++){
            one[i].checked=true;
        }
    }else{
        for(var j=0;j<one.length;j++){
            one[j].checked=false;
        }
    }
}
//处理举报信息
function  delReport() {
    var idList = listCheckedId();
    console.log(idList);
    if(idList&&idList!=""){
        $.post("/admin/delReport",{sid:idList},function (data) {
            $('input[name="check-box"]:checked').each(function () {
                $(this).parents("tr").remove();
            })
        });
    }
}
function listCheckedId() {
    var id = "";
    var first = true;
    $('input[name="check-box"]:checked').each(function () {
        if(first){
            id = $(this).parents("tr").attr("id");
            first = false;
        }else{
            id+=":"+$(this).parents("tr").attr("id");
        }
    });
    return id;
}


//打开一个窗口
//注意引用传值问题
function open(data){
    layer.open({
        title: '系统提示'
        ,content: data.content
        ,btn: ['确定','取消']
        ,yes:function(index){

            $.post(data.url,data.param,function(result){
                layer.msg(data.msg,{icon: 6});
                if(data.template)
                    reRenderInfo(data.tr,data.template,result);
                else
                    reRenderInfo(data.tr);
                renderEvent(data.template);
                layer.close(index);
            })
        }
    });
}
//重新渲染table中的内容
function  reRenderInfo(tr,template,data) {
    var idx = tr[0].rowIndex;
    tr.remove();
    laytpl(template.innerHTML).render(data, function (item) {
        var newTr = document.getElementById("info").insertRow(idx-1);
        newTr.innerHTML = item;
        newTr.id = data.id;
        renderUser();
    });
}


