# Building-advertising 自述文件
*************************************
Building-advertising，简称BA，是一个广告播放器。
##特点
1.	轻巧
		大小仅有1M多
2.	清爽
		界面简洁清爽
3.	方便
		可自动更新与页面主题更换
##架构
--APP
	|______TxTBS(由APP内嵌腾讯X5内核浏览器，在浏览器上向服务器请求获取网页来进行楼宇广告的获取与APP更新)
--H5
	|______js(js代码引用)
	|______src(资源文件存放)
		|______apk(更新的APP)
		|______image(图片)
		|______txt(文档)
		|______video(缓存的待播放视频)
	|______index.html
	|______TeleAd_1.css
--Sever
	|______node_modules
	|______pakages.json
	|______sever.js(主要后台代码)