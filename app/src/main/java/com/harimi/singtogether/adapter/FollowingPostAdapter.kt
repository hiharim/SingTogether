package com.harimi.singtogether.adapter

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.DetailReplayReviewData
import com.harimi.singtogether.Data.FollowingPostData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.ArrayList

class FollowingPostAdapter ( val followingPostDataList: ArrayList<FollowingPostData>, val context: Context
) : RecyclerView.Adapter<FollowingPostAdapter.FollowingPostViewHolder>() {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingPostAdapter.FollowingPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_fragment_my_following_post,
            parent,
            false
        )
        return FollowingPostAdapter.FollowingPostViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FollowingPostAdapter.FollowingPostViewHolder,
        position: Int
    ) {
        holder.idx.text= followingPostDataList.get(position).idx.toString()
        holder.title.text=followingPostDataList.get(position).title
        holder.singer.text=followingPostDataList.get(position).singer
        holder.cnt_play.text=followingPostDataList.get(position).cnt_play
        holder.cnt_reply.text=followingPostDataList.get(position).cnt_reply
        holder.cnt_like.text=followingPostDataList.get(position).cnt_like
        holder.nickname.text=followingPostDataList.get(position).nickname
        holder.email.text=followingPostDataList.get(position).email
        holder.song_path.text=followingPostDataList.get(position).song_path
        holder.collabo_nickname.text=followingPostDataList.get(position).collaboration_nickname
        holder.collabo_email.text=followingPostDataList.get(position).collabo_email
        holder.date.text=followingPostDataList.get(position).date
        holder.kinds.text=followingPostDataList.get(position).kinds
        holder.token.text=followingPostDataList.get(position).token
        holder.mr_idx.text= followingPostDataList.get(position).mr_idx.toString()


        if (followingPostDataList.get(position).profile.equals("null") || followingPostDataList.get(position).profile.equals("")) {
            holder.profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: FollowingPostData = followingPostDataList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.profile)
                .into(holder.profile)
        }

        if (followingPostDataList.get(position).collaboration_profile.equals("null") || followingPostDataList.get(position).collaboration_profile.equals("")) {
            holder.collabo_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: FollowingPostData = followingPostDataList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.collaboration_profile)
                .into(holder.collabo_profile)
        }

        Glide.with(holder.itemView).load(followingPostDataList.get(position).thumbnail).into(holder.thumbnail)

        if(holder.email.text.equals(holder.collabo_email.text)){
            holder.collabo_profile.visibility=View.GONE
            holder.collabo_nickname.visibility=View.GONE
            holder.and.visibility=View.GONE
            holder.collaboCardView.visibility=View.GONE
        }


        if(followingPostDataList.get(position).isLike.equals("null")) {
            holder.home_like.visibility=View.VISIBLE
            holder.home_like_red.visibility=View.INVISIBLE
        }else{
            holder.home_like.visibility=View.INVISIBLE
            holder.home_like_red.visibility=View.VISIBLE

        }

        holder.itemView.setOnClickListener{
            val activity =it!!.context as AppCompatActivity
            val postFragment = PostFragment()
            var bundle = Bundle()
            bundle.putInt("idx",followingPostDataList.get(position).idx)
            bundle.putInt("mr_idx",followingPostDataList.get(position).mr_idx)
            bundle.putString("title",followingPostDataList.get(position).title)
            bundle.putString("singer",followingPostDataList.get(position).singer)
            bundle.putString("cnt_play",followingPostDataList.get(position).cnt_play)
            bundle.putString("cnt_reply",followingPostDataList.get(position).cnt_reply)
            bundle.putString("cnt_like",followingPostDataList.get(position).cnt_like)
            bundle.putString("nickname",followingPostDataList.get(position).nickname)
            bundle.putString("email",followingPostDataList.get(position).email)
            bundle.putString("collaboration_nickname",followingPostDataList.get(position).collaboration_nickname)
            bundle.putString("song_path",followingPostDataList.get(position).song_path)
            bundle.putString("profile",followingPostDataList.get(position).profile)
            bundle.putString("collaboration_profile",followingPostDataList.get(position).collaboration_profile)
            bundle.putString("collabo_email",followingPostDataList.get(position).collabo_email)
            bundle.putString("date",followingPostDataList.get(position).date)
            bundle.putString("kinds",followingPostDataList.get(position).kinds)
            bundle.putString("token",followingPostDataList.get(position).token)
            bundle.putString("thumbnail",followingPostDataList.get(position).thumbnail)
            bundle.putString("col_token",followingPostDataList.get(position).col_token)
            bundle.putString("isLike",followingPostDataList.get(position).isLike)
            postFragment.arguments=bundle

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_frame,postFragment)
                .addToBackStack(null)
                .commit()
        }


    }

    override fun getItemCount(): Int {
        return followingPostDataList.size
    }


    class FollowingPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val idx=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_idx)
        val mr_idx=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_mr_idx)
        val thumbnail=itemView.findViewById<ImageView>(R.id.rv_fragment_home_iv_thumbnail)
        val title=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_title)
        val singer=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_singer)
        val profile=itemView.findViewById<ImageView>(R.id.rv_fragment_home_iv_profile)
        val collabo_profile=itemView.findViewById<ImageView>(R.id.rv_fragment_home_iv_collabo_profile)
        val collabo_nickname=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_collabo_nickname)
        val nickname=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_nickname)
        val cnt_play=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_count_play)
        val cnt_reply=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_count_reply)
        val cnt_like=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_like)
        val song_path=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_song_path)
        val date=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_date)
        val and=itemView.findViewById<TextView>(R.id.and)
        val kinds=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_kinds)
        val token=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_token)
        val email=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_email)
        val collabo_email=itemView.findViewById<TextView>(R.id.rv_fragment_home_tv_collabo_email)
        val collaboCardView=itemView.findViewById<CardView>(R.id.collaboCardView)
        val home_like_red=itemView.findViewById<ImageView>(R.id.home_like_red)
        val home_like=itemView.findViewById<ImageView>(R.id.home_like)
    }
}