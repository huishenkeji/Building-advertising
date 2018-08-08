var video = document.getElementById("oVideo");
var vCurr = 0;
var tCurr = 0;
var iCurr = 0;
var vList = ["3.mp4"];//初始化链接数组
var iList = ['1.jpg'];
var tList = ['我是广告'];
var oText = $(".title")[0];
setInterval(function () {
    $.ajax({
        type: "post",
        async: false,
        url: " http://banyanzhe.applinzi.com/source",  //服务器地址
        dataType: "json",
        data: {
            src: "hello"  //上传链接数组
        },
        success: function (json) {              //请求成功后的方法
            console.log(json);
            if(json.video.length > 0) {
                vList = json.video;
            }
            if(json.img.length > 0) {
                iList = json.img;
            }
            if(json.txt.length > 0) {
                tList = json.txt;
            }
            //console.log(iList);
        },
        error: function () {
            alert('fail');
        }
    });
}, 30000);
// video.addEventListener("ended",myFunction);
video.onended = function () {
    $("video").attr("src", "./src/video/" + vList[vCurr]);
    video.play();
    vCurr++;
    if (vCurr >= vList.length) vCurr = 0;
};

setInterval(function() {
    oText.innerHTML = tList[tCurr];
    tCurr++;
    if(tCurr >= tList.length) tCurr = 0;
},5000);

setInterval(function() {
    $("img").attr("src","./src/image/" + iList[iCurr]);
    iCurr++;
    if(iCurr >= iList.length) iCurr = 0;
},8000);