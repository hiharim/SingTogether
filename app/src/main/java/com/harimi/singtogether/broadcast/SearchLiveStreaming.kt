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
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import com.harimi.singtogether.adapter.LocalChattingAdapter
import com.harimi.singtogether.adapter.ReplayFragmentAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.ArrayList

class SearchLiveStreaming : AppCompatActivity() {

    private val TAG = "SEARCH_STREAMING"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    private lateinit var et_searchLiveStreaming : EditText
    private lateinit var tv_notify : TextView
    private lateinit var iv_finishActivity : ImageView
    private lateinit var iv_search : ImageButton

    private lateinit var rv_searchLiveRecyclerView : RecyclerView
    lateinit var liveFragmentAdapter: LiveFragmentAdapter
    private val liveStreamingPostList: ArrayList<LiveFragmentData> = ArrayList()

    private lateinit var rv_searchReplayRecyclerView : RecyclerView
    lateinit var replayFragmentAdapter: ReplayFragmentAdapter
    private val replayDataList: ArrayList<ReplayData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_live_streaming)

        iv_search = findViewById(R.id.iv_search)
        tv_notify = findViewById(R.id.tv_notify)
        iv_finishActivity = findViewById(R.id.iv_finishActivity)
        et_searchLiveStreaming = findViewById(R.id.et_searchLiveStreaming)


        rv_searchLiveRecyclerView =findViewById(R.id.rv_searchLiveRecyclerView)
        rv_searchLiveRecyclerView.layoutManager = LinearLayoutManager(this)
        rv_searchLiveRecyclerView.addItemDecoration(
            DividerItemDecoration( this,
                DividerItemDecoration.VERTICAL)
        )
        liveFragmentAdapter = LiveFragmentAdapter(liveStreamingPostList,this)
        rv_searchLiveRecyclerView.adapter = liveFragmentAdapter

        rv_searchReplayRecyclerView =findViewById(R.id.rv_searchReplayRecyclerView)
        rv_searchReplayRecyclerView.layoutManager = LinearLayoutManager(this)
        rv_searchReplayRecyclerView.addItemDecoration(
            DividerItemDecoration( this,
                DividerItemDecoration.VERTICAL)
        )
        replayFragmentAdapter = ReplayFragmentAdapter(replayDataList,this)
        rv_searchReplayRecyclerView.adapter = replayFragmentAdapter

        ////끝내기
        iv_finishActivity.setOnClickListener {
            finish()
        }

        //서치하기
        iv_search.setOnClickListener {
            var searchInput = et_searchLiveStreaming.text.toString()
            if (searchInput.equals("")){
                Toast.makeText(applicationContext,"검색어를 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            liveStreamingPostLoad(searchInput)
        }
    }


    fun liveStreamingPostLoad (searchInput : String ){

        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestSearchLiveStreaming(searchInput).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.isSuccessful) {
                    tv_notify.visibility = View.GONE
                    rv_searchLiveRecyclerView.visibility = View.VISIBLE
                    rv_searchReplayRecyclerView.visibility = View.VISIBLE

                    liveStreamingPostList.clear()
                    replayDataList.clear()
                    val jsonObject = JSONObject(response.body().toString())
                    Log.d(TAG, jsonObject.toString())
                    val result = jsonObject.getBoolean("result")
                    val liveStreamingList = jsonObject.getString("liveStreamingList")
                    val replayPostList = jsonObject.getString("replayPostList")

                    if (liveStreamingList.equals("") && replayPostList.equals("")){
                        tv_notify.visibility = View.VISIBLE
                        rv_searchLiveRecyclerView.visibility = View.GONE
                        rv_searchReplayRecyclerView.visibility = View.GONE
                        return
                    }

                    if(result){

                        if (!liveStreamingList.equals("")) {
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
                                val liveData = LiveFragmentData(idx, thumbnail, email, nickName, profile, title, viewer)
                                liveStreamingPostList.add(0, liveData)
                                liveFragmentAdapter.notifyDataSetChanged()
                            }
                        }


                        if (!replayPostList.equals("")) {
                            val replayArray = JSONArray(replayPostList)
                            for (i in 0 until replayArray.length()) {
                                val replayObject = replayArray.getJSONObject(i)
                                val idx = replayObject.getString("idx")
                                val thumbnail = replayObject.getString("thumbnail")
                                val replayTitle = replayObject.getString("replayTitle")
                                val uploadUserEmail = replayObject.getString("uploadUserEmail")
                                val uploadUserProfile = replayObject.getString("uploadUserProfile")
                                val uploadUserNickName = replayObject.getString("uploadUserNickName")
                                val replayLikeNumber = replayObject.getString("replayLikeNumber")
                                val replayHits = replayObject.getString("replayHits")
                                val uploadDate = replayObject.getString("uploadDate")
                                val replayReviewNumber = replayObject.getString("replayReviewNumber")

                                val replayData = ReplayData(idx, uploadUserProfile, uploadUserNickName, thumbnail, replayTitle,
                                    replayReviewNumber, replayHits, replayLikeNumber, uploadDate, uploadUserEmail,true,"1")
                                replayDataList.add(0, replayData)
                                replayFragmentAdapter.notifyDataSetChanged()
                            }
                        }
                    }else{
                        tv_notify.visibility = View.VISIBLE
                        rv_searchLiveRecyclerView.visibility = View.GONE
                        rv_searchReplayRecyclerView.visibility = View.GONE
                        liveStreamingPostList.clear()
                        liveFragmentAdapter.notifyDataSetChanged()
                        replayDataList.clear()
                        replayFragmentAdapter.notifyDataSetChanged()
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