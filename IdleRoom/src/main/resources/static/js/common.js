var common = {};
var pichost = 'http://unrestraint.oss-cn-beijing.aliyuncs.com/';
var bus=new Vue();
var classify = [
	{
		name:'书籍',
		subtitle:'book',
		subexist:false,
		id:'book1'
	},
	{
		name:'运动健身',
		subtitle:'sport',
		subexist:false,
		id:'sport1'
	},
	{
		name:'代步工具',
		subtitle:'traffic',
		subexist:false,
		id:'traffic1'
	},
	{
		name:'生活用品',
		subtitle:'daily',
		subexist:false,
		id:'daily1'
	},
	{
		name:'电子数码',
		subtitle:'digtal',
		subexist:true,
		id:'digtal1',
		submenu:[{name:'电脑',id:'com1'},{name:'手机',id:'phone1'},{name:'显示屏',id:'test1'}]
	},
	{
		name:'衣帽服饰',
		subtitle:'clothes',
		subexist:false,
		id:'clothes1'
	},
	{
		name:'彩妆护肤',
		subtitle:'skin',
		subexist:false,
		id:'skin1'
	},
	{
		name:'其他',
		subtitle:'other',
		subexist:false,
		id:'other1'
	}
];
var nav_classify = new Vue({
	el:'#classify',
	data:{
		allClass:classify
	},
	components:{
		'classifyparent':classifyparent
	}
});
var modal_classify = new Vue({
	el:'#new',
	data:{
		allClass:classify
	},
	computed:{
		items:function(){
			var items = [];
			var length = this.allClass.length;
			for(var i = 0 ;i<length;i++){
				var temp = this.allClass[i];
				if(!temp.subexist){
					items.push(temp.name);
				}
				else{
					var tempsub = temp.submenu;
					var sublength = tempsub.length;
					for(var j =0 ;j<sublength;j++){
						items.push(tempsub[j].name);
					}
				}
			}
			return items;
		}
	},
	methods:{
	}
});
var recommendVue = new Vue({
	el:'#recommend',
	data:{
		loading_rec:false,
		reset:false
	},
	created:function(){
		this.loading_rec=true;
		getInfoPost('rec','/sample',{size:8},this);
	},
	methods:{
		changeRec:function(){
			this.loading_rec=true;
			this.reset=false;
			getInfoPost('rec','/sample',{size:8},this);
		}
	},
	components:{
		'recommend':recommend
	}
});
common.search = function(){
	var key_words = $('#search').find('input').val();
    if(key_words!=""){
        bus.$emit('search',key_words);
    }
};
common.userPageSearch = function(){
    var key_words = $('#search').find('input').val();
    window.location.href = config.buy+'?type=search&key_words='+key_words+'&page=1'
};
common.goClassify = function(type){
	bus.$emit('goClassify',type);
};
common.getList=function(target){
	var type = $(target).text();
	common.goClassify(type);
};
common.clickActive = function (e){
	var target = $(e);
	var parent = $(target).parent();
	var root = $("#classify");
	var lastindex = root.data('lastindex');
	if(lastindex){
		$(lastindex).removeClass('active');
	}
	if(!parent.hasClass('cascade')){
	parent.addClass("active");
	root.data("lastindex",'#'+parent.attr('id'));
	}
	var par_dom = parent.find("ul");
	if(!par_dom.length>0){
		common.getList(e);
	}
};
common.size = function(e,max_w,max_h){
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
function updateGoodsList(_data,_this){
	var length = _data.data.content.length;
	var i = 0;
	var pros = [];
	var picurl;
	for(;i<length;i++){
        if(_data.data.content[i].pic==undefined||_data.data.content[i].pic.length==0)
            picurl=pichost+'static/images/default.png';
		else
			picurl=pichost+_data.data.content[i].pic[0];
		pros.push({
			img:picurl,
			name:_data.data.content[i].title,
			price:_data.data.content[i].price,
			id:_data.data.content[i].id,
			type:_data.data.content[i].type
		});
	}
	_this.allpage=_data.data.totalPages;
	_this.pros=pros;
	setTimeout(function(){
		_this.jumppage=true;
  		_this.loading_page=false;
	},200);
};
function updateNeedsList(_data,_this){
	var length = _data.data.content.length;
	var i = 0;
	var pubs = [];
	for(;i<length;i++){
		pubs.push({
            userPic:pichost+'static/images/avatar.jpg',
			uName:_data.data.content[i].seller,
			wantName:_data.data.content[i].title,
			price:_data.data.content[i].price,
			msg:_data.data.content[i].info,
			id:_data.data.content[i].id,
			price:_data.data.content[i].price,
			uid:_data.data.content[i].seller
		});
        (function(index){
            $.ajax({
                type:'post',
                async:true,
                data:{id:_data.data.content[index].seller},
                url:'/user/user',
                error:function(data){
                    console.log(data);
                },
                success:function(data){
                    if(data.status==0) {
                        _this.pubs[index].uName = data.data.name;
                        _this.pubs[index].userPic = pichost+data.data.pic;
                    }else{
                        console.log(data);
                    }
                }
            });
        })(i);
	}
	_this.pubs=pubs;
	_this.allpage=_data.data.totalPages;
    setTimeout(function(){
        _this.jumppage=true;
        _this.loading_page=false;
    },200);
};
function updateRecList(_data,_this){
	var length = _data.data.length;
	var i = 0;
	var rec_list = [];
	var picurl;
	for(;i<length;i++){
        if(_data.data[i].pic==undefined||_data.data[i].pic.length==0)
            picurl=pichost+'static/images/default.png';
		else
			picurl=pichost+_data.data[i].pic[0];
		rec_list.push({
			img:picurl,
			title:_data.data[i].title,
			ins:_data.data[i].info,
			id:_data.data[i].id,
			type:_data.data[i].type,
		});
	}
	_this.rec_list=rec_list;
	setTimeout(function(){
		_this.loading_rec=false;
		_this.reset=true;
	},200);
}
function getInfoPost(mode,url,data,_this){
	$.ajax({
		cache: false,
		type: "post",
		url:url,
		data:data,	
		async: true,
		error: function(request) {
			$('body').prepend('<div class="submit-shade">\
					<div class="tip-box">\
					<span id="logo" class="fa fa-times-circle failed"></span>\
        			<div id="new_goods_tip" class="tip-text">服务器错误:状态码'+request.status+'</div>\
					</div>\
				</div>');
			setTimeout(function(){
				$('.submit-shade').remove();
			},1000);
		},
		success:function(_data){
			if(_data.status==0){
				if(mode=='sell')
					updateGoodsList(_data,_this);
				else if(mode=='needs')
					updateNeedsList(_data,_this);
				else if(mode=='rec')
					updateRecList(_data,_this);
			}else{
                if(mode=='sell'||mode=="needs"){
                    _this.allpage=0;
                    _this.loading_page=false;
                }
				console.log(_data);
			}
		}
});
};
common.removeShade = function(){
	$('.submit-shade').remove();
};
common.shadeTip = function(type,msg){
    var icon;
    if(type=='failed'){
        icon = 'times';
    }else if(type=='success'){
        icon='check';
    }else if(type=='warning'){
    	icon='exclamation';
	}else{
        icon=null;
    }
    if(type!='loading'){
        $('body').prepend('<div class="submit-shade">\
						<div class="tip-box">\
						<span id="logo" class="fa fa-'+icon+'-circle '+type+'"></span>\
            			<div id="new_goods_tip" class="tip-text">'+msg+'</div>\
						</div>\
						</div>');
	}else{
        $('body').prepend('<div class="submit-shade">\
		<div class="tip-box">\
			<div class="sk-circle">\
				<div class="sk-circle1 sk-child"></div>\
				<div class="sk-circle2 sk-child"></div>\
				<div class="sk-circle3 sk-child"></div>\
				<div class="sk-circle4 sk-child"></div>\
				<div class="sk-circle5 sk-child"></div>\
				<div class="sk-circle6 sk-child"></div>\
				<div class="sk-circle7 sk-child"></div>\
				<div class="sk-circle8 sk-child"></div>\
				<div class="sk-circle9 sk-child"></div>\
				<div class="sk-circle10 sk-child"></div>\
				<div class="sk-circle11 sk-child"></div>\
				<div class="sk-circle12 sk-child"></div>\
			</div>\
			<div id="new_goods_tip" class="tip-text">'+msg+'</div>\
		</div>\
	</div>');
	}
};
common.resetTip = function(type,msg){
    $('.tip-box').remove();
    var icon;
    if(type=='failed'){
        icon = 'times';
    }else if(type=='success'){
        icon='check';
    }else if(type=='warning'){
        icon='exclamation';
    }else{
        icon=null;
    }
    if(type!='loading'){
        $('.submit-shade').append('\
       			 <div class="tip-box">\
            		<span id="logo" class="fa fa-'+icon+'-circle '+type+'"></span>\
                    <div id="new_goods_tip" class="tip-text">'+msg+'</div>\
           		 </div>');
    }else{
        $('.submit-shade').append('\
		<div class="tip-box">\
			<div class="sk-circle">\
				<div class="sk-circle1 sk-child"></div>\
				<div class="sk-circle2 sk-child"></div>\
				<div class="sk-circle3 sk-child"></div>\
				<div class="sk-circle4 sk-child"></div>\
				<div class="sk-circle5 sk-child"></div>\
				<div class="sk-circle6 sk-child"></div>\
				<div class="sk-circle7 sk-child"></div>\
				<div class="sk-circle8 sk-child"></div>\
				<div class="sk-circle9 sk-child"></div>\
				<div class="sk-circle10 sk-child"></div>\
				<div class="sk-circle11 sk-child"></div>\
				<div class="sk-circle12 sk-child"></div>\
			</div>\
			<div id="new_goods_tip" class="tip-text">'+msg+'</div>\
		</div>');
    }
};
common.reportSub = function(){
    var dom = $('#report_modal');
    var gid = sessionStorage.getItem('report');
    var _data = {};
    if(gid) {
        _data.text = dom.find('input[name=optionsRadios]:checked').val();
        _data.gid = gid;
        common.shadeTip('loading','操作中');
        $.ajax({
			cache:false,
			type:'post',
			url:'/admin/report',
			data:_data,
			async:true,
			error:function(data){
				console.log(data);
                common.resetTip('failed','操作失败');
			},
			success:function(data){
				if(data.status==0){
                    common.resetTip('success','举报成功');
				}else if(data.status==1051){
                    common.resetTip('warning', '您未登录，即将跳转至登录页...');
                    setTimeout(function () {
                        sessionStorage.setItem("lasturl",window.location.href);
                        window.location.href=config.login;
                    }, 500);
				}else if(data.status==1002){
                    common.resetTip('failed','获取分布式锁超时');
				}
                setTimeout(function () {
                    common.removeShade();
                    $('#report_modal').modal('hide');
                }, 1000);
			}
		});
    }else{
        common.shadeTip('failed','非法操作');
        setTimeout(function () {
            common.removeShade();
            $('#report_modal').modal('hide');
        }, 1000);
	}
};
function GetUrlParam(paraName) {
　　　　var url = document.location.toString();
　　　　var arrObj = url.split("?");

　　　　if (arrObj.length > 1) {
　　　　　　var arrPara = arrObj[1].split("&");
　　　　　　var arr;

　　　　　　for (var i = 0; i < arrPara.length; i++) {
　　　　　　　　arr = arrPara[i].split("=");

　　　　　　　　if (arr != null && arr[0] == paraName) {
　　　　　　　　　　return decodeURI(arr[1]);
　　　　　　　　}
　　　　　　}
　　　　　　return null;
　　　　}
　　　　else {
　　　　　　return null;
　　　　}
　　};
function report(){
    $("#report_modal").modal('show');
}