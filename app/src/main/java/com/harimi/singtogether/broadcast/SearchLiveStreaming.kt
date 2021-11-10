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
import com.harimi.singtogether.Data.SearchData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import com.harimi.singtogether.adapter.LocalChattingAdapter
import com.harimi.singtogether.adapter.ReplayFragmentAdapter
import com.harimi.singtogether.adapter.SearchDataAdapter
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
    private lateinit var tv_alert : TextView


    private lateinit var iv_finishActivity : ImageView
    private lateinit var iv_search : ImageButton
    private var like :Boolean ?= false
    private var isBadge :Boolean ?= false
    private var replayPostLikeIdx :String ?= "null"

    private lateinit var rv_searchLiveAndReplayRecyclerView : RecyclerView
    lateinit var searchDataAdapter: SearchDataAdapter
    private val searchDataList: ArrayList<SearchData> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_live_streaming)

        iv_search = findViewById(R.id.iv_search)
        tv_notify = findViewById(R.id.tv_notify)
        iv_finishActivity = findViewById(R.id.iv_finishActivity)
        et_searchLiveStreaming = findViewById(R.id.et_searchLiveStreaming)
        tv_alert = findViewById(R.id.tv_alert)

        rv_searchLiveAndReplayRecyclerView =findViewById(R.id.rv_searchLiveAndReplayRecyclerView)
        rv_searchLiveAndReplayRecyclerView.layoutManager = LinearLayoutManager(this)
        rv_searchLiveAndReplayRecyclerView.addItemDecoration(
            DividerItemDecoration( this,
                DividerItemDecoration.VERTICAL)
        )
        searchDataAdapter = SearchDataAdapter(searchDataList,this)
        rv_searchLiveAndReplayRecyclerView.adapter = searchDataAdapter



        rv_searchLiveAndReplayRecyclerView.visibility =View.GONE

        ////끝내기
        iv_finishActivity.setOnClickListener {
            finish()
        }




        //서치하기
        iv_search.setOnClickListener {
            tv_alert.visibility =View.GONE
            var searchInput = et_searchLiveStreaming.text.toString()
            if (searchInput.equals("")){
                Toast.makeText(applicationContext,"검색어를 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            searchDataList.clear()
            searchDataAdapter.notifyDataSetChanged()
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
                    rv_searchLiveAndReplayRecyclerView.visibility = View.VISIBLE
                    searchDataList.clear()

                    val jsonObject = JSONObject(response.body().toString())
                    Log.d(TAG, jsonObject.toString())
                    val result = jsonObject.getBoolean("result")
                    val liveStreamingList = jsonObject.getString("liveStreamingList")
                    val replayPostList = jsonObject.getString("replayPostList")
                    val userLikeList = jsonObject.getString("userLikeList")
                    val badgeList = jsonObject.getString("badgeList")
                    if (liveStreamingList.equals("") && replayPostList.equals("")) {
                        tv_notify.visibility = View.VISIBLE
                        rv_searchLiveAndReplayRecyclerView.visibility = View.GONE

                        return
                    }

                    if (result) {

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




                                if (!badgeList.equals("")) {
                                    val badgeArray = JSONArray(badgeList)
                                    for (j in 0 until badgeArray.length()) {
                                        var badgeObject = badgeArray.getJSONObject(j)
                                        var badgeEmail = badgeObject.getString("email")
                                        if (badgeEmail.equals(email)) {
                                            isBadge = true
                                            Log.d(TAG, isBadge.toString())
                                            break
                                        } else {
                                            isBadge = false
                                            Log.d(TAG, isBadge.toString())
                                        }
                                    }
                                } else {
                                    isBadge = false
                                }

                                val searchData = SearchData(
                                    "0",
                                    idx,
                                    email,
                                    thumbnail,
                                    nickName,
                                    profile,
                                    title,
                                    viewer,
                                    isBadge!!
                                )
                                searchDataList.add( searchData)
                                searchDataAdapter.notifyDataSetChanged()


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
                                val uploadUserNickName =
                                    replayObject.getString("uploadUserNickName")
                                val replayLikeNumber = replayObject.getString("replayLikeNumber")
                                val replayHits = replayObject.getString("replayHits")
                                val uploadDate = replayObject.getString("uploadDate")
                                val replayVideo = replayObject.getString("replayVideo")
                                val replayReviewNumber =
                                    replayObject.getString("replayReviewNumber")
                                val time = replayObject.getString("time")
                                val uploadUserFCMToken =
                                    replayObject.getString("uploadUserFCMToken")

                                if (!badgeList.equals("")) {
                                    val badgeArray = JSONArray(badgeList)
                                    for (j in 0 until badgeArray.length()) {
                                        var badgeObject = badgeArray.getJSONObject(j)
                                        var email = badgeObject.getString("email")
                                        if (email.equals(uploadUserEmail)) {
                                            isBadge = true
                                            Log.d(TAG, isBadge.toString())
                                            break
                                        } else {
                                            isBadge = false
                                            Log.d(TAG, isBadge.toString())
                                        }
                                    }
                                } else {
                                    isBadge = false
                                }

                                if (!userLikeList.equals("")) {
                                    val likeArray = JSONArray(userLikeList)
                                    for (l in 0 until likeArray.length()) {
                                        var likeObject = likeArray.getJSONObject(l)
                                        var replayPostIdx = likeObject.getString("replayPostIdx")
                                        replayPostLikeIdx =
                                            likeObject.getString("replayPostLikeIdx")
                                        if (replayPostIdx.equals(idx)) {
                                            like = true
                                            Log.d(TAG, like.toString())
                                            break
                                        } else {
                                            like = false
                                            Log.d(TAG, like.toString())
                                        }

                                    }
                                }

                                    val searchData = SearchData(
                                        "1",
                                        idx,
                                        thumbnail,
                                        replayTitle,
                                        uploadUserEmail,
                                        uploadUserProfile,
                                        uploadUserNickName,
                                        replayLikeNumber,
                                        replayHits,
                                        uploadDate,
                                        replayVideo,
                                        replayReviewNumber!!,
                                        time!!,
                                        uploadUserFCMToken,
                                        like!!,
                                        isBadge!!,
                                        replayPostLikeIdx!!
                                    )
                                    Log.d(TAG, jsonObject.toString())
                                    searchDataList.add( searchData)
                                    searchDataAdapter.notifyDataSetChanged()
                                }
                            }
                        } else {
                            tv_notify.visibility = View.VISIBLE
                            rv_searchLiveAndReplayRecyclerView.visibility = View.GONE

                            searchDataList.clear()
                            searchDataAdapter.notifyDataSetChanged()
                            Toast.makeText(
                                applicationContext,
                                "검색에 해당하는 게시물이 없습니다",
                                Toast.LENGTH_SHORT
                            ).show()
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