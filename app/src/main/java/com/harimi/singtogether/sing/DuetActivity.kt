package com.harimi.singtogether.sing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.harimi.singtogether.databinding.ActivityDuetBinding
import com.harimi.singtogether.databinding.ActivityVideo3Binding

/**
 * 다른사용자와 듀엣 하는 액티비티 화면
 * */
class DuetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDuetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDuetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}