package com.harimi.singtogether

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.PopData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.adapter.PopAdapter
import com.harimi.singtogether.databinding.FragmentPopBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
    인기순으로 songPost 게시물 보여주는 화면
 */
class PopFragment : Fragment() {

    private var TAG :String = "POP_FRAGMENT"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var binding: FragmentPopBinding
    private val popPostList: ArrayList<PopData> = ArrayList()
    private lateinit var popAdapter: PopAdapter
    private var isBadge :Boolean ?= false
    private var isBadgeCollabo :Boolean ?= false
    private lateinit var swipeRefresh: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPopBinding.inflate(inflater,container,false)
        binding.popLayout.setBackgroundColor(Color.parseColor("#f4f5f9"))
        // 서버 연결
        initRetrofit()
        swipeRefresh =binding.swipeRefresh.findViewById(R.id.swipeRefresh)
        binding.swipeRefresh.setOnRefreshListener {
            popPostList.clear()
            popAdapter.notifyDataSetChanged()
            loadHomePost()
            swipeRefresh?.isRefreshing = false  //서버 통신 완료 후 호출해줍니다.
        }
        //리사이클러뷰 설정
        binding.fragmentPopRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentPopRecyclerView.setHasFixedSize(true)
        binding.fragmentPopRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        popAdapter= PopAdapter(popPostList)
        binding.fragmentPopRecyclerView.adapter= popAdapter
        popAdapter.notifyDataSetChanged()

        return binding.root
    }

    private fun loadHomePost() {
        val userEmail=LoginActivity.user_info.loginUserEmail
        Log.e(TAG, "userEmail:$userEmail")
        retrofitService.requestPopPost(userEmail).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    val body = response.body().toString()
                    val replayObject = JSONObject(body)
                    val badgeList = replayObject.getString("badgeList")
                    val homeList = replayObject.getString("homeList")
                    val postArray = JSONArray(homeList)

                    for (i in 0 until postArray.length()) {
                        if (postArray.length() == 0 || postArray.equals("null")) {
                        } else {

                            val iObject = postArray.getJSONObject(i)
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
                            var profile = iObject.getString("profile")
                            var collaboration_profile = iObject.getString("col_profile")
                            val collabo_email = iObject.getString("collabo_email")
                            val kinds = iObject.getString("kinds")
                            val token = iObject.getString("token")
                            val col_token = iObject.getString("col_token")
                            val isLike = iObject.getString("isLike")
                            val rank = iObject.getInt("ranking")
                            val userLeaveCheck = iObject.getString("userLeaveCheck")
                            val collaborationLeaveCheck = iObject.getString("collaborationLeaveCheck")
                            // 탈퇴한 회원 프로필
//                            if(userLeaveCheck.equals("1")){
//                                profile="uploadFile/profile22.png"
//                            }else if(collaborationLeaveCheck.equals("1")){
//                                collaboration_profile="uploadFile/profile22.png"
//                            }

                            if (!badgeList.equals("")) {
                                val badgeArray = JSONArray(badgeList)
                                for (j in 0 until badgeArray.length()) {
                                    val badgeObject = badgeArray.getJSONObject(j)
                                    val badge_email = badgeObject.getString("email")

                                    if (badge_email.equals(email)) {
                                        isBadge = true
                                        break
                                    } else {
                                        isBadge = false
                                    }

                                }

                                for (j in 0 until badgeArray.length()) {
                                    val badgeObject = badgeArray.getJSONObject(j)
                                    val badge_email = badgeObject.getString("email")

                                    if (badge_email.equals(collabo_email)) {
                                        isBadgeCollabo = true
                                        break
                                    } else {
                                        isBadgeCollabo = false
                                    }
                                }

                            } else {
                                isBadge = false
                                isBadgeCollabo = false
                            }

                            val popData=PopData(idx,thumbnail,title,singer,lyrics,cnt_play,cnt_reply,cnt_like,nickname,email,profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,rank,col_token,isLike,isBadge!!,isBadgeCollabo!!,userLeaveCheck,collaborationLeaveCheck)
                            popPostList.add(0,popData)
                            popAdapter.notifyDataSetChanged()

                        }
                    }

//                    val jsonArray= JSONArray(response.body().toString())
//                    for(i in 0..jsonArray.length() -1){
//                        val iObject=jsonArray.getJSONObject(i)
//                        val idx=iObject.getInt("idx")
//                        val thumbnail=iObject.getString("thumbnail")
//                        val cnt_play=iObject.getString("cnt_play")
//                        val cnt_reply=iObject.getString("cnt_reply")
//                        val cnt_like=iObject.getString("cnt_like")
//                        val nickname=iObject.getString("nickname")
//                        val email=iObject.getString("email")
//                        val song_path=iObject.getString("song_path")
//                        val date=iObject.getString("date")
//                        val collaboration=iObject.getString("collaboration")
//                        val mr_idx=iObject.getInt("mr_idx")
//                        val title=iObject.getString("title")
//                        val singer=iObject.getString("singer")
//                        val lyrics=iObject.getString("lyrics")
//                        val profile=iObject.getString("profile")
//                        val collaboration_profile=iObject.getString("col_profile")
//                        val collabo_email=iObject.getString("collabo_email")
//                        val kinds=iObject.getString("kinds")
//                        val token=iObject.getString("token")
//                        val col_token=iObject.getString("col_token")
//                        val isLike=iObject.getString("isLike")
//                        val rank=iObject.getInt("ranking")
//                        Log.e(TAG, " "+rank.toString())
//                        val popData=PopData(idx,thumbnail,title,singer,lyrics,cnt_play,cnt_reply,cnt_like,nickname,email,profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,rank,col_token,isLike)
//                        popPostList.add(0,popData)
//                        popAdapter.notifyDataSetChanged()
//
//                    }

                } else {
                    // 통신은 성공했지만 응답에 문제가 있는 경우
                    Log.e(TAG, "loadDuet 응답 문제" + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "loadDuet 통신 실패" + t.message)
            }


        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        popPostList.clear()
        popAdapter.notifyDataSetChanged()
        loadHomePost()
    }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PopFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}