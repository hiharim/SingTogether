package com.harimi.singtogether

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.databinding.FragmentPostBinding
import com.harimi.singtogether.databinding.FragmentTotalBinding

/**
 * 인기순,최신순,팔로잉 전체보기로 볼수 있는 화면
 */
class TotalFragment : Fragment() {

    private lateinit var binding: FragmentTotalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTotalBinding.inflate(inflater,container,false)

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TotalFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}