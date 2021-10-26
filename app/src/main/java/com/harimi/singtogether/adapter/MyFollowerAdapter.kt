package com.harimi.singtogether.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.MyFollowData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView

class MyFollowerAdapter  (val myFollowDataList: ArrayList<MyFollowData>, val context: Context) : RecyclerView.Adapter<MyFollowerAdapter.MyFollowerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyFollowerAdapter.MyFollowerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_follwer, parent, false)
        return MyFollowerAdapter.MyFollowerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyFollowerAdapter.MyFollowerViewHolder, position: Int
    ) {


        // 프로필 사진
        if (myFollowDataList.get(position).uploadUserProfile.equals("null") || myFollowDataList.get(position).uploadUserProfile.equals("")) {
            holder.iv_profileImage.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: MyFollowData = myFollowDataList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.uploadUserProfile)
                .into(holder.iv_profileImage)
        }

        holder.tv_nickName.setText(myFollowDataList.get(position).uploadUserNickName)



        holder.btn_follow.setOnClickListener {

        }

    }


    override fun getItemCount(): Int {
        return myFollowDataList.size
    }


    class MyFollowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iv_profileImage = itemView.findViewById<CircleImageView>(R.id.iv_profileImage)
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName)
        val btn_follow = itemView.findViewById<Button>(R.id.btn_follow)

    }
}