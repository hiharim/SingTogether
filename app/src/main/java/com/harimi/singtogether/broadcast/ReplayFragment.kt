package com.harimi.singtogether.broadcast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentReplayBinding

class ReplayFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var replayView = inflater.inflate(R.layout.fragment_replay, container, false)



        return replayView
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReplayFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}