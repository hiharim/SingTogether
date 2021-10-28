package com.harimi.singtogether.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harimi.singtogether.*

/**
 * 전체보기 뷰페이저 어댑터
 * */
class TotalPagerAdapter(fragmentActivity: FragmentActivity, private val email :String ): FragmentStateAdapter(fragmentActivity) {

    var fragments : ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        if(position==0){
            val popFragment = PopFragment()
            val bundle = Bundle()
            bundle.putString("email",email)
            popFragment.arguments=bundle
            return popFragment

        }else if(position==1){
            val newFragment = NewFragment()
            val bundle = Bundle()
            bundle.putString("email",email)
            newFragment.arguments=bundle
            return newFragment

        }else{
            val followingFragment = FollowingFragment()
            val bundle = Bundle()
            bundle.putString("email",email)
            followingFragment.arguments=bundle
            return followingFragment
        }
    }

    fun addFragment(fragment: Fragment){
        fragments.add(fragment)
        notifyItemInserted(fragments.size-1)
    }

}