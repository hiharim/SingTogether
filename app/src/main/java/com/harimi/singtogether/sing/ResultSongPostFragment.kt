package com.harimi.singtogether.sing

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
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.databinding.FragmentResultSongPostBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
    검색결과 포스팅 탭 화면
 */
class ResultSongPostFragment : Fragment() {
    private var TAG :String = "검색결과 포스팅 탭 화면"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var binding:FragmentResultSongPostBinding
    private val homePostList: ArrayList<HomeData> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter
    private var searchInput : String?=null // 검색어
    private var isBadge :Boolean ?= false
    private var isBadgeCollabo :Boolean ?= false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchInput=it.getString("searchInput")
            Log.e("ResultSongPostFragment" ,"검색어:" + searchInput)
        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentResultSongPostBinding.inflate(inflater, container, false)

        binding.fragmentResultSongPostRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentResultSongPostRecyclerView.setHasFixedSize(true)
        binding.fragmentResultSongPostRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        homeAdapter= HomeAdapter(homePostList)
        binding.fragmentResultSongPostRecyclerView.adapter= homeAdapter
        homeAdapter.notifyDataSetChanged()
        //loadHomePost()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        homePostList.clear()
        loadHomePost()
    }

    private fun loadHomePost(){
        val which="song"
        val userEmail= LoginActivity.user_info.loginUserEmail
        searchInput?.let {
            retrofitService.loadSearchResult(it,which,userEmail).enqueue(object : Callback<String> {
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
                            if (postArray.length() ==0 || postArray.equals("")){
                                binding.tvBlank.visibility=View.VISIBLE
                                binding.fragmentResultSongPostRecyclerView.visibility=View.GONE
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
                                    for (i in 0 until badgeArray.length()) {
                                        var badgeObject = badgeArray.getJSONObject(i)
                                        var badge_email = badgeObject.getString("email")
                                        if (badge_email.equals(email)) {
                                            isBadge = true
                                            Log.d(TAG, isBadge.toString())
                                            // break
                                        } else if(badge_email.equals(collabo_email)){
                                            isBadgeCollabo = true
                                            break
                                        }else{
                                            isBadge= false
                                            Log.d(TAG, isBadge.toString())
                                        }
                                    }
                                }


                                val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike,isBadge!!,isBadgeCollabo!!)
                                homePostList.add(0,homeData)
                                homeAdapter.notifyDataSetChanged()


                            }
                        }







//                        val jsonArray = JSONArray(response.body().toString())
//                        if (jsonArray.length() == 0 || jsonArray.equals("")) {
//
//                        } else {
//
//                            for (i in 0..jsonArray.length() - 1) {
//                                val iObject=jsonArray.getJSONObject(i)
//                                val idx=iObject.getInt("idx")
//                                val thumbnail=iObject.getString("thumbnail")
//                                val cnt_play=iObject.getString("cnt_play")
//                                val cnt_reply=iObject.getString("cnt_reply")
//                                val cnt_like=iObject.getString("cnt_like")
//                                val nickname=iObject.getString("nickname")
//                                val email=iObject.getString("email")
//                                val song_path=iObject.getString("song_path")
//                                val date=iObject.getString("date")
//                                val collaboration=iObject.getString("collaboration")
//                                val mr_idx=iObject.getInt("mr_idx")
//                                val title=iObject.getString("title")
//                                val singer=iObject.getString("singer")
//                                val lyrics=iObject.getString("lyrics")
//                                val profile=iObject.getString("profile")
//                                val collaboration_profile=iObject.getString("col_profile")
//                                val collabo_email=iObject.getString("collabo_email")
//                                val kinds=iObject.getString("kinds")
//                                val token=iObject.getString("token")
//                                val col_token=iObject.getString("col_token")
//                                val isLike=iObject.getString("isLike")
//
//                                val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token,col_token,isLike)
//                                homePostList.add(0,homeData)
//                                homeAdapter.notifyDataSetChanged()
//                            }
//                        }

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
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultSongPostFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}