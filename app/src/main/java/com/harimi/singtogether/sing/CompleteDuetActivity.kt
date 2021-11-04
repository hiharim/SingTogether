package com.harimi.singtogether.sing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.databinding.ActivityCompleteDuetBinding
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * 듀엣 완성된 포스팅 보는 화면
 * */
class CompleteDuetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompleteDuetBinding
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var duet_idx : Int? = null // duet 테이블 idx
    private var count_duet : String? = null
    private val homePostList: ArrayList<HomeData> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCompleteDuetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 서버 연결
        initRetrofit()

        duet_idx=intent.getIntExtra("duet_idx",0)
        count_duet=intent.getStringExtra("cnt_duet")
        binding.activityCompleteDuetTvCnt.text=count_duet+"의 완성된 포스팅이 있습니다."

        // 뒤로가기
        binding.activityCompleteDuetIbBack.setOnClickListener {
            finish()
        }


        // 리사이클러뷰 설정
        binding.activityCompleteDuetRv.layoutManager= LinearLayoutManager(this)
        binding.activityCompleteDuetRv.setHasFixedSize(true)
        binding.activityCompleteDuetRv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        homeAdapter= HomeAdapter(homePostList)
        binding.activityCompleteDuetRv.adapter= homeAdapter

    }




        private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}