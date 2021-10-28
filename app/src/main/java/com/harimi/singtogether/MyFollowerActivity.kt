package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.MyFollowData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyFollowerAdapter
import com.harimi.singtogether.adapter.MyFollowingAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MyFollowerActivity : AppCompatActivity() {
    private var TAG :String = "MyFollowerActivity_ "
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    private lateinit var iv_back : ImageButton
    private lateinit var rv_myFollower : RecyclerView
    private val myFollowDataList: ArrayList<MyFollowData> = ArrayList()
    private lateinit var myFollowerAdapter: MyFollowerAdapter

    private var isFollow : Boolean ?= false
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


        myFollowUserLoad()
    }

    fun myFollowUserLoad() {
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetMyFollowUser(LoginActivity.user_info.loginUserEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        myFollowDataList.clear()
                        val jsonObject = JSONObject(body)
                        val getFollowerList = jsonObject.getString("getfollowerList")
                        val myFollowList = jsonObject.getString("myFollowlist")

                        val getFollowerArray = JSONArray(getFollowerList)

                        if (getFollowerArray.equals("")){

                        }else{
                            for (i in 0 until getFollowerArray.length()) {
                                val getFollowerObject = getFollowerArray.getJSONObject(i)

                                val email = getFollowerObject.getString("email")
                                val profile = getFollowerObject.getString("profile")
                                val token = getFollowerObject.getString("token")
                                val nickname = getFollowerObject.getString("nickname")


                                if (!myFollowList.equals("")){
                                    val myFollowArray = JSONArray(myFollowList)
                                    for (i in 0 until myFollowArray.length()) {
                                        var myFollowObject = myFollowArray.getJSONObject(i)
                                        var followingId = myFollowObject.getString("followingId")

                                        if (followingId.equals(email)){
                                            isFollow= true
                                            Log.d(TAG, isFollow.toString())
                                            break
                                        }else{
                                            isFollow= false
                                            Log.d(TAG, isFollow.toString())
                                        }
                                    }
                                }


                                val myFollowData = MyFollowData(email, profile, nickname, token,isFollow!!)
                                myFollowDataList.add(myFollowData)
                                myFollowerAdapter.notifyDataSetChanged()
                            }
                        }

                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
}