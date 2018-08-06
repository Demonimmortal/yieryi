function size(e,max_w,max_h){
    var image = new Image();
    image.src = e.src;
    //原图片原始地址（用于获取原图片的真实宽高，当<img>标签指定了宽、高时不受影响）
    var maxWidth = max_w;
    var maxHeight = max_h;
    var width='100%';
    var height='100%';
    // 当图片比图片框小时不做任何改变
    if (image.width < maxWidth&& image.height < maxHeight) {
        width = image.width+'px';
        height = image.height+'px';
    }
    else //原图片宽高比例 大于 图片框宽高比例,则以框的宽为标准缩放，反之以框的高为标准缩放
    {
        if (maxWidth/ maxHeight  <= image.width / image.height) //原图片宽高比例 大于 图片框宽高比例
        {
            width = maxWidth+'px';   //以框的宽度为标准
            height = 'auto';
        }
        else {   //原图片宽高比例 小于 图片框宽高比例
            width = maxHeight  * (image.width / image.height)+'px';
            height = 'auto';   //以框的高度为标准
        }
    }
    e.style.width=width;
    e.style.height=height;
};
function changeAccountInfo(val){
    if(val=='Acc'){
        var dom1 = $('#account');
        var dom2 = $('#submitAcc');
    }else{
        var dom1 = $('#person');
        var dom2 = $('#submitPer');
    }
    dom1.find('input').prop('readonly',false);
    dom1.find('textarea').prop('readonly',false);
    dom1.find("input[type='radio']").prop('disabled',false);
    dom2.removeClass('hide');
}
function cancelReset(val){
    if(val=='Acc'){
        var dom1 = $('#account');
        var dom2 = $('#submitAcc');
    }else{
        var dom1 = $('#person');
        var dom2 = $('#submitPer');
    }
    dom1.find('input').prop('readonly',true);
    dom1.find("input[type='radio']").prop('disabled',true);
    dom1.find('textarea').prop('readonly',true);
    dom2.addClass('hide');
}
function submitChangeInfo(val){
    var data = {};
    var bootstrapValidator1;
    if(val=='Acc'){
        var dom = $('#account');
        data.name = dom.find("input[name='Name']").val();
        data.campus = dom.find("input[name='campus']").val();
        data.sign = dom.find("textarea[name='sign']").val();
        bootstrapValidator1 = dom.data('bootstrapValidator').validate();
    }else{
        var dom = $('#person');
        data.sex = dom.find("input[type='radio']:checked").val();
        data.phone = dom.find("input[name='phone']").val();
        data.mail = dom.find("input[name='mail']").val();
        data.wechat = dom.find("input[name='wechat']").val();
        data.qq = dom.find("input[name='qq']").val();
        bootstrapValidator1 = dom.data('bootstrapValidator').validate();
    }
    var _data =data;
    if(bootstrapValidator1) {
        upPic.addLoading();
        $.ajax({
            url: '/user/changeInfo',
            type: 'post',
            data: data,
            async: true,
            error: function (data) {
                setTimeout(function () {
                    $('.tip-box').html('\
                        			<span id="logo" class="fa fa-times-circle failed"></span>\
                        			<div id="new_goods_tip" class="tip-text">服务器错误</div>\
                        			');
                }, 500);
                setTimeout(function () {
                    upPic.removeLoading();
                }, 1000);
            },
            success: function (data) {
                if (data.status == 0) {
                    if(val=='Acc'){
                        $(".u_name").text(_data.name);
                        $(".u_campus").text(_data.campus);
                        $(".u_sign").text(_data.sign);
                    }
                    $("name").text()
                    $('.tip-box').html('\
                        		<span id="logo" class="fa fa-check-circle success"></span>\
                        		<div id="new_goods_tip" class="tip-text">修改成功</div>\
                        ');
                    setTimeout(function () {
                        $("#new").modal('hide');
                        upPic.removeLoading();
                        cancelReset(val);
                    }, 1000);
                } else if(status==1060){
                    $('.tip-box').html('\
                        	<span id="logo" class="fa fa-times-circle failed"></span>\
                        	<div id="new_goods_tip" class="tip-text">'+data.msg+'</div>\
                     ');
                    setTimeout(function () {
                        $("#new").modal('hide');
                        upPic.removeLoading();
                    }, 2000);
                } else{
                    $('.tip-box').html('\
                        	<span id="logo" class="fa fa-times-circle failed"></span>\
                        	<div id="new_goods_tip" class="tip-text">'+data.msg+'</div>\
                     ');
                    setTimeout(function () {
                        upPic.removeLoading();
                    }, 2000);
                }
            }
        });
    }
}
var fields = {
    Name: {
        message:'test',
        validators: {
            stringLength: {
                min:0,
                max: 15,
                message: '昵称不超过15个字符'
            }
        }
    },
    sign:{
        validators: {
            stringLength: {
                min:0,
                max: 200,
                message: '签名不能超过200字'
            }
        }
    },
    phone:{
        validators: {
            regexp:{
                regexp:/^([0-9]{11})$/,
                message:'手机号格式不正确'
            }
        }
    },
    mail:{
        validators: {
            regexp:{
                regexp:/^(\w+(.\w+)*@\w+(.\w+)+)$/,
                message:'邮箱格式不正确'
            }
        }
    },
    qq:{
        validators: {
            regexp:{
                regexp:/^([0-9]{5,15})$/,
                message:'QQ号格式不正确'
            }
        }
    }
};
$("#account").bootstrapValidator({
    fields:fields
});
$("#person").bootstrapValidator({
    fields:fields
});

$.post =function (url,data,callback,error) {
    $.ajax({
        url: url,
        data: data,
        type: "POST",
        cache: false,
        error: function (err) {
            console.log(err);
            if (error) error(err);
        },
        success: function (data) {
            console.log(data);
            if (data.status == 0) {
                if (callback) callback(data.data);
            } else {
                console.log(data);
                if (error) error(data);
            }
        }
    });
}
//移除收藏
function  delLike(gid) {
    $('#coll'+gid).hide(500);
    $.post('/u/delLike',{gid:gid},function () {
        $('#coll'+gid).remove();
    },function () {
        $('#coll'+gid).show(500);
    });
}
//擦亮
function repost(gid,_this) {
    $(_this).hide(500);
    $.post("/u/repost",{gid:gid},function () {
        $(_this).remove();
    },function () {
        $(_this).show(500);
    });
}
//下架
function off(gid,_this) {
    $(_this).parent().children("input.repost").hide(500);
    $(_this).hide(500);
    $.post("/u/changeStatus",{gid:gid,status:"下架"},function () {
        $(_this).parent().prev().children("span.value").text("下架");
        $(_this).parent().children("input.repost").remove();
        $(_this).remove();
    },function () {
        $(_this).parent().children("input.repost").show(500);
        $(_this).show(500);
    });
}
//重新上架
function rePostGood(gid,_this) {
    $(_this).hide(500);
    $.post("/u/changeStatus",{gid:gid,status:"出售"},function () {
        $(_this).parent().prev().children("span.value").text("出售");
        $(_this).remove();
    },function () {
        $(_this).show(500);
    });
}
//交易成功
function success(gid,_this) {
    $(_this).parent().children().hide(500);
    $(_this).parent().children("input.del").show(500);
    $.post("/u/changeStatus",{gid:gid,status:'出售成功'},function () {
        $(_this).parent().prev().children("span.value").text("购买成功");
    },function () {
        $(_this).parent().children().show(500);
        $(_this).parent().children("input.del").hide(500);
    });
}
//取消交易
function  failure(gid,_this) {
    $(_this).parent().children().toggle(500);
     $.post("/u/changeStatus",{gid:gid,status:'出售'},function () {
         $(_this).parent().prev().children("span.value").text("出售");
     },function () {
         $(_this).parent().children().toggle(500);
     });
}

//删除商品
function deleteGood(prefix,gid) {
    $("#"+prefix+gid).hide(500);
    $.post("/u/changeStatus",{gid:gid,status:'删除'},function () {
       $("#"+prefix+gid).remove();
    },function () {
        $("#"+prefix+gid).show(500);
    });
}

function  deleteBought(prefix,gid) {
    $("#"+prefix+gid).hide(500);
    $.post("/user/delBought",{gid:gid},function () {
        $("#"+prefix+gid).remove();
    },function () {
        $("#"+prefix+gid).show(500);
    });
}
//删除消息
function  remove(mid,type) {
    $("#"+mid).hide(500);
    $.post("/user/deleteMessage",{id:mid,type:type},function () {
        $("#"+mid).remove();
    },function () {
        $("#"+mid).show(500);
    })
}

function hrefGood(gid) {
    window.open('/i?id='+gid);
    //window.location.href='/i?id='+gid;
}