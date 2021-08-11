package com.harimi.singtogether.sing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDuetBinding
import org.json.JSONArray
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
        val binding=FragmentDuetBinding.inflate(inflater,container,false)

        binding.fragmentDuetRecyclerView.layoutManager=LinearLayoutManager(context)
        binding.fragmentDuetRecyclerView.setHasFixedSize(true)
        duetAdapter= DuetAdapter(duetList)
        binding.fragmentDuetRecyclerView.adapter=duetAdapter
        loadDuet()
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

    private fun loadDuet(){
        retrofitService.requestDuet().enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e("DuetFragment", "loadDuet 통신 성공: ${response.body().toString()}")

                    val jsonArray= JSONArray(response.body().toString())
                    for(i in 0..jsonArray.length() -1){
                        val iObject=jsonArray.getJSONObject(i)
                        val idx=iObject.getInt("idx")
                        val thumbnail=iObject.getString("thumbnail")
                        val title=iObject.getString("title")
                        val singer=iObject.getString("singer")
                        val cnt_play=iObject.getString("cnt_play")
                        val cnt_reply=iObject.getString("cnt_reply")
                        val nickname=iObject.getString("nickname")
                        val profile=iObject.getString("profile")
                        val cnt_duet=iObject.getString("cnt_duet")
                        val song_path=iObject.getString("song_path")

                        val duetData=DuetData(idx, thumbnail, title, singer, cnt_play, cnt_reply, cnt_duet, nickname, profile, song_path)
                        duetList.add(0,duetData)
                        duetAdapter.notifyDataSetChanged()
                    }

                } else {
                    // 통신은 성공했지만 응답에 문제가 있는 경우
                    Log.e("DuetFragment", "loadDuet 응답 문제" + response.code())
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