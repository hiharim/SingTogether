package com.harimi.singtogether.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.MainActivity
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R

class HomeAdapter(val homePostList: ArrayList<HomeData> ,val context: Context ) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_home, parent, false)
        return  HomeViewHolder(view)
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

    override fun onBindViewHolder(holder: HomeAdapter.HomeViewHolder, position: Int) {

        /// 프로필 사진
        if (homePostList.get(position).profile.equals("null") || homePostList.get(position).profile.equals("")) {
            holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: HomeData = homePostList.get(position)
            Glide.with(holder.itemView.context)
                    .load("http://3.35.236.251/" + Image.profile)
                    .override(100, 75)
                    .into(holder.iv_profile)
        }

        ///썸네일
        if (homePostList.get(position).thumbnail.equals("null") || homePostList.get(position).thumbnail.equals("")) {
            holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: HomeData = homePostList.get(position)
            Glide.with(holder.itemView.context)
                    .load("http://3.35.236.251/" + Image.thumbnail)
                    .override(100, 75)
                    .into(holder.iv_thumbnail)
        }

        holder.tv_hits.setText(homePostList.get(position).hits)
        holder.tv_songTitle.setText(homePostList.get(position).songTitle)
        holder.tv_singer.setText(homePostList.get(position).singer)
        holder.tv_likeNumber.setText(homePostList.get(position).likeNumber)
        holder.tv_nickName.setText(homePostList.get(position).nickName)


        ///아이템 온 클릭 리스너
        holder.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {

                var bundle: Bundle = Bundle()
                bundle.putString("hits",homePostList.get(position).hits)
                bundle.putString("songTitle",homePostList.get(position).songTitle)
                bundle.putString("singer",homePostList.get(position).singer)
                bundle.putString("likeNumber",homePostList.get(position).likeNumber)
                bundle.putString("thumbnail",homePostList.get(position).thumbnail)
                bundle.putString("profile",homePostList.get(position).profile)

                val activity =v!!.context as AppCompatActivity
                val postFragment = PostFragment()
                postFragment.arguments=bundle
                activity.supportFragmentManager.beginTransaction().replace(R.id.activity_main_frame,postFragment).addToBackStack(null).commit()


            }


        })

    }

    override fun getItemCount(): Int {
        return homePostList.size
    }


    class HomeViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val iv_thumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail) //썸네일
        val iv_profile = itemView.findViewById<ImageView>(R.id.iv_profile) // 프로필 사진
        val iv_like = itemView.findViewById<ImageView>(R.id.iv_like) // 좋아요
        val iv_nonLike = itemView.findViewById<ImageView>(R.id.iv_nonLike) // 좋아요 취소

        val tv_songTitle = itemView.findViewById<TextView>(R.id.tv_songTitle) // 노래제목
        val tv_singer = itemView.findViewById<TextView>(R.id.tv_singer) // 가수
        val tv_hits = itemView.findViewById<TextView>(R.id.tv_hits) // 조회수
        val tv_likeNumber = itemView.findViewById<TextView>(R.id.tv_likeNumber) // 좋아요 개수
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName) // 닉네임

    }
}
