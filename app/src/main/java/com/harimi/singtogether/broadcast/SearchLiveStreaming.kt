package com.harimi.singtogether.broadcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import com.harimi.singtogether.adapter.LocalChattingAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.ArrayList

class SearchLiveStreaming : AppCompatActivity() {


    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    private lateinit var et_searchLiveStreaming : EditText
    private lateinit var iv_finishActivity : ImageView
    private lateinit var iv_search : ImageButton

    private lateinit var rv_searchRecyclerView : RecyclerView
    lateinit var liveFragmentAdapter: LiveFragmentAdapter
    private val liveStreamingPostList: ArrayList<LiveFragmentData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_live_streaming)

        iv_search = findViewById(R.id.iv_search)
        iv_finishActivity = findViewById(R.id.iv_finishActivity)
        et_searchLiveStreaming = findViewById(R.id.et_searchLiveStreaming)
        rv_searchRecyclerView =findViewById(R.id.rv_searchRecyclerView)

        rv_searchRecyclerView.layoutManager = LinearLayoutManager(this)
        rv_searchRecyclerView.addItemDecoration(
            DividerItemDecoration( this,
                DividerItemDecoration.VERTICAL)
        )
        liveFragmentAdapter = LiveFragmentAdapter(liveStreamingPostList,this)
        rv_searchRecyclerView.adapter = liveFragmentAdapter


        ////끝내기
        iv_finishActivity.setOnClickListener {
            finish()
        }

        //서치하기
        iv_search.setOnClickListener {
            var searchInput = et_searchLiveStreaming.text.toString()
            liveStreamingPostLoad(searchInput)
        }
    }


    fun liveStreamingPostLoad (searchInput : String ){

        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestSearchLiveStreaming(searchInput).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.isSuccessful) {

                    liveStreamingPostList.clear()
                    val jsonObject = JSONObject(response.body().toString())
                    val result = jsonObject.getBoolean("result")
                    Log.d("getHomePost: ", jsonObject.toString())
                    if(result){
                        val liveStreamingList = jsonObject.getString("liveStreamingList")
                        val jsonArray = JSONArray(liveStreamingList)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val idx = jsonObject.getString("idx")
                                val email = jsonObject.getString("email")
                                val thumbnail = jsonObject.getString("thumbnail")
                                val nickName = jsonObject.getString("nickName")
                                val profile = jsonObject.getString("profile")
                                val title = jsonObject.getString("title")
                                val viewer = jsonObject.getString("viewer")


                                val liveData = LiveFragmentData(idx, thumbnail, email, nickName, profile, title,viewer)
                                liveStreamingPostList.add(0, liveData)
                                liveFragmentAdapter.notifyDataSetChanged()
                            }
                    }else{
                        liveStreamingPostList.clear()
                        liveFragmentAdapter.notifyDataSetChanged()
                            Toast.makeText(applicationContext,"검색에 해당하는 게시물이 없습니다",Toast.LENGTH_SHORT).show()
                            return
                    }

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}