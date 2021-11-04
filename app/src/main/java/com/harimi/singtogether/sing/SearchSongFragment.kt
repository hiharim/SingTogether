package com.harimi.singtogether.sing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivitySearchSongBinding
import com.harimi.singtogether.databinding.FragmentNewBinding
import com.harimi.singtogether.databinding.FragmentSearchSongBinding
import retrofit2.Retrofit


class SearchSongFragment : Fragment() {


    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private var searchInput : String="" // 검색어
    private lateinit var binding: FragmentSearchSongBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentSearchSongBinding.inflate(inflater,container,false)

        //검색
        binding.ivSearch.setOnClickListener {
            searchInput = binding.etSearchSong.text.toString()
            Log.e("조건문 전", "searchInput"+searchInput)
            if (searchInput.equals("")){
                Toast.makeText(context,"검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadSearchResult(searchInput)
        }

        // 뒤로가기버튼 클릭
        binding.ivBtnBefore.setOnClickListener {
           // finish()
        }

        return binding.root
    }

    fun loadSearchResult(searchInput : String) {
        Log.e("SearchSongActivity" ,"loadSearchResult 검색어:" + searchInput)
        Log.e("SearchSongActivity", "searchInput"+searchInput)
        binding.activitySearchSongTabLayout.visibility=View.VISIBLE
        binding.activitySearchSongViewPager.visibility=View.VISIBLE

        val pagerAdapter = SearchPagerAdapter(requireActivity(), searchInput)
        // 3개의 Fragment Add
        pagerAdapter.addFragment(ResultMRFragment())
        pagerAdapter.addFragment(ResultDuetFragment())
        pagerAdapter.addFragment(ResultSongPostFragment())
        // viewPager 와 pagerAdapter 연결
        binding.activitySearchSongViewPager.adapter=pagerAdapter
        binding.activitySearchSongViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("SearchSongActivity", "Page ${position+1}")
            }
        })

        // TabLayout 과 viewPager 연결
        TabLayoutMediator(binding.activitySearchSongTabLayout,binding.activitySearchSongViewPager){
                tab,position->
            when(position){
                0 ->{
                    tab.text="MR"
                }
                1 ->{
                    tab.text="듀엣"
                }
                2 ->{
                    tab.text="포스팅"
                }
            }
        }.attach()

    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchSongFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}