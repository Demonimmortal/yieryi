$(document).ready(function(){
	user.check();
});
const user = {};
user.check = function(){
	var dom = $("#inner-box");
	dom.empty();
	if(sessionStorage.getItem("userid")){
		dom.append('<li id="userInfo" class="module">\
								<a href="javascript:userManage(\'user-info\');" class="fa fa-user"></a>\
								</li>\
								<li id="login-out" class="module">\
								<a href="javascript:userManage(\'login-out\');" class="fa fa-sign-out"></a>\
								</li>');
	}else{
		dom.append('<li id="login-in" class="module">\
				<a href="javascript:userManage(\'login-in\');" class="fa fa-sign-in"></a>\
				</li>');
	}
}
function userManage(op){
			if(op==="login-in"){
				window.location.href="/login";
			}
			else if(op==="login-out"){
                $.ajax({
                    url:'/user/logOut',
                    type:'post',
                    cache:false,
                    async:false,
                    error:function(data){
                        console.log(data);
                    },
                    success:function(data){
                        sessionStorage.removeItem("userid");
                        var dom = $("#inner-box");
                        dom.empty();
                        dom.append('<li id="login-in" class="module">\
						<a href="javascript:userManage(\'login-in\');" class="fa fa-sign-in"></a>\
						</li>');
                    }
                });
				sessionStorage.removeItem("userid");
				var dom = $("#inner-box");
				dom.empty();
				dom.append('<li id="login-in" class="module">\
						<a href="javascript:userManage(\'login-in\');" class="fa fa-sign-in"></a>\
						</li>');
			}
			else if(op==="user-info"){
				window.location.href=config.user;
			}
		}