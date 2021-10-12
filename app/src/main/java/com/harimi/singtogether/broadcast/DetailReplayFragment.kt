package com.harimi.singtogether.broadcast

import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import com.harimi.singtogether.databinding.FragmentDetailReplayBinding
import de.hdodenhof.circleimageview.CircleImageView


/**
 * 다시보기 방송 아이템 상세화면
 */
class DetailReplayFragment : Fragment() {

    private var TAG :String = "DETAILREPLAY_FRAGMENT "
    private var idx : String? = null
    private var thumbnail : String? = null
    private var uploadUserProfile : String? = null
    private var uploadUserNickName : String? = null
    private var uploadDate : String? = null
    private var replayTitle : String? = null
    private var replayLikeNumber : String? = null
    private var replayHits : String? = null
    private var replayReviewNumber : String? = null
    private var uploadUserEmail : String? = null

    private lateinit var fragment_detail_replay_iv_back : ImageView
    private lateinit var fragment_detail_replay_tv_title : TextView
    private lateinit var tv_hits : TextView
    private lateinit var iv_like : ImageView
    private lateinit var fragment_detail_replay_tv_like : TextView
    private lateinit var fragment_detail_replay_tv_date : TextView
    private lateinit var iv_uploadUserProfile : CircleImageView
    private lateinit var tv_UploadUserNickName : TextView
    private lateinit var tv_reviewNumber : TextView
    private lateinit var et_writeReview : EditText
    private lateinit var iv_uploadReview : ImageView
    private lateinit var fragment_post_recyclerView : RecyclerView
    private lateinit var exoplayerReplay : PlayerControlView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idx = it.getString("idx")
            thumbnail = it.getString("thumbnail")
            uploadUserProfile = it.getString("uploadUserProfile")
            uploadUserNickName = it.getString("uploadUserNickName")
            uploadDate = it.getString("uploadDate")
            replayTitle = it.getString("replayTitle")
            replayLikeNumber = it.getString("replayLikeNumber")
            replayHits = it.getString("replayHits")
            replayReviewNumber = it.getString("replayReviewNumber")
            uploadUserEmail = it.getString("uploadUserEmail")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        var detailFragmentView = inflater.inflate(R.layout.fragment_detail_replay, container, false)

        initView(detailFragmentView)
        setData()

        return detailFragmentView
    }

    fun initView(detailFragmentView : View){
        fragment_detail_replay_iv_back =detailFragmentView.findViewById(R.id.fragment_detail_replay_iv_back)
        fragment_detail_replay_tv_title =detailFragmentView.findViewById(R.id.fragment_detail_replay_tv_title)
        tv_hits =detailFragmentView.findViewById(R.id.tv_hits)
        iv_like =detailFragmentView.findViewById(R.id.iv_like)
        fragment_detail_replay_tv_like =detailFragmentView.findViewById(R.id.fragment_detail_replay_tv_like)
        fragment_detail_replay_tv_date =detailFragmentView.findViewById(R.id.fragment_detail_replay_tv_date)
        iv_uploadUserProfile =detailFragmentView.findViewById(R.id.iv_uploadUserProfile)
        tv_UploadUserNickName =detailFragmentView.findViewById(R.id.tv_UploadUserNickName)
        tv_reviewNumber =detailFragmentView.findViewById(R.id.tv_reviewNumber)
        et_writeReview =detailFragmentView.findViewById(R.id.et_writeReview)
        iv_uploadReview =detailFragmentView.findViewById(R.id.iv_uploadReview)
        fragment_post_recyclerView =detailFragmentView.findViewById(R.id.fragment_post_recyclerView)
        exoplayerReplay =detailFragmentView.findViewById(R.id.exoplayerReplay)


        /////뒤로가기
        fragment_detail_replay_iv_back.setOnClickListener {
//            var replayfragment = ReplayFragment()
//            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.activity_main_frame,replayfragment).commit()
            requireActivity().finish()
        }
    }

    fun setData(){
        fragment_detail_replay_tv_title.text =replayTitle
        tv_hits.text = replayHits
        fragment_detail_replay_tv_like.text= replayLikeNumber
        fragment_detail_replay_tv_date.text = uploadDate
        tv_UploadUserNickName.text =uploadUserNickName
        tv_reviewNumber.text = replayReviewNumber

        if (uploadUserProfile.equals("null") || uploadUserProfile.equals("")) {
            iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Glide.with(this)
                .load("http://3.35.236.251/" + uploadUserProfile)
                .thumbnail(0.1f)
                .into(iv_uploadUserProfile)
        }
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