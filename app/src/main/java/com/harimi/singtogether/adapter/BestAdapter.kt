package com.harimi.singtogether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.BestData
import com.harimi.singtogether.R

/***
 * 오늘의 가왕 어댑터
 */

class BestAdapter(val bestList: ArrayList<BestData> ) : RecyclerView.Adapter<BestAdapter.BestViewHolder>(){

    inner class BestViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val idx=v.findViewById<TextView>(R.id.rv_best_singer_tv_idx)
        val mr_idx=v.findViewById<TextView>(R.id.rv_best_singer_tv_mr_idx)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_best_singer_iv_thumbnail)
        val title=v.findViewById<TextView>(R.id.rv_best_singer_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_best_singer_tv_singer)
        val profile=v.findViewById<ImageView>(R.id.rv_best_singer_iv_profile)
        val and=v.findViewById<TextView>(R.id.best_and)
        val collabo_profile=v.findViewById<ImageView>(R.id.rv_best_singer_iv_collabo_profile)
        val collabo_nickname=v.findViewById<TextView>(R.id.rv_best_singer_tv_collabo_nickname)
        val nickname=v.findViewById<TextView>(R.id.rv_best_singer_tv_nickname)
        val date=v.findViewById<TextView>(R.id.rv_best_singer_tv_date)
        val email=v.findViewById<TextView>(R.id.rv_best_singer_tv_email)
        val kinds=v.findViewById<TextView>(R.id.rv_best_singer_tv_kinds)
        val token=v.findViewById<TextView>(R.id.rv_best_singer_tv_token)
        val cnt_play=v.findViewById<TextView>(R.id.rv_best_singer_tv_count_play)
        val cnt_reply=v.findViewById<TextView>(R.id.rv_best_singer_tv_count_reply)
        val cnt_like=v.findViewById<TextView>(R.id.rv_best_singer_tv_like)
        val song_path=v.findViewById<TextView>(R.id.rv_best_singer_tv_song_path)
        val collabo_email=v.findViewById<TextView>(R.id.rv_best_singer_tv_collabo_email)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestAdapter.BestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_best_singer, parent, false)
        return  BestViewHolder(view)
    }

    override fun onBindViewHolder(holder: BestAdapter.BestViewHolder, position: Int) {
        val curData = bestList[position]
        holder.idx.text= curData.idx.toString()
        holder.mr_idx.text= curData.mr_idx.toString()
        holder.title.text=curData.title
        holder.singer.text=curData.singer
        holder.cnt_play.text=curData.cnt_play
        holder.cnt_reply.text=curData.cnt_reply
        holder.cnt_like.text=curData.cnt_like
        holder.nickname.text=curData.nickname
        holder.email.text=curData.email
        holder.song_path.text=curData.song_path
        holder.collabo_nickname.text=curData.collaboration_nickname
        holder.collabo_email.text=curData.collabo_email
        holder.date.text=curData.date
        holder.kinds.text=curData.kinds
        holder.token.text=curData.token

        Glide.with(holder.itemView).load("http://3.35.236.251/"+curData.profile).into(holder.profile)
        Glide.with(holder.itemView).load("http://3.35.236.251/"+curData.collaboration_profile)
            .circleCrop()
            .into(holder.collabo_profile)
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        if(holder.email.text.equals(holder.collabo_email.text)){
            holder.collabo_profile.visibility=View.GONE
            holder.collabo_nickname.visibility=View.GONE
            holder.and.visibility=View.GONE
        }




    }

    override fun getItemCount(): Int {
        return bestList.size
    }
}