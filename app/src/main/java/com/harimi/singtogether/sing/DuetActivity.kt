package com.harimi.singtogether.sing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityDuetBinding
import com.harimi.singtogether.databinding.ActivityVideo3Binding

/**
 * 다른사용자와 듀엣 하는 액티비티 화면
 * ffmpeg - merge 여기서
 * */
class DuetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDuetBinding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var song_path : String? = null
    private var genre : String? = null
    private var user_profile : String? = null
    private var lyrics : String? = null // 가사
    private val with : String = "듀엣" // 솔로인지 듀엣인지
    private var way : String = "녹화" // 녹화,녹음,연습 인지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDuetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("idx",0)
        title=intent.getStringExtra("title")
        singer=intent.getStringExtra("singer")
        song_path=intent.getStringExtra("song_path")
        user_profile=intent.getStringExtra("user_profile")


        // 내 프로필
        val profile = LoginActivity.user_info.loginUserProfile
        Glide.with(this).load("http://3.35.236.251/"+profile).into(binding.activityBeforeSingIvMe)
        //상대방 프로필
        Glide.with(this).load("http://3.35.236.251/"+user_profile).into(binding.activityBeforeSingIvUser)
        // 녹화, 녹음버튼 토글
        binding.activityBeforeSingBtnVideo.setOnClickListener {
            way="녹화"
            binding.activityBeforeSingBtnVideo.background= ContextCompat.getDrawable(this, R.drawable.button_record_select)
            binding.activityBeforeSingBtnVoice.background= ContextCompat.getDrawable(this, R.drawable.button_record)
        }
        binding.activityBeforeSingBtnVoice.setOnClickListener {
            way="녹음"
            binding.activityBeforeSingBtnVideo.background= ContextCompat.getDrawable(this, R.drawable.button_record)
            binding.activityBeforeSingBtnVoice.background= ContextCompat.getDrawable(this, R.drawable.button_record_select)
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
            }
        }
    }
}