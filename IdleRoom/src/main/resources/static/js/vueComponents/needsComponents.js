var
publish = {
	props : [ 'pub' ],
	template : '<div :id="pub.id" class="publish-inf">\
						<div class="inf-header">\
							<div class="userPic">\
								<img :src="pub.userPic" alt="userpic" onload="common.size(this,40,40)"/>\
							</div>\
							<div class="headWrap">\
								<span class="key">发布者</span>\
								<span class="value">{{pub.uName}}</span>\
							</div>\
							<div class="headWrap">\
								<span class="key">求购物品</span>\
								<span class="value">{{pub.wantName}}</span>\
							</div>\
							<div class="headWrap">\
								<span class="key">期望价格</span>\
								<span class="value">{{pub.price}}</span>\
							</div>\
							<div class="button-groups">\
								<button @click="haveGoods" class="btn btn-primary">\
								我有宝贝\
								<span class="fa fa-commenting"></span>\
								</button>\
								<button class="btn btn-danger" @click="report">\
								举报\
								</button>\
							</div>\
						</div>\
						<div class="inf-body">\
							<p>{{pub.msg}}</p>\
						</div>\
					</div>',
	methods:{
		report:function(){
			sessionStorage.setItem('report',this.pub.id);
			$('#report_modal').modal('show');
		},
        showContact:function(data){
            var qq = data.data.qq ? data.data.qq : '未设置';
            var phone = data.data.phone ? data.data.phone : '未设置';
            var weixin = data.data.wechat ? data.data.wechat : '未设置';
            setTimeout(function () {
                common.removeShade();
                $('body').prepend('\
                                        <div id="has_modal" class="modal fade" tabindex="-1" data-backdrop="static" data-keyboard="false" role="dialog">\
                							<div class="modal-dialog" role="document">\
               									<div class="modal-header">\
               										<button type="button" class="close" data-dismiss="modal">&times;</button>\
                									<h4 class="modal-title text-center">我有宝贝</h4>\
                								</div>\
                								<div class="modal-content">\
                									<div class="info_tips">\
                									<p>我们已经通知买家进行交易，以下是买家的联系方式，请您和买家联系协商交易方式</p>\
                									<div class="block-contact">\
               											<div class="key"><span class="fa fa-phone"></span></div>\
                										<div class="value">'+phone+'</div>\
                									</div>\
                									<div class="block-contact">\
                										<div class="key"><span class="fa fa-weixin"></span></div>\
                										<div class="value">'+weixin+'</div>\
                									</div>\
                									<div class="block-contact">\
                										<div class="key"><span class="fa fa-qq"></span></div>\
														<div class="value">'+qq+'</div>\
													</div>\
													<em>(关闭后可在消息中心查看)</em>\
													</div>\
													<div class="footer">\
														<button onclick="closeHasModal()" class="btn btn-primary ok">确认</button>\
													</div>\
												</div>\
											</div>\
										</div>');
                $('#has_modal').modal('show');
                $('#has_modal').off('hidden.bs.modal').on('hidden.bs.modal',function(){
                    $('#has_modal').remove();
                });
            }, 500);
        },
		haveGoods:function(){
            var _this = this;
            $('body').append('\
					<div class="submit-shade">\
						<div class="reOk">\
							<div class="text">\
								您确认要继续交易吗？<br>请不要恶意点击<br>以免为他人带来不便\
							</div>\
							<div class="footer">\
								<button id="needs_ok" class="btn btn-danger ok">确认</button>\
								<button id="needs_cancel" class="btn btn-default cancel">取消</button>\
							</div>\
						</div>\
					</div>');
            $('#needs_ok').off('click').on('click', function () {
                common.removeShade();
                var purchaseuid = sessionStorage.getItem('userid');
                if(purchaseuid) {
                    if (purchaseuid == _this.pub.uid) {
                        common.shadeTip('failed', '不能向自己出售物品');
                        setTimeout(function () {
                            common.removeShade();
                        }, 500);
                    } else {
                        common.shadeTip('loading', '正在通知买家，请稍后');
                        $.ajax({
                            url: '/u/haveGood',
                            type: 'post',
                            cache: false,
                            data: {gid: _this.pub.id},
                            async: true,
                            error: function (data) {
                                console.log(data);
                            },
                            success: function (data) {
                                if (data.status == 0) {
                                    _this.showContact(data);
                                }else if(data.status==1051){
                                    common.resetTip('warning', '您未登录，即将跳转至登录页...');
                                    setTimeout(function () {
                                        sessionStorage.setItem("lasturl",window.location.href);
                                        window.location.href=config.login;
                                    }, 500);
                                }
                                setTimeout(function () {
                                    common.removeShade();
                                }, 500);
                            }
                        });
                    }
                }else{
                    common.shadeTip('warning', '您未登录，即将跳转至登录页...');
                    setTimeout(function () {
                        sessionStorage.setItem("lasturl",window.location.href);
                        window.location.href=config.login;
                    }, 500);
                }
			});
            $('#needs_cancel').off('click').on('click', function () {
                common.removeShade();
            });
		}
	}
};
function closeHasModal(){
    $('#has_modal').modal('hide');
}