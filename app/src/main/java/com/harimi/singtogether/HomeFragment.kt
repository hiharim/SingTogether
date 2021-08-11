package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.HomeAdapter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {
    private var TAG :String = "HOME ACTIVITY"

    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    val homePostList: ArrayList<HomeData> = ArrayList()

    lateinit var fragment_home_recyclerView : RecyclerView
    lateinit var homeAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var homeView = inflater.inflate(R.layout.fragment_home, container, false)


        fragment_home_recyclerView = homeView.findViewById(R.id.fragment_home_recyclerView)
        fragment_home_recyclerView.layoutManager = LinearLayoutManager(activity)
        fragment_home_recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        homeAdapter = HomeAdapter(homePostList,requireContext())
        fragment_home_recyclerView.adapter = homeAdapter

        homePostLoad ()

        return  homeView

    }


fun homePostLoad (){

    retrofit= RetrofitClient.getInstance()
    retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetHomePost()
                .enqueue(object : Callback<String> {
                    override fun onResponse(
                            call: Call<String>,
                            response: Response<String>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d("getHomePost: ", body)
                            homePostList.clear()

                            val jsonArray = JSONArray(body)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val idx = jsonObject.getString("idx")
                                val thumbnail = jsonObject.getString("thumbnail")
                                val songTitle = jsonObject.getString("songTitle")
                                val singer = jsonObject.getString("singer")
                                val hit = jsonObject.getString("hit")
                                val like = jsonObject.getString("like")
                                val profile = jsonObject.getString("profile")
                                val nickName = jsonObject.getString("nickName")
                                val uploadDate = jsonObject.getString("uploadDate")
                                Log.d(TAG, "idx($i): $idx")
                                Log.d(TAG, "songTitle($i): $songTitle")
                                Log.d(TAG, "singer($i): $singer")

                                val homeData = HomeData(thumbnail, songTitle, singer, hit, like, profile, nickName,uploadDate)
                                homePostList.add(0, homeData)
                                homeAdapter.notifyDataSetChanged()
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
                HomeFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }


}