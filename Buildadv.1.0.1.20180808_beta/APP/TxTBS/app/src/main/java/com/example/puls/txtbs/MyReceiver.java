package com.example.puls.txtbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String localPkgName = context.getPackageName();//取得MyReceiver所在的App的包名
        Uri data = intent.getData();
        String installedPkgName = data.getSchemeSpecificPart();//取得安装的Apk的包名，只在该app覆盖安装后自启动
        //检查安装包是否存在，如果存在，就删除
        File file=new File("/sdcard/Download/1.apk");
        if(file.exists()){
            file.delete();
        }
        if((action.equals(Intent.ACTION_PACKAGE_ADDED)
                || action.equals(Intent.ACTION_PACKAGE_REPLACED)) && installedPkgName.equals(localPkgName)){
            Intent launchIntent = new Intent(context,MainActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        }
    }
}
