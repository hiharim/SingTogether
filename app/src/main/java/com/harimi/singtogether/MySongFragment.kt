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
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MySongAdapter
import com.harimi.singtogether.databinding.FragmentDuetBinding
import com.harimi.singtogether.databinding.FragmentMySongBinding
import com.harimi.singtogether.sing.DuetAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * 마이페이지 '듀엣' 프래그먼트
 */
class MySongFragment : Fragment() {

    var TAG :String = "MySongFragment "
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val mySongList : ArrayList<MySongData> = ArrayList()
    private lateinit var mySongAdapter: MySongAdapter
    private var myEmail : String?=null
    private var isBadge :Boolean ?= false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            myEmail=it.getString("email")
            Log.e(TAG ,"myEmail:" + myEmail)
        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentMySongBinding.inflate(inflater,container,false)

        binding.fragmentMySongRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentMySongRecyclerView.setHasFixedSize(true)
        binding.fragmentMySongRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        mySongAdapter= MySongAdapter(mySongList)
        binding.fragmentMySongRecyclerView.adapter=mySongAdapter
        loadMySong(binding.tvNoMySong,binding.fragmentMySongRecyclerView)

        return binding.root
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MySongFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun loadMySong(noSong : TextView, recyclerview : RecyclerView) {

        retrofitService.requestMySong(myEmail!!).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e("MySongFragment", " 통신 성공: ${response.body().toString()}")
                    mySongList.clear()
                    val body = response.body().toString()
                    val replayObject = JSONObject(body)
                    val badgeList = replayObject.getString("badgeList")
                    val outputData = replayObject.getString("outputData")
                        if (outputData.equals("") ) {
                            noSong.visibility = View.VISIBLE
                            recyclerview.visibility = View.GONE
                        } else {
                            val postArray = JSONArray(outputData)
                            noSong.visibility = View.GONE
                            recyclerview.visibility = View.VISIBLE

                            for(i in 0..postArray.length() -1) {
                                val iObject=postArray.getJSONObject(i)
                                val duet_idx=iObject.getInt("duet_idx")
                                val mr_idx=iObject.getInt("mr_idx")
                                val thumbnail=iObject.getString("thumbnail")
                                val title=iObject.getString("title")
                                val singer=iObject.getString("singer")
                                val cnt_play=iObject.getString("cnt_play")
                                val cnt_reply=iObject.getString("cnt_reply")
                                val email=iObject.getString("email")
                                val nickname=iObject.getString("nickname")
                                val profile=iObject.getString("profile")
                                val cnt_duet=iObject.getString("cnt_duet")
                                val lyrics=iObject.getString("lyrics")
                                val song_path=iObject.getString("duet_path")
                                val mr_path=iObject.getString("song_path")
                                val extract_path=iObject.getString("extract_path")
                                val duet_date=iObject.getString("date")
                                val kinds=iObject.getString("kinds")
                                val token=iObject.getString("token")
                                val userLeaveCheck = iObject.getString("leaveCheck")
                                var path=song_path

                                if (!badgeList.equals("")) {
                                    val badgeArray = JSONArray(badgeList)
                                    for (i in 0 until badgeArray.length()) {
                                        var badgeObject = badgeArray.getJSONObject(i)
                                        var badge_email = badgeObject.getString("email")
                                        if (badge_email.equals(email)) {
                                            isBadge = true
                                            break
                                        } else {
                                            isBadge = false
                                        }
                                    }
                                } else {
                                    isBadge = false
                                }

                                val mySongData=MySongData(duet_idx,mr_idx, thumbnail, title, singer, cnt_play, cnt_reply, cnt_duet,email, nickname, profile,path,duet_date,mr_path,extract_path,kinds,lyrics,token,isBadge!!,userLeaveCheck)
                                mySongList.add(0,mySongData)
                                mySongAdapter.notifyDataSetChanged()
                            }

                        }

                } else {
                    // 통신은 성공했지만 응답에 문제가 있는 경우
                    Log.e("MySongFragment", " 응답 문제" + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("MySongFragment", " 통신 실패" + t.message)
            }


        })
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }



}