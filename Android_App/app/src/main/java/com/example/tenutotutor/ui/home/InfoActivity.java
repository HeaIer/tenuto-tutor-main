package com.example.tenutotutor.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.example.tenutotutor.databinding.ActivityInfoBinding;
import com.example.tenutotutor.R;


public class InfoActivity extends AppCompatActivity {
    private ActivityInfoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        WebView webView2 = findViewById(R.id.webView2);
        webView2.loadUrl("file:android_asset/info.html");
    }

}