免root版跳一跳辅助。
原理：
1 手机安装jumpNoRoot辅助，会覆盖浮层在小程序上面
2 点击两点，辅助计算距离，按像素距离/速度（1920x1080 大概是1.35）=毫秒数
3 运行android自带程序 input swipe x y x y ms
4 运行input程序需要root，所以pc端跟辅助进行socket连接，接收app端的命令在pc端执行。具体是usb连接pc端，用adb shell input 方式执行模拟跳转。
