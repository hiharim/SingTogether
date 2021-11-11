package com.harimi.singtogether.adapter

import android.content.Context
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
import com.harimi.singtogether.Data.DetailReplayReviewData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

class DetailReplayReviewAdapter(
    val detailReplayReviewList: ArrayList<DetailReplayReviewData>, val context: Context
) : RecyclerView.Adapter<DetailReplayReviewAdapter.DetailReplayReviewViewHolder>() {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailReplayReviewAdapter.DetailReplayReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_detail_replay_review,
            parent,
            false
        )
        return DetailReplayReviewAdapter.DetailReplayReviewViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DetailReplayReviewAdapter.DetailReplayReviewViewHolder,
        position: Int
    ) {

        if (detailReplayReviewList.get(position).uploadUserEmail.equals(LoginActivity.user_info.loginUserEmail)){
            holder.iv_editAndDelete.visibility = View.VISIBLE
        }else{
            holder.iv_editAndDelete.visibility = View.GONE
        }

        if (detailReplayReviewList.get(position).uploadUserProfile.equals("null") || detailReplayReviewList.get(position).uploadUserProfile.equals("")) {
            holder.iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: DetailReplayReviewData = detailReplayReviewList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.uploadUserProfile)
                .into(holder.iv_uploadUserProfile)
        }

        holder.tv_uploadUserNickname.setText(detailReplayReviewList.get(position).uploadUserNickname)
        holder.tv_review.setText(detailReplayReviewList.get(position).review)
        holder.tv_uploadDate.setText(detailReplayReviewList.get(position).uploadDate)

        ///아이템 온 클릭 리스너
        holder.iv_editAndDelete.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var popupMenu = PopupMenu(context, v)
                popupMenu.inflate(R.menu.edit_and_delete)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener(

                    object : PopupMenu.OnMenuItemClickListener {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                            when (menuItem.itemId) {
                                R.id.post_edit -> {
                                    val editText = EditText(context)
                                    editText.setText(detailReplayReviewList.get(position).review)
                                    val editBuilder = AlertDialog.Builder(context)
                                    editBuilder.setTitle("수정")
                                    editBuilder.setView(editText)
                                    editBuilder.setPositiveButton("확인") { dialog, which ->
                                        retrofit = RetrofitClient.getInstance()
                                        retrofitService = retrofit.create(RetrofitService::class.java)
                                        retrofitService.requestEditReplayReview(detailReplayReviewList.get(position).idx, editText.text.toString()).enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {

                                                if (response.isSuccessful) {
                                                    val body = response.body().toString()
                                                    Log.d("Adapter: ", body)
                                                    val jsonObject = JSONObject(
                                                        response.body().toString()
                                                    )
                                                    val result = jsonObject.getBoolean("result")
                                                    if (result) {
                                                        holder.tv_review.setText(editText.text.toString())

                                                    }
                                                }
                                            }

                                            override fun onFailure(call: Call<String>, t: Throwable) {

                                            }
                                        })

                                    }
                                    editBuilder.setNegativeButton("취소") { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    editBuilder.show()
                                }
                                R.id.post_delete -> {
                                    val deleteBuilder = AlertDialog.Builder(context)
                                    //빌더 타이틀
                                    deleteBuilder.setTitle("삭제")
                                    //빌더 메세지
                                    deleteBuilder.setMessage("삭제 하시겠습니까?")
                                    deleteBuilder.setPositiveButton("네") { dialog, which ->
                                        retrofit = RetrofitClient.getInstance()
                                        retrofitService =
                                            retrofit.create(RetrofitService::class.java)
                                        retrofitService.requestDeleteReplayReview(
                                            detailReplayReviewList.get(position).idx,detailReplayReviewList.get(position).replayIdx).enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful) {
                                                    val body = response.body().toString()
                                                    Log.d("Adapter: ", body)
                                                    val jsonObject = JSONObject(
                                                        response.body().toString()
                                                    )
                                                    val result = jsonObject.getBoolean("result")
                                                    if (result) {
                                                        detailReplayReviewList.removeAt(position)
                                                        notifyItemRemoved(position)
                                                        notifyDataSetChanged()
                                                    }
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<String>,
                                                t: Throwable
                                            ) {
                                            }
                                        })
                                    }
                                    deleteBuilder.setNegativeButton("아니요") { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    deleteBuilder.show()
                                }
                            }
                            return false
                        }
                    })

            }
        })

    }

    override fun getItemCount(): Int {
        return detailReplayReviewList.size
    }


    class DetailReplayReviewViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        val iv_uploadUserProfile = itemView.findViewById<CircleImageView>(R.id.iv_uploadUserProfile)
        val tv_uploadUserNickname = itemView.findViewById<TextView>(R.id.tv_uploadUserNickname)
        val tv_review = itemView.findViewById<TextView>(R.id.tv_review)
        val tv_uploadDate = itemView.findViewById<TextView>(R.id.tv_uploadDate)
        val iv_editAndDelete =itemView.findViewById<ImageView>(R.id.iv_editAndDelete)

    }
}