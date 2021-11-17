package com.harimi.singtogether

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyPagePagerAdapter
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LookAtUserProfileActivity : AppCompatActivity() {
    private var TAG :String = "LookAtUserProfileActivity "
    private lateinit var iv_profileImage : CircleImageView
    private lateinit var tv_nickname : TextView
    private lateinit var tv_follwingNumber : TextView
    private lateinit var tv_followNumber : TextView
    private lateinit var btn_follow : Button
    private lateinit var btn_followCancel : Button
    private lateinit var ib_back : ImageButton
    private lateinit var iv_badge : ImageView
    private lateinit var tv_follwing : TextView
    private lateinit var tv_follow : TextView


    private lateinit var tabLayout_lookAtUserProfile : TabLayout
    private lateinit var viewPager_lookAtUserProfile : ViewPager2
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    private lateinit var otherUserEmail : String
    private lateinit var followingUserNumber : String
    private lateinit var followUserNumber : String


    private lateinit var nickname : String
    private lateinit var profile : String
    private var isFollow : Boolean ?= false
    private var isBadge : Boolean ?= false
    private lateinit var getFollowNumber : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_look_at_user_profile)

        initView()


        val pagerAdapter = MyPagePagerAdapter(this ,otherUserEmail)
        // 3개의 Fragment Add
        pagerAdapter.addFragment(MySongFragment())
        pagerAdapter.addFragment(MyBroadcastFragment())
        pagerAdapter.addFragment(MyPostFragment())

        viewPager_lookAtUserProfile.adapter=pagerAdapter
        viewPager_lookAtUserProfile.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("ViewPagerFragment", "Page ${position+1}")
            }
        })
        TabLayoutMediator(tabLayout_lookAtUserProfile,viewPager_lookAtUserProfile){
                tab,position->
            when(position){
                0 ->{
                    tab.text="포스팅"

                }
                1 ->{
                    tab.text="듀엣"

                }
                2 ->{
                    tab.text="방송"
                }

            }
        }.attach()
    }


    private fun initView(){
        iv_profileImage =findViewById(R.id.iv_profileImage)
        tv_nickname =findViewById(R.id.tv_nickname)
        btn_follow =findViewById(R.id.btn_follow)
        tv_follwingNumber =findViewById(R.id.tv_follwingNumber)
        tv_followNumber =findViewById(R.id.tv_followNumber)
        tabLayout_lookAtUserProfile =findViewById(R.id.tabLayout_lookAtUserProfile)
        viewPager_lookAtUserProfile =findViewById(R.id.viewPager_lookAtUserProfile)
        btn_followCancel =findViewById(R.id.btn_followCancel)
        ib_back =findViewById(R.id.ib_back)
        iv_badge =findViewById(R.id.iv_badge)
        tv_follwing =findViewById(R.id.tv_follwing)
        tv_follow =findViewById(R.id.tv_follow)



        var intent = intent
        otherUserEmail = intent.getStringExtra("otherUserEmail")
        nickname = intent.getStringExtra("nickname")
        profile = intent.getStringExtra("profile")
        followingUserNumber = intent.getStringExtra("followingUserNumber")
        followUserNumber = intent.getStringExtra("followUserNumber")
        isFollow = intent.getBooleanExtra("isFollow",false)
        isBadge = intent.getBooleanExtra("isBadge",false)
        getFollowNumber = followUserNumber.toString()
        Log.d(TAG, otherUserEmail)


        if (isBadge ==true){
            iv_badge.visibility =View.VISIBLE
        }else{
            iv_badge.visibility =View.GONE
        }

        // 사용자 프로필
        if (iv_profileImage.equals("null")){

        }else{
            Glide.with(this).load("http://3.35.236.251/"+profile).fitCenter().into(iv_profileImage)
        }
        tv_nickname.setText(nickname)
        tv_followNumber.setText(followUserNumber)
        tv_follwingNumber.setText(followingUserNumber)

        if (isFollow ==false){
            btn_follow.visibility = View.VISIBLE
            btn_followCancel.visibility = View.GONE
        }else{
            btn_follow.visibility = View.GONE
            btn_followCancel.visibility = View.VISIBLE
        }

        tv_follwing.setOnClickListener {
            val intent= Intent(this, MyFollowingActivity::class.java)
            intent.putExtra("myEmail",otherUserEmail)
            intent.putExtra("nowPage","otherUserPage")
            startActivity(intent)


        }

        tv_follow.setOnClickListener {
            val intent= Intent(this, MyFollowerActivity::class.java)
            intent.putExtra("myEmail",otherUserEmail)
            intent.putExtra("nowPage","otherUserPage")
            startActivity(intent)
        }


        btn_follow.setOnClickListener {

            btn_follow.isEnabled = false
            btn_followCancel.isEnabled =false
            retrofit= RetrofitClient.getInstance()
            retrofitService=retrofit.create(RetrofitService::class.java)
            retrofitService.requestAddFollow(
                LoginActivity.user_info.loginUserEmail,
                otherUserEmail
            )
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d(TAG, body)
                            var jsonObject = JSONObject(response.body().toString())
                            var result = jsonObject.getBoolean("result")
                            if (result) {

                                val getFollowerNum = tv_followNumber.text.toString()
                                val num = getFollowerNum.toInt() +1
                                isFollow = true
                                btn_follow.visibility = View.GONE
                                btn_followCancel.visibility = View.VISIBLE
//                                val getFollowNum = getFollowNumber.toInt() + 1
                                tv_followNumber.setText(num.toString())

                                btn_follow.isEnabled = true
                                btn_followCancel.isEnabled =true
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })
        }

        btn_followCancel.setOnClickListener {
            btn_follow.isEnabled = false
            btn_followCancel.isEnabled =false
            retrofit= RetrofitClient.getInstance()
            retrofitService=retrofit.create(RetrofitService::class.java)
            retrofitService.requestDeleteFollow(
                LoginActivity.user_info.loginUserEmail,
                otherUserEmail
            )
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d(TAG, body)
                            var jsonObject = JSONObject(response.body().toString())
                            var result = jsonObject.getBoolean("result")
                            if (result) {


                                isFollow = false
                                val getFollowerNum = tv_followNumber.text.toString()
                                val num = getFollowerNum.toInt() -1
                                btn_follow.visibility = View.VISIBLE
                                btn_followCancel.visibility = View.GONE
                                tv_followNumber.setText(num.toString())

                                btn_follow.isEnabled = true
                                btn_followCancel.isEnabled =true
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })
        }

        ib_back.setOnClickListener {
            finish()
        }
    }
}