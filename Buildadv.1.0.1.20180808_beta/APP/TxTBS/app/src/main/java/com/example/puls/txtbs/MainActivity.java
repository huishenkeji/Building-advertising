package com.example.puls.txtbs;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLClientInfoException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    com.tencent.smtt.sdk.WebView webView;
    Integer version;
    long ID;
    BroadcastReceiver broadcastReceiver;
    String ReqUrl;
    String DownUrl;
    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PageShow();
        version= BuildConfig.VERSION_CODE;

        //构造请求链接
        ReqUrl="http://banyanzhe.applinzi.com/down?version="+version;

        //定时询问服务端是否有新版本
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                RequestOkHttp(ReqUrl);
            }
        },0,30000);

    }



    public void PageShow(){
        webView=(com.tencent.smtt.sdk.WebView)findViewById(R.id.webview);

        webView.loadUrl("http://www.banyanzhe.applinzi.com");
        webView.setWebViewClient(new WebViewClient());
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebviewCient());


        webView.setWebChromeClient(new WebChromeClient(){
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        settings.setAppCacheEnabled(true); //启用应用缓存
        settings.setDomStorageEnabled(true); //启用或禁用DOM缓存。
        settings.setDatabaseEnabled(true); //启用或禁用DOM缓存。

        if (isNetworkConnected(MainActivity.this)) { //判断是否联网
            settings.setCacheMode(WebSettings.LOAD_DEFAULT); //默认的缓存使用模式
        } else {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY); //不从网络加载数据，只从缓存加载
        }
        //webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }

    public static boolean isNetworkConnected(Context context) {
      if (context != null) {
          ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
          if (mNetworkInfo != null) {
              //mNetworkInfo.isAvailable();
              return true;//有网
              }
      }
      return false;//没有网
    }


    public void RequestOkHttp(String url){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request对象，设置一个url地址,设置请求方式。
        Request request = new Request.Builder().url(url).method("GET",null).build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            public void onFailure(Call call, IOException e) {}
            //请求成功执行的方法
            public void onResponse(Call call, Response response) throws IOException {
                String re=response.body().string();

                if(re.length()>0){
                    DownUrl=re;
                    //开启下载线程
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //调用下载方法
                            DownApk(DownUrl);
                        }
                    }).start();
                }
                else {
                    //如果返回数据为空，则说明当前为最新版本
                    Log.i("最新版本：",""+version);
                    //Toast.makeText(getApplicationContext(), "任务:"  + " 下载完成!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    //使用DownloadManager实现下载
    public void DownApk(String downUrl){
        String apkUrl=downUrl;
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setDestinationInExternalPublicDir(  Environment.DIRECTORY_DOWNLOADS  , "1.apk" ) ;
        ID= downloadManager.enqueue(request);
        listener(ID);
        return;
    }


    private void listener(final long Id) {
        // 注册广播监听系统的下载完成事件。
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (ID == Id) {
                    //Toast.makeText(getApplicationContext(), "任务:" + Id + " 下载完成!", Toast.LENGTH_LONG).show();
                    //InstallApk();
                    install(Environment.getExternalStorageDirectory()+"/Download/1.apk");
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }


    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
        super.onDestroy();
    }


    //传统安装方法，会弹出安装界面，需要用户交互
    public  void  InstallApk(){
        File apkfile = new File(Environment.getExternalStorageDirectory()+"/Download/1.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        startActivity(intent);
        //结束当前进程
        //android.os.Process.killProcess(android.os.Process.myPid());
    }


    //静默安装，需要root权限
    public boolean install(String apkPath) {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String command = "pm install -r " + apkPath + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            // 读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            Log.d("TAG", "install msg is " + msg);
            // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
            if (!msg.contains("Failure")) {
                result = true;
                //Toast.makeText(MainActivity.this,"安装成功",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage(), e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                Log.e("TAG", e.getMessage(), e);
            }
        }
        return result;
    }
}
