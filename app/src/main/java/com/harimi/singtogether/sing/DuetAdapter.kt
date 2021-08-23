package com.harimi.singtogether.sing

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
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.PostFragment
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

        Glide.with(holder.itemView).load(curData.profile).into(holder.profile)
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        holder.itemView.setOnClickListener { v->
            // DetailDuetFragment 로 이동
            val activity =v!!.context as AppCompatActivity
            val detailDuetFragment = DetailDuetFragment()
            var bundle =Bundle()
            bundle.putInt("idx",curData.idx)
            bundle.putString("title",curData.title)
            bundle.putString("singer",curData.singer)
            bundle.putString("cnt_play",curData.cnt_play)
            bundle.putString("cnt_reply",curData.cnt_reply)
            bundle.putString("cnt_duet",curData.cnt_duet)
            bundle.putString("nickname",curData.nickname)
            bundle.putString("duet_path",curData.duet_path)
            bundle.putString("profile",curData.profile)
            detailDuetFragment.arguments=bundle


            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_frame,detailDuetFragment)
                .addToBackStack(null)
                .commit()

        }
    }

    override fun getItemCount()=duetList.size


}