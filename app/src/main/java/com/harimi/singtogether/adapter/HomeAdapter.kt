package com.harimi.singtogether.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.MainActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import com.harimi.singtogether.sing.DetailDuetFragment
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class HomeAdapter(val homePostList: ArrayList<HomeData> ) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    inner class HomeViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val idx=v.findViewById<TextView>(R.id.rv_fragment_home_tv_idx)
        val mr_idx=v.findViewById<TextView>(R.id.rv_fragment_home_tv_mr_idx)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_fragment_home_iv_thumbnail)
        val title=v.findViewById<TextView>(R.id.rv_fragment_home_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_fragment_home_tv_singer)
        val profile=v.findViewById<CircleImageView>(R.id.rv_fragment_home_iv_profile)
        val iv_like=v.findViewById<ImageView>(R.id.home_like)
        val iv_like_red=v.findViewById<ImageView>(R.id.home_like_red)
        val collabo_profile=v.findViewById<CircleImageView>(R.id.rv_fragment_home_iv_collabo_profile)
        val collabo_nickname=v.findViewById<TextView>(R.id.rv_fragment_home_tv_collabo_nickname)
        val nickname=v.findViewById<TextView>(R.id.rv_fragment_home_tv_nickname)
        val cnt_play=v.findViewById<TextView>(R.id.rv_fragment_home_tv_count_play)
        val cnt_reply=v.findViewById<TextView>(R.id.rv_fragment_home_tv_count_reply)
        val cnt_like=v.findViewById<TextView>(R.id.rv_fragment_home_tv_like)
        val song_path=v.findViewById<TextView>(R.id.rv_fragment_home_tv_song_path)
        val date=v.findViewById<TextView>(R.id.rv_fragment_home_tv_date)
        val and=v.findViewById<TextView>(R.id.and)
        val kinds=v.findViewById<TextView>(R.id.rv_fragment_home_tv_kinds)
        val token=v.findViewById<TextView>(R.id.rv_fragment_home_tv_token)
        val col_token=v.findViewById<TextView>(R.id.rv_fragment_home_tv_col_token)
        val email=v.findViewById<TextView>(R.id.rv_fragment_home_tv_email)
        val collabo_email=v.findViewById<TextView>(R.id.rv_fragment_home_tv_collabo_email)
        val isLike=v.findViewById<TextView>(R.id.rv_fragment_home_tv_isLike)
        val isBadge=v.findViewById<TextView>(R.id.rv_fragment_home_tv_isBadge)
        val isBadgeCollabo=v.findViewById<TextView>(R.id.rv_fragment_home_tv_isBadgeCollabo)
        val badge=v.findViewById<ImageView>(R.id.badge_home)
        val badge_collabo=v.findViewById<ImageView>(R.id.badge_colloabo_home)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_home, parent, false)
        return  HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeAdapter.HomeViewHolder, position: Int) {
        val curData = homePostList[position]
        holder.idx.text = curData.idx.toString()
        holder.title.text = curData.title
        holder.singer.text = curData.singer
        holder.cnt_play.text = curData.cnt_play
        holder.cnt_reply.text = curData.cnt_reply
        holder.cnt_like.text = curData.cnt_like
        holder.nickname.text = curData.nickname
        holder.email.text = curData.email
        holder.song_path.text = curData.song_path
        holder.collabo_nickname.text = curData.collaboration_nickname
        holder.collabo_email.text = curData.collabo_email
        holder.date.text = curData.date
        holder.kinds.text = curData.kinds
        holder.token.text = curData.token
        holder.col_token.text = curData.col_token
        holder.isLike.text = curData.isLike
        holder.mr_idx.text = curData.mr_idx.toString()
        holder.isBadge.text = curData.isBadge.toString()
        holder.isBadgeCollabo.text = curData.isBadgeCollabo.toString()


        // 밷지
        if (holder.isBadge.text.equals("true") && holder.isBadgeCollabo.text.equals("true")) {
            holder.badge.visibility = View.VISIBLE
            holder.badge_collabo.visibility=View.VISIBLE
        }else if(holder.isBadge.text.equals("true") && holder.isBadgeCollabo.text.equals("false")){
            holder.badge.visibility = View.VISIBLE
            holder.badge_collabo.visibility=View.GONE

        }else if(holder.isBadge.text.equals("false") && holder.isBadgeCollabo.text.equals("true")){
            holder.badge.visibility = View.GONE
            holder.badge_collabo.visibility=View.VISIBLE
        }else if(holder.isBadge.text.equals("false") && holder.isBadgeCollabo.text.equals("false")){
            holder.badge.visibility = View.GONE
            holder.badge_collabo.visibility=View.GONE
        }

        Glide.with(holder.itemView).load("http://3.35.236.251/"+curData.profile).into(holder.profile)
        Glide.with(holder.itemView).load("http://3.35.236.251/"+curData.collaboration_profile).into(holder.collabo_profile)
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        // 솔로일때 콜라보숨기기
        if(holder.email.text.equals(holder.collabo_email.text)) {
            holder.collabo_profile.visibility=View.GONE
            holder.collabo_nickname.visibility=View.GONE
            holder.and.visibility=View.GONE
            holder.badge_collabo.visibility=View.GONE
        }else{
            holder.collabo_profile.visibility=View.VISIBLE
            holder.collabo_nickname.visibility=View.VISIBLE
            holder.and.visibility=View.VISIBLE
            //holder.badge_collabo.visibility=View.GONE
        }

        // 좋아요
        val userEmail=LoginActivity.user_info.loginUserEmail
        if(holder.isLike.text.equals(userEmail)) {
            holder.iv_like.visibility=View.INVISIBLE
            holder.iv_like_red.visibility=View.VISIBLE
        }else{
            holder.iv_like.visibility=View.VISIBLE
            holder.iv_like_red.visibility=View.INVISIBLE
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
                            var bundle =Bundle()
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
                            bundle.putString("isBadge",curData.isBadge.toString())
                            bundle.putString("isBadgeCollabo",curData.isBadgeCollabo.toString())
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


//        holder.itemView.setOnClickListener { v->
//            // PostFragment 로 이동
//            val activity =v!!.context as AppCompatActivity
//            val postFragment = PostFragment()
//            var bundle =Bundle()
//            bundle.putInt("idx",curData.idx)
//            bundle.putInt("mr_idx",curData.mr_idx)
//            bundle.putString("title",curData.title)
//            bundle.putString("singer",curData.singer)
//            bundle.putString("cnt_play",curData.cnt_play)
//            bundle.putString("cnt_reply",curData.cnt_reply)
//            bundle.putString("cnt_like",curData.cnt_like)
//            bundle.putString("nickname",curData.nickname)
//            bundle.putString("email",curData.email)
//            bundle.putString("collaboration_nickname",curData.collaboration_nickname)
//            bundle.putString("song_path",curData.song_path)
//            bundle.putString("profile",curData.profile)
//            bundle.putString("collaboration_profile",curData.collaboration_profile)
//            bundle.putString("collabo_email",curData.collabo_email)
//            bundle.putString("date",curData.date)
//            bundle.putString("kinds",curData.kinds)
//            bundle.putString("token",curData.token)
//            bundle.putString("col_token",curData.col_token)
//            bundle.putString("isLike",curData.isLike)
//            bundle.putString("thumbnail",curData.thumbnail)
//            postFragment.arguments=bundle
//
//            activity.supportFragmentManager.beginTransaction()
//                .replace(R.id.activity_main_frame,postFragment)
//                .addToBackStack(null)
//                .commit()
//
//        }

    }

    override fun getItemCount(): Int {
        return homePostList.size
    }



}
