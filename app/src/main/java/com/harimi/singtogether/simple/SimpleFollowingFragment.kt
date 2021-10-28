package com.harimi.singtogether.simple

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentPopBinding
import com.harimi.singtogether.databinding.FragmentSimpleFollowingBinding
import retrofit2.Retrofit

/**
 *
 */
class SimpleFollowingFragment : Fragment() {

    private var TAG :String = "SimpleFollowingFragment"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var binding: FragmentSimpleFollowingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=  FragmentSimpleFollowingBinding.inflate(inflater,container,false)
        // 서버 연결
        initRetrofit()
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SimpleFollowingFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}