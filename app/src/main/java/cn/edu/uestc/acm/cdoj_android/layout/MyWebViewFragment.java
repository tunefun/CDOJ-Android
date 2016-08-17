package cn.edu.uestc.acm.cdoj_android.layout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebViewFragment;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by great on 2016/8/16.
 */
public class MyWebViewFragment extends WebViewFragment {
    final String acmWebUrl = "http://acm.uestc.edu.cn/";
    final String mimeType = "text/html";
    final String encoding = "utf-8";
    String webData;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getWebView().getSettings().setJavaScriptEnabled(true);
        getWebView().loadDataWithBaseURL(acmWebUrl, webData, mimeType, encoding, null);
    }

    public  void addHTMLData(String data) {
        webData = data;
    }
    public void addJSData(String data) {
        this.webData = this.webData.replace("{{{replace_data_here}}}", data);
        if (getWebView() != null) {
            getWebView().loadDataWithBaseURL(acmWebUrl, webData, mimeType, encoding, null);
        }
    }
}