
//控制页面字体 取一次值就行了，pad开的窗口尺寸不一样
//if(window.localStorage.fontSize){
//     $('html').css('font-size',window.localStorage.fontSize);
//}else{
//     var d_W = document.documentElement.clientWidth;
//     var d_H = document.documentElement.clientHeight;
//     var f_S = 100 * ((d_W * (1- (768 / 1024 - d_H / d_W))) / 750) + 'px';
//     $('html').css('font-size', f_S);
//     window.localStorage.fontSize = f_S;
//}

// var f_S = 100 * ((d_W * (1- (768 / 1024 - d_H / d_W))) / 750) + 'px';
// $('html').css('font-size', f_S);
//$('html').css('font-size',100*(document.body.clientWidth/640)+'px');
(function (doc, win) {
    var docEl = doc.documentElement,
        resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
        recalc    = function () {
            var clientWidth = docEl.clientWidth;
            if (clientWidth>=640) {
                clientWidth = 640;
            };
            if (!clientWidth) return;
            docEl.style.fontSize = 100 * (clientWidth / 640) + 'px';
        };
    if (!doc.addEventListener) return;
    win.addEventListener(resizeEvt, recalc, false);
    doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);