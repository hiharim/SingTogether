package com.harimi.singtogether.sing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.harimi.singtogether.R;

/**
 * 노래 녹화,녹음,연습 하는 액티비티
 */
public class RecordActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
    }
}