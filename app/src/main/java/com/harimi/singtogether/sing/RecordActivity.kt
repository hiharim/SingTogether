package com.harimi.singtogether.sing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityRecordBinding

class RecordActivity: AppCompatActivity()  {

    private lateinit var binding: ActivityRecordBinding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var song_path : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("RECORD_IDX",0)
        title=intent.getStringExtra("RECORD_TITLE")
        singer=intent.getStringExtra("RECORD_SINGER")
        song_path=intent.getStringExtra("RECORD_SONG_PATH")

        binding.toolbarRecord.setBackgroundColor(resources.getColor(R.color.dark_purple))

    }


}
