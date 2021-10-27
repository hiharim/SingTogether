package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.DetailDuetReviewData
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import com.harimi.singtogether.adapter.MyFollowingAdapter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MyFollowingActivity : AppCompatActivity() {
    private var TAG :String = "MyFollowingActivity_ "
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
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
        myFollowingUserLoad()
    }

    fun myFollowingUserLoad() {
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetMyFollowingUser(LoginActivity.user_info.loginUserEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        myFollowingDataList.clear()

//                        val jsonArray = JSONArray(body)
//                        for (i in 0 until jsonArray.length()) {
//                            val jsonObject = jsonArray.getJSONObject(i)
//
//                            val idx = jsonObject.getString("idx")
//                            val uploadUserEmail = jsonObject.getString("uploadUserEmail")
//                            val uploadUserProfile = jsonObject.getString("uploadUserProfile")
//                            val uploadUserNickname = jsonObject.getString("uploadUserNickname")
//                            val review = jsonObject.getString("review")
//                            val uploadDate = jsonObject.getString("uploadDate")
//                            val detailDuetIdx = jsonObject.getString("detailDuetIdx")
//
//
//                            val myFollowingData = MyFollowingData(
//                                idx,
//                                uploadUserEmail,
//                                uploadUserNickname,
//                                uploadUserProfile,
//                                review,
//                                uploadDate,
//                                detailDuetIdx
//                            )
//                            myFollowingDataList.add(myFollowingData)
//                            myFollowingAdapter.notifyDataSetChanged()
//                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
}