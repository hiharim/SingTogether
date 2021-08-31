package com.harimi.singtogether.broadcast

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
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.adapter.LiveFragmentAdapter
import com.harimi.singtogether.databinding.FragmentLiveBinding
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 실시간 방송 프래그먼트
 */
class LiveFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var TAG :String = "LIVE_FRAGMENT"

    val liveStreamingPostList: ArrayList<LiveFragmentData> = ArrayList()
    lateinit var rv_fragmentLivePost : RecyclerView
    lateinit var liveFragmentAdapter: LiveFragmentAdapter
    lateinit var  tv_noLive: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        Log.d("라이브: ", "onCreate")
    }

    override fun onPause() {
        super.onPause()
        Log.d("라이브: ", "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("라이브: ", "onResume")
        liveStreamingPostList.clear()
        liveStreamingPostLoad()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Log.d("라이브: ", "onCreateView")
        var liveFragmentView = inflater.inflate(R.layout.fragment_live, container, false)

        rv_fragmentLivePost = liveFragmentView.findViewById(R.id.rv_fragmentLivePost)
        tv_noLive = liveFragmentView.findViewById(R.id.tv_noLive)

        rv_fragmentLivePost.layoutManager = LinearLayoutManager(activity)
        rv_fragmentLivePost.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        liveFragmentAdapter = LiveFragmentAdapter(liveStreamingPostList,requireContext())
        rv_fragmentLivePost.adapter = liveFragmentAdapter

        liveStreamingPostLoad ()

        return liveFragmentView

    }


    fun liveStreamingPostLoad (){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetLiveStreamingPost()
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d("getHomePost: ", body)
                        liveStreamingPostList.clear()

                        val jsonArray = JSONArray(body)
                        if (jsonArray.length()==0 || jsonArray.equals("null")){
                            tv_noLive.visibility =View.VISIBLE
                            rv_fragmentLivePost.visibility =View.INVISIBLE
                        }else {
                            rv_fragmentLivePost.visibility =View.VISIBLE
                            tv_noLive.visibility =View.INVISIBLE

                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val idx = jsonObject.getString("idx")
                                val email = jsonObject.getString("email")
                                val thumbnail = jsonObject.getString("thumbnail")
                                val nickName = jsonObject.getString("nickName")
                                val profile = jsonObject.getString("profile")
                                val title = jsonObject.getString("title")
//
                                Log.d(TAG, "idx($i): $idx")
                                Log.d(TAG, "songTitle($i): $title")
                                Log.d(TAG, "singer($i): $email")

                                val liveData = LiveFragmentData(
                                    idx,
                                    thumbnail,
                                    email,
                                    nickName,
                                    profile,
                                    title
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