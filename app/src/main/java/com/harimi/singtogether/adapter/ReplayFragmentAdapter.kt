package com.harimi.singtogether.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView

class ReplayFragmentAdapter(val ReplayPostList: ArrayList<ReplayData>, val context: Context) : RecyclerView.Adapter<ReplayFragmentAdapter.ReplayFragmentViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReplayFragmentAdapter.ReplayFragmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_replay, parent, false)
        return ReplayFragmentAdapter.ReplayFragmentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ReplayFragmentAdapter.ReplayFragmentViewHolder,
        position: Int
    ) {

    }

    override fun getItemCount(): Int {
        return ReplayPostList.size
    }

    class ReplayFragmentViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val iv_thumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail) //썸네일
        val iv_uploadUserProfile = itemView.findViewById<CircleImageView>(R.id.iv_uploadUserProfile) // 프로필 사진
        val tv_iv_uploadUserNickName = itemView.findViewById<TextView>(R.id.tv_iv_uploadUserNickName) // 닉네임
        val tv_replayLikeNumber = itemView.findViewById<ImageView>(R.id.tv_replayLikeNumber) // 좋아요
        val tv_replayReviewNumber = itemView.findViewById<TextView>(R.id.tv_replayReviewNumber) // 댓글 수
        val tv_replayTitle = itemView.findViewById<TextView>(R.id.tv_replayTitle) // 제목
        val tv_replayHits = itemView.findViewById<TextView>(R.id.tv_replayHits) // 조회수


    }

}