package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.harimi.singtogether.databinding.ActivityMainBinding
import com.harimi.singtogether.sing.DetailDuetFragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar




class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity_"
    private lateinit var binding: ActivityMainBinding
    private val fragmentHome : HomeFragment = HomeFragment() // 홈
    private val fragmentSing : SingFragment = SingFragment() // 부르기
    private val fragmentBroadcast : BraodcastFragment=BraodcastFragment() // 방송
    private val fragmentMyPage : MyPageFragment = MyPageFragment() // 마이페이지
    private val fragmentDetailDuet : DetailDuetFragment = DetailDuetFragment() // 디테일 듀엣프래그먼트
    private val postFragment : PostFragment = PostFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG,LoginActivity.user_info.loginUserFCMToken)
        if(savedInstanceState == null) {
            binding.chipNavigationBar.setItemSelected(R.id.home,true)
            changeFragment(fragmentHome)
        }

        binding.chipNavigationBar.setOnItemSelectedListener{ id->
            when(id) {
                R.id.home -> changeFragment(fragmentHome)
                R.id.sing-> changeFragment(fragmentSing)
                R.id.live -> changeFragment(fragmentBroadcast)
                R.id.setting -> changeFragment(fragmentMyPage)
            }
        }

        val fcm=intent.getStringExtra("Notification")
        if(fcm !=null) {
            if(fcm.equals("DetailDuetFragment")) {
                val bundleData=intent.getBundleExtra("bundle")
                fragmentDetailDuet.arguments = bundleData
                changeFragment(fragmentDetailDuet)
            }
        }

        val GloryPost=intent.getStringExtra("GloryPost")
        if(GloryPost !=null) {
            if(GloryPost.equals("GloryPost")) {
                val bundleData=intent.getBundleExtra("bundle")
                postFragment.arguments = bundleData
                changeFragment(postFragment)
            }
        }

    }

    private fun changeFragment(fragment :Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.activity_main_frame,fragment)
            .commit()
    }
}