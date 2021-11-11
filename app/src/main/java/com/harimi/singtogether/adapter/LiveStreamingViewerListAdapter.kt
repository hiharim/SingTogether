package com.harimi.singtogether.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.LiveStreamingViewerListData
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.SignalingClient
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get
import de.hdodenhof.circleimageview.CircleImageView

class LiveStreamingViewerListAdapter (val liveStreamingViewerList: ArrayList<LiveStreamingViewerListData>, val context: Context)
    : RecyclerView.Adapter<LiveStreamingViewerListAdapter.LiveStreamingViewerListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveStreamingViewerListAdapter.LiveStreamingViewerListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_viewer_list, parent, false)
        return LiveStreamingViewerListAdapter.LiveStreamingViewerListViewHolder(view)
//                }
    }

    override fun onBindViewHolder(holder: LiveStreamingViewerListAdapter.LiveStreamingViewerListViewHolder, position: Int) {

        /// 프로필 사진
        if (liveStreamingViewerList.get(position).profile.equals("null") || liveStreamingViewerList.get(position).profile.equals("")) {
            holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: LiveStreamingViewerListData = liveStreamingViewerList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.profile)
                .override(100, 75)
                .into(holder.iv_profile)
        }

        holder.tv_nickName.setText(liveStreamingViewerList.get(position).nickName)

        holder.iv_viewerOut.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("강퇴")
            builder.setMessage("시청자를 강제퇴장시키시겠습니까? ")

            builder.setPositiveButton("네") { dialog, which ->
                Toast.makeText(context,
                    "강퇴시켰습니다", Toast.LENGTH_SHORT).show()

                get()!!.viewerOutOfHere(liveStreamingViewerList.get(position).socketId)
                liveStreamingViewerList.removeAt(position)

                notifyItemRemoved(position)
                notifyDataSetChanged()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
                return@setNegativeButton
            }
            builder.show()

        }

    }

    override fun getItemCount(): Int {
        return liveStreamingViewerList.size
    }


    class LiveStreamingViewerListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val iv_profile = itemView.findViewById<CircleImageView>(R.id.iv_profile)
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName)
        val iv_viewerOut = itemView.findViewById<ImageView>(R.id.iv_viewerOut)

    }
}
