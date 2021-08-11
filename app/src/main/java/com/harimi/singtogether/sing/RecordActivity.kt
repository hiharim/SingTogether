package com.harimi.singtogether.sing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityRecordBinding

class RecordActivity: AppCompatActivity()  {

    private lateinit var binding: ActivityRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


}
