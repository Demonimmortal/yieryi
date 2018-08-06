var goodsVue = new Vue({
		el: '#list',
		data: {
			pros : [],
			allpage:0,
			routes:[
			        {'url':config.index,'name':'首页','now':false},
			        {'url':config.buy,'name':'校园二手','now':false},
			        {'url':'javascript:bus.$emit(\'goClassify\',\'*\');','name':'所有分类','now':true}
			        ],
			loading_page:false,
			jumppage:true,
			current:1,
			page:1,
			size:15,
			type:'*',
			status:'出售',
			key_words:'*'
		},
	  	created: function() { 
  			this.routes = this.routes.splice(0,2);
  			var type = GetUrlParam('type');
  			var page = GetUrlParam('page');
  			var key_words = GetUrlParam('key_words');
  			var data;
  			if(type)
  				this.type = type;
  			if(page)
  				this.current=this.page = page;
  			if(key_words)
  				this.key_words=key_words;
  			if(this.type=='search'){
	  			 data = { 
						'title':key_words?key_words:this.key_words,
						'info':key_words?key_words:this.key_words,
						'status':this.status,
						'page':this.page-1,
						'size':this.size
				};
	  			this.routes.push({'url':'javascript:bus.$emit(\'search\',\''+this.key_words+'\');','name':this.key_words,'now':true});
	  			url = '/search';
	  		}else{
	  			data = { 
						'type':this.type,
						'status':this.status,
						'page':this.page-1,
						'size':this.size
				};
	  			this.routes.push({'url':'javascript:bus.$emit(\'goClassify\',\''+this.type+'\');','name':this.type,'now':true});
	  			url = '/page';
	  		}
  			this.jumppage=false;
	  		this.loading_page=true;
	  		getInfoPost('sell',url,data,this);
		},
		mounted:function(){
			_this = this;
			bus.$on('goClassify',function(type){
				_this.routes = _this.routes.splice(0,2);
				_this.routes.push({'url':'javascript:bus.$emit(\'goClassify\',\''+type+'\');','name':type,'now':true});
		  		_this.type=type;
		  		var state = {
		  			url:window.location.href
		  		}
		  		history.pushState(state,null,config.buy+'?type='+type+'&page=1');
		  			var data = {
							'type':type,
							'status':_this.status,
							'page':0,
							'size':_this.size
					};
                _this.jumppage=false;
                _this.loading_page=true;
		  		getInfoPost('sell','/page',data,_this);
			});
			bus.$on('search',function(key_words){
				_this.type = 'search';
				_this.key_words = key_words!=''?key_words:_this.key_words;
				_this.page=1;
				_this.routes = _this.routes.splice(0,2);
				_this.routes.push({'url':'javascript:bus.$emit(\'search\',\''+_this.key_words+'\');','name':_this.key_words,'now':true});
				var state = {
			  			url:window.location.href
			  		}
				history.pushState(state,null,config.buy+'?type=search&key_words='+_this.key_words+'&page=1');
		  		 data = { 
							'title':key_words?_this.key_words:'*',
							'info':key_words?_this.key_words:'*',
							'status':_this.status,
							'page':_this.page-1,
							'size':_this.size
				};
                _this.jumppage=false;
                _this.loading_page=true;
		  		getInfoPost('sell','/search',data,_this);
			});
			bus.$on('goPage',function(page){
		  		var data;
		  		var url;
		  		_this.page=page;
		  		var state = {
			  			url:window.location.href
			  		}
		  		if(_this.type=='search'){
		  			 data = { 
							'title':key_words,
							'info':key_words,
							'status':_this.status,
							'page':page-1,
							'size':_this.size
					};
		  			history.pushState(state,null,config.buy+'?type=search&page='+page);
		  			url = '/search';
		  		}else{
		  			data = { 
							'type':_this.type,
							'status':_this.status,
							'page':page-1,
							'size':_this.size
					};
		  			history.pushState(state,null,config.buy+'?type='+_this.type+'&page='+page);
		  			url = '/page';
		  		}
                _this.jumppage=false;
                _this.loading_page=true;
		  		console.log(data,url);
		  		getInfoPost('sell',url,data,_this);
			});
		},
		components: {
			'product':product,
			'pagecode':pagecode,
			'position':position
		}
});
var goods = {};
goods.issue = function(pic,successfun){
		var postDom = $('#postProduct');
		var data = {};
		data.title = postDom.find("input[name='Name']").val();
		data.price = postDom.find("input[name='price']").val();
		data.type = postDom.find("#modal_classify option:selected").val()
		data.info = postDom.find("textarea[name='ins']").val();
		data.pic = pic;
		console.log(data);
		$.ajax({
			cache : false,
			type : "POST",
			url : '/u/postProduct',
			data : data,
			async : true,
			error : function(request) {
				console.log('failure');
			},
			success : successfun
		});
};

goods.check = function(){
	if (sessionStorage.getItem("userid")){
		$("#new").modal('show');
	}else{
        common.shadeTip('warning', '您未登录，即将跳转至登录页...');
        setTimeout(function () {
            sessionStorage.setItem("lasturl",window.location.href);
            window.location.href=config.login+"?URL="+window.location.href;
        }, 500);
	}
};
goods.initModal = function(){
    var length =  uploader.files.length;
    var i = 0;
    for (; i <length; i++) {
        uploader.removeFile(uploader.files[0]);
    }
    $('.uppic-preview').remove();
    $('#uppic-progress-now').css('width','0%');
    $('#uppic-progress-now').html('0%');
    $('#postProduct').find('input').val("");
    $('#modal_instruct').val("");
    $('#modal_classify').find("option").eq(0).prop("selected",true);
};
goods.checkSubForm=function(){
	var postDom = $('#postProduct');
	var title = postDom.find("input[name='Name']").val();
	var price = postDom.find("input[name='price']").val();
	var info = postDom.find("textarea[name='ins']").val();
	if(title.length==0||title.length>20){
		$('body').prepend('<div class="submit-shade">\
				<div class="tip-box">\
				<span id="logo" class="fa fa-exclamation-circle warning"></span>\
				<div id="new_goods_tip" class="tip-text">商品名称不为空且不超过20个字符</div>\
				</div>\
			</div>');
		setTimeout(function(){
	    	upPic.removeLoading();
		},1000);
		return false;
	}
	if(price.length==0||price<0){
		var msg = '请输入商品价格';
		if(price<0){
			var msg = '商品价格不能为负';
		}
		$('body').prepend('<div class="submit-shade">\
				<div class="tip-box">\
				<span id="logo" class="fa fa-exclamation-circle warning"></span>\
				<div id="new_goods_tip" class="tip-text">'+msg+'</div>\
				</div>\
			</div>');
		setTimeout(function(){
	    	upPic.removeLoading();
		},1000);
		return false;
	}
	if(info.length==0||info.length>100){
		$('body').prepend('<div class="submit-shade">\
				<div class="tip-box">\
				<span id="logo" class="fa fa-exclamation-circle warning"></span>\
				<div id="new_goods_tip" class="tip-text">商品简介不能为空不能超过100个字符</div>\
				</div>\
			</div>');
		setTimeout(function(){
	    	upPic.removeLoading();
		},1000);
		return false;
	}
	return true;
}
$('#new').on('hidden.bs.modal',function(){
	goods.initModal();
});
