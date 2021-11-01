package com.harimi.singtogether

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.harimi.singtogether.Data.BestData
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.BestAdapter
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.adapter.TotalPagerAdapter
import com.harimi.singtogether.databinding.FragmentHomeBinding
import com.harimi.singtogether.databinding.FragmentPostBinding
import com.harimi.singtogether.simple.SimpleFollowingFragment
import com.harimi.singtogether.simple.SimpleNewFragment
import com.harimi.singtogether.simple.SimplePagerAdapter
import com.harimi.singtogether.simple.SimplePopFragment
import com.harimi.singtogether.sing.SearchSongActivity
import com.harimi.singtogether.sing.SingPagerAdapter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.LocalDate

class HomeFragment : Fragment() {
    private var TAG :String = "HOME FRAGMENT"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val homePostList: ArrayList<HomeData> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter
    private val bestList: ArrayList<BestData> = ArrayList()
    private lateinit var bestAdapter: BestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding= FragmentHomeBinding.inflate(inflater,container,false)
        binding.homeLayout.setBackgroundColor(Color.parseColor("#f4f5f9"))

        // 오늘의 가왕 리사이클러뷰 설정
        binding.fragmentHomeRvBest.layoutManager=LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        binding.fragmentHomeRvBest.setHasFixedSize(true)
        bestAdapter= BestAdapter(bestList)
        binding.fragmentHomeRvBest.adapter=bestAdapter
        bestAdapter.notifyDataSetChanged()


        val pagerAdapter = SimplePagerAdapter(requireActivity())
        binding.homeViewPager.adapter=pagerAdapter
        pagerAdapter.addFragment(SimplePopFragment())
        pagerAdapter.addFragment(SimpleNewFragment())
        pagerAdapter.addFragment(SimpleFollowingFragment())

        // viewPager 와 pagerAdapter 연결
        binding.homeViewPager.adapter=pagerAdapter
        binding.homeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
                if(position==0){
                    binding.fragmentHomeTvTabName.text="인기순"
                }else if(position==1){
                    binding.fragmentHomeTvTabName.text="최신순"
                }else{
                    binding.fragmentHomeTvTabName.text="팔로잉"
                }
            }
        })
        binding.indicator.setViewPager(binding.homeViewPager)


        // 검색버튼 클릭
        binding.fragmentHomeIvSearch.setOnClickListener {
            val intent= Intent(context, SearchSongActivity::class.java)
            startActivity(intent)
        }

        // 전체보기 클릭
        binding.fragmentHomeTvTotal.setOnClickListener {
            val totalFragment = TotalFragment()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.activity_main_frame,totalFragment).
                addToBackStack(null)
                .commit()
        }

        // 명예의전당 클릭
        binding.fragmentHomeIvBest.setOnClickListener {

        }

        return binding.root

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.d("리플레이: ", "onResume")
        //homePostList.clear()
        bestList.clear()
        //loadHomePost()
        loadBestSinger()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadBestSinger() {
        val current: LocalDate = LocalDate.now()
        val today_date=current.toString() // 오늘 날짜
        retrofitService.requestBestSinger(today_date).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e(TAG, " loadBestSinger() 통신 성공: "+response.body().toString())

                    val jsonArray= JSONArray(response.body().toString())
                    for(i in 0..jsonArray.length() -1){
                        val iObject=jsonArray.getJSONObject(i)
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
                        val profile=iObject.getString("circle_profile")
                        val collaboration_profile=iObject.getString("col_profile")
                        val collabo_email=iObject.getString("collabo_email")
                        val kinds=iObject.getString("kinds")
                        val token=iObject.getString("token")
                        val bestData = BestData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token)
                        bestList.add(0,bestData)
                        bestAdapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e(TAG, "loadBestSinger() 응답 문제" + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "loadBestSinger() 통신 실패" + t.message)
            }


        })
    }


    private fun loadHomePost() {
        retrofitService.requestGetHomePost2().enqueue(object : Callback<String> {
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
                        val homeData = HomeData(idx,thumbnail, title, singer,lyrics, cnt_play, cnt_reply, cnt_like,nickname,email, profile, song_path, collaboration,collabo_email, collaboration_profile, date,kinds,mr_idx,token)
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