package com.harimi.singtogether

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.FollowingPostData
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.FollowingPostAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * 팔로잉한 songPost 게시물 보여주는 화면
 */
class FollowingFragment : Fragment() {
    private var TAG :String = "FollowingFragment_"

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val followingPostDataList: ArrayList<FollowingPostData> = ArrayList()
    private lateinit var followingPostAdapter: FollowingPostAdapter
    private lateinit var fragment_following_recyclerView:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 서버 연결
            initRetrofit()
        }
    }

    override fun onResume() {
        super.onResume()
        followingPostDataList.clear()
        followingPostAdapter.notifyDataSetChanged()
        loadMyFollowingPost()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var FollowingView = inflater.inflate(R.layout.fragment_following, container, false)
        initView(FollowingView)
        return FollowingView
    }

    private fun initView(FollowingView : View){
        fragment_following_recyclerView =FollowingView.findViewById(R.id.fragment_following_recyclerView)
        fragment_following_recyclerView.layoutManager = LinearLayoutManager(activity)
        fragment_following_recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        followingPostAdapter = FollowingPostAdapter(followingPostDataList,requireContext())
        fragment_following_recyclerView.adapter = followingPostAdapter
        loadMyFollowingPost()

    }


    private fun loadMyFollowingPost(){

        retrofit = RetrofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
        retrofitService.requestLoadMyFollowingPost(LoginActivity.user_info.loginUserEmail).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e(TAG, " 통신 성공: "+response.body().toString())


                        val jsonObject = JSONObject(response.body().toString())
                        val outputData=jsonObject.getString("outputData")

                        val outputDataArray= JSONArray(outputData)
                        for (i in 0 until outputDataArray.length()) {
                            val outputDataObject=outputDataArray.getJSONObject(i)
                            val idx=outputDataObject.getInt("idx")
                            val thumbnail=outputDataObject.getString("thumbnail")
                            val cnt_play=outputDataObject.getString("cnt_play")
                            val cnt_reply=outputDataObject.getString("cnt_reply")
                            val cnt_like=outputDataObject.getString("cnt_like")
                            val nickname=outputDataObject.getString("nickname")
                            val email=outputDataObject.getString("email")
                            val song_path=outputDataObject.getString("song_path")
                            val date=outputDataObject.getString("date")
                            val collaboration=outputDataObject.getString("collaboration")
                            val mr_idx=outputDataObject.getInt("mr_idx")
                            val title=outputDataObject.getString("title")
                            val singer=outputDataObject.getString("singer")
                            val lyrics=outputDataObject.getString("lyrics")
                            val profile=outputDataObject.getString("profile")
                            val collaboration_profile=outputDataObject.getString("col_profile")
                            val collabo_email=outputDataObject.getString("collabo_email")
                            val kinds=outputDataObject.getString("kinds")
                            val token=outputDataObject.getString("token")
                            val col_token=outputDataObject.getString("col_token")


                            val followingPostData = FollowingPostData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token)
                            followingPostDataList.add(0,followingPostData)
                            followingPostAdapter.notifyDataSetChanged()
                        }

                } else {
                    // 통신은 성공했지만 응답에 문제가 있는 경우
                    Log.e("DuetFragment", "loadDuet 응답 문제" + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("DuetFragment", "loadDuet 통신 실패" + t.message)
            }


        })
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FollowingFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}