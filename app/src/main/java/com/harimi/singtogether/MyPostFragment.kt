package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.MyBroadcastData
import com.harimi.singtogether.Data.MyPostData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyBroadcastAdapter
import com.harimi.singtogether.adapter.MyPostAdapter
import com.harimi.singtogether.simple.SimpleAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MyPostFragment : Fragment() {
    private var TAG = "MyPostFragment_"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var like :Boolean ?= false
    private var replayPostLikeIdx :String ?= "null"
    private lateinit var tv_noMyPost: TextView

//    private val myPostDataList : ArrayList<MyPostData> = ArrayList()
//    private lateinit var myPostAdapter: MyPostAdapter
    private lateinit var simpleAdapter: SimpleAdapter
    private val homePostList: ArrayList<HomeData> = ArrayList()
    private lateinit var fragment_post_recyclerView: RecyclerView
    private var myEmail : String?=null
    private var isBadge :Boolean ?= false
    private var isBadgeCollabo :Boolean ?= false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            myEmail=it.getString("email")
            Log.e(TAG ,"myEmail:" + myEmail)

            // 서버 연결
//            initRetrofit()
        }
    }

    override fun onResume() {
        super.onResume()
        homePostList.clear()
        simpleAdapter.notifyDataSetChanged()
        loadMyPost()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var myPostView = inflater.inflate(R.layout.fragment_my_post, container, false)
        tv_noMyPost =myPostView.findViewById(R.id.tv_noMyPost)
        fragment_post_recyclerView =myPostView.findViewById(R.id.fragment_post_recyclerView)

        fragment_post_recyclerView.layoutManager= LinearLayoutManager(context)
        fragment_post_recyclerView.setHasFixedSize(true)
        fragment_post_recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        simpleAdapter= SimpleAdapter(homePostList)
        fragment_post_recyclerView.adapter=simpleAdapter


        loadMyPost()
        return myPostView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPostFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    fun loadMyPost(){
//        val userEmail=LoginActivity.user_info.loginUserEmail
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestLoadMyPost(myEmail!!)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)

                        homePostList.clear()
                        val replayObject = JSONObject (body)
                        val badgeList = replayObject.getString("badgeList")
                        val outputData = replayObject.getString("outputData")

                        if (outputData.equals("")) {
                            tv_noMyPost.visibility = View.VISIBLE
                            fragment_post_recyclerView.visibility = View.GONE

                        }else {

                            val jsonArray = JSONArray(outputData)
                            Log.d(TAG, jsonArray.length().toString())
                            tv_noMyPost.visibility = View.GONE
                            fragment_post_recyclerView.visibility = View.VISIBLE
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
                                val col_token = iObject.getString("col_token")
                                val isLike = iObject.getString("isLike")
                                val userLeaveCheck = iObject.getString("userLeaveCheck")
                                val collaborationLeaveCheck = iObject.getString("collaborationLeaveCheck")


                                if (!badgeList.equals("")) {
                                    val badgeArray = JSONArray(badgeList)
                                    for (j in 0 until badgeArray.length()) {
                                        val badgeObject = badgeArray.getJSONObject(j)
                                        val badge_email = badgeObject.getString("email")

                                        if(badge_email.equals(email)) {
                                            isBadge=true
                                            break
                                        }else{
                                            isBadge = false
                                        }

                                    }

                                    for (j in 0 until badgeArray.length()) {
                                        val badgeObject = badgeArray.getJSONObject(j)
                                        val badge_email = badgeObject.getString("email")

                                        if(badge_email.equals(collabo_email)) {
                                            isBadgeCollabo=true
                                            break
                                        }else{
                                            isBadgeCollabo=false
                                        }
                                    }

                                }else{
                                    isBadge=false
                                    isBadgeCollabo=false
                                }

                                val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike,isBadge!!,isBadgeCollabo!!,userLeaveCheck,collaborationLeaveCheck)
                                homePostList.add(homeData)
                                simpleAdapter.notifyDataSetChanged()
                            }
                        }

                    }

                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                }
            })
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}