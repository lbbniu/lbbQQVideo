var ws = {};
var client_id = 0;
var userlist = {};
var GET = getRequest();
var webim = {
    'server' : 'ws://app205-kt.vliang.com:9503'
}
$(document).ready(function () {
    //使用原生WebSocket
    ws = new WebSocket(webim.server);
    listenEvent();

});
function listenEvent() {
    /**
     * 连接建立时触发
     */
    ws.onopen = function (e) {
        //连接成功
        console.log("connect webim server success.");
        //发送登录信息
        msg = new Object();
        msg.cmd = 'login';
        msg.uid = 1;
        ws.send($.toJSON(msg));
    };

    //有消息到来时触发
    ws.onmessage = function (e) {
        var message = $.evalJSON(e.data);
        console.log(message);
        var cmd = message.cmd;
        if (cmd == 'login')
        {
            client_id = message.fd;
            //获取在线列表
            ws.send($.toJSON({cmd:"getFanye",courseid:1}));
        }
        else if (cmd == 'getFanye')
        {
            
        }
        else if (cmd == 'fanye')//执行翻页函数
        {
            //showHistory(message);
        }
        else if (cmd == 'offline')//退出收到的数据
        {
            
        }
    };

    /**
     * 连接关闭事件
     */
    ws.onclose = function (e) {
        // if (confirm("聊天服务器已关闭")) {
        //     //alert('您已退出聊天室');
        //     //location.href = 'index.html';
        // }
    };

    /**
     * 异常事件
     */
    ws.onerror = function (e) {
        alert("异常:" + e.data);
        console.log("onerror");
    };
}
/**
 * 当有一个新用户连接上来时
 * @param dataObj
 */
function GetDateT(time_stamp) {
    var d;
    d = new Date();

    if (time_stamp) {
        d.setTime(time_stamp * 1000);
    }
    var h, i, s;
    h = d.getHours();
    i = d.getMinutes();
    s = d.getSeconds();

    h = ( h < 10 ) ? '0' + h : h;
    i = ( i < 10 ) ? '0' + i : i;
    s = ( s < 10 ) ? '0' + s : s;
    return h + ":" + i + ":" + s;
}

function getRequest() {
    var url = location.search; // 获取url中"?"符后的字串
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);

        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            var decodeParam = decodeURIComponent(strs[i]);
            var param = decodeParam.split("=");
            theRequest[param[0]] = param[1];
        }

    }
    return theRequest;
}

function sendMsg(courseid, page) {
    var msg = {};
    msg.cmd = 'fanye';
    msg.courseid = courseid;
    msg.page = page;
    ws.send($.toJSON(msg)); 
}





