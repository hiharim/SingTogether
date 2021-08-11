package com.harimi.singtogether.sing

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.R

class DuetAdapter(val duetList : ArrayList<DuetData>) : RecyclerView.Adapter<DuetAdapter.DuetViewHolder>() {

    inner class DuetViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val idx=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_idx)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_fragment_duet_iv_thumbnail)
        val title=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_singer)
        val profile=v.findViewById<ImageView>(R.id.rv_fragment_duet_iv_profile)
        val nickname=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_nickname)
        val cnt_play=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_count_play)
        val cnt_reply=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_count_reply)
        val cnt_duet=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_count_duet)
        val duet_path=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_duet_path)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuetViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_duet,parent,false)

        return DuetViewHolder(view)
    }

    override fun onBindViewHolder(holder: DuetViewHolder, position: Int) {
        val curData = duetList[position]
        holder.idx.text= curData.idx.toString()
        holder.title.text=curData.title
        holder.singer.text=curData.singer
        holder.cnt_play.text=curData.cnt_play
        holder.cnt_reply.text=curData.cnt_reply
        holder.cnt_duet.text=curData.cnt_duet
        holder.nickname.text=curData.nickname
        holder.duet_path.text=curData.duet_path
        // todo : 썸네일 Glide 로
        Glide.with(holder.itemView).load(curData.profile).into(holder.profile)

        holder.itemView.setOnClickListener { v->
            // BeforeSingActivity 로 이동
//            val intent= Intent(v.context,BeforeSingActivity::class.java)
//
//            ContextCompat.startActivity(v.context,intent,null)
        }
    }

    override fun getItemCount()=duetList.size


}