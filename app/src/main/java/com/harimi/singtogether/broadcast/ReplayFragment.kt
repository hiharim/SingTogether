package com.harimi.singtogether.broadcast

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.HomeAdapter
import com.harimi.singtogether.adapter.ReplayFragmentAdapter

import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ReplayFragment : Fragment() {

    private var TAG :String = "REPLAY ACTIVITY "

    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    val replayDataList: ArrayList<ReplayData> = ArrayList()
    lateinit var  rv_fragmentReplayPost: RecyclerView
    lateinit var  tv_noReplay: TextView
    lateinit var replayAdapter: ReplayFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {



        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var replayView = inflater.inflate(R.layout.fragment_replay, container, false)
        rv_fragmentReplayPost = replayView.findViewById(R.id.rv_fragmentReplayPost)
        tv_noReplay = replayView.findViewById(R.id.tv_noReplay)

        rv_fragmentReplayPost.layoutManager = LinearLayoutManager(activity)
        rv_fragmentReplayPost.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        replayAdapter = ReplayFragmentAdapter(replayDataList,requireContext())
        rv_fragmentReplayPost.adapter = replayAdapter

        replayPostLoad()

        return replayView
    }


    fun replayPostLoad (){

        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetReplayPost()
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d("getReplayPost: ", body)
                        replayDataList.clear()

                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {

                            if (jsonArray.length() ==0){
                                tv_noReplay.visibility =View.VISIBLE
                                rv_fragmentReplayPost.visibility =View.INVISIBLE

                            }else {
                                rv_fragmentReplayPost.visibility =View.VISIBLE
                                tv_noReplay.visibility =View.INVISIBLE

                                val jsonObject = jsonArray.getJSONObject(i)
                                val idx = jsonObject.getString("idx")
                                val thumbnail = jsonObject.getString("thumbnail")
                                val replayTitle = jsonObject.getString("replayTitle")
                                val uploadUserEmail = jsonObject.getString("uploadUserEmail")
                                val uploadUserProfile = jsonObject.getString("uploadUserProfile")
                                val uploadUserNickName = jsonObject.getString("uploadUserNickName")
                                val replayLikeNumber = jsonObject.getString("replayLikeNumber")
                                val replayHits = jsonObject.getString("replayHits")
                                val replayReviewNumber = jsonObject.getString("replayReviewNumber")
                                val uploadDate = jsonObject.getString("uploadDate")
                                Log.d(TAG, "idx($i): $idx")


                                val replayData = ReplayData(
                                    idx,
                                    uploadUserProfile,
                                    uploadUserNickName,
                                    thumbnail,
                                    replayTitle,
                                    replayReviewNumber,
                                    replayHits,
                                    replayLikeNumber,
                                    uploadDate,
                                    uploadUserEmail
                                )
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
        fun newInstance(param1: String, param2: String) =
            ReplayFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onStop() {
        super.onStop()
        Log.d("getReplayPost: ", "onStop")
    }
}