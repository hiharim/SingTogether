package com.harimi.singtogether.broadcast

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentLiveBinding


/**
 실시간 방송 프래그먼트
 */
class LiveFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        Log.d("라이브: ", "onCreate")
    }

    override fun onPause() {
        super.onPause()
        Log.d("라이브: ", "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("라이브: ", "onResume")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding=FragmentLiveBinding.inflate(inflater,container,false)
        Log.d("라이브: ", "onCreateView")
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() =
                LiveFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}