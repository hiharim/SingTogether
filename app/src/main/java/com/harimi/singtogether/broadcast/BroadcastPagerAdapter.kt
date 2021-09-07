package com.harimi.singtogether.broadcast

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harimi.singtogether.HomeFragment

/***
 * 방송 프래그먼트 PagerStateAdapter
 */

class BroadcastPagerAdapter(fragmentActivity: FragmentActivity):FragmentStateAdapter(fragmentActivity) {

    var fragments : ArrayList<Fragment> = ArrayList()


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        //return fragments[position]

        return when(position){
            0 -> LiveFragment()
            1 -> ReplayFragment()
            else -> HomeFragment()
        }
    }

    fun addFragment(fragment: Fragment){
        fragments.add(fragment)
        notifyItemInserted(fragments.size-1)
    }

    fun removeFragment(){
        fragments.removeLast()
        notifyItemRemoved(fragments.size)
    }


}