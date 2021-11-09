package com.harimi.singtogether.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.DetailDuetReviewData
import com.harimi.singtogether.Data.LiveFragmentData
import com.harimi.singtogether.Data.ReplayData
import com.harimi.singtogether.Data.SearchData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.LiveStreamingViewActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.ArrayList

class SearchDataAdapter (val searchDataList: ArrayList<SearchData>, val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    private val TAG = "SearchDataAdapter_"
    override fun getItemViewType(position: Int): Int {
         return if (searchDataList.get(position).getType.equals("0")){
            0
        }else{
             Log.d(TAG,"1");
            1
         }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType==0){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_live,parent ,false)
            return LiveFragmentViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_replay,parent ,false)
            return ReplayFragmentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
        if (searchDataList.get(position).getType.equals("0")){
            holder as LiveFragmentViewHolder
            Log.d(TAG,"onBindViewHolder: 0");

            // 프로필 사진
            if (searchDataList.get(position).profile.equals("null") || searchDataList.get(
                    position
                ).profile.equals("")) {
                holder.iv_profile.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                val Image: SearchData = searchDataList.get(position)
                Glide.with(holder.itemView.context)
                    .load("http://3.35.236.251/" + Image.profile)
                    .into(holder.iv_profile)
            }
            ///썸네일
            if (searchDataList.get(position).thumbnail.equals("null") || searchDataList.get(
                    position
                ).thumbnail.equals("")) {
                holder.iv_thumbnail.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                val Image: SearchData = searchDataList.get(position)
                Glide.with(holder.itemView.context)
                    .load("http://3.35.236.251/" + Image.thumbnail)
                    .into(holder.iv_thumbnail)
            }

            holder.tv_liveTitle.setText(searchDataList.get(position).title)
            holder.tv_viewer.setText(searchDataList.get(position).viewer)
            holder.tv_nickName.setText(searchDataList.get(position).nickName)



        }else{
            holder as ReplayFragmentViewHolder
            Log.d(TAG,"onBindViewHolder: 1");

            if (searchDataList.get(position).uploadUserProfile.equals("null") || searchDataList.get(position).uploadUserProfile.equals("")) {
                holder.iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                val Image: SearchData = searchDataList.get(position)
                Glide.with(holder.itemView.context)
                    .load("http://3.35.236.251/" + Image.uploadUserProfile)
                    .into(holder.iv_uploadUserProfile)
            }

            ///썸네일
            if (searchDataList.get(position).thumbnail.equals("null") || searchDataList.get(position).thumbnail.equals("")) {
                holder.iv_thumbnail.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                val Image: SearchData = searchDataList.get(position)
                Glide.with(holder.itemView.context)
                    .load("http://3.35.236.251/" + Image.thumbnail)
                    .override(200, 200)
                    .into(holder.iv_thumbnail)
            }

            if (searchDataList.get(position).isBadge ==true){
                holder.iv_badge.visibility =View.VISIBLE
            }else{
                holder.iv_badge.visibility =View.GONE
            }

            holder.tv_iv_uploadUserNickName.setText(searchDataList.get(position).uploadUserNickName)
            holder.tv_replayTitle.setText(searchDataList.get(position).replayTitle)
            holder.tv_replayHits.setText(searchDataList.get(position).replayHits)
            holder.tv_replayReviewNumber.setText(searchDataList.get(position).replayReviewNumber)
            holder.tv_replayLikeNumber.setText(searchDataList.get(position).replayLikeNumber)
            holder.time.setText(searchDataList.get(position).time)


            if (searchDataList.get(position).liked == true){
                holder.iv_clickLike.visibility = View.VISIBLE
                holder.iv_normalLike.visibility = View.GONE
            }else{
                holder.iv_clickLike.visibility = View.GONE
                holder.iv_normalLike.visibility = View.VISIBLE
            }
        }

    }

    override fun getItemCount(): Int {
        return searchDataList.size
    }


    class LiveFragmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val iv_thumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail) //썸네일
        val iv_profile = itemView.findViewById<CircleImageView>(R.id.iv_profile) // 프로필 사진
        val tv_liveTitle = itemView.findViewById<TextView>(R.id.tv_liveTitle) // 타이틀
        val tv_viewer = itemView.findViewById<TextView>(R.id.tv_viewer) // 시청자
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName) // 닉네임

    }

    class ReplayFragmentViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val iv_thumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail) //썸네일
        val iv_uploadUserProfile = itemView.findViewById<CircleImageView>(R.id.iv_uploadUserProfile) // 프로필 사진
        val tv_iv_uploadUserNickName = itemView.findViewById<TextView>(R.id.tv_iv_uploadUserNickName) // 닉네임
        val tv_replayLikeNumber = itemView.findViewById<TextView>(R.id.tv_replayLikeNumber) // 좋아요 수
        val tv_replayReviewNumber = itemView.findViewById<TextView>(R.id.tv_replayReviewNumber) // 댓글 수
        val tv_replayTitle = itemView.findViewById<TextView>(R.id.tv_replayTitle) // 제목
        val tv_replayHits = itemView.findViewById<TextView>(R.id.tv_replayHits) // 조회수
        val iv_clickLike = itemView.findViewById<ImageView>(R.id.iv_clickLike) // 좋아요
        val iv_normalLike = itemView.findViewById<ImageView>(R.id.iv_normalLike) // 좋아요
        val time = itemView.findViewById<TextView>(R.id.time) // 좋아요
        val iv_badge = itemView.findViewById<ImageView>(R.id.iv_badge)


    }
}