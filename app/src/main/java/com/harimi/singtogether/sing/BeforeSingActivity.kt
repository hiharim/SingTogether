package com.harimi.singtogether.sing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.harimi.singtogether.R

/**
 * 노래부르기전에 녹음,녹화,솔로,듀엣 설정하는 화면
 **/
class BeforeSingActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_sing)
    }
}