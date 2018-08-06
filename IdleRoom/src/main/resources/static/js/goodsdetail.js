var goodsdetails = new Vue({
	el:'#details',
	data: {
		routes:[
		        {'url':config.index,'name':'首页','now':false},
		        {'url':config.buy,'name':'校园二手','now':false}],
		goodsid:'-1',
		topblockdata:{uName:'',imgsrc:[],title:'',price:'',credit:0},
        messagelist:[],
		info:'',
		jumppage:false,
		loading_page:true,
        notfound:false
	},
    created: function() {
        var type = GetUrlParam('type');
        var id = GetUrlParam('id');
        var title = GetUrlParam('title');
        var data;
        if(type)
            this.routes.push({'url':config.buy+'?type='+type+'&page=1','name':type,'now':false});
        if(title)
            this.routes.push({'url':'javascript:void(0);','name':title,'now':true});
        if(id)
        	this.goodsid = id;
        this.jumppage=false,
		this.loading_page=true
        this.DataReq();
    },
	mounted:function(){
		var _this = this;
        bus.$on('newMessage',function(){
        	_this.newMessage();
		});
	},
	methods:{
		DataReq:function(){
            var _this = this;
            $.ajax({
                type:'post',
                async:true,
                data:{id:_this.goodsid},
                url:'/i',
                error:function(data){
                    console.log(data);
                },
                success:function(data){
                    if(data.status==0) {
                        _this.info = data.data.info;
                        _this.setTopData(data.data,_this);
                        if(data.data.comment)
                        _this.updateMessage(data, _this);
                        setTimeout(function(){
                            _this.jumppage=true;
                            _this.loading_page=false;
                        },200);
                    }if(data.status==404){
                        setTimeout(function(){
                            _this.jumppage=false;
                            _this.loading_page=false;
                            _this.notfound=true;
                        },200);
                    }
                    else{
                        console.log(data);
                    }
                }
            });
		},
		setTopData:function(_data,_this){
			imgsrc = [];
			if(_data.pic.length==0){
				imgsrc.push(config.host+"static/images/default.png");
			}
			else {
				var length = _data.pic.length;
				for(var i=0;i<length;i++){
					imgsrc.push(pichost+_data.pic[i]);
				}
			}
            $.ajax({
                type:'post',
                async:true,
                data:{id:_data.seller},
                url:'/user/user',
                error:function(data){
                    console.log(data);
                },
                success:function(data){
                    if(data.status==0) {
                        _this.topblockdata.uName = data.data.name;
                        _this.topblockdata.credit = Math.round(data.data.credit/20);
                    }else{
                        console.log(data);
                    }
                }
            });

        _this.topblockdata = {
                'uName':_data.seller,
				'imgsrc':imgsrc,
				'title':_data.title,
				'price':_data.price,
				'credit':0,
				'date':_data.date,
				'gid':_data.id,
                'uid':_data.seller,
                'status':_data.status
            }
		},
        newMessage:function(){
            var data = {};
            data.gid = this.goodsid;
            data.text = $('.area-pub').find('textarea').eq(0).val();
            data.group = "";
            data.to = "";
            this.postMessage(data);
        },
        postMessage:function(_data){
            var _this = this;
            $.ajax({
                type:'post',
                async:true,
                data:_data,
                url:'/u/comment',
                error:function(data){
                    console.log(data);
                },
                success:function(data){
                    if(data.status==0){
                        console.log(data);
                        _this.appendMessage(data,_this);
                    }else if(data.status==1051){
                        console.log(data);
                        sessionStorage.setItem("lasturl",window.location.href);
                        window.location.href = config.login;
                    }
                }
            });
        },
        updateMessage:function(_data,_this){
            var messagelist = [];
            var hasTo,to='',toId='';
            var mlist_msg_length;
            var comment = _data.data.comment;
            var mNum = comment.length;
            var nowTopicMain;
            for(var i=0;i<mNum;i++){
                var mlist_msg = new Array();
                mlist_msg_length = comment[i].length;
                nowTopicMain = comment[i][0].from;
                for(var j=0;j<mlist_msg_length;j++){
                    hasTo = false;
                    if(comment[i][j].to!=nowTopicMain){
                        hasTo = true;
                    }
                    to =comment[i][j].toName;
                    toId = comment[i][j].to;
                    mlist_msg.push({
                        cid: comment[i][j].cid,
                        date: comment[i][j].date,
                        from: comment[i][j].fromName,
                        fromId: comment[i][j].from,
                        gid: comment[i][j].gid,
                        text: comment[i][j].text,
                        hasTo: hasTo,
                        to: to,
                        toId: toId,
                        pCid:comment[i][0].cid
                    });
                }
                messagelist.push(mlist_msg);
            }
            _this.messagelist = messagelist;
        },
        appendMessage:function(_data,_this){
            var comment = _data.data;
            var mlist_msg = new Array();
            mlist_msg.push({
                cid: comment.cid,
                date: comment.date,
                from: comment.fromName,
                fromId: comment.from,
                gid: comment.gid,
                text: comment.text,
                hasTo: false,
                to: '',
                toId: '',
                pCid:''
            });
            _this.messagelist.push(mlist_msg);
        }
	},
	components:{
		'position':position,
		'detailsmessage':detailsmessage,
		'detailstopblock':detailstopblock,
		'detailsmiddleblock':detailsmiddleblock
	}
});