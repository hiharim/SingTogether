package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.annotations.SerializedName
import com.harimi.singtogether.Data.MyBroadcastData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyBroadcastAdapter
import com.harimi.singtogether.adapter.MySongAdapter
import com.harimi.singtogether.databinding.FragmentMyBroadcastBinding
import com.harimi.singtogether.databinding.FragmentMySongBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * 마이페이지 ' 내 방송' 프래그먼트
 */
class MyBroadcastFragment : Fragment() {
    private var TAG = "MYBRODCAST_FRAGMENT"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val myBroadcastList : ArrayList<MyBroadcastData> = ArrayList()
    private lateinit var myBroadcastAdapter: MyBroadcastAdapter
    private var like :Boolean ?= false
    private var replayPostLikeIdx :String ?= "null"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentMyBroadcastBinding.inflate(inflater,container,false)

        binding.fragmentMyBroadcastRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentMyBroadcastRecyclerView.setHasFixedSize(true)
        binding.fragmentMyBroadcastRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        myBroadcastAdapter= MyBroadcastAdapter(myBroadcastList,requireContext())
        binding.fragmentMyBroadcastRecyclerView.adapter=myBroadcastAdapter
        loadMyBroadcast()

        return binding.root
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")


        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.detach(this).attach(this).commit()
        myBroadcastList.clear()
        myBroadcastAdapter.notifyDataSetChanged()
        loadMyBroadcast()


    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyBroadcastFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    fun loadMyBroadcast (){
        val userEmail=LoginActivity.user_info.loginUserEmail
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestMyBroadcast(userEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        myBroadcastList.clear()
                        val replayObject = JSONObject (body)
                        Log.d(TAG, replayObject.toString())
                        val replayPostList = replayObject.getString("replayPostList")
                        val postArray = JSONArray(replayPostList)
                        val userLikeList = replayObject.getString("userLikeList")

                        for (i in 0 until postArray.length()) {
                            if (postArray.length() ==0 || postArray.equals("null")){
//                                tv_noReplay.visibility =View.VISIBLE
//                                rv_fragmentReplayPost.visibility =View.GONE
                            }else {
//                                rv_fragmentReplayPost.visibility =View.VISIBLE
//                                tv_noReplay.visibility =View.GONE

                                var postObject = postArray.getJSONObject(i)
                                var idx = postObject.getString("idx")
                                var thumbnail = postObject.getString("thumbnail")
                                var replayTitle = postObject.getString("replayTitle")
                                var uploadUserEmail = postObject.getString("uploadUserEmail")
                                var uploadUserProfile = postObject.getString("uploadUserProfile")
                                var uploadUserNickName = postObject.getString("uploadUserNickName")
                                var replayLikeNumber = postObject.getString("replayLikeNumber")
                                var replayHits = postObject.getString("replayHits")
                                var replayReviewNumber = postObject.getString("replayReviewNumber")
                                var uploadDate = postObject.getString("uploadDate")
                                var replayVideo = postObject.getString("replayVideo")



                                if (!userLikeList.equals("")){
                                    val likeArray = JSONArray(userLikeList)
                                    for (i in 0 until likeArray.length()) {
                                        var likeObject = likeArray.getJSONObject(i)
                                        var replayPostIdx = likeObject.getString("replayPostIdx")
                                        replayPostLikeIdx = likeObject.getString("replayPostLikeIdx")
                                        if (replayPostIdx.equals(idx)){
                                            like= true
                                            Log.d(TAG, like.toString())
                                            break
                                        }else{
                                            like= false
                                            Log.d(TAG, like.toString())
                                        }
                                    }
                                }
                                val myBroadcastData = MyBroadcastData(idx, uploadUserProfile, uploadUserNickName, thumbnail, replayTitle,
                                    replayReviewNumber, replayHits, replayLikeNumber, uploadDate, uploadUserEmail,like!!,replayPostLikeIdx!!,replayVideo)
                                myBroadcastList.add(0, myBroadcastData)
                                myBroadcastAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
//    private fun loadMyBroadcast() {
//        val userEmail=LoginActivity.user_info.loginUserEmail
//        retrofitService.requestMyBroadcast(userEmail).enqueue(object : Callback<String> {
//            // 통신에 성공한 경우
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                if (response.isSuccessful) {
//                    // 응답을 잘 받은 경우
//                    Log.e("MyBroadcastFragment", " 통신 성공: ${response.body().toString()}")
//
//
////                    val jsonArray= JSONArray(response.body().toString())
////                    for(i in 0..jsonArray.length() -1){
////                        val iObject=jsonArray.getJSONObject(i)
////                        val idx=iObject.getInt("idx")
////                        val thumbnail=iObject.getString("thumbnail")
////                        val replayVideo=iObject.getString("replayVideo")
////                        val replayReviewNumber=iObject.getString("replayReviewNumber")
////                        val replayLikeNumber=iObject.getString("replayLikeNumber")
////                        val replayTitle=iObject.getString("replayTitle")
////                        val replayHits=iObject.getString("replayHits")
////                        val uploadDate=iObject.getString("uploadDate")
////                        var uploadUserEmail = iObject.getString("uploadUserEmail")
////                        var uploadUserProfile = iObject.getString("uploadUserProfile")
////                        var uploadUserNickName = iObject.getString("uploadUserNickName")
////
////
////                        val myBroadcastData= MyBroadcastData(idx, thumbnail, replayVideo,replayReviewNumber,replayLikeNumber,replayTitle,replayHits,uploadDate
////                            ,uploadUserEmail,uploadUserProfile,uploadUserNickName)
////                        myBroadcastList.add(0,myBroadcastData)
////                        myBroadcastAdapter.notifyDataSetChanged()
////                    }
////
////                } else {
////                    // 통신은 성공했지만 응답에 문제가 있는 경우
////                    Log.e("MyBroadcastFragment", " 응답 문제" + response.code())
////                }
////            }
////
////            override fun onFailure(call: Call<String>, t: Throwable) {
////                Log.e("MyBroadcastFragment", " 통신 실패" + t.message)
////            }
//                }
//                }
//        })
//    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}