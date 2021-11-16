package com.harimi.singtogether.sing

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class DuetAdapter(val duetList: ArrayList<DuetData>) : RecyclerView.Adapter<DuetAdapter.DuetViewHolder>() {

    private var TAG :String = "듀엣 어댑터"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    inner class DuetViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val duet_idx=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_duet_idx)
        val mr_idx=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_mr_idx)
        val thumbnail=v.findViewById<ImageView>(R.id.rv_fragment_duet_iv_thumbnail)
        val badge=v.findViewById<ImageView>(R.id.badge)
        val title=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_singer)
        //val profile=v.findViewById<ImageView>(R.id.rv_fragment_duet_iv_profile)
        val profile=v.findViewById<CircleImageView>(R.id.rv_fragment_duet_iv_profile)
        val nickname=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_nickname)
        val email=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_email)
        val cnt_play=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_count_play)
        val cnt_reply=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_count_reply)
        val cnt_duet=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_count_duet)
        val duet_path=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_duet_path)
        val extract_path=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_extract_path)
        val mr_path=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_mr)
        val duet_date=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_date)
        val kinds=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_kinds)
        val lyrics=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_lyrics)
        val token=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_token)
        val isBadge=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_isBadge)
        val userLeaveCheck=v.findViewById<TextView>(R.id.rv_fragment_duet_tv_userLeaveCheck)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuetViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(
            R.layout.rv_fragment_duet,
            parent,
            false
        )

        return DuetViewHolder(view)
    }

    override fun onBindViewHolder(holder: DuetViewHolder, position: Int) {
        val curData = duetList[position]
        holder.duet_idx.text= curData.duet_idx.toString()
        holder.mr_idx.text= curData.mr_idx.toString()
        holder.title.text=curData.title
        holder.singer.text=curData.singer
        holder.cnt_play.text=curData.cnt_play
        holder.cnt_reply.text=curData.cnt_reply
        holder.cnt_duet.text=curData.cnt_duet
        holder.email.text=curData.email
        holder.nickname.text=curData.nickname
        holder.duet_path.text=curData.duet_path
        holder.mr_path.text=curData.mr_path
        holder.extract_path.text=curData.extract_path
        holder.duet_date.text=curData.date
        holder.kinds.text=curData.kinds
        holder.lyrics.text=curData.lyrics
        holder.token.text=curData.token
        holder.isBadge.text=curData.isBadge.toString()
        holder.userLeaveCheck.text=curData.userLeaveCheck

        //밷지
        if(holder.isBadge.text.equals("true")){
            holder.badge.visibility=View.VISIBLE
        }else{
            holder.badge.visibility =View.GONE
        }

        Glide.with(holder.itemView).load("http://3.35.236.251/" + curData.profile).into(holder.profile)
        Glide.with(holder.itemView).load(curData.thumbnail).into(holder.thumbnail)

        val context: Context = holder.itemView.getContext()

        holder.itemView.setOnClickListener{
            Log.e(TAG, "클릭 idx : " + curData.duet_idx)
            retrofit = RetrofitClient.getInstance()
            retrofitService = retrofit.create(RetrofitService::class.java)
            retrofitService.requestUpdateDuetHits(curData.duet_idx.toString()).enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.e(TAG, body)
                        val jsonObject = JSONObject(
                            response.body().toString()
                        )
                        val result = jsonObject.getBoolean("result")
                        if (result) {
                            // DetailDuetFragment 로 이동
                            val activity = context as AppCompatActivity
                            val detailDuetFragment = DetailDuetFragment()
                            val bundle = Bundle()
                            bundle.putInt("duet_idx", curData.duet_idx)
                            bundle.putInt("mr_idx", curData.mr_idx)
                            bundle.putString("thumbnail", curData.thumbnail)
                            bundle.putString("title", curData.title)
                            bundle.putString("singer", curData.singer)
                            bundle.putString("cnt_play", curData.cnt_play)
                            bundle.putString("cnt_reply", curData.cnt_reply)
                            bundle.putString("cnt_duet", curData.cnt_duet)
                            bundle.putString("email", curData.email)
                            bundle.putString("nickname", curData.nickname)
                            bundle.putString("duet_path", curData.duet_path)
                            bundle.putString("mr_path", curData.mr_path)
                            bundle.putString("extract_path", curData.extract_path)
                            bundle.putString("profile", curData.profile)
                            bundle.putString("date", curData.date)
                            bundle.putString("kinds", curData.kinds)
                            bundle.putString("lyrics", curData.lyrics)
                            bundle.putString("token", curData.token)
                            bundle.putString("isBadge", curData.isBadge.toString())
                            bundle.putString("userLeaveCheck", curData.userLeaveCheck)
                            detailDuetFragment.arguments = bundle


                            activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.activity_main_frame, detailDuetFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }else{
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                        Log.e(TAG, "응답 문제" + response.code())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e(TAG, "통신 실패" + t.message)
                }
            })

        }

//        holder.itemView.setOnClickListener {
//            // DetailDuetFragment 로 이동
//            //val activity =v!!.context as AppCompatActivity
//            val activity =context as AppCompatActivity
//            val detailDuetFragment = DetailDuetFragment()
//            var bundle =Bundle()
//            bundle.putInt("duet_idx",curData.duet_idx)
//            bundle.putInt("mr_idx",curData.mr_idx)
//            bundle.putString("thumbnail",curData.thumbnail)
//            bundle.putString("title",curData.title)
//            bundle.putString("singer",curData.singer)
//            bundle.putString("cnt_play",curData.cnt_play)
//            bundle.putString("cnt_reply",curData.cnt_reply)
//            bundle.putString("cnt_duet",curData.cnt_duet)
//            bundle.putString("email",curData.email)
//            bundle.putString("nickname",curData.nickname)
//            bundle.putString("duet_path",curData.duet_path)
//            bundle.putString("mr_path",curData.mr_path)
//            bundle.putString("extract_path",curData.extract_path)
//            bundle.putString("profile",curData.profile)
//            bundle.putString("date",curData.date)
//            bundle.putString("kinds",curData.kinds)
//            bundle.putString("lyrics",curData.lyrics)
//            bundle.putString("token",curData.token)
//            detailDuetFragment.arguments=bundle
//
//            activity.supportFragmentManager.beginTransaction()
//                .replace(R.id.activity_main_frame,detailDuetFragment)
//                .addToBackStack(null)
//                .commit()
//        }


    }

    override fun getItemCount()=duetList.size


}