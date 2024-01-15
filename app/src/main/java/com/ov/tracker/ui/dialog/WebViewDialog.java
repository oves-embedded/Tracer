package com.ov.tracker.ui.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


import com.google.gson.Gson;
import com.ov.tracker.R;
import com.ov.tracker.entity.CharacteristicDomain;
import com.ov.tracker.utils.LogUtil;

import java.util.Base64;

public class WebViewDialog extends DialogFragment {

    private View root_view;

    private WebView webView;

    //声明WebSettings子类
//    WebSettings webSettings = webView.getSettings();
//    //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
//        webSettings.setJavaScriptEnabled(true);
//    //设置自适应屏幕，两者合用
//        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
//        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
//
//    //缩放操作
//        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
//        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
//        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
//    //其他细节操作
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
//        webSettings.setAllowFileAccess(true); //设置可以访问文件
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
//        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
//        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        root_view = inflater.inflate(R.layout.dialog_webview, container, false);
//        webView = root_view.findViewById(R.id.webView);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setVerticalScrollBarEnabled(false);
////        webView.getSettings().setUseWideViewPort(true);
////        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.setNetworkAvailable(true);
//        webView.setWebViewClient(new MyClient());
//        webView.setWebChromeClient(new MyWebChromeClient());

        root_view = inflater.inflate(R.layout.dialog_webview, container, false);
        webView = root_view.findViewById(R.id.webView);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setNetworkAvailable(true);
        webView.setWebViewClient(new MyClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.loadUrl("file:///android_asset/index.html");

        return root_view;
    }

    public void pushData(CharacteristicDomain domain) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String base64Str = Base64.getEncoder().encodeToString(new Gson().toJson(domain).getBytes());
            LogUtil.info("pushData==>evaluateJavascript==>" + base64Str);
            webView.evaluateJavascript("javascript:addData('" + base64Str + "')", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //此处为 js 返回的结果
                }
            });
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    class MyClient extends WebViewClient {
        //监听到页面发生跳转的情况，默认打开web浏览器
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //在webView中加载要打开的链接
            view.loadUrl(request.getUrl().toString());
            LogUtil.debug("MyClient==>shouldOverrideUrlLoading");
            return true;
        }

        //页面开始加载
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            LogUtil.debug("MyClient==>onPageStarted");
        }

        //页面加载完成的回调方法
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            LogUtil.debug("MyClient==>onPageFinished");
            //在webView中加载js代码
//            webView.loadUrl("javascript:alert('hello')");
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        //监听网页进度 newProgress进度值在0-100
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            LogUtil.debug("MyWebChromeClient==>onProgressChanged");
        }

        //设置Activity的标题与 网页的标题一致
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            LogUtil.debug("MyWebChromeClient==>onReceivedTitle");
        }
    }
}
