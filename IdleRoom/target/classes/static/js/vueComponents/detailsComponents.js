var detailsreply = {
    props: ['reply_msg'],
    template: '\
			<span :id="reply_msg.cid" class="msg">\
				<span class="msg-reply">\
					<a class="reply-user">{{reply_msg.from}}</a>\
					<a v-if="reply_msg.hasTo==true" class="rreply">{{reply_msg.to}}</a>\
					{{reply_msg.text}}\
				</span>\
				<span class="msg-control">\
					<a id="report" @click="report()" href="javascript:void(0)">举报</a>\
					{{reply_msg.date}}\
					<a class="reply_btn" @click="reply()" href="javascript:void(0);">回复</a>\
				</span>\
			</span>',
    methods: {
        reply: function () {
            var to = {};
            to.pCid = this.reply_msg.pCid;
            to.cid = this.reply_msg.cid;
            to.Id = this.reply_msg.fromId;
            to.Name = this.reply_msg.from;
            $('#' + to.pCid + 'reply').val("@" + to.Name + ":");
            $('#' + to.pCid + 'reply').data('to', to);
        },
        report: function () {
            console.log('report');
        }
    }
};
var detailsmessage = {
    props: ['msg'],
    data: function () {
        return {
            toggleHeight: 0,
            toggleTip: '展开',
        };
    },
    template: '\
		<div v-if="hasMessage" :id="msg[0].cid" class="message">\
			<span class="head">来自<a class="from">{{msg[0].from}}</a></span>\
			<div class="content">\
				<p>{{msg[0].text}}</p>\
				<span class="reply-control">\
					<a @click="report();" href="javascript:void(0);">举报</a>\
					{{msg[0].date}}\
					<a @click="toggleReply();" href="javascript:void(0);" class="reply_btn">{{toggleTip}}回复({{msg.length-1}})</a>\
				</span>\
				<div v-if="hasReply" class="reply" :style="{height:toggleHeight}">\
					<detailsreply v-for="reply_msg in replyList" :reply_msg="reply_msg"></detailsreply>\
				</div>\
			</div>\
			<div class="bottom">\
				<input :id="msg[0].cid+\'reply\'"type="text"/>\
				<button @click="sendReply()" class="button">发&nbsp;表</button>\
			</div>\
		</div>',
    methods: {
        toggleReply: function () {
            if (this.toggleHeight == 0) {
                this.toggleHeight = 'auto';
                this.toggleTip = '收起';
            } else {
                this.toggleHeight = '0';
                this.toggleTip = '展开';
            }
        },
        report: function () {
            /*var data = {
                gid:this.msg[0].cid
            }
            common.report(data);*/
            console.log('report');
        },
        sendReply: function () {
            var _this = this;
            var text = $('#' + this.msg[0].cid + 'reply').val();
            var toUCheck = text.split(":")[0];
            var sendtext = text.split(":")[1];
            var data;
            var to = $('#' + this.msg[0].cid + 'reply').data('to');
            if (text && text != '') {
                if (to && toUCheck == '@' + to.Name) {
                    data = {
                        gid: this.msg[0].gid,
                        group: to.pCid,
                        to: to.Id,
                        text: sendtext
                    };
                } else {
                    data = {
                        gid: this.msg[0].gid,
                        group: this.msg[0].cid,
                        to: this.msg[0].fromId,
                        text: text
                    };
                }
                $.ajax({
                    cache: false,
                    type: "post",
                    url: '/u/comment',
                    async: false,
                    data: data,
                    error: function (data) {
                        console.log(data);
                    },
                    success: function (data) {
                        if (data.status == 0) {
                            $('#' + _this.msg[0].cid + 'reply').val('')
                            _this.appendReply(data, _this);
                        }
                        else if (data.status == 1061) {
                            console.log(data);
                        }
                        else if(data.status == 1051){
                            sessionStorage.setItem("lasturl",window.location.href);
                            window.location.href = config.login;
                        }
                    }
                });
            }
        },
        appendReply: function (_data, _this) {
            var reply = _data.data;
            var hasTo = false;
            if(reply.to!=_this.msg[0].fromId)
                hasTo = true;
            _this.msg.push({
                cid: reply.cid,
                date: reply.date,
                from: reply.fromName,
                fromId: reply.from,
                gid: reply.gid,
                text: reply.text,
                hasTo: hasTo,
                to: reply.toName,
                toId: reply.to,
                pCid: _this.msg[0].cid
            });
        }
    },
    computed: {
        hasMessage: function () {
            if (this.msg.length > 0)
                return true;
            else
                return false;
        },
        hasReply: function () {
            if (this.msg.length > 1)
                return true;
            else
                return false;
        },
        replyList: function () {
            return this.msg.slice(1);
        }
    },
    components: {
        'detailsreply': detailsreply
    }
};
var detailstopblock = {
    props: ['picdata'],
    data: function () {
        return {
            currentPic: 0,
            lastPic:0
        }
    },
    template: '\
			<div class="top-block">\
				<div class="info-left">\
					<div class="goodsPic">\
					<img class="Pic" :src="nowPic" onload="common.size(this,380,380);" alt="#">\
					<div class="move"></div>\
					<div id="showWindow" class="showWindow">\
							<img class="showImg" :src="nowPic" alt="#">\
					</div>\
					</div>\
					<div class="picList">\
						<a class="pre" @click="pre()" href="javascript:void(0)"><span class="fa fa-caret-left"></span></a>\
						<div class="pic">\
							<ul id="piclist" data-lastactive="0">\
								<li class="active"><img :src="picdata.imgsrc[0]" @click="clickPic(-1)" onload="common.size(this,78,78);"/></li>\
								<li v-if="picdata.imgsrc.length>1" v-for="(imgsrc,index) in sliceImgsrc"><img :src="imgsrc" @click="clickPic(index)" onload="common.size(this,78,78);"/></li>\
							</ul>\
						</div>\
						<a class="next" @click="next()" href="javascript:void(0)"><span class="fa fa-caret-right"></span></a>\
					</div>\
				</div>\
				<div class="info-right">\
					<h1>{{picdata.title}}</h1>\
					<div class="price">\
						<span class="fa fa-jpy"></span>\
						<span id="price">{{picdata.price}}</span>\
					</div>\
					<div class="info-line">\
						<div class="block">\
							<div class="key"><span class="fa fa-user"></span></div>\
							<div class="value">{{picdata.uName}}</div>\
						</div>\
						<div class="block">\
							<div class="key">信用评级<span class="fa fa-info-circle"></span></div>\
							<div class="credit-value">\
								<span v-for="i in picdata.credit" class="fa fa-star"></span>\
							</div>\
						</div>\
					</div>\
					<div class="info-line">\
						<div class="key">联系方式(购买可见)</div>\
						<div class="contact">\
							<div class="block-contact">\
								<div class="key"><span class="fa fa-phone"></span></div>\
								<div id="phone" class="value">*</div>\
							</div>\
							<div class="block-contact">\
								<div class="key"><span class="fa fa-weixin"></span></div>\
								<div id="weixin" class="value">*</div>\
							</div>\
							<div class="block-contact">\
								<div class="key"><span class="fa fa-qq"></span></div>\
								<div id="qq" class="value">*</div>\
							</div>\
						</div>\
					</div>\
					<div class="info-line">\
						<div class="block">\
							<div class="key"><span class="fa fa-calendar"></span></div>\
							<div class="value">{{picdata.date}}</div>\
						</div>\
					</div>\
					<div class="info-line">\
                        <div class="block">\
                             <div class="key"><span class="fa fa-bookmark-o"></span></div>\
                             <div class="value">{{picdata.status}}</div>\
                        </div>\
                    </div>\
					<div class="info-btngroups">\
						<button v-if="picdata.status==\'出售\'" @click="purchase" class="button buy"><span class="fa fa-shopping-cart"></span>立即购买</button>\
						<button v-else  class="button buy" style="background:#666; "><span class="fa fa-shopping-cart"></span>立即购买</button>\
						<button @click="collect" class="button collect"><span class="fa fa-heart"></span>添加收藏</button>\
						<button @click="report" class="report">举报</button>\
					</div>\
				</div>\
			</div>\
			',
    computed: {
        totalPic: function () {
            return this.picdata.imgsrc.length;
        },
        nowPic: function () {
            return this.picdata.imgsrc[this.currentPic];
        },
        sliceImgsrc:function(){
            return this.picdata.imgsrc.slice(1);
        }
    },
    mounted: function () {
        this.bigImg();
    },
    methods: {
        clickPic:function(index){
            var lilist = $('#piclist').find('li');
            var moveFrom = lilist.eq(this.currentPic);
            var moveTo = lilist.eq(index + 1);
            this.lastPic = this.currentPic;
            this.currentPic = index+1;
            moveFrom.removeClass('active');
            moveTo.addClass('active');
            if (this.totalPic == 5 && index + 1 == 0)
                dom.css('left', 0);
            if (this.totalPic == 5 && index + 1 == 4)
                dom.css('left', '-90px');
        },
        pre: function () {
            var dom = $('#piclist');
            var lastindex = this.currentPic;
            if (lastindex > 0) {
                var lilist = dom.find('li');
                var moveFrom = lilist.eq(lastindex)
                var moveTo = lilist.eq(lastindex - 1);
                moveFrom.removeClass('active');
                moveTo.addClass('active');
                this.currentPic = lastindex - 1;
                if (this.totalPic == 5 && lastindex - 1 == 0)
                    dom.css('left', 0);
            }
        },
        next: function () {
            var dom = $('#piclist');
            var lastindex = this.currentPic;
            if (lastindex < this.totalPic - 1) {
                var lilist = dom.find('li');
                var moveFrom = lilist.eq(lastindex)
                var moveTo = lilist.eq(lastindex + 1);
                moveFrom.removeClass('active');
                moveTo.addClass('active');
                this.currentPic = lastindex + 1;
                if (this.totalPic == 5 && lastindex + 1 == 4)
                    dom.css('left', '-90px');
            }
        },
        bigImg: function () {
            var gPic = $(".goodsPic");
            var move = $(".move");
            var sWindow = $(".showWindow");
            var sImg = $(".showImg");
            gPic.off('mouseover').on('mouseover', function () {//鼠标移动到box上显示大图片和选框
                sWindow.css('display', 'block');
                move.css('display', 'block');
            });
            gPic.off('mouseout').on('mouseout', function () {//鼠标移动到box上显示大图片和选框
                sWindow.css('display', 'none');
                move.css('display', 'none');
            });
            gPic.off('mousemove').on('mousemove', function (e) {//获取鼠标位置
                var x = e.clientX;//鼠标相对于视口的位置
                var y = e.clientY;
                var t = gPic.offset().top;//box相对于视口的位置
                var l = gPic.offset().left;
                var _left = x - l - move.outerWidth() / 2;//计算move的位置
                var _top = y - t - move.outerHeight() / 2;
                if (_top <= 0)//滑到box的最顶部
                    _top = 0;
                else if (_top >= gPic.outerHeight() - move.outerHeight())//滑到box的最底部
                    _top = gPic.outerHeight() - move.outerHeight();
                if (_left <= 0)//滑到box的最左边
                    _left = 0;
                else if (_left >= gPic.outerWidth() - move.outerWidth())//滑到box的最右边
                    _left = gPic.outerWidth() - move.outerWidth();
                move.css('top', _top + "px");//设置move的位置
                move.css('left', _left + "px");
                var w = _left / (gPic.outerWidth() - move.outerWidth());//计算移动的比例
                var h = _top / (gPic.outerHeight() - move.outerHeight());
                var s_top = (sImg.outerHeight() - sWindow.outerHeight()) * h;//计算大图的位置
                var s_left = (sImg.outerWidth() - sWindow.outerWidth()) * w;
                sImg.css('top', -s_top + "px");//设置大图的位置信息
                sImg.css('left', -s_left + "px");
            });
        },
        purchase: function () {
            var _this = this;
            $('body').append('\
					<div class="submit-shade">\
						<div class="reOk">\
							<div class="text">\
								您确认要继续交易吗？<br>请不要恶意点击<br>以免为他人带来不便\
							</div>\
							<div class="footer">\
								<button id="details_ok" class="btn btn-danger ok">确认</button>\
								<button id="details_cancel" class="btn btn-default cancel">取消</button>\
							</div>\
						</div>\
					</div>');
            $('#details_ok').off('click').on('click', function () {
                common.removeShade();
                var purchaseuid = sessionStorage.getItem('userid');
                if(purchaseuid) {
                    if (purchaseuid == _this.picdata.uid) {
                        common.shadeTip('failed', '不能购买自己发布的商品');
                        setTimeout(function () {
                            common.removeShade();
                        }, 500);
                    } else {
                        common.shadeTip('loading', '购买中，请稍后...');
                        $.ajax({
                            url: '/u/changeStatus',
                            type: 'post',
                            cache: false,
                            data: {gid: _this.picdata.gid, status: '正在交易'},
                            async: true,
                            error: function (data) {
                                console.log(data);
                            },
                            success: function (data) {
                                if (data.data != null) {
                                    $(".buy").attr('disabled',true);
                                    $(".buy").css("background","#666");
                                    common.resetTip('success', '购买成功,正在获取联系方式');
                                    $.ajax({
                                        url: '/user/user',
                                        type: 'post',
                                        data: {id: _this.picdata.uid},
                                        cache: false,
                                        async: true,
                                        error: function (data) {
                                            console.log(data);
                                        },
                                        success: function (data) {
                                            common.resetTip('success', '已为您更新联系方式，可在个人中心查看');
                                            if (data.status == 0) {
                                                var qq = data.data.qq ? data.data.qq : '未设置';
                                                var phone = data.data.phone ? data.data.phone : '未设置';
                                                var weixin = data.data.wechat ? data.data.wechat : '未设置';
                                                $("#qq").text(qq);
                                                $("#phone").text(phone);
                                                $("#weixin").text(weixin);
                                                setTimeout(function () {
                                                    common.removeShade();
                                                }, 1000);
                                            }
                                        }
                                    });
                                } else {
                                    common.resetTip('failed', '购买失败,商品正在交易');
                                    setTimeout(function () {
                                        common.removeShade();
                                    }, 500);
                                }
                            }
                        });
                    }
                }else{
                    common.shadeTip('warning', '您未登录，即将跳转至登录页...');
                    sessionStorage.setItem("lasturl",window.location.href);
                    window.location.href=config.login;
                }
            });
            $('#details_cancel').off('click').on('click', function () {
                common.removeShade();
            });
        },
        collect: function () {
            var _this = this;
            common.shadeTip('loading','操作中');
            $.ajax({
                url: '/u/addLike',
                type: 'post',
                async: true,
                data: {gid: _this.picdata.gid},
                error: function (data) {
                    console.log(data);
                    common.resetTip('failed','操作失败');
                    setTimeout(function(){
                        common.removeShade();
                    },1500);
                },
                success: function (data) {
                    if (data.status == 0) {
                        common.resetTip('success','收藏成功');
                    } else {
                        console.log(data);
                        common.resetTip('failed','操作失败');
                    }
                    setTimeout(function(){
                        common.removeShade();
                    },1500);
                }
            });
        },
        report:function(){
            sessionStorage.setItem('report',this.picdata.gid);
            $('#report_modal').modal('show');
        }
    }
};
var detailsmiddleblock = {
    props: ['messagelist', 'info'],
    template: '\
			<div class="middle-block">\
				<ul class="nav nav-tabs">\
				<li role="presentation" class="active">\
					<a data-toggle="tab" href="#info">商品描述</a>\
				</li>\
				<li role="presentation">\
					<a data-toggle="tab" href="#comment">留言</a>\
				</li>\
				</ul>\
				<div class="tab-content">\
				<div id="info" class="tab-pane fade in active">\
					<div class="content">\
						{{info}}\
					</div>\
				</div>\
				<div id="comment" class="tab-pane fade in">\
					<div class="content">\
						<div class="area-show">\
							<detailsmessage v-for="msg in messagelist":msg="msg"></detailsmessage>\
						</div>\
						<div class="area-pub">\
							<textarea class="textarea"rows="6"></textarea>\
								<button @click="newMessage" class="button">发&nbsp;表</button>\
						</div>\
					</div>\
				</div>\
				</div>\
			</div>\
			',
    methods: {
        newMessage: function () {
            bus.$emit('newMessage');
        }
    },
    components: {
        'detailsmessage': detailsmessage
    }
}