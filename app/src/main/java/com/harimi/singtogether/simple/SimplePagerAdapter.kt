package com.harimi.singtogether.simple

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harimi.singtogether.FollowingFragment
import com.harimi.singtogether.HomeFragment
import com.harimi.singtogether.NewFragment
import com.harimi.singtogether.PopFragment

class SimplePagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    var fragments : ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 ->  SimplePopFragment()
            1 -> SimpleNewFragment()
            2 -> SimpleFollowingFragment()
            else -> HomeFragment()
        }
    }

    fun addFragment(fragment: Fragment){
        fragments.add(fragment)
        notifyItemInserted(fragments.size-1)
    }

}