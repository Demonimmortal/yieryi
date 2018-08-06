var
product = {
	props : ['pro'],
	template : '<div class="product" :id="pro.id">\
					<div class="pro-img"><img  :src="pro.img" onload="common.size(this,160,180);" alt="loading..."/></div>\
					<div class="pro-inf text-center">\
						<p class="name ">{{pro.name}}</p>\
						<p class="price "><i class="fa fa-jpy"></i>{{pro.price}}</p>\
					</div>\
					<div class="shade-none">\
						<div class="btn-groups">\
							<button class="btn btn-danger" :data-goodsid="pro.id" @click="jumpdetail()">查看商品详情</button>\
						</div>\
					</div>\
				</div>',
	methods:{
		jumpdetail:function(){
            window.location.href = config.detail+'?type='+this.pro.type+'&id='+this.pro.id+'&title='+this.pro.name;
		}
	}
};