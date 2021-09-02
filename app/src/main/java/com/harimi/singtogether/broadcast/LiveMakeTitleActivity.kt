package com.harimi.singtogether.broadcast

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityLiveMakeTitleBinding


/**
 * 스트리밍 시작 전에 제목 입력하는 액티비티
 * */
class LiveMakeTitleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveMakeTitleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLiveMakeTitleBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //뒤로가기 눌렀을때
        binding.activityLiveStartIvClose.setOnClickListener {
//            val bundle = Bundle()
//            val liveFragment = LiveFragment()
//            liveFragment.arguments = bundle
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.add(R.id.activity_main_frame, liveFragment)
//            transaction.commit()

            finish()
        }

        // 실시간 스트리밍 시작 버튼 클릭
        binding.activityLiveStartBtn.setOnClickListener {

            var liveTitle = binding.activityLiveStartEt.text.toString()

            if (liveTitle==null || liveTitle == "") {
                Toast.makeText(this,"제목을 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                val intent = Intent(this, LiveThumbnailCaptureActivity::class.java)
                intent.putExtra("liveTitle", liveTitle)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}