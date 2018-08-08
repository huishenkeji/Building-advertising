const express = require('express'); 
const express_static = require('express-static');
const bodyParser = require('body-parser');
const fs = require('fs');
const path = require("path");
const ApkReader = require('adbkit-apkreader');
const util = require('util');

let server = express();
server.use(bodyParser.urlencoded());

server.get('/',function(req,res){
    res.redirect('/index.html');
});

server.all('*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Content-Type,Content-Length, Authorization, Accept,X-Requested-With");
    res.header("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");
    res.header("X-Powered-By",'3.2.1')
    res.header("Content-Type", "application/json;charset=utf-8");
    next();
});

//android请求版本信息
server.use('/down',function(req,res){
    var apk = path.join("www/src/apk");
    //所有apk
    var apk_arr = readDirSync(apk);
    //所有版本号
    var version_arr = [];
    //取出.apk文件的文件名部分（即版本号）
    for(var i=0;i<apk_arr.length;i++){
        version_arr.push(path.basename(apk_arr[i],'.apk'));
    }
    //取得最大的版本号，即最新的版本
    var max = Math.max.apply(null,version_arr);
    console.log(max);
    ApkReader.open('www/src/apk/'+max+'.apk').then(reader =>
        reader.readManifest()).then(function(manifest){
        let manifestJson = util.inspect(manifest, { depth: null });
        // let index = manifestJson.indexOf('versionCode');
        // console.log(manifestJson.substr(index,15));
        //console.log(manifestJson);
        var str = manifestJson.substr(1,manifestJson.length-2);
        let apk_version = str.split(',')[0].split(":")[1].trim();
        //协议（http）
        let protocol = req.protocol;
        //域名（banyanzhe.applinzi.com）
        let host = req.headers.host;
        let version = req.query['version'];
        let apk_src = protocol+'://'+host+'/src/apk/'+max+'.apk';
        if(version==apk_version){
            // res.redirect('/index.html');
            res.send('').end();
        }else{
            res.send(apk_src).end();
        }
    });
});

//前台请求资源信息
server.use('/source',function(req,res){
    if(req.body.src == "hello"){
        //遍历视频文件夹
        var json_src = {video:[],img:[],txt:[]};
        var video_path = path.join("www/src/video");
        json_src.video = readDirSync(video_path);
        var img_path = path.join("www/src/image");
        json_src.img = readDirSync(img_path);
        var data = fs.readFileSync("www/src/txt/main.txt","utf-8");
        json_src.txt = data.split('|');

        res.send(json_src);
    }else{
        res.send('no data').end();
    }
});

function readDirSync(path){
    var pa = fs.readdirSync(path);
    var src_arr = [];
	pa.forEach(function(ele,index){
		var info = fs.statSync(path+"/"+ele)	
		if(info.isDirectory()){
			console.log("dir: "+ele)
			readDirSync(path+"/"+ele);
		}else{
            src_arr.push(ele);
		}
    });
    return src_arr;
}

server.use(express_static('./www/'));

server.listen(5050);