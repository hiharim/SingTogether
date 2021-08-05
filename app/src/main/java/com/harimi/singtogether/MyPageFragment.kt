package com.harimi.singtogether

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.databinding.FragmentMyPageBinding


class MyPageFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. 뷰 바인딩 설정
        val binding=FragmentMyPageBinding.inflate(inflater,container,false)

        // 2. 바인딩으로 TextView 등에 접근
        // 사용자 닉네임
        binding.fragmentMyPageTvNickname.text="닉네임"

        // 3. 프래그먼트 레이아웃 뷰 반환
        return binding.root
    }

    companion object {

        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}