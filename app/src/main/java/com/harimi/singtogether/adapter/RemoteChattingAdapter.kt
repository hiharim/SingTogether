package com.harimi.singtogether.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.LocalChattingData

import com.harimi.singtogether.Data.RemoteChattingData
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView

class RemoteChattingAdapter  (val remoteChattingList: ArrayList<RemoteChattingData>, val context: Context) : RecyclerView.Adapter<RemoteChattingAdapter.RemoteChattingViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemoteChattingAdapter.RemoteChattingViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_local_chatting, parent, false)
        return RemoteChattingAdapter.RemoteChattingViewHolder(view)
    }


    override fun onBindViewHolder(
        holder: RemoteChattingAdapter.RemoteChattingViewHolder, position: Int) {


        if (remoteChattingList.get(position).profile.equals("null") || remoteChattingList.get(position).profile.equals("")) {
            holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: RemoteChattingData = remoteChattingList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.profile)
                .into(holder.iv_profile)
        }

        holder.tv_nickName.setText(remoteChattingList.get(position).nickName)
        holder.et_chattingText.setText(remoteChattingList.get(position).chattingText)

    }


    override fun getItemCount(): Int {
        return remoteChattingList.size
    }


    class RemoteChattingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val iv_profile = itemView.findViewById<CircleImageView>(R.id.iv_profile)
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName)
        val et_chattingText = itemView.findViewById<EditText>(R.id.et_chattingText)

    }
}