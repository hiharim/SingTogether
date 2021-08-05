package com.harimi.singtogether

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.broadcast.BroadcastPagerAdapter
import com.harimi.singtogether.broadcast.LiveFragment
import com.harimi.singtogether.broadcast.LiveStartActivity
import com.harimi.singtogether.broadcast.ReplayFragment
import com.harimi.singtogether.databinding.FragmentBroadcastBinding

/**
 방송 프래그먼트
 */
class BraodcastFragment : Fragment() {

    // 초기화 리소스들이 들어가는곳
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    // Layout 을 inflate 하는곳, view 객체를 얻어서 초기화
   override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding=FragmentBroadcastBinding.inflate(inflater,container,false)
        val pagerAdapter =BroadcastPagerAdapter(requireActivity())
        // 2개의 Fragment Add
        pagerAdapter.addFragment(LiveFragment())
        pagerAdapter.addFragment(ReplayFragment())
        // viewPager 와 pagerAdapter 연결
        binding.fragmentBroadcastViewPager.adapter=pagerAdapter
        binding.fragmentBroadcastViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })

        // TabLayout 과 viewPager 연결
        TabLayoutMediator(binding.fragmentBroadcastTabLayout,binding.fragmentBroadcastViewPager){
            tab,position->
            when(position){
                0 ->{
                    tab.text="실시간"
                }
                1 ->{
                    tab.text="다시보기"
                }
            }
        }.attach()

        // 비디오버튼 클릭
        binding.fragmentMyPageIvLive.setOnClickListener{
            val intent= Intent(context,LiveStartActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BraodcastFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}