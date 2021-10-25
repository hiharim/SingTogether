package com.harimi.singtogether.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harimi.singtogether.MyBroadcastFragment
import com.harimi.singtogether.MyPostFragment
import com.harimi.singtogether.MySongFragment
import com.harimi.singtogether.sing.ResultDuetFragment
import com.harimi.singtogether.sing.ResultMRFragment
import com.harimi.singtogether.sing.ResultSongPostFragment

/**
 * 마이페이지 화면 PagerAdapter
 * */
class MyPagePagerAdapter(fragmentActivity: FragmentActivity, private val email :String ) : FragmentStateAdapter(fragmentActivity) {

    var fragments : ArrayList<Fragment> = ArrayList()


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        if(position==0){
            val myPostFragment = MyPostFragment()
            val bundle = Bundle()
            bundle.putString("email",email)
            myPostFragment.arguments=bundle
            return myPostFragment

        }else if(position==1){
            val mySongFragment = MySongFragment()
            val bundle = Bundle()
            bundle.putString("email",email)
            mySongFragment.arguments=bundle
            return mySongFragment

        }else{
            val myBroadcastFragment = MyBroadcastFragment()
            val bundle = Bundle()
            bundle.putString("email",email)
            myBroadcastFragment.arguments=bundle
            return myBroadcastFragment
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