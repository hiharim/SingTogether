package com.harimi.singtogether.sing

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
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
    private var lyrics : String? = null // 가사
    private var with : String = "솔로" // 솔로인지 듀엣인지
    private var way : String = "녹화" // 녹화,녹음,연습 인지


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBeforeSingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("IDX",0)
        title=intent.getStringExtra("TITLE")
        singer=intent.getStringExtra("SINGER")
        song_path=intent.getStringExtra("SONG_PATH")
        genre=intent.getStringExtra("GENRE")
        lyrics=intent.getStringExtra("LYRICS")


        binding.activityBeforeSingTvTitle.text=title
        binding.activityBeforeSingTvSinger.text=singer

        // 솔로 버튼 클릭
        binding.activityBeforeSingBtnSolo.setOnClickListener {
            with="솔로"
            binding.activityBeforeSingBtnSolo.background=ContextCompat.getDrawable(this,R.drawable.button_shape_select)
            binding.activityBeforeSingBtnDuet.background=ContextCompat.getDrawable(this,R.drawable.button_shape)
        }
        // 듀엣 버튼 클릭
        binding.activityBeforeSingBtnDuet.setOnClickListener {
            with="듀엣"
            binding.activityBeforeSingBtnSolo.background=ContextCompat.getDrawable(this,R.drawable.button_shape)
            binding.activityBeforeSingBtnDuet.background=ContextCompat.getDrawable(this,R.drawable.button_shape_select)
        }

        // 녹화, 녹음, 연습 버튼 토글
        binding.activityBeforeSingBtnVideo.setOnClickListener {
            way="녹화"
            binding.activityBeforeSingBtnVideo.background=ContextCompat.getDrawable(this,R.drawable.button_record_select)
            binding.activityBeforeSingBtnVoice.background=ContextCompat.getDrawable(this,R.drawable.button_record)
            binding.activityBeforeSingBtnOff.background=ContextCompat.getDrawable(this,R.drawable.button_record)
        }
        binding.activityBeforeSingBtnVoice.setOnClickListener {
            way="녹음"
            binding.activityBeforeSingBtnVideo.background=ContextCompat.getDrawable(this,R.drawable.button_record)
            binding.activityBeforeSingBtnVoice.background=ContextCompat.getDrawable(this,R.drawable.button_record_select)
            binding.activityBeforeSingBtnOff.background=ContextCompat.getDrawable(this,R.drawable.button_record)
        }
        binding.activityBeforeSingBtnOff.setOnClickListener {
            way="연습"
            binding.activityBeforeSingBtnVideo.background=ContextCompat.getDrawable(this,R.drawable.button_record)
            binding.activityBeforeSingBtnVoice.background=ContextCompat.getDrawable(this,R.drawable.button_record)
            binding.activityBeforeSingBtnOff.background=ContextCompat.getDrawable(this,R.drawable.button_record_select)
        }

        // 부르기 버튼 클릭
        binding.activityBeforeSingBtnStart.setOnClickListener {
            if(way=="녹음") {
                val intent= Intent(this,RecordActivity::class.java)
                intent.putExtra("RECORD_IDX",idx)
                intent.putExtra("RECORD_TITLE",title)
                intent.putExtra("RECORD_SINGER",singer)
                intent.putExtra("RECORD_SONG_PATH",song_path)
                intent.putExtra("RECORD_LYRICS",lyrics)
                intent.putExtra("WITH",with)
                intent.putExtra("WAY",way)
                startActivity(intent)
                finish()
            }else if (way=="녹화"){
                //val intent= Intent(this,Video3Activity::class.java)
                val intent= Intent(this,Video2Activity::class.java)
                //val intent= Intent(this,VideoActivity::class.java)
                intent.putExtra("RECORD_IDX",idx)
                intent.putExtra("RECORD_TITLE",title)
                intent.putExtra("RECORD_SINGER",singer)
                intent.putExtra("RECORD_SONG_PATH",song_path)
                intent.putExtra("RECORD_LYRICS",lyrics)
                intent.putExtra("WITH",with)
                intent.putExtra("WAY",way)
                startActivity(intent)
                finish()
            }// 연습

        }

        // 닫기 클릭
        binding.activityBeforeSingBtnClose.setOnClickListener {
            finish()
        }



    }
}