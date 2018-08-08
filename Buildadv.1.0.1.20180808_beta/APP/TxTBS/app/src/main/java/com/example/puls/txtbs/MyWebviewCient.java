package com.example.puls.txtbs;

import android.util.Log;

import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class MyWebviewCient extends WebViewClient {
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      String url) {
        WebResourceResponse response = null;
        response = super.shouldInterceptRequest(view, url);
        return response;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Log.i("启动了JS方法", "on page finished");
        view.loadUrl("javascript:myFunction()");
    }

}
