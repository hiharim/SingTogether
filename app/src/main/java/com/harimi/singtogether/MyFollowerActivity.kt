package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.MyFollowData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.adapter.MyFollowerAdapter
import com.harimi.singtogether.adapter.MyFollowingAdapter

class MyFollowerActivity : AppCompatActivity() {
    private lateinit var iv_back : ImageButton
    private lateinit var rv_myFollower : RecyclerView
    private val myFollowDataList: ArrayList<MyFollowData> = ArrayList()
    private lateinit var myFollowerAdapter: MyFollowerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_follower)

        iv_back = findViewById(R.id.iv_back)
        rv_myFollower = findViewById(R.id.rv_myFollower)
        rv_myFollower.layoutManager = LinearLayoutManager(this)
        rv_myFollower.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        myFollowerAdapter = MyFollowerAdapter(myFollowDataList, this)
        rv_myFollower.adapter = myFollowerAdapter

    }
}