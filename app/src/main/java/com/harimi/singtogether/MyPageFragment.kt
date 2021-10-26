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
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyPagePagerAdapter
import com.harimi.singtogether.databinding.FragmentMyPageBinding
import com.harimi.singtogether.sing.DuetFragment
import com.harimi.singtogether.sing.MRFragment
import com.harimi.singtogether.sing.SingPagerAdapter
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * 마이페이지 화면
 * */
class MyPageFragment : Fragment() {
    private var TAG :String = "MyPageFragment"
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

        // 1. 뷰 바인딩 설정
        val binding=FragmentMyPageBinding.inflate(inflater,container,false)
        val nickname=LoginActivity.user_info.loginUserNickname
        val profile =LoginActivity.user_info.loginUserProfile

        // 2. 바인딩으로 TextView 등에 접근
        // 사용자 닉네임
        binding.fragmentMyPageTvNickname.text=nickname

        // 사용자 프로필
        if (profile.equals("null")){

        }else{
            Glide.with(this).load("http://3.35.236.251/"+profile).into(binding.fragmentMyPageIvProfile)
        }

        val pagerAdapter = MyPagePagerAdapter(requireActivity(),LoginActivity.user_info.loginUserEmail)


        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestLookAtMyFollow(
            LoginActivity.user_info.loginUserEmail
        )
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        var jsonObject = JSONObject(response.body().toString())
                        var result = jsonObject.getBoolean("result")
                        if (result) {
                            var followingUserNumber = jsonObject.getString("followingUserNumber")
                            var followUserNumber = jsonObject.getString("followUserNumber")

                            binding.tvMyFollow.setText(followUserNumber)
                            binding.tvMyFollowing.setText(followingUserNumber)
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                }
            })


        binding.tvMyFollowing.setOnClickListener {
            val intent= Intent(context, MyFollowingActivity::class.java)
            intent.putExtra("myEmail",LoginActivity.user_info.loginUserEmail)
            startActivity(intent)
        }

        binding.tvMyFollow.setOnClickListener {
            val intent= Intent(context, MyFollowerActivity::class.java)
            intent.putExtra("myEmail",LoginActivity.user_info.loginUserEmail)
            startActivity(intent)
        }

        // 3개의 Fragment Add
        pagerAdapter.addFragment(MyPostFragment())
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
                    tab.text="포스팅"
               }
                1 ->{
                    tab.text="듀엣초대"
                }
                2 ->{
                    tab.text="방송"
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