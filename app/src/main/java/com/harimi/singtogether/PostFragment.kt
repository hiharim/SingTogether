package com.harimi.singtogether

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.harimi.singtogether.R
import kotlin.math.log


class PostFragment : Fragment() {
    var TAG :String = "PostFragment "

    private var hits : String? = null
    private var songTitle : String? = null
    private var singer : String? = null
    private var likeNumber : String? = null
    private var thumbnail : String? = null
    private var profile : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hits = it.getString("hits")
            songTitle = it.getString("songTitle")
            singer = it.getString("singer")
            likeNumber = it.getString("likeNumber")
            thumbnail = it.getString("thumbnail")
            profile = it.getString("profile")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val postView = inflater.inflate(R.layout.fragment_post, container, false)
        val tv : TextView = postView.findViewById(R.id.tv)
        tv.text = songTitle.toString()

        return postView
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PostFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}