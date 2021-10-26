package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import com.harimi.singtogether.adapter.MyFollowingAdapter

class MyFollowingActivity : AppCompatActivity() {

    private lateinit var iv_back : ImageButton
    private lateinit var rv_myFollowing : RecyclerView
    private val myFollowingDataList: ArrayList<MyFollowingData> = ArrayList()
    private lateinit var myFollowingAdapter: MyFollowingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_following)

        iv_back = findViewById(R.id.iv_back)
        rv_myFollowing = findViewById(R.id.rv_myFollowing)
        rv_myFollowing.layoutManager = LinearLayoutManager(this)
        rv_myFollowing.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        myFollowingAdapter = MyFollowingAdapter(myFollowingDataList, this)
        rv_myFollowing.adapter = myFollowingAdapter




    }
}