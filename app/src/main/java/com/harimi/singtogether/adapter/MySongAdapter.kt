package com.harimi.singtogether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.R


/***
 *  내 노래 어댑터 - MySongFragment
 */

class MySongAdapter (val mySongList : ArrayList<MySongData>) : RecyclerView.Adapter<MySongAdapter.MySongViewHolder>(){

    inner class MySongViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val idx=v.findViewById<TextView>(R.id.rv_my_song_tv_idx_duet)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_my_song_iv)
        val title=v.findViewById<TextView>(R.id.rv_my_song_tv_title)
        val date=v.findViewById<TextView>(R.id.rv_my_song_tv_date)
        val nickname=v.findViewById<TextView>(R.id.rv_my_song_tv_nickname)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MySongViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_my_song,parent,false)

        return MySongViewHolder(view)
    }

    override fun onBindViewHolder(holder: MySongViewHolder, position: Int) {
        val curData = mySongList[position]
        holder.idx.text= curData.idx.toString()
        holder.title.text=curData.title
        holder.nickname.text=curData.nickname
        holder.date.text=curData.date
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        holder.itemView.setOnClickListener { v->
            // 디테일듀엣프래그먼트로 이동
        }
    }

    override fun getItemCount()=mySongList.size

}