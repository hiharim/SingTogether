package com.harimi.singtogether.sing

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDuetBinding
import com.harimi.singtogether.databinding.FragmentResultDuetBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
    검색결과 듀엣 탭 화면
 */
class ResultDuetFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val duetList : ArrayList<DuetData> = ArrayList()
    private lateinit var duetAdapter: DuetAdapter
    private var searchInput : String?=null // 검색어
    private var isBadge :Boolean ?= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchInput=it.getString("searchInput")
            Log.e("ResultDuetFragment" ,"검색어:" + searchInput)
        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding=FragmentResultDuetBinding.inflate(inflater, container, false)

        binding.fragmentResultDuetRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentResultDuetRecyclerView.setHasFixedSize(true)
        duetAdapter= DuetAdapter(duetList)
        binding.fragmentResultDuetRecyclerView.adapter=duetAdapter

        duetAdapter.notifyDataSetChanged()
        loadDuet()
        return binding.root
    }

    private fun loadDuet() {
        val which="duet"
        val userEmail= LoginActivity.user_info.loginUserEmail
        searchInput?.let {
            retrofitService.loadSearchResult(it,which,userEmail).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        Log.e("DuetFragment", "loadDuet 통신 성공: ${response.body().toString()}")
                        val body = response.body().toString()
                        val replayObject = JSONObject(body)
                        val badgeList = replayObject.getString("badgeList")
                        val postList = replayObject.getString("duetList")
                        val postArray = JSONArray(postList)

                        for (i in 0 until postArray.length()) {
                            if (postArray.length() == 0 || postArray.equals("null")) {


                            } else {
                                val iObject = postArray.getJSONObject(i)
                                val duet_idx = iObject.getInt("duet_idx")
                                val mr_idx = iObject.getInt("mr_idx")
                                val thumbnail = iObject.getString("thumbnail")
                                val title = iObject.getString("title")
                                val singer = iObject.getString("singer")
                                val cnt_play = iObject.getString("cnt_play")
                                val cnt_reply = iObject.getString("cnt_reply")
                                val email = iObject.getString("email")
                                val nickname = iObject.getString("nickname")
                                val profile = iObject.getString("profile")
                                val cnt_duet = iObject.getString("cnt_duet")
                                val lyrics = iObject.getString("lyrics")
                                val song_path = iObject.getString("duet_path")
                                val mr_path = iObject.getString("song_path")
                                val extract_path = iObject.getString("extract_path")
                                val duet_date = iObject.getString("date")
                                val kinds = iObject.getString("kinds")
                                val token = iObject.getString("token")
                                var path = song_path

                                if (!badgeList.equals("")) {
                                    val badgeArray = JSONArray(badgeList)
                                    for (i in 0 until badgeArray.length()) {
                                        var badgeObject = badgeArray.getJSONObject(i)
                                        var badge_email = badgeObject.getString("email")
                                        if (badge_email.equals(email)) {
                                            isBadge = true
                                            break
                                        } else {
                                            isBadge = false
                                        }
                                    }
                                } else {
                                    isBadge = false
                                }

                                val duetData = DuetData(duet_idx, mr_idx, thumbnail, title, singer, cnt_play, cnt_reply, cnt_duet, email, nickname, profile, path, duet_date, mr_path, extract_path, kinds, lyrics, token, isBadge!!)
                                duetList.add(0,duetData)
                                duetAdapter.notifyDataSetChanged()

                            }

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
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

    companion object {


        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultDuetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}