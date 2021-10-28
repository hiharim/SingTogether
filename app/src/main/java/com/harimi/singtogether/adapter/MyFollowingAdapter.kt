package com.harimi.singtogether.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.Data.MyFollowingData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.LookAtUserProfileActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MyFollowingAdapter  (val myFollowingDataList: ArrayList<MyFollowingData>, val context: Context) : RecyclerView.Adapter<MyFollowingAdapter.MyFollowingViewHolder>(){

    private var TAG  = "MyFollowingAdapter_"
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFollowingAdapter.MyFollowingViewHolder{

        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_following, parent, false)
        return MyFollowingAdapter.MyFollowingViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyFollowingAdapter.MyFollowingViewHolder, position: Int) {


        holder.btn_follow.visibility =View.GONE


        // 프로필 사진
        if (myFollowingDataList.get(position).uploadUserProfile.equals("null") || myFollowingDataList.get(position).uploadUserProfile.equals("")) {
            holder.iv_profileImage.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            val Image: MyFollowingData = myFollowingDataList.get(position)
            Glide.with(holder.itemView.context)
                .load("http://3.35.236.251/" + Image.uploadUserProfile)
                .into(holder.iv_profileImage)
        }
        holder.tv_nickName.setText(myFollowingDataList.get(position).uploadUserNickName)


        holder.iv_profileImage.setOnClickListener {
            retrofit= RetrofitClient.getInstance()
            retrofitService=retrofit.create(RetrofitService::class.java)
            retrofitService.requestLookAtUserProfile(
                myFollowingDataList.get(position).email!!,
                LoginActivity.user_info.loginUserEmail
            )
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d(TAG, body)
                            var jsonObject = JSONObject(response.body().toString())
                            var result = jsonObject.getBoolean("result")
                            if (result) {
//                                    var otherUserInformation = jsonObject.getString("otherUserInformation")
                                var followingUserNumber =
                                    jsonObject.getString("followingUserNumber")
                                var followUserNumber = jsonObject.getString("followUserNumber")
                                var isFollow = jsonObject.getBoolean("isFollow")

                                val intent = Intent(
                                    context,
                                    LookAtUserProfileActivity::class.java
                                )
                                intent.putExtra("otherUserEmail", myFollowingDataList.get(position).email!!)
                                Log.d(TAG, myFollowingDataList.get(position).email!!)
                                intent.putExtra("nickname", myFollowingDataList.get(position).uploadUserNickName!!)
                                intent.putExtra("profile", myFollowingDataList.get(position).uploadUserProfile!!)
                                intent.putExtra("followingUserNumber", followingUserNumber)
                                intent.putExtra("followUserNumber", followUserNumber)
                                intent.putExtra("isFollow", isFollow)
                                context.startActivity(intent)
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })
        }

        holder.btn_follow.setOnClickListener {
            val addBuilder = AlertDialog.Builder(context)
            addBuilder.setTitle("팔로우")
            //빌더 메세지
            addBuilder.setMessage("팔로우를 하시겠습니까?")
            addBuilder.setPositiveButton("네") { dialog, which ->
                retrofit = RetrofitClient.getInstance()
                retrofitService =
                    retrofit.create(RetrofitService::class.java)
                retrofitService.requestAddFollow(
                    LoginActivity.user_info.loginUserEmail,myFollowingDataList.get(position).email).enqueue(object :
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
//                                myFollowingDataList.removeAt(position)
//                                notifyItemRemoved(position)
//                                notifyDataSetChanged()

                                holder.btn_follow.visibility =View.GONE
                                holder.btn_followCancel.visibility =View.VISIBLE

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
            addBuilder.setNegativeButton("아니요") { dialog, which ->
                dialog.dismiss()
            }
            addBuilder.show()

        }
        holder.btn_followCancel.setOnClickListener {
            val deleteBuilder = AlertDialog.Builder(context)
            deleteBuilder.setTitle("팔로우 취소")
            //빌더 메세지
            deleteBuilder.setMessage("팔로우를 취소 하시겠습니까?")
            deleteBuilder.setPositiveButton("네") { dialog, which ->
                retrofit = RetrofitClient.getInstance()
                retrofitService =
                    retrofit.create(RetrofitService::class.java)
                retrofitService.requestDeleteFollow(
                    LoginActivity.user_info.loginUserEmail,myFollowingDataList.get(position).email).enqueue(object :
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
//                                myFollowingDataList.removeAt(position)
//                                notifyItemRemoved(position)
//                                notifyDataSetChanged()

                                holder.btn_follow.visibility =View.VISIBLE
                                holder.btn_followCancel.visibility =View.GONE

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


    override fun getItemCount(): Int {
        return myFollowingDataList.size
    }


    class MyFollowingViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        val iv_profileImage = itemView.findViewById<CircleImageView>(R.id.iv_profileImage)
        val tv_nickName = itemView.findViewById<TextView>(R.id.tv_nickName)
        val btn_followCancel = itemView.findViewById<Button>(R.id.btn_followCancel)
        val btn_follow = itemView.findViewById<Button>(R.id.btn_follow)
    }
}