var upconfig = {
    accessid : '',
    accesskey : '',
    host : '',
    policyBase64 : '',
    signature : '',
    callbackbody : '',
    filename : '',
    key : '',
    expire : 0,
    g_object_name : '',
    g_object_name_type : 'random_name',
    now : Date.parse(new Date()) / 1000,
};
//为保证单文件上传，将upPic.remove()写到选择图片按钮的onclick事件中
var upPic = {};
function send_request()
{
    var xmlhttp = null;
    if (window.XMLHttpRequest)
    {
        xmlhttp=new XMLHttpRequest();
    }
    else if (window.ActiveXObject)
    {
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }

    if (xmlhttp!=null)
    {
        serverUrl = '/upload'
        xmlhttp.open( "POST", serverUrl, false );
        xmlhttp.send( null );
        return xmlhttp.responseText;
    }
    else
    {
        alert("Your browser does not support XMLHTTP.");
    }
};
function get_signature()
{
    //可以判断当前expire是否超过了当前时间,如果超过了当前时间,就重新取一下.3s 做为缓冲
    upconfig.now = Date.parse(new Date()) / 1000;
    if (upconfig.expire < upconfig.now + 3)
    {
        var body = send_request();
        var obj = eval ("(" + body + ")");
        upconfig.host = obj['host']
        upconfig.policyBase64 = obj['policy']
        upconfig.accessid = obj['accessid']
        upconfig.signature = obj['signature']
        upconfig.expire = parseInt(obj['expire'])
        upconfig.callbackbody = obj['callback']
        upconfig.key = obj['dir']
        return true;
    }
    return false;
};

function random_string(len) {
    len = len || 32;
    var chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
    var maxPos = chars.length;
    var pwd = '';
    for (i = 0; i < len; i++) {
        pwd += chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}
function get_suffix(filename) {
    pos = filename.lastIndexOf('.');
    suffix = '';
    if (pos != -1) {
        suffix = filename.substring(pos);
    }
    return suffix;
}

function calculate_object_name(filename)
{
    if (upconfig.g_object_name_type == 'local_name')
    {
        upconfig.g_object_name += "${filename}";
    }
    else if (upconfig.g_object_name_type == 'random_name')
    {
        suffix = get_suffix(filename);
        upconfig.g_object_name = upconfig.key + random_string(10) + suffix;
    }
    return '';
}

function get_uploaded_object_name(filename)
{
    if (g_object_name_type == 'local_name')
    {
        tmp_name = upconfig.g_object_name;
        tmp_name = tmp_name.replace("${filename}", filename);
        return tmp_name;
    }
    else if(upconfig.g_object_name_type == 'random_name')
    {
        return upconfig.g_object_name;
    }
}

function set_upload_param(up, filename, ret)
{
    if (ret == false)
    {
        ret = get_signature();
    }
    upconfig.g_object_name = upconfig.key;
    if (filename != '') {
        suffix = get_suffix(filename)
        calculate_object_name(filename)
    }
    new_multipart_params = {
        'key' : upconfig.g_object_name,
        'policy': upconfig.policyBase64,
        'OSSAccessKeyId': upconfig.accessid,
        'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
        'callback' : upconfig.callbackbody,
        'signature': upconfig.signature
    };

    up.setOption({
        'url': upconfig.host,
        'multipart_params': new_multipart_params
    });

    up.start();
}
var uploader = new plupload.Uploader({
    runtimes : 'html5,flash,silverlight,html4',
    browse_button : 'selectPic',
    url : 'http://oss.aliyuncs.com',

    filters: {
        mime_types : [ //只允许上传图片和zip,rar文件
            { title : "Image files", extensions : "jpg,gif,png,bmp" },
        ],
        max_file_size : '2mb', //最大只能上传2mb的文件
        prevent_duplicates : true //不允许选取重复文件
    },

    init: {
        PostInit: function() {
            upconfig.fileList = [];
            //下面注册事件的id替换为上传头像的确认按钮
            $('#submitPic').on('click',function() {
                if(uploader.files.length==0){
                    $('body').prepend('<div class="submit-shade">\
							<div class="tip-box">\
							<span id="logo" class="fa fa-times-circle failed"></span>\
                			<div id="new_goods_tip" class="tip-text">请至少上传一个图片</div>\
							</div>\
						</div>');
                    setTimeout(function(){
                        upPic.removeLoading();
                    },1000);
                }else{
                        upPic.addLoading();
                        set_upload_param(uploader, '', false);
                }
                return false;
            });
        },
        FilesAdded: function(up, files) {
            upPic.checkPic();
            plupload.each(files, function(file) {
                previewImage(file,function(imgsrc){
                    $('#previewDiv').prepend('\
							<img id="'+file.id+'" alt="pic" onload="size(this,150,150);" src="'
                        + imgsrc + '">');
                });
            });

        },

        BeforeUpload: function(up, file) {
            set_upload_param(up, file.name, true);
        },
        FileUploaded: function(up, file, info) {
            if (info.status == 200)
            {
                console.log(upconfig.g_object_name);
                $.ajax({
                    url:'/user/changeInfo',
                    type:'post',
                    async:true,
                    data:{pic:upconfig.g_object_name},
                    error:function(data){
                        setTimeout(function () {
                            $('.tip-box').html('\
                        			<span id="logo" class="fa fa-times-circle failed"></span>\
                        			<div id="new_goods_tip" class="tip-text">服务器错误</div>\
                        			');
                        }, 500);
                        setTimeout(function () {
                            upPic.removeLoading();
                        }, 1000);
                    },
                    success:function(data){
                        if(data.status==0) {
                            $('#userPic').prop('src','http://unrestraint.oss-cn-beijing.aliyuncs.com/'+upconfig.g_object_name)
                            setTimeout(function () {
                                $('.tip-box').html('\
                        			<span id="logo" class="fa fa-check-circle success"></span>\
                        			<div id="new_goods_tip" class="tip-text">更换成功</div>\
                        			');
                            }, 500);
                            setTimeout(function () {
                                $("#new").modal('hide');
                                upPic.removeLoading();
                            }, 1000);
                        }else{
                            setTimeout(function () {
                                $('.tip-box').html('\
                        			<span id="logo" class="fa fa-times-circle failed"></span>\
                        			<div id="new_goods_tip" class="tip-text">服务器错误</div>\
                        			');
                            }, 500);
                            setTimeout(function () {
                                upPic.removeLoading();
                            }, 1000);
                        }
                    }
                });
            }
            else
            {
                console.log('FileUpFailed:info-'+info.response);
            }
        },

        Error: function(up, err) {
            if (err.code == -600) {
                $('body').prepend('<div class="submit-shade">\
						<div class="tip-box">\
						<span id="logo" class="fa fa-times-circle failed"></span>\
            			<div id="new_goods_tip" class="tip-text">上传图片不能超过2m</div>\
						</div>\
					</div>');
                setTimeout(function(){
                    upPic.removeLoading();
                },1000);
            }
            else if (err.code == -601) {
                $('body').prepend('<div class="submit-shade">\
						<div class="tip-box">\
						<span id="logo" class="fa fa-times-circle failed"></span>\
            			<div id="new_goods_tip" class="tip-text">请上传图片类型的文件</div>\
						</div>\
					</div>');
                setTimeout(function(){
                    upPic.removeLoading();
                },1000);
            }
            else if (err.code == -602) {
                $('body').prepend('<div class="submit-shade">\
						<div class="tip-box">\
						<span id="logo" class="fa fa-times-circle failed"></span>\
            			<div id="new_goods_tip" class="tip-text">该图片已在上传列表</div>\
						</div>\
					</div>');
                setTimeout(function(){
                    upPic.removeLoading();
                },1000);
            }
            else
            {
                $('body').prepend('<div class="submit-shade">\
						<div class="tip-box">\
						<span id="logo" class="fa fa-times-circle failed"></span>\
            			<div id="new_goods_tip" class="tip-text">"\nError xml:"' + err.response + '</div>\
						</div>\
					</div>');
                setTimeout(function(){
                    upPic.removeLoading();
                },1000);
            }
        }
    }
});
function previewImage(file,callback){
    if(!file || !/image\//.test(file.type)) return; //确保文件是图片
    if(file.type=='image/gif'){ //gif使用FileReader进行预览,因为mOxie.Image只支持jpg和png
        var gif = new moxie.file.FileReader();
        gif.onload = function(){
            callback(gif.result);
            gif.destroy();
            gif = null;
        };
        gif.readAsDataURL(file.getSource());
    }else{
        var image = new moxie.image.Image();
        image.onload = function() {
            image.downsize( 150,150);//先压缩一下要预览的图片,宽300，高300
            var imgsrc = image.type=='image/jpeg' ? image.getAsDataURL('image/jpeg',80) : image.getAsDataURL(); //得到图片src,实质为一个base64编码的数据
            callback && callback(imgsrc); //callback传入的参数为预览图片的url
            image.destroy();
            image = null;
        };
        image.load( file.getSource());
    }
};
upPic.remove=function(){
    var file = uploader.files[0]
    if(file){
        uploader.removeFile(file);
        $('#previewDiv').find('img').remove();
    }
    console.log(uploader);
};
upPic.checkPic = function(){
    var length = uploader.files.length;
    if(length>1){
        $('body').prepend('<div class="submit-shade">\
				<div class="tip-box">\
				<span id="logo" class="fa fa-times-circle failed"></span>\
				<div id="new_goods_tip" class="tip-text">最多上传1张图片</div>\
				</div>\
			</div>');
        setTimeout(function(){
            upPic.removeLoading();
        },1000);
    }
    for(var i=1;i<length;i++){
        uploader.removeFile(uploader.files[1]);
    }
};
upPic.addLoading = function(){
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
			<div id="new_goods_tip" class="tip-text">数据上传中，请稍后</div>\
		</div>\
	</div>');
};
upPic.removeLoading = function(){
    $('.submit-shade').remove();
};
uploader.init();