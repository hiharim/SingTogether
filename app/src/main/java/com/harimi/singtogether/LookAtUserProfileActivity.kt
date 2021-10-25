package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.harimi.singtogether.adapter.MyPagePagerAdapter
import de.hdodenhof.circleimageview.CircleImageView

class LookAtUserProfileActivity : AppCompatActivity() {

    private lateinit var iv_profileImage : CircleImageView
    private lateinit var tv_nickname : TextView
    private lateinit var tv_follwingNumber : TextView
    private lateinit var tv_followNumber : TextView
    private lateinit var btn_follow : Button
    private lateinit var tabLayout_lookAtUserProfile : TabLayout
    private lateinit var viewPager_lookAtUserProfile : ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_look_at_user_profile)

        iv_profileImage =findViewById(R.id.iv_profileImage)
        tv_nickname =findViewById(R.id.tv_nickname)
        btn_follow =findViewById(R.id.btn_follow)
        tv_follwingNumber =findViewById(R.id.tv_follwingNumber)
        tv_followNumber =findViewById(R.id.tv_followNumber)
        tabLayout_lookAtUserProfile =findViewById(R.id.tabLayout_lookAtUserProfile)
        viewPager_lookAtUserProfile =findViewById(R.id.viewPager_lookAtUserProfile)
        val pagerAdapter = MyPagePagerAdapter(this)

        // 2개의 Fragment Add
        pagerAdapter.addFragment(MySongFragment())
        pagerAdapter.addFragment(MyBroadcastFragment())

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
                    tab.text="듀엣초대"
                }
               1 ->{
                    tab.text="방송"
                }
                2 ->{
                    tab.text="포스팅"
                }

            }
        }.attach()
    }
}