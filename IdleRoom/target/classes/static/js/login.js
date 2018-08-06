/*
	验证码请求
	const getCode;
*/
$.getQueryVariable = function(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] == variable){return pair[1];}
    }
    return(false);
}
const login={};
//注册功能UI切换
login.change=function(val){
	/* val为true表示切换到密码登录模式
	 * val为false表示切换到验证码登录模式
	 * */
	if(val=='logInByPwd'){
		$("#logInByPwd").removeClass("hide");
		$("#logInByCode").addClass("hide");
		$("#register").addClass("hide");
		$("#changePwd").addClass("hide");
	}
	else if(val=='logByCode'){
		$("#logInByPwd").addClass("hide");
		$("#logInByCode").removeClass("hide");
		$("#register").addClass("hide");
	}else if(val=='register'){
		$("#logInByPwd").addClass("hide");
		$("#logInByCode").addClass("hide");
		$("#register").removeClass("hide");
	}
	else if(val=='changePwd'){
		$("#logInByPwd").addClass("hide");
		$("#logInByCode").addClass("hide");
		$("#register").addClass("hide");
		$("#changePwd").removeClass("hide");
	}
}

const fields = {
	uName: {
		threshold: 6,
		message: '用户名验证失败',
		validators: {
			callback:{
				message:'邮箱或手机号格式不正确',
				callback:function(value,validator,$field){
					var reg1 = /^(\w+(.\w+)*@\w+(.\w+)+)$/;
					var reg2 = /^([0-9]{11})$/;
					var boolean = false;
					if(reg1.test(value)){
						boolean = true;
						$field.attr('input-type','mail');
					}else{
						if(reg2.test(value)) {
							boolean = true;
							$field.attr('input-type', 'phone');
						}
					}
					if(boolean){
						return true;
					}else{
						$field.attr('input-type', 'error');
						return false;
					}
				}
			}
		}
	},
	Password: {
		validators: {
			notEmpty: {
				message: '密码长度必须在8到16位之间'
			},
			stringLength: {
				min: 8,
				max: 16,
				message: '密码长度必须在8到16位之间'
			},
			regexp:{
				regexp: /^\S*$/,
				message: '密码不能出现空字符'
			}
		}
	},
	code:{
		live:'submitted',
		validators: {
			notEmpty: {
				message: '验证码不能为空'
			},
			regexp:{
				regexp: /^\d{6}$/,
				message: '验证码为6位纯数字'
			}
		}
	}
};
$("#logInByPwd").bootstrapValidator({
	message: 'This value is not valid',
	feedbackIcons: {
		valid: 'glyphicon glyphicon-ok',
		invalid: 'glyphicon glyphicon-remove',
		validating: 'glyphicon glyphicon-refresh'
	},
	submitButtons: '.submitBtn',
	fields:fields
});
$("#logInByCode").bootstrapValidator({
	message: 'This value is not valid',
	feedbackIcons: {
		valid: 'glyphicon glyphicon-ok',
		invalid: 'glyphicon glyphicon-remove',
		validating: 'glyphicon glyphicon-refresh'
	},
	submitButtons: '.submitBtn',
	fields:fields
});
$("#register").bootstrapValidator({
	message: 'This value is not valid',
	feedbackIcons: {
		valid: 'glyphicon glyphicon-ok',
		invalid: 'glyphicon glyphicon-remove',
		validating: 'glyphicon glyphicon-refresh'
	},
	submitButtons: '.submitBtn',
	fields:fields
});
$("#changePwd").bootstrapValidator({
	message: 'This value is not valid',
	feedbackIcons: {
		valid: 'glyphicon glyphicon-ok',
		invalid: 'glyphicon glyphicon-remove',
		validating: 'glyphicon glyphicon-refresh'
	},
	submitButtons: '.submitBtn',
	fields:fields
});
login.getCode=function(formId){
	var bootstrapValidator = $("#"+formId).data('bootstrapValidator');
	bootstrapValidator.validateField('uName');
	var type = $("#" + formId + " :input[name='uName']").attr('input-type');
	var udata = $("#" + formId + " :input[name='uName']").val();
    var code = $("#"+formId+" :input[name='code']");
	if(type&&type!='error') {
		(function(obj) {
			var count = 1;
			var sum = 120;
			obj.text('重发(120s)');
			obj.attr('disabled', 'disabled');
			var i = setInterval(function () {
				if (count > 120) {
					obj.attr('disabled', false);
					obj.text('点击发送');
					clearInterval(i);
				} else {
					obj.text('重发(' + parseInt(sum - count) + 's)');
				}
				count++;
			}, 1000);
		})($("#" + formId).find(".getcode").eq(0));
			$.ajax({
				cache: false,
				type: "POST",
				url: '/user/getCode',
				data: {
					'type': type,
					'value': udata
				},
				async: true,
				error: function (request) {
					console.log('failure');
				},
				success: function (data) {
                    if (data.status != 0) {
                        login.logErrTipSet([{
                            docEle: code,
                            addError: true,
                            msg: data.msg
                        }]);
                    }
                }
			});
	}
}
login.login=function(val){
	var bootstrapValidator = $("#"+val).data('bootstrapValidator');
	bootstrapValidator.validate();
	if(bootstrapValidator.isValid()){
		//根据指定val索引获取dom对象
		var uName = $("#"+val+" :input[name='uName']");
		var data={};//数据提取
		//从dom对象中提取数据
		data.type = uName.attr('input-type');//提取账户类型
		data.value = uName.val();//提取帐号信息
		//提取密码或者验证码
		if(val=='logInByPwd'){
			var pass = $("#"+val+" :input[name='Password']");
			data.pwd = pass.val();
		}
		if(val=='logInByCode'){
			var code = $("#"+val+" :input[name='code']");
			data.code = code.val();
		}
		if(data.type&&data.type!='error'){
			//如果验证通过进入下一步，准备提交
			console.log('login-url:/user/' + val);
			var option = {
				cache : false,
				type : "POST",
				url : '/user/' + val,
				data : data,
				async : false,
				error : function(request) {
					alert("请求失败！");
				},
				success : function(data) {
					var status = data.status;
					var msg = data.msg;
					console.log(data);
					if (status == 0) {
                        sessionStorage.setItem("userid",data.data);

                        var href = $.getQueryVariable("URL");

                        if(href){
                            window.location.href=href;
                        }else{
                            var url = sessionStorage.getItem('lasturl');
                            if(url){
                                window.location.href = url;
                            }else{
                                window.location.href = config.buy;
                            }
                        }
					} else if (status==1053||status==1063) {
						var dom;
						if(pass)
							dom = pass;
						else if(code)
							dom = code;
						login.logErrTipSet([{
							docEle:uName,
							addError:true,
							msg:msg
						},{
							docEle:dom,
							addError:false
						}]);
					}else if(status==1055||status==1056){
						login.logErrTipSet([{
							docEle:code,
							addError:true,
							msg:msg
						}]);
					}else if(status==1059){
						login.logErrTipSet([{
							docEle:code,
							addError:true,
							msg:msg
						}]);
					}else if(status==404){
						login.logErrTipSet([{
							docEle:uName,
							addError:true,
							msg:msg
						}]);
					}else{
						login.sysTip('failed',msg);
					}
				}
			}
			$.ajax(option);
		}
	}
}
login.register=function(){
	//提取dom对象
	var bootstrapValidator = $('#register').data('bootstrapValidator');
	bootstrapValidator.validate();
	if(bootstrapValidator.isValid()){
		var uName = $("#register"+" :input[name='uName']");
		var pass = $("#register"+" :input[name='Password']");
		var code = $("#register"+" :input[name='code']");

		var data={};//提取输入框中的数据
		var valid;//提取数据的有效性属性
		//提取登录类型、帐号
		data.type = uName.attr('input-type');
		data.value = uName.val();
		data.pwd = pass.val();
		data.code = code.val();
		if(data.type&&data.type!='error'){
			//如果验证通过进入下一步，准备提交
			//数据拼装
			var option = {
				cache : false,
				type : "POST",
				url :'/user/register' ,
				data : data,
				async : false,
				error : function(request) {
					alert("请求失败！");
				},
				success : function(data) {
					var status = data.status;
					var msg = data.msg;
					if (status == 0) {
                        login.sysTip('success','注册成功',2000);
                        login.change('logInByPwd');
					}else if(status==1057) {
						login.logErrTipSet([{
							docEle:uName,
							addError:true,
							msg:msg
						}]);
					}else if(status==1055||status==1056||status==1059){
						login.logErrTipSet([{
							docEle:code,
							addError:true,
							msg:msg
						}]);
					}else{
						login.sysTip('failed',msg);
					}
				}
			};
			$.ajax(option);
		}
	}
};
login.changePwd=function(){
	//提取dom对象
	var bootstrapValidator = $('#changePwd').data('bootstrapValidator');
	bootstrapValidator.validate();
	if(bootstrapValidator.isValid()){
		var uName = $("#changePwd"+" :input[name='uName']");
		var pass = $("#changePwd"+" :input[name='Password']");
		var code = $("#changePwd"+" :input[name='code']");

		var data={};//提取输入框中的数据
		var valid;//提取数据的有效性属性
		//提取登录类型、帐号
		data.type = uName.attr('input-type');
		data.value = uName.val();
		data.pwd = pass.val();
		data.code = code.val();
		if(data.type&&data.type!='error'){
			//如果验证通过进入下一步，准备提交
			//数据拼装
			var option = {
				cache : false,
				type : "POST",
				url :'/user/changePwd' ,
				data : data,
				async : false,
				error : function(request) {
					alert("请求失败！");
				},
				success : function(data) {
					var status = data.status;
					var msg = data.msg;
					console.log(data);
					if (status == 0) {
						login.sysTip('success','修改成功',2000);
						login.change('logInByPwd');
					} else if (status==1058||status==404) {
						login.logErrTipSet([{
							docEle:uName,
							addError:true,
							msg:msg
						}]);
					}else if(status==1055||status==1056){
						login.logErrTipSet([{
							docEle:code,
							addError:true,
							msg:msg
						}]);
					}
					else{
						login.sysTip('failed',msg);
					}
				}
			};
			$.ajax(option);
		}
	}
};
/*{
	docEle:$test,
	addError:false,
	msg:msg
}*/
login.logErrTipSet = function(dom){
	var length = dom.length;
	if(length){
		var now;
		var iTag;
		for(var i=0;i<length;i++){
			now = dom[i];

			if(now.addError){
				now.docEle.parent().append('<small class="help-block errorinfo" style="display: block;">' + now.msg + '</small>');
			}
			now.docEle.parent().parent().addClass("has-error");
			iTag = now.docEle.parent().find('i');
			iTag.removeClass('glyphicon-ok');
			iTag.addClass('glyphicon-remove');
		}
	}
};
$('input').on('input',function(){
	var dom = $('.errorinfo');
	if(dom){
		dom.remove();
	}
});
login.sysTip = function(type,msg,timeOut){
		$('.login-wrap').prepend('<div class="submit-shade">\
						<div class="tip-box">\
						<span id="logo" class="glyphicon glyphicon-ok '+type+'"></span>\
            			<div id="new_goods_tip" class="tip-text">'+msg+'</div>\
						</div>\
						</div>');
		if(timeOut){
	        setTimeout(function(){
		        $('.submit-shade').remove();
	        },timeOut);
        }
}