package com.harimi.singtogether.sing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentMRBinding
import com.harimi.singtogether.databinding.FragmentResultMRBinding
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 검색결과 mr 탭 화면
 */
class ResultMRFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val mrList : ArrayList<MRData> = ArrayList()
    private lateinit var mrAdapter: MRAdapter
    private var searchInput : String?=null // 검색어

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchInput=it.getString("searchInput")
            Log.e("ResultMRFragment" ,"검색어:" + searchInput)
        }
        // 서버 연결
        initRetrofit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentResultMRBinding.inflate(inflater, container, false)
        //리사이클러뷰 설정
        binding.fragmentResultMrRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentResultMrRecyclerView.setHasFixedSize(true)
        binding.fragmentResultMrRecyclerView.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        mrAdapter= MRAdapter(mrList)
        binding.fragmentResultMrRecyclerView.adapter=mrAdapter
        loadMR()

        return binding.root
    }

    // 레트로핏으로 MR 데이터 받아오기
    private fun loadMR() {
        val which="mr"
        searchInput?.let {
            retrofitService.loadSearchResult(it,which).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        Log.e("MRFragment", "getMR 통신 성공: ${response.body().toString()}")

                        val jsonArray = JSONArray(response.body().toString())
                        for (i in 0..jsonArray.length() - 1) {
                            val iObject = jsonArray.getJSONObject(i)
                            val idx = iObject.getInt("idx")
                            val title = iObject.getString("title")
                            val singer = iObject.getString("singer")
                            val song_path = iObject.getString("song_path")
                            val genre = iObject.getString("genre")
                            val lyrics = iObject.getString("lyrics")

                            val mrData = MRData(idx, title, singer, song_path, genre, lyrics)
                            mrList.add(0, mrData)
                            mrAdapter.notifyDataSetChanged()
                        }

                    } else {
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                        Log.e("MRFragment", "getMR 응답 문제" + response.code())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("MRFragment", "getMR 통신 실패" + t.message)
                }


            })
        }
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultMRFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}