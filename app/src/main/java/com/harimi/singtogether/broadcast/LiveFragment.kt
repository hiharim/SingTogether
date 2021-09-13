package com.harimi.singtogether.broadcast

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
실시간 방송 프래그먼트
 */
class LiveFragment : Fragment() {

    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private var TAG: String = "LIVE_FRAGMENT"

    val liveStreamingPostList: ArrayList<LiveFragmentData> = ArrayList()
    lateinit var rv_fragmentLivePost: RecyclerView
    lateinit var liveFragmentAdapter: LiveFragmentAdapter
    lateinit var tv_noLive: TextView
    lateinit var swipeRefresh: SwipeRefreshLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        Log.d(TAG, "onCreate")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")


    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.detach(this).attach(this).commit()
        liveStreamingPostList.clear()
        liveFragmentAdapter.notifyDataSetChanged()
        liveStreamingPostLoad()

//        swipeRefresh.performClick()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        ////performClick 이란 지정된 객체를 한번 실행시키라는 메소드

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        var liveFragmentView = inflater.inflate(R.layout.fragment_live, container, false)
        initView(liveFragmentView)
        liveStreamingPostLoad()

        return liveFragmentView
    }


    fun initView(liveFragmentView: View) {
        swipeRefresh = liveFragmentView.findViewById(R.id.swipeRefresh)
        rv_fragmentLivePost = liveFragmentView.findViewById(R.id.rv_fragmentLivePost)
        tv_noLive = liveFragmentView.findViewById(R.id.tv_noLive)

        rv_fragmentLivePost.layoutManager = LinearLayoutManager(activity)
        rv_fragmentLivePost.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        liveFragmentAdapter = LiveFragmentAdapter(liveStreamingPostList, requireContext())
        rv_fragmentLivePost.adapter = liveFragmentAdapter
        ///리사이클러뷰 새로고침
        swipeRefresh.setOnRefreshListener {
            // 사용자가 아래로 드래그 했다가 놓았을 때 호출 됩니다.
            // 이때 새로고침 화살표가 계속 돌아갑니다.

            liveStreamingPostList.clear()
            liveFragmentAdapter.notifyDataSetChanged()
            liveStreamingPostLoad()
            swipeRefresh.isRefreshing = false  //서버 통신 완료 후 호출해줍니다.
        }
    }

    fun liveStreamingPostLoad() {
        retrofit = RetrofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetLiveStreamingPost().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                liveStreamingPostList.clear()
                if (response.isSuccessful) {
                    val body = response.body().toString()
                    Log.d(TAG,"getHomePost: "+ body)


                    val jsonArray = JSONArray(body)
                    if (jsonArray.length() == 0 || jsonArray.equals("null")) {
                        tv_noLive.visibility = View.VISIBLE
                        rv_fragmentLivePost.visibility = View.INVISIBLE
                    } else {
                        rv_fragmentLivePost.visibility = View.VISIBLE
                        tv_noLive.visibility = View.INVISIBLE

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val idx = jsonObject.getString("idx")
                            val email = jsonObject.getString("email")
                            val thumbnail = jsonObject.getString("thumbnail")
                            val nickName = jsonObject.getString("nickName")
                            val profile = jsonObject.getString("profile")
                            val title = jsonObject.getString("title")
                            val viewer = jsonObject.getString("viewer")

                            val liveData = LiveFragmentData(
                                idx,
                                thumbnail,
                                email,
                                nickName,
                                profile,
                                title,
                                viewer
                            )
                            liveStreamingPostList.add(0, liveData)
                            liveFragmentAdapter.notifyDataSetChanged()
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
            LiveFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}