var needsVue=new Vue({
	el: '#list',
	  data: {
		  pubs : [],
		loading:false,
		allpage:0,
		routes:[
		        {'url':config.index,'name':'首页','now':false},
		        {'url':config.want,'name':'校园求购','now':false},
		        {'url':'javascript:bus.$emit(\'goClassify\',\'*\');','name':'所有分类','now':true}
		        ],
		loading_page:false,
		jumppage:true,
		current:1,
		page:1,
		size:5,
		type:'*',
		status:'求购',
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
          getInfoPost('needs',url,data,this);
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
		  		history.pushState(state,null,config.want+'?type='+type+'&page=1');
		  			var data = {
							'type':type,
							'status':_this.status,
							'page':0,
							'size':_this.size
					};
                _this.jumppage=false;
                _this.loading_page=true;
		  		getInfoPost('needs','/page',data,_this);
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
				history.pushState(state,null,config.want+'?type=search&key_words='+_this.key_words+'&page=1');
		  		 data = { 
							'title':key_words?_this.key_words:'*',
							'info':key_words?_this.key_words:'*',
							'status':_this.status,
							'page':_this.page-1,
							'size':_this.size
				};
                _this.jumppage=false;
                _this.loading_page=true;
		  		getInfoPost('needs','/search',data,_this);
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
							'title':_this.key_words,
							'info':_this.key_words,
							'status':_this.status,
							'page':page-1,
							'size':_this.size
					};
		  			history.pushState(state,null,config.want+'?type=search&page='+page);
		  			url = '/search';
		  		}else{
		  			data = { 
							'type':_this.type,
							'status':_this.status,
							'page':page-1,
							'size':_this.size
					};
		  			history.pushState(state,null,config.want+'?type='+_this.type+'&page='+page);
		  			url = '/page';
		  		}
                _this.jumppage=false;
                _this.loading_page=true;
		  		console.log(data,url);
		  		getInfoPost('needs',url,data,_this);
			});
		},
		methods:{
			
		},
		components: {
			'publish':publish,
			'pagecode':pagecode,
			'position':position
  }
});
var needs ={};
needs.issue = function(){
    var bootstrapValidator = $("#new").data('bootstrapValidator');
    bootstrapValidator.validate();
    if(bootstrapValidator.isValid()) {
    	common.shadeTip('loading','发布中，请稍后');
        var postDom = $('#postPublish');
        var data = {};
        data.title = postDom.find("input[name='Name']").val();
        data.price = postDom.find("input[name='price']").val();
        data.type = postDom.find("#modal_classify option:selected").val()
        data.info = postDom.find("textarea[name='ins']").val();
        $.ajax({
            cache: false,
            type: "POST",
            url: '/u/postWant',
            data: data,
            async: true,
            error: function (request) {
                common.resetTip('failed','服务器错误');
                setTimeout(function(){
                    common.removeShade();
                    $('#new').modal('hide');
                },1500);
            },
            success: function (data) {
            	if(data.status==0){
                    common.resetTip('success','发布成功');
                    setTimeout(function(){
                    	common.removeShade();
                    	$('#new').modal('hide');
					},1500);
				}
				else{
                    common.resetTip('failed','发布失败');
                    setTimeout(function(){
                        common.removeShade();
                    },1500);
				}
            }
        });
    }
};
needs.check = function(){
	if (sessionStorage.getItem("userid")) {
		$("#new").modal('show');
	}else{
        common.shadeTip('warning', '您未登录，即将跳转至登录页...');
        setTimeout(function () {
            sessionStorage.setItem("lasturl",window.location.href);
            window.location.href=config.login;
        }, 500);
	}
}
needs.initModal = function(){
    $('#postPublish').find('input').val("");
    $('#modal_instruct').val("");
    $('#modal_classify').find("option").eq(0).prop("selected",true);
};
var fields = {
    Name: {
        validators: {
            notEmpty:{
                message:'求购物品名称不能为空'
            },
            stringLength: {
            	min:0,
                max: 10,
                message: '名称不能超过10个字符'
            },
        }
    },
    price: {
    	message:'请输入有效的数字',
        validators: {
            notEmpty: {
                message: '期望价格不能为空'
            },
            callback:{
                message:'价格不能小于0',
                callback:function(value,validator,$field){
                    return value>=0?true:false;
                }
            }
        }
    },
    ins:{
        threshold: 6,
        validators: {
        	notEmpty:{
                message: '请输入6-100字的简要说明'
			},
            stringLength: {
                min: 6,
                max: 100,
                message: '请输入6-100字的简要说明'
            },
        }
    }
};
$("#new").bootstrapValidator({
    fields:fields
});
$('#new').on('hidden.bs.modal',function(){
    needs.initModal();
});