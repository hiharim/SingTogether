package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.harimi.singtogether.Data.UserData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.databinding.ActivityProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

// 회원가입할때 프로필,닉네임 설정하는 액티비티
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 서버 연결
        initRetrofit()

        val email=intent.getStringExtra("이메일")
        val nickname=intent.getStringExtra("닉네임")
        val social="a"
        val token="a"


        // 완료 버튼 클릭 - 회원가입완료
        binding.activityProfileBtn.setOnClickListener {
            Log.d("서버에 보내는값: ", email+" "+nickname+" "+social + " "+ token)
            if (email != null && nickname != null) {
                retrofitService.requestSignIn(email, nickname, social, token)
                    .enqueue(object : Callback<String> {
        //                    override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
        //
        //                        Log.d("응답 Response:: ", response.toString())
        //                        if(!response.body().toString().isEmpty()){
        //                            Log.d("응답성공Response:: ", response.body().toString())
        //                        }
        //
        //
        //                    }
        //
        //                    override fun onFailure(call: Call<UserData>, t: Throwable) {
        //                        Log.d("실패:", "Failed API call with call: " + call +
        //                                " + exception: " + t)
        //                    }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if(!response.body().toString().isEmpty()){
                                Log.d("응답성공Response:: ", response.body().toString())
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.d("실패:", "Failed API call with call: " + call +
                                    " + exception: " + t)
                        }

                    })
            }

        }
    }

    private fun initRetrofit(){
        retrofit=RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }


}