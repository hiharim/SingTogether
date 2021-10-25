package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.databinding.FragmentSingBinding
import com.harimi.singtogether.sing.DuetFragment
import com.harimi.singtogether.sing.MRFragment
import com.harimi.singtogether.sing.SingPagerAdapter

/**
 노래부르기 프래그먼트
 */
class SingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding= FragmentSingBinding.inflate(inflater,container,false)
        val pagerAdapter = SingPagerAdapter(requireActivity())
        // 2개의 Fragment Add

        pagerAdapter.addFragment(DuetFragment())
        pagerAdapter.addFragment(MRFragment())
        // viewPager 와 pagerAdapter 연결
        binding.fragmentSingViewPager.adapter=pagerAdapter
        binding.fragmentSingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })

        // TabLayout 과 viewPager 연결
        TabLayoutMediator(binding.fragmentSingTabLayout,binding.fragmentSingViewPager){
                tab,position->
            when(position){
                0 ->{
                    tab.text="듀엣"
                }
                1 ->{
                    tab.text="mr"
                }
            }
        }.attach()


        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SingFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}