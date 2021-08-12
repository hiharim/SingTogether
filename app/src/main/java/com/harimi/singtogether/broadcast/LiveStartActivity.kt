package com.harimi.singtogether.broadcast

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityLiveStartBinding
import com.harimi.singtogether.databinding.ActivityProfileBinding

/**
 * 스트리밍 시작 전에 제목 입력하는 액티비티
 * */
class LiveStartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLiveStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 실시간 스트리밍 시작 버튼 클릭
        binding.activityLiveStartBtn.setOnClickListener {
            val intent =Intent(this,StreamingActivity::class.java)
            startActivity(intent)
        }
    }
}