package com.harimi.singtogether.sing

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDuetBinding
import com.harimi.singtogether.databinding.FragmentPopBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * 듀엣 프래그먼트
 */
class DuetFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val duetList : ArrayList<DuetData> = ArrayList()
    private lateinit var duetAdapter: DuetAdapter
    private lateinit var binding: FragmentDuetBinding
    private var isBadge :Boolean ?= false

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
        binding=FragmentDuetBinding.inflate(inflater,container,false)
        binding.duetBack.setBackgroundColor(Color.parseColor("#f4f5f9"))


        initView(binding.root)
        //loadDuet()
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DuetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    // 재개상태로 전환될때마다 필요한 초기화 작업 수행
//    override fun onPause() {
//        super.onPause()
//        Log.d("리플레이: ", "onPause")
//        duetList.clear()
//        loadDuet()
//    }

    fun initView(v: View){
        binding.fragmentDuetRecyclerView.layoutManager=LinearLayoutManager(context)
        binding.fragmentDuetRecyclerView.setHasFixedSize(true)
        duetAdapter= DuetAdapter(duetList)
        binding.fragmentDuetRecyclerView.adapter=duetAdapter
        loadDuet()
    }

    override fun onResume() {
        super.onResume()
        Log.d("리플레이: ", "onResume")
        duetList.clear()
        loadDuet()
    }

    private fun loadDuet() {
        retrofitService.requestDuet().enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e("DuetFragment", "loadDuet 통신 성공: ${response.body().toString()}")
                    val body = response.body().toString()
                    val replayObject = JSONObject(body)
                    val badgeList = replayObject.getString("badgeList")
                    val duetList = replayObject.getString("duetList")
                    val postArray = JSONArray(duetList)

                    for (i in 0 until postArray.length()) {
                        if (postArray.length() == 0 || postArray.equals("null")) {


                        } else {
                            val iObject = postArray.getJSONObject(i)
                            val duet_idx = iObject.getInt("duet_idx")
                            val mr_idx = iObject.getInt("mr_idx")
                            val thumbnail = iObject.getString("thumbnail")
                            val title = iObject.getString("title")
                            val singer = iObject.getString("singer")
                            val cnt_play = iObject.getString("cnt_play")
                            val cnt_reply = iObject.getString("cnt_reply")
                            val email = iObject.getString("email")
                            val nickname = iObject.getString("nickname")
                            val profile = iObject.getString("profile")
                            val cnt_duet = iObject.getString("cnt_duet")
                            val lyrics = iObject.getString("lyrics")
                            val song_path = iObject.getString("duet_path")
                            val mr_path = iObject.getString("song_path")
                            val extract_path = iObject.getString("extract_path")
                            val duet_date = iObject.getString("date")
                            val kinds = iObject.getString("kinds")
                            val token = iObject.getString("token")
                            var path = song_path

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

                            val duetData = DuetData(duet_idx, mr_idx, thumbnail, title, singer, cnt_play, cnt_reply, cnt_duet, email, nickname, profile, path, duet_date, mr_path, extract_path, kinds, lyrics, token, isBadge!!)
                            duetList.add(0,duetData)
                            duetAdapter.notifyDataSetChanged()

                            }

                        }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("DuetFragment", "loadDuet 통신 실패" + t.message)
            }


        })
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}