package com.harimi.singtogether.broadcast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import com.harimi.singtogether.databinding.FragmentDetailReplayBinding


/**
 * 다시보기 방송 아이템 상세화면
 */
class DetailReplayFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentDetailReplayBinding.inflate(inflater,container,false)

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailReplayFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}