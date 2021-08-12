package com.harimi.singtogether.sing

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding


/**
 * 듀엣 아이템 상세화면 프래그먼트
 */
class DetailDuetFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentDetailDuetBinding.inflate(inflater,container,false)

        //듀엣참여 버튼 클릭
        binding.fragmentDetailDuetBtnJoin.setOnClickListener {
            // RecordActivity 로 이동
            val intent= Intent(context,RecordActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailDuetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}