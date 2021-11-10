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
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.databinding.FragmentHomeBinding
import com.harimi.singtogether.databinding.FragmentNewBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
    최신순으로 songPost 게시물 보여주는 화면
 */
class NewFragment : Fragment() {
    private var TAG :String = "NEW FRAGMENT"
    private var myEmail : String?=null
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var binding: FragmentNewBinding
    private val homePostList: ArrayList<HomeData> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter
    private var isBadge :Boolean ?= false
    private var isBadgeCollabo :Boolean ?= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 서버 연결
            initRetrofit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNewBinding.inflate(inflater,container,false)
        binding.newLayout.setBackgroundColor(Color.parseColor("#f4f5f9"))
        // 서버 연결
        initRetrofit()

        //리사이클러뷰 설정
        binding.fragmentNewRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentNewRecyclerView.setHasFixedSize(true)
        binding.fragmentNewRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        homeAdapter= HomeAdapter(homePostList)
        binding.fragmentNewRecyclerView.adapter= homeAdapter
        homeAdapter.notifyDataSetChanged()

        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        homePostList.clear()
        homeAdapter.notifyDataSetChanged()
        loadHomePost()
    }

    private fun loadHomePost() {
        val userEmail=LoginActivity.user_info.loginUserEmail
        retrofitService.requestNewPost(userEmail).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e(TAG, "loadHomePost 통신 성공: "+response.body().toString())
                    val body = response.body().toString()
                    val replayObject = JSONObject (body)
                    val badgeList = replayObject.getString("badgeList")
                    val homeList = replayObject.getString("homeList")
                    val postArray = JSONArray(homeList)

                    for (i in 0 until postArray.length()) {
                        if (postArray.length() ==0 || postArray.equals("null")){
                        }else {
                            val iObject=postArray.getJSONObject(i)
                            val idx=iObject.getInt("idx")
                            val thumbnail=iObject.getString("thumbnail")
                            val cnt_play=iObject.getString("cnt_play")
                            val cnt_reply=iObject.getString("cnt_reply")
                            val cnt_like=iObject.getString("cnt_like")
                            val nickname=iObject.getString("nickname")
                            val email=iObject.getString("email")
                            val song_path=iObject.getString("song_path")
                            val date=iObject.getString("date")
                            val collaboration=iObject.getString("collaboration")
                            val mr_idx=iObject.getInt("mr_idx")
                            val title=iObject.getString("title")
                            val singer=iObject.getString("singer")
                            val lyrics=iObject.getString("lyrics")
                            val profile=iObject.getString("profile")
                            val collaboration_profile=iObject.getString("col_profile")
                            val collabo_email=iObject.getString("collabo_email")
                            val kinds=iObject.getString("kinds")
                            val token=iObject.getString("token")
                            val col_token=iObject.getString("col_token")
                            val isLike=iObject.getString("isLike")

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
                            Log.e(TAG,"i : $i")
                            Log.e(TAG,"isBadge : $isBadge")
                            Log.e(TAG,"isBageCollabo : $isBadgeCollabo")

                            val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike,isBadge!!,isBadgeCollabo!!)
                            homePostList.add(0,homeData)
                            homeAdapter.notifyDataSetChanged()
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
//                        val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike)
//                        homePostList.add(0,homeData)
//                        homeAdapter.notifyDataSetChanged()
//                    }

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
            NewFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}