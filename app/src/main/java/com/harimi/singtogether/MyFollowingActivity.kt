package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MyFollowingActivity : AppCompatActivity() {
    private var TAG :String = "MyFollowingActivity_ "
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    private lateinit var iv_back : ImageButton
    private lateinit var tv_alert : TextView


    private lateinit var rv_myFollowing : RecyclerView
    private val myFollowingDataList: ArrayList<MyFollowingData> = ArrayList()
    private lateinit var myFollowingAdapter: MyFollowingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_following)

        iv_back = findViewById(R.id.iv_back)
        rv_myFollowing = findViewById(R.id.rv_myFollowing)
        tv_alert = findViewById(R.id.tv_alert)


        rv_myFollowing.layoutManager = LinearLayoutManager(this)
        rv_myFollowing.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        myFollowingAdapter = MyFollowingAdapter(myFollowingDataList, this)
        rv_myFollowing.adapter = myFollowingAdapter



        iv_back.setOnClickListener {
            finish()
        }
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
                        val jsonObject = JSONObject(body)

                        val result = jsonObject.getBoolean("result")

                        if (result){
                            tv_alert.visibility =View.GONE
                            rv_myFollowing.visibility =View.VISIBLE

                            val getfollowingList = jsonObject.getString("getfollowingList")
                            val getfollowingArray = JSONArray(getfollowingList)
                            for (i in 0 until getfollowingArray.length()) {
                                val getfollowingObject = getfollowingArray.getJSONObject(i)

                                val email = getfollowingObject.getString("email")
                                val profile = getfollowingObject.getString("profile")
                                val token = getfollowingObject.getString("token")
                                val nickname = getfollowingObject.getString("nickname")

                                val myFollowingData = MyFollowingData(
                                    email,
                                    profile,
                                    nickname,
                                    token
                                )
                                myFollowingDataList.add(myFollowingData)
                                myFollowingAdapter.notifyDataSetChanged()
                            }
                        }else{
                            tv_alert.visibility =View.VISIBLE
                            rv_myFollowing.visibility =View.GONE
                        }

                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
}