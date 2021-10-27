package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.harimi.singtogether.databinding.ActivityMainBinding
import com.ismaeldivita.chipnavigation.ChipNavigationBar



class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity_"
    private lateinit var binding: ActivityMainBinding
    private val fragmentHome : HomeFragment = HomeFragment() // 홈
    private val fragmentSing : SingFragment = SingFragment() // 부르기
    private val fragmentBroadcast : BraodcastFragment=BraodcastFragment() // 방송
    private val fragmentMyPage : MyPageFragment = MyPageFragment() // 마이페이지

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

    }

    private fun changeFragment(fragment :Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.activity_main_frame,fragment)
            .commit()
    }
}