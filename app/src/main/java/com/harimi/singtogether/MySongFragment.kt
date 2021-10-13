package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MySongAdapter
import com.harimi.singtogether.databinding.FragmentDuetBinding
import com.harimi.singtogether.databinding.FragmentMySongBinding
import com.harimi.singtogether.sing.DuetAdapter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * 마이페이지 '내 노래' 프래그먼트
 */
class MySongFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val mySongList : ArrayList<MySongData> = ArrayList()
    private lateinit var mySongAdapter: MySongAdapter

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
        val binding= FragmentMySongBinding.inflate(inflater,container,false)

        binding.fragmentMySongRecyclerView.layoutManager= LinearLayoutManager(context)
        binding.fragmentMySongRecyclerView.setHasFixedSize(true)
        binding.fragmentMySongRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        mySongAdapter= MySongAdapter(mySongList)
        binding.fragmentMySongRecyclerView.adapter=mySongAdapter
        loadMySong()

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

//    private fun loadMySong() {
//        val nickname=LoginActivity.user_info.loginUserNickname
//        retrofitService.requestMySong(nickname).enqueue(object : Callback<String> {
//            // 통신에 성공한 경우
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                if (response.isSuccessful) {
//                    // 응답을 잘 받은 경우
//                    Log.e("MySongFragment", " 통신 성공: ${response.body().toString()}")
//
//                    val jsonArray= JSONArray(response.body().toString())
//                    for(i in 0..jsonArray.length() -1){
//                        val iObject=jsonArray.getJSONObject(i)
//                        val idx=iObject.getInt("idx")
//                        val thumbnail=iObject.getString("thumbnail")
//                        val title=iObject.getString("title")
//                        val nickname=iObject.getString("nickname")
//                        val date=iObject.getString("date")
//
//                        val mySongData= MySongData(idx, thumbnail, title,nickname,date)
//                        mySongList.add(0,mySongData)
//                        mySongAdapter.notifyDataSetChanged()
//                    }
//
//                } else {
//                    // 통신은 성공했지만 응답에 문제가 있는 경우
//                    Log.e("MySongFragment", " 응답 문제" + response.code())
//                }
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Log.e("MySongFragment", " 통신 실패" + t.message)
//            }
//
//
//        })
//    }

    private fun loadMySong() {
        val nickname=LoginActivity.user_info.loginUserNickname
        retrofitService.requestMySong(nickname).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e("MySongFragment", " 통신 성공: ${response.body().toString()}")
                    val body = response.body().toString()

                    val jsonArray= JSONArray(body)
                    if (jsonArray.length() == 0 || jsonArray.equals("")) {

                    } else {

                        for (i in 0..jsonArray.length() - 1) {
                            val iObject = jsonArray.getJSONObject(i)
                            val idx = iObject.getInt("idx")
                            val thumbnail = iObject.getString("thumbnail")
                            val title = iObject.getString("title")
                            val nickname = iObject.getString("nickname")
                            val date = iObject.getString("date")

                            val mySongData = MySongData(idx, thumbnail, title, nickname, date)
                            mySongList.add(0, mySongData)
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