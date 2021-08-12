package com.harimi.singtogether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.MyBroadcastData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.R

class MyBroadcastAdapter (val myBroadcastList : ArrayList<MyBroadcastData>) : RecyclerView.Adapter<MyBroadcastAdapter.MyBroadcastViewHolder>() {

    inner class MyBroadcastViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val idx=v.findViewById<TextView>(R.id.rv_my_broadcast_tv_idx)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_my_broadcast_iv)
        val title=v.findViewById<TextView>(R.id.rv_my_broadcast_tv_title)
        val date=v.findViewById<TextView>(R.id.rv_my_broadcast_tv_date)
        val hit=v.findViewById<TextView>(R.id.rv_my_broadcast_tv_hit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBroadcastViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_my_broadcast,parent,false)

        return MyBroadcastViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBroadcastViewHolder, position: Int) {
        val curData = myBroadcastList[position]
        holder.idx.text= curData.idx.toString()
        holder.title.text=curData.title
        holder.hit.text=curData.hit
        holder.date.text=curData.date
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        holder.itemView.setOnClickListener { v->
            // 디테일 리플레이 프래그먼트로 이동
        }
    }

    override fun getItemCount()=myBroadcastList.size

}