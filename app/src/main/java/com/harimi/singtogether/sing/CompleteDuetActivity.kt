package com.harimi.singtogether.sing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.HomeData
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

        loadCompleteDuet()
        // 리사이클러뷰 설정
        binding.activityCompleteDuetRv.layoutManager= LinearLayoutManager(this)
        binding.activityCompleteDuetRv.setHasFixedSize(true)
        binding.activityCompleteDuetRv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        homeAdapter= HomeAdapter(homePostList)
        binding.activityCompleteDuetRv.adapter= homeAdapter

    }

    private fun loadCompleteDuet() {
        duet_idx?.let {
            retrofitService.loadCompleteDuet(it).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        val jsonArray = JSONArray(response.body().toString())
                        for (i in 0..jsonArray.length() - 1) {
                            val iObject = jsonArray.getJSONObject(i)
                            val idx = iObject.getInt("idx")
                            val thumbnail = iObject.getString("thumbnail")
                            val cnt_play = iObject.getString("cnt_play")
                            val cnt_reply = iObject.getString("cnt_reply")
                            val cnt_like = iObject.getString("cnt_like")
                            val nickname = iObject.getString("nickname")
                            val email = iObject.getString("email")
                            val song_path = iObject.getString("song_path")
                            val date = iObject.getString("date")
                            val collaboration = iObject.getString("collaboration")
                            val mr_idx = iObject.getInt("mr_idx")
                            val title = iObject.getString("title")
                            val singer = iObject.getString("singer")
                            val lyrics = iObject.getString("lyrics")
                            val profile = iObject.getString("profile")
                            val collaboration_profile = iObject.getString("col_profile")
                            val collabo_email = iObject.getString("collabo_email")
                            val kinds = iObject.getString("kinds")
                            val token = iObject.getString("token")

                            val homeData = HomeData(
                                idx,
                                thumbnail,
                                title,
                                singer,
                                lyrics,
                                cnt_play,
                                cnt_reply,
                                cnt_like,
                                nickname,
                                email,
                                profile,
                                song_path,
                                collaboration,
                                collabo_email,
                                collaboration_profile,
                                date,
                                kinds,
                                mr_idx,
                                token
                            )
                            homePostList.add(0, homeData)
                            homeAdapter.notifyDataSetChanged()
                        }

                    } else {
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                        Log.e("DuetFragment", "loadDuet 응답 문제" + response.code())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("DetailDuetFragment", "deleteSong()  통신 실패" + t.message)
                }


            })
        }
    }


        private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}