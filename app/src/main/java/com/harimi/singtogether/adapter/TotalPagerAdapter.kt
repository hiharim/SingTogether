package com.harimi.singtogether.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harimi.singtogether.*
import com.harimi.singtogether.broadcast.LiveFragment
import com.harimi.singtogether.broadcast.ReplayFragment

/**
 * 전체보기 뷰페이저 어댑터
 * */
class TotalPagerAdapter(fragmentActivity: FragmentActivity ): FragmentStateAdapter(fragmentActivity) {

    var fragments : ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 ->  PopFragment()
            1 -> NewFragment()
            2 -> FollowingFragment()
            else -> HomeFragment()
        }
    }

    fun addFragment(fragment: Fragment){
        fragments.add(fragment)
        notifyItemInserted(fragments.size-1)
    }

}