package com.calorieminer.minerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.calorieminer.minerapp.CustomClass.AppConstants;


public class ReadmeFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_readme);

        WebView webView = findViewById(R.id.readmeview);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(AppConstants.WEBVIEW_HTML_PATH);

    }
}
