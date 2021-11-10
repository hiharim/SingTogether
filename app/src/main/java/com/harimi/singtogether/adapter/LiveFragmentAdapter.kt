package com.harimi.singtogether.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.LiveFragment
import com.harimi.singtogether.broadcast.LiveStreamingViewActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LiveFragmentAdapter(val LiveStreamingPostList: ArrayList<LiveFragmentData>, val context: Context) :
    RecyclerView.Adapter<LiveFragmentAdapter.LiveFragmentViewHolder>(){

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveFragmentAdapter.LiveFragmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_fragment_live,
            parent,
            false
        )
        return  LiveFragmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiveFragmentAdapter.LiveFragmentViewHolder, position: Int) {
        // 프로필 사진
        if (LiveStreamingPostList.get(position).profile.equals("null") || LiveStreamingPostList.get(
                position
            ).profile.equals("")) {
            holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: LiveFragmentData = LiveStreamingPostList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.profile)
                .into(holder.iv_profile)
        }
        ///썸네일
        if (LiveStreamingPostList.get(position).thumbnail.equals("null") || LiveStreamingPostList.get(
                position
            ).thumbnail.equals("")) {
            holder.iv_thumbnail.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: LiveFragmentData = LiveStreamingPostList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.thumbnail)
                .into(holder.iv_thumbnail)
        }

        if (LiveStreamingPostList.get(position).isBadge ==true){
            holder.iv_badge.visibility =View.VISIBLE
        }else{
            holder.iv_badge.visibility =View.GONE
        }

        holder.tv_liveTitle.setText(LiveStreamingPostList.get(position).title)
        holder.tv_viewer.setText(LiveStreamingPostList.get(position).viewer)
        holder.tv_nickName.setText(LiveStreamingPostList.get(position).nickName)

        ///아이템 온 클릭 리스너
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val Idx = LiveStreamingPostList.get(position).idx
                retrofit = RetrofitClient.getInstance()
                retrofitService = retrofit.create(RetrofitService::class.java)
                retrofitService.requestLiveStreamingCheck(Idx).enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {

                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d("getHomePost: ", body)
                            val jsonObject = JSONObject(response.body().toString())
                            val result = jsonObject.getBoolean("result")
                            if (result) {
                                val intent = Intent(context, LiveStreamingViewActivity::class.java)
                                intent.putExtra("roomIdx", LiveStreamingPostList.get(position).idx)
                                context.startActivity(intent, null)

                            } else {
                                Toast.makeText(context, "스트리머가 이미 방송을 종료하였습니다.", Toast.LENGTH_SHORT).show()
                                LiveStreamingPostList.removeAt(position)
                                notifyItemRemoved(position)
                                notifyDataSetChanged()
                                return
                            }
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })
            }
        })

    }

    override fun getItemCount(): Int {
        return LiveStreamingPostList.size
    }

    class LiveFragmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val iv_thumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail) //썸네일
        val iv_profile = itemView.findViewById<CircleImageView>(R.id.iv_profile) // 프로필 사진
        val tv_liveTitle = itemView.findViewById<TextView>(R.id.tv_liveTitle) // 타이틀
        val tv_viewer = itemView.findViewById<TextView>(R.id.tv_viewer) // 시청자
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName) // 닉네임
        val iv_badge = itemView.findViewById<ImageView>(R.id.iv_badge) //썸네일
    }


}
