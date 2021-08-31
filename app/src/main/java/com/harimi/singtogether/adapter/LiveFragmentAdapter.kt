package com.harimi.singtogether.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.LiveStreamingViewActivity
import de.hdodenhof.circleimageview.CircleImageView

class LiveFragmentAdapter (val LiveStreamingPostList: ArrayList<LiveFragmentData>, val context: Context) : RecyclerView.Adapter<LiveFragmentAdapter.LiveFragmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveFragmentAdapter.LiveFragmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_live, parent, false)
        return  LiveFragmentViewHolder(view)
//                .apply {
//                    itemView.setOnClickListener {
////                        val getPosition : Int = adapterPosition
////                        val homeData :HomeData = homePostList.get(getPosition)
//
//                        val activity =view!!.context as AppCompatActivity
//                        val postFragment = PostFragment()
//                        activity.supportFragmentManager.beginTransaction().replace(R.id.fragment_home_recyclerView,postFragment).addToBackStack(null).commit()
//
//                    }
//                }
    }

    override fun onBindViewHolder(holder: LiveFragmentAdapter.LiveFragmentViewHolder, position: Int) {

        // 프로필 사진
        if (LiveStreamingPostList.get(position).profile.equals("null") || LiveStreamingPostList.get(position).profile.equals("")) {
            holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: LiveFragmentData = LiveStreamingPostList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.profile)
//                .override(100, 75)
                .into(holder.iv_profile)
        }

        ///썸네일
        if (LiveStreamingPostList.get(position).thumbnail.equals("null") || LiveStreamingPostList.get(position).thumbnail.equals("")) {
            holder.iv_thumbnail.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: LiveFragmentData = LiveStreamingPostList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.thumbnail)
//                .override(100, 75)
                .into(holder.iv_thumbnail)
        }

        holder.tv_liveTitle.setText(LiveStreamingPostList.get(position).title)
        holder.tv_viewer.setText("0")
        holder.tv_nickName.setText(LiveStreamingPostList.get(position).nickName)


        ///아이템 온 클릭 리스너
        holder.itemView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {

//                var bundle: Bundle = Bundle()
//                bundle.putString("idx",LiveStreamingPostList.get(position).idx)
//
//                val activity =v!!.context as AppCompatActivity
//                val postFragment = PostFragment()
//                postFragment.arguments=bundle
//                activity.supportFragmentManager.beginTransaction().replace(R.id.activity_main_frame,postFragment).addToBackStack(null).commit()
                val intent = Intent(context,LiveStreamingViewActivity::class.java)
                intent.putExtra("roomIdx",LiveStreamingPostList.get(position).idx)
                ContextCompat.startActivity(context,intent,null)

            }


        })

    }

    override fun getItemCount(): Int {
        return LiveStreamingPostList.size
    }


    class LiveFragmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val iv_thumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail) //썸네일
        val iv_profile = itemView.findViewById<CircleImageView>(R.id.iv_profile) // 프로필 사진
        val tv_liveTitle = itemView.findViewById<TextView>(R.id.tv_liveTitle) // 좋아요
        val tv_viewer = itemView.findViewById<TextView>(R.id.tv_viewer) // 좋아요 취소
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName) // 노래제목
    }
}
