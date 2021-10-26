package com.harimi.singtogether.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView

class MyFollowingAdapter  (val myFollowingDataList: ArrayList<MyFollowingData>, val context: Context) : RecyclerView.Adapter<MyFollowingAdapter.MyFollowingViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFollowingAdapter.MyFollowingViewHolder{

        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_following, parent, false)
        return MyFollowingAdapter.MyFollowingViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyFollowingAdapter.MyFollowingViewHolder, position: Int) {


        // 프로필 사진
        if (myFollowingDataList.get(position).uploadUserProfile.equals("null") || myFollowingDataList.get(position).uploadUserProfile.equals("")) {
            holder.iv_profileImage.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: MyFollowingData = myFollowingDataList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.uploadUserProfile)
                .into(holder.iv_profileImage)
        }

        holder.tv_nickName.setText(myFollowingDataList.get(position).uploadUserNickName)



        holder.btn_followCancel.setOnClickListener {

        }

    }


    override fun getItemCount(): Int {
        return myFollowingDataList.size
    }


    class MyFollowingViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        val iv_profileImage = itemView.findViewById<CircleImageView>(R.id.iv_profileImage)
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName)
        val btn_followCancel = itemView.findViewById<Button>(R.id.btn_followCancel)

    }
}