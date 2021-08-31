package com.harimi.singtogether

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.adapter.MyPagePagerAdapter
import com.harimi.singtogether.databinding.FragmentMyPageBinding
import com.harimi.singtogether.sing.DuetFragment
import com.harimi.singtogether.sing.MRFragment
import com.harimi.singtogether.sing.SingPagerAdapter

/**
 * 마이페이지 화면
 * */
class MyPageFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        val binding=FragmentMyPageBinding.inflate(inflater,container,false)

        val nickname=LoginActivity.user_info.loginUserNickname
        val profile =LoginActivity.user_info.loginUserProfile
        // 2. 바인딩으로 TextView 등에 접근
        // 사용자 닉네임
        binding.fragmentMyPageTvNickname.text=nickname
        // 사용자 프로필
        Glide.with(this).load(profile).into(binding.fragmentMyPageIvProfile)

        val pagerAdapter = MyPagePagerAdapter(requireActivity())
        // 2개의 Fragment Add
        pagerAdapter.addFragment(MySongFragment())
        pagerAdapter.addFragment(MyBroadcastFragment())
        // viewPager 와 pagerAdapter 연결
        binding.fragmentMyPageViewPager.adapter=pagerAdapter
        binding.fragmentMyPageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })

        // TabLayout 과 viewPager 연결
        TabLayoutMediator(binding.fragmentMyPageTabLayout,binding.fragmentMyPageViewPager){
                tab,position->
            when(position){
                0 ->{
                    tab.text="내 노래"
               }
                1 ->{
                    tab.text="내 방송"
                }
            }
        }.attach()

        // 설정 버튼 클릭 -> SettingFragment 로 이동
        binding.fragmentMyPageIvSetting.setOnClickListener{
            val settingFragment = SettingFragment()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.activity_main_frame,settingFragment).addToBackStack(null).commit()
        }

        // 3. 프래그먼트 레이아웃 뷰 반환
        return binding.root
    }

    companion object {

        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}