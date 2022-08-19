package com.example.tenutotutor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.R;
import com.example.tenutotutor.databinding.FragmentLibraryBinding;
import com.example.tenutotutor.databinding.FragmentMeBinding;
import com.example.tenutotutor.ui.library.Midi;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

public class FirstPageActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(FirstPageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, 1000);



    }

}
