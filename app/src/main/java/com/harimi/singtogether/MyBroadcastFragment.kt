package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.MyBroadcastData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.MyBroadcastAdapter
import com.harimi.singtogether.adapter.MySongAdapter
import com.harimi.singtogether.databinding.FragmentMyBroadcastBinding
import com.harimi.singtogether.databinding.FragmentMySongBinding
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * 마이페이지 ' 내 방송' 프래그먼트
 */
class MyBroadcastFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val myBroadcastList : ArrayList<MyBroadcastData> = ArrayList()
    private lateinit var myBroadcastAdapter: MyBroadcastAdapter

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
        myBroadcastAdapter= MyBroadcastAdapter(myBroadcastList)
        binding.fragmentMyBroadcastRecyclerView.adapter=myBroadcastAdapter
        loadMyBroadcast()

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyBroadcastFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun loadMyBroadcast() {
        val nickname=LoginActivity.user_info.loginUserNickname
        retrofitService.requestMyBroadcast().enqueue(object : Callback<String> {
        //retrofitService.requestMyBroadcast(nickname).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e("MyBroadcastFragment", " 통신 성공: ${response.body().toString()}")

                    val jsonArray= JSONArray(response.body().toString())
                    for(i in 0..jsonArray.length() -1){
                        val iObject=jsonArray.getJSONObject(i)
                        val idx=iObject.getInt("idx")
                        val thumbnail=iObject.getString("thumbnail")
                        val title=iObject.getString("replayTitle")
                        val hit=iObject.getString("replayHits")
                        val date=iObject.getString("uploadDate")

                        val myBroadcastData= MyBroadcastData(idx, thumbnail, title,hit,date)
                        myBroadcastList.add(0,myBroadcastData)
                        myBroadcastAdapter.notifyDataSetChanged()
                    }

                } else {
                    // 통신은 성공했지만 응답에 문제가 있는 경우
                    Log.e("MyBroadcastFragment", " 응답 문제" + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("MyBroadcastFragment", " 통신 실패" + t.message)
            }


        })
    }

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}