package com.harimi.singtogether.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.MyBroadcastData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.DetailReplayActivity
import com.harimi.singtogether.broadcast.LiveStreamingViewActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MyBroadcastAdapter (val myBroadcastList : ArrayList<MyBroadcastData>,val context: Context) : RecyclerView.Adapter<MyBroadcastAdapter.MyBroadcastViewHolder>() {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    inner class MyBroadcastViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val iv_thumbnail=v.findViewById<ImageView>(R.id.iv_thumbnail)
        val tv_replayTitle=v.findViewById<TextView>(R.id.tv_replayTitle)
        val tv_uploadDate=v.findViewById<TextView>(R.id.tv_uploadDate)
        val tv_hits=v.findViewById<TextView>(R.id.tv_hits)
        val tv_like=v.findViewById<TextView>(R.id.tv_like)
        val tv_review=v.findViewById<TextView>(R.id.tv_review)
        val tv_nickName=v.findViewById<TextView>(R.id.tv_nickName)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBroadcastViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_my_broadcast,parent,false)

        return MyBroadcastViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBroadcastViewHolder, position: Int) {


        holder.tv_replayTitle.text=myBroadcastList.get(position).replayTitle
        holder.tv_hits.text=myBroadcastList.get(position).replayHits
        holder.tv_uploadDate.text=myBroadcastList.get(position).uploadDate
        holder.tv_like.text=myBroadcastList.get(position).replayLikeNumber
        holder.tv_review.text=myBroadcastList.get(position).replayReviewNumber
        holder.tv_nickName.text=myBroadcastList.get(position).uploadUserNickName


        Glide.with(holder.itemView).load("http://3.35.236.251/"+myBroadcastList.get(position).thumbnail).into(holder.iv_thumbnail)

        holder.itemView.setOnClickListener { v->


            retrofit = RetrofitClient.getInstance()
            retrofitService = retrofit.create(RetrofitService::class.java)
            retrofitService.requestUpdateReplayHIts(myBroadcastList.get(position).idx).enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d("Adapter: ", body)
                        val jsonObject = JSONObject(
                            response.body().toString()
                        )
                        val result = jsonObject.getBoolean("result")
                        if (result) {
                            val intent = Intent(context, DetailReplayActivity::class.java)
                            intent.putExtra("idx", myBroadcastList.get(position).idx)
                            intent.putExtra("thumbnail", myBroadcastList.get(position).thumbnail)
                            intent.putExtra("uploadUserProfile", myBroadcastList.get(position).uploadUserProfile)
                            intent.putExtra("uploadUserNickName", myBroadcastList.get(position).uploadUserNickName)
                            intent.putExtra("uploadDate", myBroadcastList.get(position).uploadDate)
                            intent.putExtra("replayTitle", myBroadcastList.get(position).replayTitle)
                            intent.putExtra("replayLikeNumber", myBroadcastList.get(position).replayLikeNumber)
                            intent.putExtra("replayHits", myBroadcastList.get(position).replayHits)
                            intent.putExtra("replayReviewNumber", myBroadcastList.get(position).replayReviewNumber)
                            intent.putExtra("uploadUserEmail", myBroadcastList.get(position).uploadUserEmail)
                            intent.putExtra("replayPostLikeIdx", myBroadcastList.get(position).replayPostLikeIdx)
                            intent.putExtra("liked", myBroadcastList.get(position).liked)
                            intent.putExtra("replayVideo", myBroadcastList.get(position).replayVideo)
                            intent.putExtra("userLeaveCheck", myBroadcastList.get(position).userLeaveCheck)
                            intent.putExtra("uploadUserFCMToken", myBroadcastList.get(position).uploadUserFCMToken)
                            intent.putExtra("isBadge", myBroadcastList.get(position).isBadge)
                            context.startActivity(intent, null)
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })

        }
    }

    override fun getItemCount()=myBroadcastList.size

}