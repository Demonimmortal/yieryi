var position = {
		props:['routes'],
		template:	'<ol class="breadcrumb">\
						<li v-for="route in routes" :class="{active:route.now==true}">\
							{{route.now==true?route.name:null}}\
							<a v-if="route.now!=true" :href="route.url">{{route.name}}</a>\
						</li>\
					</ol>'
};
var classifysub = {
		props:['submenu'],
		template:'<li :id="submenu.id"><a onclick="common.clickActive(this)">{{submenu.name}}</a></li>'
};
var classifyparent = {
		props:['firstmenu'],
		template:'	<li :id="firstmenu.id">\
						<a onclick="common.clickActive(this)" class="cascade-title">{{firstmenu.name}}</a>\
						<ul v-if="firstmenu.subexist==true"  class="cascade-second unspread">\
							<classifysub  v-for="submenu in firstmenu.submenu" :submenu="submenu"></classifysub>\
						</ul>\
					</li>',
				components:{
					'classifysub':classifysub
				}
};
var recommend = {
	props:['rec_info'],
	template:'\
				<div class="recommend" :data-id="rec_info.id" @click="jump()">\
					<div class="imgwrap">\
						<img :src="rec_info.img" onload="common.size(this,70,80);" alt="src" />\
					</div>\
					<div class="instruct">\
						<span class="name">{{rec_info.title}}</span>\
						<span class="text">{{rec_info.ins}}</span>\
					</div>\
				</div>',
	methods:{
		jump:function(){
            sessionStorage.setItem("lasturl",window.location.href);
			window.location.href = config.detail+'?type='+this.rec_info.type+'&id='+this.rec_info.id+'&title='+this.rec_info.title;
		}
	}
};
var
pagecode = {
	props : ['allpage','currentpage'],
	data : function() {
		return {
			current:1,
			showItem : 5,
		}
	},
	template : '\
			<div class="footer" v-if="allpage!=0">\
				<nav aria-label="Page navigation">\
				<ul class="pagination">\
					<li v-show="current>showItem" @click="current--  && go(current-4)"><a href="javascript:void(0);">&laquo; </a></li>\
					<li v-for="index in pages" @click="go(index)" :class="{\'active\':current == index}" :key="index">\
            			<a href="javascript:void(0);" >{{index}}</a>\
					</li>\
					<li v-show="current<allpage-showItem+1" @click="current++ && go(current+3)"><a href="javascript:void(0);"> &raquo; </a></li>\
				</ul>\
				</nav>\
			</div>',
	computed : {
		pages : function() {
			var pag = [];
			if (this.current < this.showItem) { //如果当前的激活的项 小于要显示的条数
				//总页数和要显示的条数那个大就显示多少条
				var i = Math.min(this.showItem, this.allpage);
				while (i) {
					pag.unshift(i--);
				}
			} else { //当前页数大于显示页数了
				var middle = this.current - Math.floor(this.showItem / 2), //从哪里开始
				i = this.showItem;
				if (middle > (this.allpage - this.showItem)) {
					middle = (this.allpage - this.showItem) + 1
				}
				while (i--) {
					pag.push(middle++);
				}
			}
			return pag;
		}
	},
	mounted:function(){
		this.current=this.currentpage;
	},
	methods : {
		go : function(index) {
			if (index == this.current)
				return;
			this.current = index;
			bus.$emit('goPage',index);
		}
	}
};