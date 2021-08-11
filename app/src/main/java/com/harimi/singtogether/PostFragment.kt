package com.harimi.singtogether

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.log


class PostFragment : Fragment() {
    var TAG :String = "PostFragment "

    private var idx : String? = null
    private var hits : String? = null
    private var songTitle : String? = null
    private var singer : String? = null
    private var likeNumber : String? = null
    private var thumbnail : String? = null
    private var uploadUserProfile : String? = null
    private var uploadUserNickName : String? = null
    private var uploadDate : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idx = it.getString("idx")
            hits = it.getString("hits")
            songTitle = it.getString("songTitle")
            singer = it.getString("singer")
            likeNumber = it.getString("likeNumber")
            thumbnail = it.getString("thumbnail")
            uploadUserProfile = it.getString("uploadUserProfile")
            uploadUserNickName = it.getString("uploadUserNickName")
            uploadDate = it.getString("uploadDate")

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val postView = inflater.inflate(R.layout.fragment_post, container, false)

        val tv_songTitle :TextView = postView.findViewById(R.id.tv_songTitle)
        val tv_singer :TextView = postView.findViewById(R.id.tv_singer)
        val tv_hits :TextView = postView.findViewById(R.id.tv_hits)
        val tv_uploadDate :TextView = postView.findViewById(R.id.tv_uploadDate)
        val tv_reviewNumber :TextView = postView.findViewById(R.id.tv_reviewNumber)
        val iv_mic :ImageView = postView.findViewById(R.id.iv_mic)
        val et_writeReview :EditText = postView.findViewById(R.id.et_writeReview)
        val iv_uploadReview :ImageView = postView.findViewById(R.id.iv_uploadReview)
        val tv_UploadUserNickName :TextView = postView.findViewById(R.id.tv_UploadUserNickName)
        val iv_uploadUserProfile : CircleImageView = postView.findViewById(R.id.iv_uploadUserProfile)

        if (uploadUserProfile.equals("null") || uploadUserProfile.equals("")) {
            iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Glide.with(requireActivity())
                .load("http://3.35.236.251/" + uploadUserProfile)
                .override(100, 75)
                .into(iv_uploadUserProfile)
        }

        tv_songTitle.text =songTitle.toString()
        tv_singer.text =singer.toString()
        tv_uploadDate.text =uploadDate.toString()
        tv_hits.text =hits.toString()
        tv_UploadUserNickName.text =uploadUserNickName.toString()


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