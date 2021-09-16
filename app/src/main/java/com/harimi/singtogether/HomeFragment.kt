package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.databinding.FragmentDuetBinding
import com.harimi.singtogether.databinding.FragmentHomeBinding
import com.harimi.singtogether.sing.DuetAdapter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class HomeFragment : Fragment() {
    private var TAG :String = "HOME ACTIVITY"

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val homePostList: ArrayList<HomeData> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding= FragmentHomeBinding.inflate(inflater,container,false)
        binding.fragmentHomeRecyclerView.layoutManager=LinearLayoutManager(context)
        binding.fragmentHomeRecyclerView.setHasFixedSize(true)
        homeAdapter= HomeAdapter(homePostList)
        binding.fragmentHomeRecyclerView.adapter= homeAdapter
        homeAdapter.notifyDataSetChanged()


        return binding.root

    }
    override fun onResume() {
        super.onResume()
        Log.d("리플레이: ", "onResume")
        homePostList.clear()
        loadHomePost()
    }

    private fun loadHomePost(){
        retrofitService.requestGetHomePost().enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e(TAG, "loadHomePost 통신 성공: "+response.body().toString())

                    val jsonArray= JSONArray(response.body().toString())
                    for(i in 0..jsonArray.length() -1){
                        val iObject=jsonArray.getJSONObject(i)
                        val idx=iObject.getInt("idx")
                        val thumbnail=iObject.getString("thumbnail")
                        val cnt_play=iObject.getString("cnt_play")
                        val cnt_reply=iObject.getString("cnt_reply")
                        val cnt_like=iObject.getString("cnt_like")
                        val nickname=iObject.getString("nickname")
                        val song_path=iObject.getString("song_path")
                        val date=iObject.getString("date")
                        val collaboration=iObject.getString("collaboration")
                        val title=iObject.getString("title")
                        val singer=iObject.getString("singer")
                        val lyrics=iObject.getString("lyrics")
                        val profile=iObject.getString("profile")

                        //todo : 상대방 프로필받아오는걸로 고치기
                        val collaboration_profile=iObject.getString("profile")

                        val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like, nickname, profile, song_path, collaboration, collaboration_profile, date)
                        homePostList.add(0,homeData)
                        homeAdapter.notifyDataSetChanged()
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
                HomeFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}