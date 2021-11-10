package com.harimi.singtogether

import android.content.Intent
import android.icu.text.CaseMap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Api
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson

import com.harimi.singtogether.Network.*
import com.harimi.singtogether.adapter.MyPagePagerAdapter
import com.harimi.singtogether.databinding.FragmentMyPageBinding
import com.harimi.singtogether.sing.DuetFragment
import com.harimi.singtogether.sing.MRFragment
import com.harimi.singtogether.sing.SingPagerAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * 마이페이지 화면
 * */

class MyPageFragment : Fragment() {
    private var TAG :String = "MyPageFragment_"


    private lateinit var fragment_my_page_iv_profile:CircleImageView
    private lateinit var fragment_my_page_tv_nickname:TextView
    private lateinit var iv_badge:ImageView


    private lateinit var tv_followingNumber:TextView
    private lateinit var tv_followNumber:TextView
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")

        setFollowAndFollowing(tv_followNumber,tv_followingNumber)
        setMyProfile(fragment_my_page_tv_nickname,fragment_my_page_iv_profile,iv_badge)
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart")

    }



    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.e(TAG, "onCreateView")
        // 1. 뷰 바인딩 설정
        val binding=FragmentMyPageBinding.inflate(inflater,container,false)

        tv_followNumber = binding.tvFollowNumber.findViewById(R.id.tv_followNumber)
        tv_followingNumber = binding.tvFollowingNumber.findViewById(R.id.tv_followingNumber)
        fragment_my_page_iv_profile = binding.fragmentMyPageIvProfile.findViewById(R.id.fragment_my_page_iv_profile)
        fragment_my_page_tv_nickname = binding.fragmentMyPageTvNickname.findViewById(R.id.fragment_my_page_tv_nickname)
        iv_badge = binding.ivBadge.findViewById(R.id.iv_badge)

        setMyProfile(fragment_my_page_tv_nickname,fragment_my_page_iv_profile,iv_badge)
        setFollowAndFollowing(binding.tvFollowNumber, binding.tvFollowingNumber)

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
        val pagerAdapter = MyPagePagerAdapter(requireActivity(),LoginActivity.user_info.loginUserEmail)
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

        binding.profileEdit.setOnClickListener {
            val intent = Intent(context, ProfileEditActivity::class.java)
            startActivity(intent)
        }

        // 3. 프래그먼트 레이아웃 뷰 반환
        return binding.root
    }
    private fun setMyProfile(nickName: TextView, profileImage: CircleImageView, badge:ImageView){

        retrofit = RetrofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)

        retrofitService.requestAutoLogin(LoginActivity.user_info.loginUserEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "shared " + response.body() + response.message())
                        val jsonObject = JSONObject(response.body().toString())
                        val result = jsonObject.getBoolean("result")
                        Log.d(TAG, "shared " + result.toString())

                        if (result) {
                            val email = jsonObject.getString("email")
                            val nickname = jsonObject.getString("nickname")
                            val profile = jsonObject.getString("profile")
                            val social = jsonObject.getString("social")
                            val token = jsonObject.getString("token")
                            val isBadge = jsonObject.getBoolean("isBadge")

                            LoginActivity.user_info.loginUserGetBadge = isBadge
                            LoginActivity.user_info.loginUserEmail = email.toString()
                            LoginActivity.user_info.loginUserNickname = nickname.toString()
                            LoginActivity.user_info.loginUserProfile = profile.toString()
                            LoginActivity.user_info.loginUserSocial = social.toString()
                            LoginActivity.user_info.loginUserFCMToken = token.toString()

                            // 사용자 닉네임
                            nickName.text=nickname


                            // 사용자 프로필
                            if (profile.equals("null")){

                            }else{
                                Glide.with(requireActivity()).load("http://3.35.236.251/"+profile).into(profileImage)
                            }

                            if (LoginActivity.user_info.loginUserGetBadge ==true){
                                badge.visibility =View.VISIBLE
                            }else{
                                badge.visibility =View.GONE
                            }
                        }

                    } else {
                        Log.e("onResponse", "실패 : " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(
                        "실패:", "Failed API call with call: " + call +
                                " + exception: " + t
                    )
                }

            })
    }

    private fun setFollowAndFollowing(follow:TextView, following:TextView){

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

                            follow.setText(followUserNumber)
                            following.setText(followingUserNumber)
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                }
            })

    }

    companion object {

        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}