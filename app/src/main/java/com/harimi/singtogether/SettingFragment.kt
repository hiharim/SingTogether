package com.harimi.singtogether

import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.broadcast.LiveStreamingViewActivity
import com.kakao.sdk.user.UserApiClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
    마이페이지 프래그먼트
 */
class SettingFragment : Fragment() {
    private var TAG = "SETTING_FRAGMENT"
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    private lateinit var mAuth : FirebaseAuth  ;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val settingFragment =  inflater.inflate(R.layout.fragment_setting, container, false)
        val fragment_setting_tv_logout =settingFragment.findViewById<TextView>(R.id.fragment_setting_tv_logout)
        val fragment_setting_tv_out =settingFragment.findViewById<TextView>(R.id.fragment_setting_tv_out)
//        val fragment_setting_tv_edit_profile =settingFragment.findViewById<TextView>(R.id.fragment_setting_tv_edit_profile)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_client))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this.requireActivity(),gso)

        ////구글 로그아웃
        fragment_setting_tv_logout.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("로그아웃")
            builder.setMessage("로그아웃을 하시겠습니까? ")
            builder.setPositiveButton("네") { dialogInterface: DialogInterface, i: Int ->

                val pref = requireActivity().getSharedPreferences("userEmail", MODE_PRIVATE)
                val edit = pref.edit() // 수정모드
                edit.putString("email", "") // 값 넣기
                edit.apply() // 적용하기

                if (LoginActivity.user_info.loginUserSocial.equals("google")){
                    FirebaseAuth.getInstance().signOut(); //구글로그아웃
                    googleSignInClient.signOut()
                }else if(LoginActivity.user_info.loginUserSocial.equals("kakao")){
                    //카카오 로그아웃
                    UserApiClient.instance.logout { error ->
                        if (error != null) {
                            Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                        }
                        else {
                            Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                        }
                    }
                }

                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
            }
            builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->

            }
            builder.show()

        }

        ////회원탈퇴
        fragment_setting_tv_out.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("회원탈퇴")
            builder.setMessage("회원탈퇴를 하시겠습니까? ")
            builder.setPositiveButton("네") { dialogInterface: DialogInterface, i: Int ->
                retrofit= RetrofitClient.getInstance()
                retrofitService=retrofit.create(RetrofitService::class.java)
                retrofitService.requestDeleteUser(LoginActivity.user_info.loginUserEmail)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val body = response.body().toString()
                                Log.d(TAG, body)
                                var jsonObject = JSONObject(response.body().toString())
                                var result = jsonObject.getBoolean("result")

                                if (result) {
                                    val pref = requireActivity().getSharedPreferences("userEmail", MODE_PRIVATE)
                                    val edit = pref.edit() // 수정모드
                                    edit.putString("email", "") // 값 넣기
                                    edit.apply() // 적용하기

                                    if (LoginActivity.user_info.loginUserSocial.equals("google")){
                                        mAuth = FirebaseAuth.getInstance();
                                        mAuth.currentUser!!.delete()
                                    }else{
                                        // 카카오 회원탈퇴
                                        kakaoUnLinkUser()
                                    }
                                    val intent = Intent(context, LoginActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                        }
                    })


            }
            builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->

            }
            builder.show()
        }

//        fragment_setting_tv_edit_profile.setOnClickListener {
//            val intent = Intent(context, ProfileEditActivity::class.java)
//            startActivity(intent)
//        }
        return  settingFragment
    }

    // 카카오 회원탈퇴
    fun kakaoUnLinkUser(){
        // 연결 끊기
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Toast.makeText(context, "회원 탈퇴 실패 $error", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "연결 끊기 실패", error)
            }
            else {
                Toast.makeText(context, "회원 탈퇴 성공", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
            }
        }
    }

    companion object {

        fun newInstance(param1: String, param2: String) =
                SettingFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }


}