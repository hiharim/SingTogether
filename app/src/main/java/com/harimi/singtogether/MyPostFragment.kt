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
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MyPostFragment : Fragment() {
    private var TAG = "MyPostFragment_"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var like :Boolean ?= false
    private var replayPostLikeIdx :String ?= "null"
    private lateinit var tv_noMyPost: TextView

    private val myPostDataList : ArrayList<MyPostData> = ArrayList()
    private lateinit var myPostAdapter: MyPostAdapter
    private lateinit var fragment_post_recyclerView: RecyclerView
    private var myEmail : String?=null


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
        myPostDataList.clear()
        myPostAdapter.notifyDataSetChanged()
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
        myPostAdapter= MyPostAdapter(myPostDataList,requireContext())
        fragment_post_recyclerView.adapter=myPostAdapter


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
        val userEmail=LoginActivity.user_info.loginUserEmail
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestLoadMyPost(userEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        myPostDataList.clear()

                        Log.d(TAG, body)
                        if (body.equals("null")) {
                            tv_noMyPost.visibility = View.VISIBLE
                            fragment_post_recyclerView.visibility = View.GONE

                        }else {
                            val jsonArray = JSONArray(body)
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
                                val myPostData = MyPostData(
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
                                    token,
                                    col_token,
                                    isLike
                                )
                                myPostDataList.add(0, myPostData)
                                myPostAdapter.notifyDataSetChanged()
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