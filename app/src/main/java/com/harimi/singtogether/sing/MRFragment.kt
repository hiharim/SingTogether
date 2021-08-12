package com.harimi.singtogether.sing

import android.graphics.Color
import android.graphics.drawable.ClipDrawable.VERTICAL
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentMRBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.math.sin


/**
 * 노래부르기화면 에서 mr 프래그먼트 화면
 */
class MRFragment : Fragment() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private val mrList : ArrayList<MRData> = ArrayList()
    private lateinit var mrAdapter: MRAdapter

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
        val binding= FragmentMRBinding.inflate(inflater, container, false)
        binding.mrBack.setBackgroundColor(Color.parseColor("#f4f5f9"))
        binding.genreBack.setBackgroundColor(Color.parseColor("#ffffff"))
        //리사이클러뷰 설정
        binding.fragmentMRRecyclerView.layoutManager=LinearLayoutManager(context)
        binding.fragmentMRRecyclerView.setHasFixedSize(true)
        binding.fragmentMRRecyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        mrAdapter= MRAdapter(mrList)
        binding.fragmentMRRecyclerView.adapter=mrAdapter

        // 스피너 설정
        var sData=resources.getStringArray(R.array.genre)
        var sAdapter=ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,sData)
        binding.spinner.adapter=sAdapter
        binding.spinner.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        loadMR()

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MRFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    // 레트로핏으로 MR 데이터 받아오기
    private fun loadMR() {
        retrofitService.requestMR().enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 응답을 잘 받은 경우
                    Log.e("MRFragment", "getMR 통신 성공: ${response.body().toString()}")

                    val jsonArray=JSONArray(response.body().toString())
                    for(i in 0..jsonArray.length() -1){
                        val iObject=jsonArray.getJSONObject(i)
                        val idx=iObject.getInt("idx")
                        val title=iObject.getString("title")
                        val singer=iObject.getString("singer")
                        val song_path=iObject.getString("song_path")
                        val genre=iObject.getString("genre")

                        val mrData=MRData(idx,title, singer,song_path,genre)
                        mrList.add(0,mrData)
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

    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}