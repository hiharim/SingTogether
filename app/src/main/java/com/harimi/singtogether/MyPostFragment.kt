package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.MyBroadcastData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyBroadcastAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MyPostFragment : Fragment() {
    private var TAG = "MYBRODCAST_FRAGMENT"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var like :Boolean ?= false
    private var replayPostLikeIdx :String ?= "null"
    private lateinit var tv_noMyBroadcast: TextView

//    private val myBroadcastList : ArrayList<MyBroadcastData> = ArrayList()
//    private lateinit var myBroadcastAdapter: MyBroadcastAdapter
//    private lateinit var fragment_my_broadcast_recyclerView: RecyclerView
    private var myEmail : String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            myEmail=it.getString("email")
            Log.e(TAG ,"myEmail:" + myEmail)

            // 서버 연결
            initRetrofit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var myPostView = inflater.inflate(R.layout.fragment_my_post, container, false)
        tv_noMyBroadcast =myPostView.findViewById(R.id.tv_noMyBroadcast)
//        fragment_my_broadcast_recyclerView =myPostView.findViewById(R.id.fragment_my_broadcast_recyclerView)
//
//        fragment_my_broadcast_recyclerView.layoutManager= LinearLayoutManager(context)
//        fragment_my_broadcast_recyclerView.setHasFixedSize(true)
//        fragment_my_broadcast_recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
//        myBroadcastAdapter= MyBroadcastAdapter(myBroadcastList,requireContext())
//        fragment_my_broadcast_recyclerView.adapter=myBroadcastAdapter


//        loadMyPost()
        return myPostView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPostFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
//    fun loadMyPost (){
//        val userEmail=LoginActivity.user_info.loginUserEmail
//        retrofit= RetrofitClient.getInstance()
//        retrofitService=retrofit.create(RetrofitService::class.java)
//        retrofitService.requestMyBroadcast(userEmail)
//            .enqueue(object : Callback<String> {
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//                    if (response.isSuccessful) {
//                        val body = response.body().toString()
//                        myBroadcastList.clear()
//                        val replayObject = JSONObject (body)
//                        Log.d(TAG, replayObject.toString())
//
//                        val replayPostList = replayObject.getString("replayPostList")
//                        if (replayPostList.equals("") || replayPostList.equals("null")){
//                            tv_noMyBroadcast.visibility =View.VISIBLE
//                            fragment_my_broadcast_recyclerView.visibility =View.GONE
//                        }else{
//                            val postArray = JSONArray(replayPostList)
//                            for (i in 0 until postArray.length()) {
//                                if (postArray.length() ==0 || postArray.equals("")){
//
//                                }else {
//                                    val userLikeList = replayObject.getString("userLikeList")
//                                    fragment_my_broadcast_recyclerView.visibility =View.VISIBLE
//                                    tv_noMyBroadcast.visibility =View.GONE
//
//                                    var postObject = postArray.getJSONObject(i)
//                                    var idx = postObject.getString("idx")
//                                    var thumbnail = postObject.getString("thumbnail")
//                                    var replayTitle = postObject.getString("replayTitle")
//                                    var uploadUserEmail = postObject.getString("uploadUserEmail")
//                                    var uploadUserProfile = postObject.getString("uploadUserProfile")
//                                    var uploadUserNickName = postObject.getString("uploadUserNickName")
//                                    var replayLikeNumber = postObject.getString("replayLikeNumber")
//                                    var replayHits = postObject.getString("replayHits")
//                                    var replayReviewNumber = postObject.getString("replayReviewNumber")
//                                    var uploadDate = postObject.getString("uploadDate")
//                                    var replayVideo = postObject.getString("replayVideo")
//
//
//
//                                    if (!userLikeList.equals("")){
//                                        val likeArray = JSONArray(userLikeList)
//                                        for (i in 0 until likeArray.length()) {
//                                            var likeObject = likeArray.getJSONObject(i)
//                                            var replayPostIdx = likeObject.getString("replayPostIdx")
//                                            replayPostLikeIdx = likeObject.getString("replayPostLikeIdx")
//                                            if (replayPostIdx.equals(idx)){
//                                                like= true
//                                                Log.d(TAG, like.toString())
//                                                break
//                                            }else{
//                                                like= false
//                                                Log.d(TAG, like.toString())
//                                            }
//                                        }
//                                    }
//                                    val myBroadcastData = MyBroadcastData(idx, uploadUserProfile, uploadUserNickName, thumbnail, replayTitle,
//                                        replayReviewNumber, replayHits, replayLikeNumber, uploadDate, uploadUserEmail,like!!,replayPostLikeIdx!!,replayVideo)
//                                    myBroadcastList.add(0, myBroadcastData)
//                                    myBroadcastAdapter.notifyDataSetChanged()
//                                }
//                            }
//                        }
//
//                    }
//                }
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                }
//            })
//    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}