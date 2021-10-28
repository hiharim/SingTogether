package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyPagePagerAdapter
import com.harimi.singtogether.adapter.TotalPagerAdapter
import com.harimi.singtogether.databinding.FragmentPostBinding
import com.harimi.singtogether.databinding.FragmentTotalBinding
import retrofit2.Retrofit

/**
 * 인기순,최신순,팔로잉 전체보기로 볼수 있는 화면
 */
class TotalFragment : Fragment() {

    private lateinit var binding: FragmentTotalBinding
    private var TAG :String = "TotalFragment"
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTotalBinding.inflate(inflater,container,false)

        val pagerAdapter = TotalPagerAdapter(requireActivity(),LoginActivity.user_info.loginUserEmail)

        // 3개의 Fragment Add
        pagerAdapter.addFragment(MyPostFragment())
        pagerAdapter.addFragment(MySongFragment())
        pagerAdapter.addFragment(MyBroadcastFragment())

        // viewPager 와 pagerAdapter 연결
        binding.fragmentTotalViewPager.adapter=pagerAdapter
        binding.fragmentTotalViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })

        // TabLayout 과 viewPager 연결
        TabLayoutMediator(binding.fragmentTotalTabLayout,binding.fragmentTotalViewPager){
                tab,position->
            when(position){
                0 ->{
                    tab.text="인기순"
                }
                1 ->{
                    tab.text="최신순"
                }
                2 ->{
                    tab.text="팔로잉"
                }
            }
        }.attach()

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TotalFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}