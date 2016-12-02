function Base64() {
    // private property
    _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    // public method for encoding
    this.encode = function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;
        input = _utf8_encode(input);
        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output +
            _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
            _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
        }
        return output;
    };

    // public method for decoding
    this.decode = function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
        while (i < input.length) {
            enc1 = _keyStr.indexOf(input.charAt(i++));
            enc2 = _keyStr.indexOf(input.charAt(i++));
            enc3 = _keyStr.indexOf(input.charAt(i++));
            enc4 = _keyStr.indexOf(input.charAt(i++));
            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;
            output = output + String.fromCharCode(chr1);
            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }
        output = _utf8_decode(output);
        return output;
    };

    // private method for UTF-8 encoding
    _utf8_encode = function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);
            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }
        return utftext;
    };

    // private method for UTF-8 decoding
    _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;
        while ( i < utftext.length ) {
            c = utftext.charCodeAt(i);
            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }
        return string;
    }
}
//SHA1加密算法
function SHA1(msg) {
    function rotate_left(n, s) {
        var t4 = (n << s ) | (n >>> (32 - s));
        return t4;
    }
    function lsb_hex(val) {
        var str = "";
        var i;
        var vh;
        var vl;

        for ( i = 0; i <= 6; i += 2) {
            vh = (val >>> (i * 4 + 4)) & 0x0f;
            vl = (val >>> (i * 4)) & 0x0f;
            str += vh.toString(16) + vl.toString(16);
        }
        return str;
    }
    function cvt_hex(val) {
        var str = "";
        var i;
        var v;

        for ( i = 7; i >= 0; i--) {
            v = (val >>> (i * 4)) & 0x0f;
            str += v.toString(16);
        }
        return str;
    }
    function Utf8Encode(string) {
        string = string.replace(/\r\n/g, "\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if ((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
    }
    var blockstart;
    var i, j;
    var W = new Array(80);
    var H0 = 0x67452301;
    var H1 = 0xEFCDAB89;
    var H2 = 0x98BADCFE;
    var H3 = 0x10325476;
    var H4 = 0xC3D2E1F0;
    var A, B, C, D, E;
    var temp;

    msg = Utf8Encode(msg);

    var msg_len = msg.length;

    var word_array = [];
    for ( i = 0; i < msg_len - 3; i += 4) {
        j = msg.charCodeAt(i) << 24 | msg.charCodeAt(i + 1) << 16 | msg.charCodeAt(i + 2) << 8 | msg.charCodeAt(i + 3);
        word_array.push(j);
    }

    switch (msg_len % 4) {
        case 0:
            i = 0x080000000;
            break;
        case 1:
            i = msg.charCodeAt(msg_len - 1) << 24 | 0x0800000;
            break;

        case 2:
            i = msg.charCodeAt(msg_len - 2) << 24 | msg.charCodeAt(msg_len - 1) << 16 | 0x08000;
            break;

        case 3:
            i = msg.charCodeAt(msg_len - 3) << 24 | msg.charCodeAt(msg_len - 2) << 16 | msg.charCodeAt(msg_len - 1) << 8 | 0x80;
            break;
    }

    word_array.push(i);

    while ((word_array.length % 16) != 14)
        word_array.push(0);

    word_array.push(msg_len >>> 29);
    word_array.push((msg_len << 3) & 0x0ffffffff);

    for ( blockstart = 0; blockstart < word_array.length; blockstart += 16) {

        for ( i = 0; i < 16; i++)
            W[i] = word_array[blockstart + i];
        for ( i = 16; i <= 79; i++)
            W[i] = rotate_left(W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16], 1);

        A = H0;
        B = H1;
        C = H2;
        D = H3;
        E = H4;

        for ( i = 0; i <= 19; i++) {
            temp = (rotate_left(A, 5) + ((B & C) | (~B & D)) + E + W[i] + 0x5A827999) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        for ( i = 20; i <= 39; i++) {
            temp = (rotate_left(A, 5) + (B ^ C ^ D) + E + W[i] + 0x6ED9EBA1) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        for ( i = 40; i <= 59; i++) {
            temp = (rotate_left(A, 5) + ((B & C) | (B & D) | (C & D)) + E + W[i] + 0x8F1BBCDC) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        for ( i = 60; i <= 79; i++) {
            temp = (rotate_left(A, 5) + (B ^ C ^ D) + E + W[i] + 0xCA62C1D6) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        H0 = (H0 + A) & 0x0ffffffff;
        H1 = (H1 + B) & 0x0ffffffff;
        H2 = (H2 + C) & 0x0ffffffff;
        H3 = (H3 + D) & 0x0ffffffff;
        H4 = (H4 + E) & 0x0ffffffff;

    }

    var temp = cvt_hex(H0) + cvt_hex(H1) + cvt_hex(H2) + cvt_hex(H3) + cvt_hex(H4);

    return temp.toLowerCase();
}
//title–消息标题，111
//content – 消息内容
//type – 消息类型，1:消息 2:通知
//platform - 0:全部平台，1：ios, 2：android
//groupName - 推送组名，多个组用英文逗号隔开.默认:全部组。eg.group1,group2 .
//userIds - 推送用户id, 多个用户用英文逗号分隔，eg. user1,user2。
var push_url = "https://p.apicloud.com/api/push/message";
function push(bodyParam) {
    bodyParam.platform = 0;
    bodyParam.userIds = api.deviceId;
    var now = Date.now();
    var appKey = SHA1("A6999359375355" + "UZ" + "517EC3E7-8C65-1147-07D5-D2E0F943C4A7" + "UZ" + now) + "." + now;
    var headers = {
        'X-Requested-With' : 'XMLHttpRequest',
        'X-APICloud-AppId' : 'A6999359375355',
        'X-APICloud-AppKey' : appKey,
        'Content-Type' : 'application/x-www-form-urlencoded; charset=UTF-8'
    };
    api.ajax({
        url : push_url,
        method : 'post',
        cache : false,
        headers : headers,
        data : {
            values : bodyParam
        }
    }, function(ret, err) {
        //api.alert({msg:ret});
    });
}
var push_timer;
function init_push() {
    var time = isEmpty($api.getStorage('notice_time')) ? '' : $api.getStorage('notice_time');
    if (!isEmpty(time)) {
        clearInterval(push_timer);
        push_timer = setInterval(function() {
            var date = new Date(Date.now());
            var hourse = extra(date.getHours());
            var minute = extra(date.getMinutes());
            var s = extra(date.getSeconds());
            if (time == (hourse + ':' + minute) && s == '00') {
                push({
                    title : '学习提醒',
                    content : get_loc_val('mine', 'nick') + '同学，时间到了，赶紧开始学习吧！',
                    type : 2,
                    platform : 0,
                    userIds : api.deviceId
                });
            }
        }, 1000);
    }
}
function extra(x) {
    //如果传入数字小于10，数字前补一位0。
    if (parseInt(x) < 10) {
        return "0" + parseInt(x);
    } else {
        return x;
    }
}
//绑定用户
function bind_push(){
    var push = api.require('push');
    var username=get_loc_val('mine','nick');
    push.bind({
        userName:username,
        userId:api.deviceId
    },function(ret,err){
    });
}
//设置监听 处理消息类型的推送
function setListener() {
    var push = api.require('push');
    push.setListener(
        function (ret, err) {
            if (ret) {
                api.alert({msg: ret.data});
                var data=JSON.parse(ret.data);
                api.alert({title:'parse',msg:data});
                if(api.systemType == 'android'){

                }else{

                }
            }
        }
    );
}
//

function mykeyback(){
    var timePicker;
    var clickCount = 1;
    api.addEventListener({
        name : 'keyback'
    }, function(ret, err) {
        clearTimeout(timePicker);
        if (clickCount != 2) {
            api.toast({
                msg :window.lang['alert_exit']} );
            clickCount++;
            timePicker = setTimeout(function() {
                clickCount = 1;
            }, 1000);
        } else {
            clickCount = 1;
            api.toLauncher();
        }
    });
}



//* 英语 en  中文 zh //
if(isEmpty($api.getStorage('lang'))){
    $api.setStorage('lang','zh');
}

window.langIndex=$api.getStorage('lang');
//测试地址
var common_url = 'http://app205.vliang.com/Api_v1';
//公共请求方法
function ajaxRequest(url, method, params, callBack,isFalg) {
    if(api.connectionType == 'none') {
        //禁掉下拉刷新
        api.refreshHeaderLoadDone();
        //禁掉等待输入框
        api.hideProgress();
        api.toast({msg:window.lang['alert_line'],location:'middle'});
        return ;
    }
    var appType="client";
    var appId = 'VMING';
    var key = 'HAOHAOXUEXITIANTIANXIANGSHANG';
    var now = Date.now();
    var appKey = SHA1(appId + "VMING" + key + "VMING" + now) + "." + now+'.'+appType;
    var headers = {
        'X-Requested-With' : 'XMLHttpRequest',
        'X-Token-With' : appKey,
        'Content-Type' : 'application/json;charset=UTF-8',
        'Accept':'application/json'
    };
    if(isFalg){
        var info =is_login();
        if(!info){
            api.toast({msg:window.lang['alert_login'],location:'middle'});
            api.hideProgress();
            api.refreshHeaderLoadDone();
            return false;
        }
        params=params==''?{}:params;
        for(var p in info){
            params[p]=info[p];
        }
    }
    params.lang=window.langIndex;
    params.deviceId=api.deviceId;
    if(url.indexOf(".json")<0){
        if(url.indexOf("?")>0){
            var arr = url.split("?");
            url =arr[0]+".json"+arr[1];
        }else{
            url += ".json";
        }
    }
    var data = {
        body : JSON.stringify(params)
    };
    if(method == "get"||method =="delete"){
        var urlquery = "";
        for(var key in params){
            urlquery += key+"="+ params[key]+"&";
        }
        if(urlquery !=""){
            if(url.indexOf("?")>0){
                url += "&"+urlquery;
            }else{
                url += "?"+urlquery;
            }
        }
        data = {}
    }
    api.ajax({
        url : common_url + '/' + url,
        method : method,
        cache : false,
        timeout: 1200,
        headers : headers,
        data : data
    }, function(ret, err) {
        api.hideProgress();
        api.refreshHeaderLoadDone();
        if(err){
            api.toast({msg:err.msg,location:'middle'});
        }else if(ret){
            callBack(ret, err);
        }
    });
}

function is_login(param) {
    var last_api = getstor('last_api');
    if (!last_api) {
        return false;
    } else {
        var now = Date.now();
        switch(last_api) {
            case '1':
                //普通手机登录
                var token = getstor('token'), expire = getstor('expire');
                if (!token || !expire) {
                    return false;
                }
                //计算时间，判断是否过期
                if (now / 1000 > expire - 120) {
                    //alert('com_expire');
                    if (param == 'mine') {
                        return false;
                    }//过期
                    return {
                        'token' : token,
                        'last_api' : last_api
                    };
                    //过期
                } else {
                    var token = SHA1('not_expire' + '-' + now) + '.' + now + '.' + getstor('uid');
                    var b = new Base64();
                    var str = b.encode(token);
                    return {
                        'token' : str,
                        'last_api' : last_api
                    };
                    //没有过期
                }
                break;
            case '2':
                //微信登录
                var wx_openid = getstor('wx_openid');
                if (!wx_openid) {
                    api.toast({
                        msg : '授权异常！'
                    });
                    return false;
                }
                var wx_token = get_loc_val('wx', 'token');
                var wx_expire = get_loc_val('wx', 'wx_expire');
                if (isEmpty(wx_token) || isEmpty(wx_expire)) {
                    return false;
                }
                var obj = {
                    'wx_openid' : wx_openid,
                    'wx_token' : wx_token,
                    'last_api' : last_api
                };
                //计算时间，判断是否过期
                if (now / 1000 > wx_expire - 120) {//刷新token
                    if (param == 'mine') {
                        return false;
                    }//过期
                    return obj;
                } else {
                    return obj;
                }
                break;

            case '3':
                //新浪登录
                var sina_uid = getstor('sina_uid');
                if (!sina_uid) {
                    api.toast({
                        msg : '授权异常！'
                    });
                    return false;
                }
                var sina_token = get_loc_val('sina', 'sina_token');
                var sina_expire = get_loc_val('sina', 'sina_expire');
                if (isEmpty(sina_token) || isEmpty(sina_expire)) {
                    return false;
                }
                var obj = {
                    'sina_uid' : sina_uid,
                    'sina_token' : sina_token,
                    'last_api' : last_api
                };
                if (now / 1000 > sina_expire - 120) {//刷新token
                    if (param == 'mine') {
                        return false;
                    }//过期
                    return obj;
                } else {
                    return obj;
                }
                break;
            default:
                return false;
                break;
        }
    }
}

function getstor(key) {
	var val = get_loc_val('mine', key);
	if (val) {
		return val;
	} else {
		return false;
	}
}
//时间戳转成对应日期时间，格式为：2009-03-23
function timetoDate(tm) {
	var date = new Date(parseInt(tm) * 1000);
	var month = date.getMonth() + 1;
	var day = date.getDate();
	if (month < 10)
		month = "0" + month;
	if (day < 10)
		day = "0" + day;

	return date.getFullYear() + "-" + month + "-" + day;
}

//时间戳转日期
function formatDate(now, t) {
    var date = new Date(parseInt(now*1000));
    if (t == 'Y') {
        Y = date.getFullYear();
        return Y;
    }
    if (t == 'M') {
        M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1);
        return M;
    }
    if (t == 'D') {
        D = date.getDate();
        return D;
    }
    if (t == 'h') {
        h = date.getHours();
        return h;
    }
    if (t == 'm') {
        m = date.getMinutes();
        return m;
    }
    if (t == 's') {
        s = date.getSeconds();
        return s;
    }
}
//秒数转成时分秒
function formatSec(value) {
    var theTime = parseInt(value);
    // 秒
    var theTime1 = 0;
    // 分
    var theTime2 = 0;
    // 小时
    if (theTime >= 60) {
        theTime1 = parseInt(theTime / 60);
        theTime = parseInt(theTime % 60);
        if (theTime1 >= 60) {
            theTime2 = parseInt(theTime1 / 60);
            theTime1 = parseInt(theTime1 % 60);
        }
    }
    var i, s, h;
    if (theTime2 >= 10) {
        h = theTime2;
    } else {
        h = '0' + theTime2;
    }
    if (theTime1 >= 10) {
        i = theTime1;
    } else {
        i = '0' + theTime1;
    }
    if (theTime >= 10) {
        s = theTime;
    } else {
        s = '0' + theTime;
    }
    return h + ':' + i + ':' + s;
}
function time2str(time){
        var d = new Date();
        var t = parseInt(d.getTime()/1000);
        time = parseInt(time/1000);
        var range = t-time;
        if(range<0){
                return "未知";
        }else if(range < 60){
               return range+"秒前";
        }else if(range < 3600 ){
               return parseInt(range/60)+"分钟前";
        }else if(range < 86400){
              return parseInt(range/3600)+"小时前";
        }else if(range < 864000){
             return parseInt(range/86400)+"天前";
        }else{
                return "10天前";
        }
}

Date.prototype.Format = function (fmt) { //author: meizz
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};



//判断是否为空
function isEmpty(data) {
	if (isEmpty1(data) || isEmpty2(data)) {
		return true;
	}
	return false;
}

function isEmpty1(data) {
	if (data == undefined || data == null || data == "" || data == 'NULL' || data == false || data == 'false') {
		return true;
	}
	return false;
}

function isEmpty2(v) {
	switch (typeof v) {
		case 'undefined' :
			return true;
		case 'string' :
			if ($api.trim(v).length == 0)
				return true;
			break;
		case 'boolean' :
			if (!v)
				return true;
			break;
		case 'number' :
			if (0 === v)
				return true;
			break;
		case 'object' :
			if (null === v)
				return true;
			if (undefined !== v.length && v.length == 0)
				return true;
			for (var k in v) {
				return false;
			}
			return true;
			break;
	}
	return false;
}

function get_loc_val(key, index) {
	var val = $api.getStorage(key);
	if (isEmpty(val)) {
		return false;
	}
	if (isEmpty(val[index])) {
		return false;
	}
	return val[index];
}
/**
 *
 * @param appBundle
 * @param callback
 */
function app_installed(appBundle, callback) {
	api.appInstalled({
		appBundle : appBundle
	}, function(ret, err) {
		if (ret.installed) {
			callback(true);
		} else {
			callback(false);
		}
	});
}
/**
 * lbbniu hahah
 * @param str
 * @param array
 * @returns {boolean}
 */
function in_array(str, array) {
    for (var p in array) {
        if (array[p] == str) {
            return true;
        }
    }
    return false;
}

/*
* 版本更新
 */
function version_update(callback){
    if (api.systemType == 'android'){
        var mam = api.require('mam');
        var m=0;
        mam.checkUpdate(function(ret, err) {
            if (ret) {
                if(ret.status) {
                    var result = ret.result;
                    if(result.closed){
                        api.toast({msg :result.closeTip,location:'middle'});
                    }
                    if(result.update){
                        api.confirm({
                            title : window.lang.alert_find_new_version + result.version + ','+window.lang.alert_release_time+'：' + result.time,
                            msg : result.updateTip,
                            buttons : [window.lang.alert_sure, window.lang.alert_cancel]
                        }, function(ret1, err1) {
                            if (ret1.buttonIndex == 1) {
                                if(typeof callback =='function'){
                                    callback();
                                }
                                if (result&&result.update) {
                                    if(api.systemType=='ios'){
                                        api.openApp({iosUrl: result.source});
                                    }else{
                                        api.openApp({
                                                androidPkg: 'android.intent.action.VIEW',
                                                mimeType: 'text/html',
                                                uri: result.source},
                                            function( ret, err ){
                                                if(ret){
                                                    m =1;
                                                }
                                            });
                                    }
                                    if(m==1){
                                        if(typeof callback == 'function'){
                                            callback();
                                        }
                                    }
                                }
                            } else {
                                api.toast({
                                    msg :window.lang.alert_clance_update,
                                    location:'middle'
                                });
                            }
                        });
                    }
                }
            }
        });
    }
}


/*
 * 底部加载数据方法
* @author zhangpeng
 */
function bottom_load(callback){
    api.addEventListener({
        name:'scrolltobottom',
        extra:{
            threshold:0            //设置距离底部多少距离时触发，默认值为0，数字类型
        }
    },function(ret,err){
        if (typeof(callback)== 'function'){
            callback(ret, err);
        }
    });

}

/*
*头部刷新加载数据
*visible: true,
* loadingImg: 'widget://image/refresh.png',
*bgColor: '#ccc',
textColor: '#fff',
 textDown: '下拉刷新...',
 textUp: '松开刷新...',
 showTime: true
* @author zhangpeng
 */
function refresh_header(param,callback){
    var  params={
        visible: true,
        loadingImg: 'widget://image/logo.png',
        bgColor: '#ccc',
        textColor: '#fff',
        textDown: '下拉刷新...',
        textUp: '松开刷新...',
        showTime: false
    };
    $.extend(params,param);
    api.setRefreshHeaderInfo(params, function( ret, err ){

        //在这里从服务器加载数据，加载完成后调用api.refreshHeaderLoadDone()方法恢复组件到默认状态
        if(typeof callback =="function"){
            callback();
        }
    });
}


/* 可用于文件缓存
 * @author zhangpeng
 * string filename 文件路径
 * max data 内容
 * function callback 回调函数
 */
function writeFile(filename, data, callback) {
	var path= 'fs://'+filename;
    api.writeFile({
        path : path,
        data : JSON.stringify(data)
    }, function(ret, err) {
        if (typeof(callback)== 'function'){
            callback(ret, err);
        }
    });
}
/* 读取文件缓存
 * @author zhangpeng
 * string filename 文件路径
 * function callback 回调函数
 */
function readFile(filename, callback) {
	var path= 'fs://'+filename;
    api.readFile({
        path : path
    }, function(ret, err) {
        if(ret&&ret.status){
            ret.data=JSON.parse(ret.data);
        }
        if (typeof(callback)== 'function'){
            callback(ret, err);
        }

    });
}
/*模版替换
*@author zhangpeng  aaa
* string id  模版标识
* object data 替换的对象
 */
function T(id,data){
    var html=document.getElementById(id).innerHTML;
   return doT.template(html)(data);
}

/*
*@author zhangpeng
*string  str 取值范围['en','zh']
*/
function LangSwitcher(str){
    $api.setStorage('lang',str);
}

/**
 *获取信息
 * @constructor
 */
function cache_list(){

}

//不经常变化数据
window.init_cache={
    phone_zh:"fs://init/phone_zh", //手机号区号
    phone_en:"fs://init/phone_en",
    area_zh:"fs://init/area_zh",//地址区号
    area_en:"fs://init/area_en",
    time_zh:'fs://init/time_zh',//时区
    time_en:'fs://init/time_en'//时区
};
/*
* 更新固定不变的信息
*/
function u_init_cache(callback){
    ajaxRequest('com/init','get',{},function(ret,err){
        if(ret.status=='200'){
            for(var p in ret.result){
                api.writeFile({
                    path : 'fs://init/'+p+'.db',
                    data : JSON.stringify( ret.result[p])
                }, function(ret1, err1) {

                });
            }
        }
        if(typeof callback=="function"){
            callback();
        }
    });

}

/*
 * img 图片路径///
 */
function upload(img, callBack) {
    var info =is_login();
    if(!info){
        api.toast({msg:window.lang['alert_login'],location:'middle'});
        api.hideProgress();
        api.refreshHeaderLoadDone();
        return false;
    }
    var bodyParam = [];
    for(var p in info){
        bodyParam[p]=info[p];
    }
    upload_img('Test/test_up_img_post',{name:img},bodyParam,callBack);
}
//upload //
function upload_img(url, img, bodyParam, callBack) {
    var appType="client";
    var appId = 'VMING';
    var key = 'HAOHAOXUEXITIANTIANXIANGSHANG';
    var now = Date.now();
    var appKey = SHA1(appId + "VMING" + key + "VMING" + now) + "." + now+'.'+appType;
    var headers = {
        'X-Requested-With' : 'XMLHttpRequest',
        'X-Token-With' : appKey
    };
    api.ajax({
        url : common_url + '/' + url,
        method : 'post',
        headers : headers,
        data : {
            values : bodyParam,
            files : img
        }
    }, function(ret, err) {
        api.hideProgress();
        api.hideProgress();
        api.refreshHeaderLoadDone();
        if(err){
            api.toast({msg:err.msg,location:'middle'});
        }else if(ret){
            callBack(ret, err);
        }
    });
}


//前后太切换的时候 先判断当前是否处于呼叫状态，在重新刷新签名登录
/*
* 获取签名
 */
function is_sign(callback){
    ajaxRequest('Com/sign','get',{},function(ret,err){
        if(err){
            api.toast({msg:err.msg,location:'middle'});
        }else{
            if(ret&&ret.status=='200'){
                if(typeof callback =='function'){
                    var param={};
                    param.sign=ret.result;
                    callback(param);
                }
            }else{
                api.toast({msg:ret.msg,location:'middle'});
            }
        }
    },true);
}

function login(){
    if(!is_login()){
        alert('false');
        return ;
    }
    var uid = get_loc_val('mine','uid');
    if(!uid){
        return ;
    }
    is_sign(function(ret1){
        danLogin(uid,ret1.sign);
    });
}
function danLogin(uid,sign,callback){
    var lbbQQVideo = api.require('lbbQQVideo');
    var param = {identifier:uid,usersig:sign};
    lbbQQVideo.login(param, function(ret,err){
        if(ret.action == "com.tencent.avsdk.ACTION_RECV_INVITE"){//收到视频邀请
            //视频接受和拒绝页面
            api.confirm({
                title: '视频',
                msg:"收到邀请视频通话",
                buttons:['接受', '拒绝']
            },function(ret2,err){
                if(ret2.buttonIndex == 1){
                    //api.alert({msg: '点击了确定'});
                    lbbQQVideo.pairInviteAccept();
                    //
                }else{
                    lbbQQVideo.pairInviteRefuse();
                }
            });
        }else if(ret.action == "com.tencent.avsdk.ACTION_INVITE_ACCEPTED"){//收到接受邀请
            api.sendEvent({name: 'is_wait_hide'});
            //jieshou();
            openVideo(true);
        }else if(ret.action == "com.tencent.avsdk.ACTION_INVITE_REFUSED"){//接受到拒绝邀请
            //退出房间
            api.sendEvent({name: 'is_wait_hide'});
            lbbQQVideo.exitRoom();
            api.alert({msg:"对方拒绝邀请"});
        }else if(ret.action == "com.tencent.avsdk.ACTION_ROOM_JOIN_COMPLETE"){//加入房间成功
            //jieshou();
            openVideo(true);
        }else if(ret.action == "com.tencent.avsdk.ACTION_START_CONTEXT_COMPLETE"){//登录回调
            if(ret.retcode){
                api.alert({msg:"登录失败,重新点击登录，保证id唯一"});
            }else{
                if(typeof callback=='function'){
                    callback();
                }
                //登录成功
                api.alert({msg:"登录成功"});
            }
        }else if(ret.action == "com.tencent.avsdk.ACTION_PEER_LEAVE"){ //对方退出房间
            api.sendEvent({name: 'is_wait_hide'});
            lbbQQVideo.exitRoom();
            api.closeWin({name:"video_top"});
            api.alert({msg:"对方退出房间"});
        }else if(ret.action == "com.tencent.avsdk.ACTION_ROOM_CREATE_COMPLETE"){
            api.sendEvent({name: 'is_wait_show'});
            api.alert({msg:"邀请成功，等待接受"});
        }else if(ret.action == "com.tencent.avsdk.ACTION_PEER_CAMERA_OPEN"){
            //openVideo();
        }else if(ret.action == "videoview_clicked"){
            //api.alert({msg:ret});
            api.sendEvent({name:"videoview_clicked",extra:ret});
        }else if(ret.action == "com.tencent.avsdk.ACTION_MULTI_ROOM_CREATE_COMPLETE"){
            //api.alert({msg:"房间创建成功"});
            //openVideoMulti();
            openVideo(false);
        }
        //api.alert({msg:ret});

    });
}

function openVideo(isDan){
    api.openWin({
        name:"video_top",
        url:"../common/top-win.html",
        bgColor:'#fff',
        pageParam:{isDan:isDan}
    });
}
function hujiao(to_uid){
    var lbbQQVideo = api.require('lbbQQVideo');
    var peerIdentifier = to_uid;
    var param = {peerIdentifier:peerIdentifier,isVideo:true};
    lbbQQVideo.invitePair(param);
}
function is_wait(){
    var is_request=false;
    var t=30000; //30秒后自拒绝
    api.addEventListener({
        name: 'is_wait_show'
    }, function( ret, err ){
        if( ret ){
            setTimeout(function(){
                if(!is_request){
                    var lbbQQVideo = api.require('lbbQQVideo');
                    lbbQQVideo.exitRoom();
                    api.hideProgress();
                    api.toast({msg:'由于对方长时间未响应,呼叫终止',location:'middle'});
                }
            },t);
            api.showProgress({
                title: '邀请成功，等待接受',
                modal: false
            });
        }
    });
    api.addEventListener({
        name: 'is_wait_hide'
    }, function( ret, err ){
        if( ret ){
            is_request=true;
            api.hideProgress();
        }
    });

}


//即时聊函数
function is_rediter(to_uid){
    if(to_uid){
        if(is_login('mine')){
            is_online(to_uid,function(){
                is_wait();
                ajaxRequest('Com/sign_expire','get',{},function(ret1,err1){
                    if(err1){
                        api.toast({msg:window.lang['alert_line'],location:'middle'});
                    }else{
                        if(ret1){
                            if(ret1.status=='200'){
                                hujiao(to_uid);
                            }else if(ret1.status=='0010'){
                                is_sign(function(ret2){
                                    danLogin(getstor('uid'),ret2.sign,function(){
                                        hujiao(to_uid);
                                    });
                                });
                            }
                        }
                    }
                },true);
            });
        }else{
            api.alert({msg:'请先登录'});//
        }
     }
}

//判断用户是否在线
function is_online(user,callback){
    ajaxRequest('Com/online_get','get',{uid:user},function(ret,err){
        if(err){
            api.toast({msg:window.lang['alert_line']});
        }else if(ret&&ret.status=='200'){
            callback();
        }else{
            api.toast({msg:ret.msg});
        }
    },true);
}
//从后台切换到前台//
function switch_resume(){
    api.addEventListener({
        name:'resume'
    },function(ret,err){
        //operation
    });

}
