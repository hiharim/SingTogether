package com.harimi.singtogether.sing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// 검색 페이저 어댑터
class SearchPagerAdapter(fragmentActivity: FragmentActivity, private val searchInput :String) : FragmentStateAdapter(
    fragmentActivity
) {
    var fragments : ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        //return fragments[position]
        Log.e("SearchPagerAdapter", "searchInput :" +searchInput)
        Log.e("SearchPagerAdapter", "position :" +position)
        if(position==0){
            val resultMRFragment = ResultMRFragment()
            val bundle =Bundle()
            bundle.putString("searchInput",searchInput)
            resultMRFragment.arguments=bundle
            return resultMRFragment

        }else if(position==1){
            val resultDuetFragment = ResultDuetFragment()
            val bundle =Bundle()
            bundle.putString("searchInput",searchInput)
            resultDuetFragment.arguments=bundle
            return resultDuetFragment

        }else{
            val resultSongFragment = ResultSongPostFragment()
            val bundle =Bundle()
            bundle.putString("searchInput",searchInput)
            resultSongFragment.arguments=bundle
            return resultSongFragment
        }
    }

    fun addFragment(fragment: Fragment){
        fragments.add(fragment)
        Log.e("SearchPagerAdapter", "addFragment , searchInput :" +searchInput)
        notifyItemInserted(fragments.size - 1)
    }

}