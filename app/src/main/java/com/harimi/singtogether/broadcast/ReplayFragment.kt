package com.harimi.singtogether.broadcast

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.adapter.ReplayFragmentAdapter

import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ReplayFragment : Fragment() {

    private var TAG :String = "REPLAY_ACTIVITY "

    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    private val replayDataList: ArrayList<ReplayData> = ArrayList()
    private lateinit var  rv_fragmentReplayPost: RecyclerView
    private lateinit var  tv_noReplay: TextView
    private lateinit var replayAdapter: ReplayFragmentAdapter
    private var like :Boolean ?= false
    private var isBadge :Boolean ?= false
    private var replayPostLikeIdx :String ?= "null"
    private lateinit var swipeRefresh: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        Log.d("리플레이: ", "onCreate")
    }


    override fun onPause() {
        super.onPause()
        Log.d("리플레이: ", "onPause")

    }

    // 재개상태로 전환될때마다 필요한 초기화 작업 수행
    override fun onResume() {
        super.onResume()
        Log.d("리플레이: ", "onResume")


        replayDataList.clear()
        replayAdapter.notifyDataSetChanged()
        replayPostLoad()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("리플레이: ", "onCreateView")
        var replayView = inflater.inflate(R.layout.fragment_replay, container, false)
        initView(replayView)
        return replayView

    }

    fun initView(replayView : View){
        rv_fragmentReplayPost = replayView.findViewById(R.id.rv_fragmentReplayPost)
        tv_noReplay = replayView.findViewById(R.id.tv_noReplay)
        rv_fragmentReplayPost.layoutManager = LinearLayoutManager(activity)
        rv_fragmentReplayPost.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        replayAdapter = ReplayFragmentAdapter(replayDataList,requireContext())
        rv_fragmentReplayPost.adapter = replayAdapter
        swipeRefresh = replayView.findViewById(R.id.swipeRefresh)


        replayPostLoad()

        swipeRefresh.setOnRefreshListener {
            // 사용자가 아래로 드래그 했다가 놓았을 때 호출 됩니다.
            // 이때 새로고침 화살표가 계속 돌아갑니다.

            replayDataList.clear()
            replayAdapter.notifyDataSetChanged()
            replayPostLoad()
            swipeRefresh.isRefreshing = false  //서버 통신 완료 후 호출해줍니다.
        }
    }

    fun replayPostLoad (){

        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetReplayPost(LoginActivity.user_info.loginUserEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        replayDataList.clear()
                        val replayObject = JSONObject (body)
                        val replayPostList = replayObject.getString("replayPostList")
                        val postArray = JSONArray(replayPostList)
                        val userLikeList = replayObject.getString("userLikeList")
                        val badgeList = replayObject.getString("badgeList")

                        for (i in 0 until postArray.length()) {
                            if (postArray.length() ==0 || postArray.equals("null")){
                                tv_noReplay.visibility =View.VISIBLE
                                rv_fragmentReplayPost.visibility =View.GONE
                            }else {
                                rv_fragmentReplayPost.visibility =View.VISIBLE
                                tv_noReplay.visibility =View.GONE
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
                                var time = postObject.getString("time")
                                var uploadUserFCMToken = postObject.getString("uploadUserFCMToken")
                                var userLeaveCheck = postObject.getString("userLeaveCheck")
                                // 탈퇴한 회원 프로필
//                                if(userLeaveCheck.equals("1")){
//                                    uploadUserProfile="uploadFile/profile22.png"
//                                }


                                if (!badgeList.equals("")) {
                                    val badgeArray = JSONArray(badgeList)
                                    for (i in 0 until badgeArray.length()) {
                                        var badgeObject = badgeArray.getJSONObject(i)
                                        var email = badgeObject.getString("email")
                                        if (email.equals(uploadUserEmail)){
                                            isBadge= true
                                            Log.d(TAG, isBadge.toString())
                                            break
                                        }else{
                                            isBadge= false
                                            Log.d(TAG, isBadge.toString())
                                        }
                                    }
                                }else{
                                    isBadge= false
                                }

                                if (!userLikeList.equals("")){
                                val likeArray = JSONArray(userLikeList)
                                    for (l in 0 until likeArray.length()) {
                                        var likeObject = likeArray.getJSONObject(l)
                                        var replayPostIdx = likeObject.getString("replayPostIdx")
                                        replayPostLikeIdx = likeObject.getString("replayPostLikeIdx")
                                        if (replayPostIdx.equals(idx)){
                                            like= true
                                            Log.d(TAG,"liked1: "+ like.toString())
                                            break
                                        }else{
                                            like= false
                                            Log.d(TAG,"liked2: "+ like.toString())
                                        }
                                    }
                                }else{
                                    like= false
                                }
                                val replayData = ReplayData(idx, uploadUserProfile, uploadUserNickName, thumbnail, replayTitle,
                                    replayReviewNumber, replayHits, replayLikeNumber, uploadDate, uploadUserEmail,like!!,replayPostLikeIdx!!,replayVideo,time,uploadUserFCMToken,isBadge!!,userLeaveCheck)

                                    replayDataList.add(0, replayData)
                                replayAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ReplayFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onStop() {
        super.onStop()
        Log.d("리플레이: ", "onStop")
    }
}