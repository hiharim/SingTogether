package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.BestData
import com.harimi.singtogether.Data.BestDuetData
import com.harimi.singtogether.Data.BestSoloData
import com.harimi.singtogether.Data.DetailReplayReviewData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.BestAdapter
import com.harimi.singtogether.adapter.BestDuetAdapter
import com.harimi.singtogether.adapter.BestSoloAdapter
import com.harimi.singtogether.adapter.ReplayFragmentAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GloryActivity : AppCompatActivity() {
    private var TAG :String = "GloryActivity_"
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    private lateinit var iv_backArrow : ImageButton
    private lateinit var tv_year : TextView
    private lateinit var iv_forwardArrow : ImageButton
    private lateinit var rv_bestDuet : RecyclerView
    private lateinit var rv_bestSolo : RecyclerView
    private lateinit var tv_duetText : TextView
    private lateinit var tv_soloText : TextView

    private lateinit var getYear : String

    private val bestDuetList: ArrayList<BestDuetData> = ArrayList()
    private lateinit var bestDuetAdapter: BestDuetAdapter

    private val bestSoloList: ArrayList<BestSoloData> = ArrayList()
    private lateinit var bestSoloAdapter: BestSoloAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glory)

        iv_backArrow = findViewById(R.id.iv_backArrow)
        tv_year = findViewById(R.id.tv_year)
        iv_forwardArrow = findViewById(R.id.iv_forwardArrow)
        rv_bestDuet = findViewById(R.id.rv_bestDuet)
        rv_bestSolo = findViewById(R.id.rv_bestSolo)
        tv_duetText = findViewById(R.id.tv_duetText)
        tv_soloText = findViewById(R.id.tv_soloText)

        rv_bestDuet.layoutManager = LinearLayoutManager(this)
        rv_bestDuet.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        bestDuetAdapter = BestDuetAdapter(bestDuetList,this)
        rv_bestDuet.adapter = bestDuetAdapter

        rv_bestSolo.layoutManager = LinearLayoutManager(this)
        rv_bestSolo.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        bestSoloAdapter = BestSoloAdapter(bestSoloList,this)
        rv_bestSolo.adapter = bestSoloAdapter

        var nowYear : String = SimpleDateFormat("yyyy-MM-dd").format(Date())
        var timeArr = nowYear.split("-")
        var year = timeArr[0].toInt() -1
        getYear = year.toString()
        Log.d(TAG, getYear)



    }

    private fun loadGloryPost(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestLoadGloryPost(getYear,LoginActivity.user_info.loginUserEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        bestDuetList.clear()
                        bestSoloList.clear()

                        val jsonObject = JSONObject(body)
                        val duetDataList=jsonObject.getString("DuetDataList")
                        val SoloDataList=jsonObject.getString("SoloDataList")

                        val duetDataArray= JSONArray(duetDataList)
                        for(i in 0..duetDataArray.length() -1){
                            val duetDataObject=duetDataArray.getJSONObject(i)
                            val idx=duetDataObject.getInt("idx")
                            val thumbnail=duetDataObject.getString("thumbnail")
                            val cnt_play=duetDataObject.getString("cnt_play")
                            val cnt_reply=duetDataObject.getString("cnt_reply")
                            val cnt_like=duetDataObject.getString("cnt_like")
                            val nickname=duetDataObject.getString("nickname")
                            val email=duetDataObject.getString("email")
                            val song_path=duetDataObject.getString("song_path")
                            val date=duetDataObject.getString("date")
                            val collaboration=duetDataObject.getString("collaboration")
                            val mr_idx=duetDataObject.getInt("mr_idx")
                            val title=duetDataObject.getString("title")
                            val singer=duetDataObject.getString("singer")
                            val lyrics=duetDataObject.getString("lyrics")
                            val profile=duetDataObject.getString("circle_profile")
                            val collaboration_profile=duetDataObject.getString("col_profile")
                            val collabo_email=duetDataObject.getString("collabo_email")
                            val kinds=duetDataObject.getString("kinds")
                            val token=duetDataObject.getString("token")
                            val col_token=duetDataObject.getString("col_token")
                            val isLike=duetDataObject.getString("isLike")
                            val bestDuetData = BestDuetData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike)
                            bestDuetList.add(0,bestDuetData)
                            bestDuetAdapter.notifyDataSetChanged()
                        }

                        val SoloDataArray= JSONArray(SoloDataList)
                        for(i in 0..SoloDataArray.length() -1){
                            val SoloDataObject=SoloDataArray.getJSONObject(i)
                            val idx=SoloDataObject.getInt("idx")
                            val thumbnail=SoloDataObject.getString("thumbnail")
                            val cnt_play=SoloDataObject.getString("cnt_play")
                            val cnt_reply=SoloDataObject.getString("cnt_reply")
                            val cnt_like=SoloDataObject.getString("cnt_like")
                            val nickname=SoloDataObject.getString("nickname")
                            val email=SoloDataObject.getString("email")
                            val song_path=SoloDataObject.getString("song_path")
                            val date=SoloDataObject.getString("date")
                            val collaboration=SoloDataObject.getString("collaboration")
                            val mr_idx=SoloDataObject.getInt("mr_idx")
                            val title=SoloDataObject.getString("title")
                            val singer=SoloDataObject.getString("singer")
                            val lyrics=SoloDataObject.getString("lyrics")
                            val profile=SoloDataObject.getString("circle_profile")
                            val collaboration_profile=SoloDataObject.getString("col_profile")
                            val collabo_email=SoloDataObject.getString("collabo_email")
                            val kinds=SoloDataObject.getString("kinds")
                            val token=SoloDataObject.getString("token")
                            val col_token=SoloDataObject.getString("col_token")
                            val isLike=SoloDataObject.getString("isLike")
                            val bestSoloData = BestSoloData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike)
                            bestSoloList.add(0,bestSoloData)
                            bestSoloAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
}