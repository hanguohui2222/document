package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GnWebViewWindow extends AmigoActivity {

    WebView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_webview_window);
        view = (WebView)findViewById(R.id.myWebView);
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        
        
        view.loadUrl("http://bbs.amigo.cn");
        view.setWebViewClient(new WebViewClient(){
            @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
              view.loadUrl(url);
             return true;
         }
        });
    }
}
