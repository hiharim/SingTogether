package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    val homePostList : ArrayList<HomeData> = ArrayList()
    lateinit var fragment_home_recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var homeView = inflater.inflate(R.layout.fragment_home, container, false)

//        homePostList.add(HomeData("null","사랑","박효신","3","5","null","닉네임"))
        fragment_home_recyclerView = homeView.findViewById(R.id.fragment_home_recyclerView)as RecyclerView
        fragment_home_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fragment_home_recyclerView.adapter = HomeAdapter(homePostList)

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
//                        val jsonObject = JSONObject(body)
//                        val profile_image = jsonObject.getString("profile")
//                        Log.d("get_profile_image: ", profile_image)
                        val jsonArray = JSONArray(body)

                        for (i in 0 until jsonArray.length()) {

                            val jsonObject = jsonArray.getJSONObject(i)
                            val idx = jsonObject.getString("idx")
                            val thumbnail = jsonObject.getString("thumbnail")
                            val songTitle = jsonObject.getString("songTitle")
                            val singer = jsonObject.getBoolean("singer")
                            val hit = jsonObject.getBoolean("hit")
                            val like = jsonObject.getBoolean("like")
                            val proflie = jsonObject.getBoolean("proflie")
                            val nickName = jsonObject.getBoolean("nickName")

                            Log.d(TAG, "idx($i): $idx")
                            Log.d(TAG, "songTitle($i): $songTitle")
                            Log.d(TAG, "singer($i): $singer")
                        }
                    }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {

            }
        })
}


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


}