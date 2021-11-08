package com.harimi.singtogether.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.BestData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/***
 * 오늘의 가왕 어댑터
 */

class BestAdapter(val bestList: ArrayList<BestData> ) : RecyclerView.Adapter<BestAdapter.BestViewHolder>(){

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

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
        val col_token=v.findViewById<TextView>(R.id.rv_fragment_best_singer_tv_col_token)
        val badge=v.findViewById<ImageView>(R.id.badge)
        val badge_collabo=v.findViewById<ImageView>(R.id.badge_collabo)
        val isLike=v.findViewById<TextView>(R.id.rv_fragment_best_singer_tv_isLike)
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
        holder.col_token.text=curData.col_token
        holder.isLike.text=curData.isLike

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



        val context: Context = holder.itemView.getContext()
        holder.itemView.setOnClickListener {
            retrofit = RetrofitClient.getInstance()
            retrofitService = retrofit.create(RetrofitService::class.java)
            retrofitService.requestUpdateSongPostHits(curData.idx).enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        val jsonObject = JSONObject(
                            response.body().toString()
                        )
                        val result = jsonObject.getBoolean("result")
                        if (result) {
                            val activity =context as AppCompatActivity
                            val postFragment = PostFragment()
                            var bundle = Bundle()
                            bundle.putInt("idx",curData.idx)
                            bundle.putInt("mr_idx",curData.mr_idx)
                            bundle.putString("title",curData.title)
                            bundle.putString("singer",curData.singer)
                            bundle.putString("cnt_play",curData.cnt_play)
                            bundle.putString("cnt_reply",curData.cnt_reply)
                            bundle.putString("cnt_like",curData.cnt_like)
                            bundle.putString("nickname",curData.nickname)
                            bundle.putString("email",curData.email)
                            bundle.putString("collaboration_nickname",curData.collaboration_nickname)
                            bundle.putString("song_path",curData.song_path)
                            bundle.putString("profile",curData.profile)
                            bundle.putString("collaboration_profile",curData.collaboration_profile)
                            bundle.putString("collabo_email",curData.collabo_email)
                            bundle.putString("date",curData.date)
                            bundle.putString("kinds",curData.kinds)
                            bundle.putString("token",curData.token)
                            bundle.putString("col_token",curData.col_token)
                            bundle.putString("isLike",curData.isLike)
                            bundle.putString("thumbnail",curData.thumbnail)
                            postFragment.arguments=bundle

                            activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.activity_main_frame,postFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }else{
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                }
            })
        }


    }

    override fun getItemCount(): Int {
        return bestList.size
    }
}