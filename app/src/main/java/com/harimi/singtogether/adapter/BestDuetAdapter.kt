package com.harimi.singtogether.adapter

import android.R.attr.fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.BestDuetData
import com.harimi.singtogether.MainActivity
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.BestDuetAdapter.BestDuetViewHolder
import de.hdodenhof.circleimageview.CircleImageView


class BestDuetAdapter(val bestDuetList: ArrayList<BestDuetData>, val context: Context) : RecyclerView.Adapter<BestDuetViewHolder>(){

    inner class BestDuetViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val idx=v.findViewById<TextView>(R.id.rv_fragment_home_tv_idx)
        val mr_idx=v.findViewById<TextView>(R.id.rv_fragment_home_tv_mr_idx)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_fragment_home_iv_thumbnail)
        val title=v.findViewById<TextView>(R.id.rv_fragment_home_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_fragment_home_tv_singer)
        val profile=v.findViewById<CircleImageView>(R.id.rv_fragment_home_iv_profile)
        val and=v.findViewById<TextView>(R.id.and)
        val collabo_profile=v.findViewById<CircleImageView>(R.id.rv_fragment_home_iv_collabo_profile)
        val collabo_nickname=v.findViewById<TextView>(R.id.rv_fragment_home_tv_collabo_nickname)
        val nickname=v.findViewById<TextView>(R.id.rv_fragment_home_tv_nickname)
        val date=v.findViewById<TextView>(R.id.rv_fragment_home_tv_date)
        val email=v.findViewById<TextView>(R.id.rv_fragment_home_tv_email)
        val kinds=v.findViewById<TextView>(R.id.rv_fragment_home_tv_kinds)
        val token=v.findViewById<TextView>(R.id.rv_fragment_home_tv_token)
        val cnt_play=v.findViewById<TextView>(R.id.rv_fragment_home_tv_count_play)
        val cnt_reply=v.findViewById<TextView>(R.id.rv_fragment_home_tv_count_reply)
        val cnt_like=v.findViewById<TextView>(R.id.rv_fragment_home_tv_like)
        val song_path=v.findViewById<TextView>(R.id.rv_fragment_home_tv_song_path)
        val collabo_email=v.findViewById<TextView>(R.id.rv_fragment_home_tv_collabo_email)
//        val collaboCardView=v.findViewById<CardView>(R.id.collaboCardView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestDuetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_best_duet_and_solo,
            parent,
            false
        )
        return BestDuetViewHolder(view)
    }
    override fun onBindViewHolder(holder: BestDuetViewHolder, position: Int) {

        val curData = bestDuetList[position]
        holder.idx.text= curData.idx.toString()
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
        holder.mr_idx.text= curData.mr_idx.toString()

        Glide.with(holder.itemView).load("http://3.35.236.251/" + curData.profile).into(holder.profile)
        Glide.with(holder.itemView).load("http://3.35.236.251/" + curData.collaboration_profile).into(
            holder.collabo_profile
        )
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        if(holder.email.text.equals(holder.collabo_email.text)){
            holder.collabo_profile.visibility=View.GONE
            holder.collabo_nickname.visibility=View.GONE
            holder.and.visibility=View.GONE
//            holder.collaboCardView.visibility=View.GONE
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)

            intent.putExtra("GloryPost","GloryPost")
            val bundle = Bundle()
            bundle.putInt("idx", curData.idx)
            bundle.putInt("mr_idx", curData.mr_idx)
            bundle.putString("title", curData.title)
            bundle.putString("singer", curData.singer)
            bundle.putString("cnt_play", curData.cnt_play)
            bundle.putString("cnt_reply", curData.cnt_reply)
            bundle.putString("cnt_like", curData.cnt_like)
            bundle.putString("nickname", curData.nickname)
            bundle.putString("email", curData.email)
            bundle.putString("collaboration_nickname", curData.collaboration_nickname)
            bundle.putString("song_path", curData.song_path)
            bundle.putString("profile", curData.profile)
            bundle.putString("collaboration_profile", curData.collaboration_profile)
            bundle.putString("collabo_email", curData.collabo_email)
            bundle.putString("date", curData.date)
            bundle.putString("kinds", curData.kinds)
            bundle.putString("token", curData.token)
            bundle.putString("thumbnail", curData.thumbnail)
            intent.putExtra("bundle",bundle)
            context.startActivity(intent)


        }

    }

    override fun getItemCount(): Int {
        return bestDuetList.size
    }
}