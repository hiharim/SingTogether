package com.harimi.singtogether.sing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityDuetBinding
import com.harimi.singtogether.databinding.ActivityVideo3Binding

/**
 * 다른사용자와 듀엣 하기전에 녹화,녹음 선택하는 화면
 *
 * */
class DuetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDuetBinding
    private var duet_idx : Int? = null
    private var mr_idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var duet_path : String? = null
    private var mr_path : String? = null
    private var extract_path : String? = null
    private var kinds : String? = null
    private var genre : String? = null
    private var user_profile : String? = null
    private var user_nickname: String? = null
    private var user_email: String? = null
    private var lyrics : String? = null // 가사
    private val with : String = "듀엣" // 솔로인지 듀엣인지
    private var way : String = "녹화" // 녹화,녹음,연습 인지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDuetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        duet_idx=intent.getIntExtra("duet_idx",0)
        mr_idx=intent.getIntExtra("mr_idx",0)
        title=intent.getStringExtra("title")
        singer=intent.getStringExtra("singer")
        duet_path=intent.getStringExtra("duet_path")
        mr_path=intent.getStringExtra("mr_path")
        extract_path=intent.getStringExtra("extract_path")
        user_profile=intent.getStringExtra("profile")
        user_nickname=intent.getStringExtra("nickname")
        user_email=intent.getStringExtra("email")
        lyrics=intent.getStringExtra("lyrics")
        kinds=intent.getStringExtra("kinds")

        Log.e("DuetActivity", "가사 : " + lyrics)
        binding.activityBeforeSingTvTitle.text=title
        binding.activityBeforeSingTvSinger.text=singer

        // 내 프로필
        val profile = LoginActivity.user_info.loginUserProfile
        Glide.with(this).load("http://3.35.236.251/"+profile).into(binding.activityBeforeSingIvMe)
        //상대방 프로필
        Glide.with(this).load("http://3.35.236.251/"+user_profile).into(binding.activityBeforeSingIvUser)
        // 상대방 닉네임
        binding.activityDuetTvUserNickname.text=user_nickname

        // 녹화, 녹음버튼 토글
//        binding.activityBeforeSingBtnVideo.setOnClickListener {
//            way="녹화"
//            binding.activityBeforeSingBtnVideo.background= ContextCompat.getDrawable(this, R.drawable.button_record_select)
//            binding.activityBeforeSingBtnVoice.background= ContextCompat.getDrawable(this, R.drawable.button_record)
//        }
//        binding.activityBeforeSingBtnVoice.setOnClickListener {
//            way="녹음"
//            binding.activityBeforeSingBtnVideo.background= ContextCompat.getDrawable(this, R.drawable.button_record)
//            binding.activityBeforeSingBtnVoice.background= ContextCompat.getDrawable(this, R.drawable.button_record_select)
//        }

        if(kinds.equals("녹음")){
            binding.activityBeforeSingBtnVoice.visibility= View.VISIBLE
            binding.activityBeforeSingBtnVideo.visibility=View.GONE
        }else{
            binding.activityBeforeSingBtnVoice.visibility= View.GONE
            binding.activityBeforeSingBtnVideo.visibility=View.VISIBLE
        }

        // 부르기 버튼 클릭
        binding.activityBeforeSingBtnStart.setOnClickListener {
            if(kinds.equals("녹음")) {
                //val intent= Intent(this,RecordActivity::class.java)
                val intent= Intent(this,MergeAudioActivity::class.java)
                intent.putExtra("RECORD_DUET_IDX",duet_idx)
                intent.putExtra("RECORD_MR_IDX",mr_idx)
                intent.putExtra("RECORD_TITLE",title)
                intent.putExtra("RECORD_SINGER",singer)
                intent.putExtra("RECORD_MR_PATH",mr_path)
                intent.putExtra("RECORD_SONG_PATH",duet_path)
                intent.putExtra("RECORD_EXTRACT_PATH",extract_path)
                intent.putExtra("RECORD_LYRICS",lyrics)
                intent.putExtra("WITH",with)
                intent.putExtra("WAY",way)
                intent.putExtra("COLLABORATION",user_nickname)
                intent.putExtra("COLLABO_EMAIL",user_email)
                startActivity(intent)
                finish()

            }else  {
                val intent= Intent(this,MergeActivity::class.java)
                intent.putExtra("RECORD_DUET_IDX",duet_idx)
                intent.putExtra("RECORD_MR_IDX",mr_idx)
                intent.putExtra("RECORD_TITLE",title)
                intent.putExtra("RECORD_SINGER",singer)
                intent.putExtra("RECORD_SONG_PATH",duet_path)
                intent.putExtra("RECORD_MR_PATH",mr_path)
                intent.putExtra("RECORD_EXTRACT_PATH",extract_path)
                intent.putExtra("RECORD_LYRICS",lyrics)
                intent.putExtra("WITH",with)
                intent.putExtra("WAY",way)
                intent.putExtra("COLLABORATION",user_nickname)
                intent.putExtra("COLLABO_EMAIL",user_email)
                startActivity(intent)
                finish()
            }
        }
    }
}