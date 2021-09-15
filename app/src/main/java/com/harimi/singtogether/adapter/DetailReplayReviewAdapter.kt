package com.harimi.singtogether.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.DetailReplayReviewData
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView

class DetailReplayReviewAdapter(val detailReplayReviewList: ArrayList<DetailReplayReviewData>, val context: Context) : RecyclerView.Adapter<DetailReplayReviewAdapter.DetailReplayReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailReplayReviewAdapter.DetailReplayReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_detail_replay_review, parent, false)
        return DetailReplayReviewAdapter.DetailReplayReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailReplayReviewAdapter.DetailReplayReviewViewHolder, position: Int) {

        if (detailReplayReviewList.get(position).uploadUserProfile.equals("null") || detailReplayReviewList.get(position).uploadUserProfile.equals("")) {
            holder.iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: DetailReplayReviewData = detailReplayReviewList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.uploadUserProfile)
                .into(holder.iv_uploadUserProfile)
        }

        holder.tv_uploadUserNickname.setText(detailReplayReviewList.get(position).uploadUserNickname)
        holder.tv_review.setText(detailReplayReviewList.get(position).review)
        holder.tv_uploadDate.setText(detailReplayReviewList.get(position).uploadDate)

        ///아이템 온 클릭 리스너
//        holder.itemView.setOnClickListener(object : View.OnClickListener{
//            override fun onClick(v: View?) {
//            }
//
//        })

    }

    override fun getItemCount(): Int {
        return detailReplayReviewList.size
    }


    class DetailReplayReviewViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        val iv_uploadUserProfile = itemView.findViewById<CircleImageView>(R.id.iv_uploadUserProfile)
        val tv_uploadUserNickname = itemView.findViewById<TextView>(R.id.tv_uploadUserNickname)
        val tv_review = itemView.findViewById<TextView>(R.id.tv_review)
        val tv_uploadDate = itemView.findViewById<TextView>(R.id.tv_uploadDate)

    }
}