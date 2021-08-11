package com.harimi.singtogether.sing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityBeforeSingBinding

/**
 * 노래부르기전에 녹음,녹화,솔로,듀엣 설정하는 화면
 **/
class BeforeSingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBeforeSingBinding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var song_path : String? = null
    private var genre : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBeforeSingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("IDX",0)
        title=intent.getStringExtra("TITLE")
        singer=intent.getStringExtra("SINGER")
        song_path=intent.getStringExtra("SONG_PATH")
        genre=intent.getStringExtra("GENRE")


        binding.activityBeforeSingTvTitle.text=title
        binding.activityBeforeSingTvSinger.text=singer

        // 솔로,듀엣 버튼 토글

        // 녹화, 녹음, off 버튼 토글

        // 부르기 버튼 클릭
        binding.activityBeforeSingBtnStart.setOnClickListener {

        }


    }
}