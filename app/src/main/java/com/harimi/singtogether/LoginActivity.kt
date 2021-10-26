package com.harimi.singtogether

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import org.json.JSONObject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding
    private var user_email: String? = null // 이메일
    private var user_nickname: String? = null // 닉네임
    private var user_profile: String? = null // 프로필
    private var user_social: String? = null // 소셜 구분
    private var user_token: String? = null // 토큰

    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

    private lateinit var gso: GoogleSignInOptions.Builder

    var auth: FirebaseAuth? = null
    val GOOGLE_REQUEST_CODE = 99
    val TAG = "googleLogin"
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val context: Context = this;

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            // Log and toast
            Log.d(TAG, token)

            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })

        // 구글 버튼 클릭시
        binding.activityLoginBtnLoginGoogle.setOnClickListener {
            signIn()
        }


        // 카카오 로그인 버튼 클릭
        binding.activityLoginBtnLoginKakao.setOnClickListener {
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
                            kakaoUserInfo()
                            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                                UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
                            } else {
                                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                            }
            }
        }
        // 카카오 토큰존재여부 확인하기
//        if (AuthApiClient.instance.hasToken()) {
//            UserApiClient.instance.accessTokenInfo { _, error ->
//                if (error != null) {
//                    if (error is KakaoSdkError && error.isInvalidTokenError() == true) {
//                        //로그인 필요
//                    }
//                    else {
//                        //기타 에러
//                    }
//                }
//                else {
//                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
//                    Log.e(TAG, "카카오 사용자 토큰", )
//                }
//            }
//        }
//        else {
//            //로그인 필요
//        }
//    }

    // 카카오 로그인 공통 callback 구성
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패", error)
        }
        else if (token != null) {
            Log.i(TAG, "로그인 성공 ${token.accessToken}")
            // 토큰 정보 보기
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    Log.e(TAG, "토큰 정보 보기 실패", error)
                }
                else if (tokenInfo != null) {
                    Log.i(TAG, "토큰 정보 보기 성공" +
                            "\n회원번호: ${tokenInfo.id}" +
                            "\n만료시간: ${tokenInfo.expiresIn} 초"+
                            "\n카카오토큰: ${tokenInfo.appId}")
                    user_token=tokenInfo.appId.toString()



                }
            }


            saveDate(user_email.toString())

            // 프로필액티비티로 이동

            user_info.loginUserEmail = user_email.toString()
            user_info.loginUserNickname = user_nickname.toString()
            user_info.loginUserProfile =user_profile.toString()
            user_info.loginUserSocial =user_social.toString()


            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("EMAIL",user_email)
            intent.putExtra("NICKNAME",user_nickname)
            intent.putExtra("PROFILE",user_profile)
            intent.putExtra("SOCIAL",user_social)
            intent.putExtra("TOKEN",user_token)
            startActivity(intent)
            finish()
        }
    }

    private fun kakaoUserInfo(){
        // 카카오 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(
                        TAG, "사용자 정보 요청 성공" +
                        "\n회원번호: ${user.id}" +
                        "\n이메일: ${user.kakaoAccount?.email}" +
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                        "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )
                user_email= user.kakaoAccount?.email
                user_nickname=user.kakaoAccount?.profile?.nickname
                user_profile=user.kakaoAccount?.profile?.thumbnailImageUrl
                user_social="kakao"
            }
        }
    }

    //구글 로그인 클릭했을 때
    private fun signIn() {

//        구글 빌드
            auth = FirebaseAuth.getInstance()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.firebase_client))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(applicationContext,gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ////구글 로그인 성공시 가져오는 데이터들
        if (requestCode == GOOGLE_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.displayName)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.email)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.idToken)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.photoUrl)

                user_email= account.email
                user_nickname= account.displayName
                user_profile= account.photoUrl.toString()
                user_social="google"

                user_info.loginUserEmail = user_email.toString()
                user_info.loginUserNickname = user_nickname.toString()
                user_info.loginUserProfile =user_profile.toString()
                user_info.loginUserSocial =user_social.toString()

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "로그인 성공")
                    val user = auth!!.currentUser
                    Log.d(TAG, "firebaseAuthWithGoogle:" + user)

                    loginSuccess()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    ///구글 로그인이 성공할 시
    private fun loginSuccess(){

        retrofit = RetrofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)

        retrofitService.requestAutoLogin(user_email.toString())
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "shared " + response.body() + response.message())
                        val jsonObject = JSONObject(response.body().toString())
                        val result = jsonObject.getBoolean("result")
                        Log.d(TAG, "shared " + result.toString())


                        if (result) {
                            val email = jsonObject.getString("email")
                            val nickname = jsonObject.getString("nickname")
                            val profile = jsonObject.getString("profile")
                            val social = jsonObject.getString("social")

                            user_info.loginUserEmail = email.toString()
                            user_info.loginUserNickname = nickname.toString()
                            user_info.loginUserProfile =profile.toString()
                            user_info.loginUserSocial =social.toString()
                            saveDate(email)
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()


                            return
                        } else {
                            val intent = Intent(applicationContext, ProfileActivity::class.java)
                            intent.putExtra("EMAIL",user_email)
                            intent.putExtra("NICKNAME",user_nickname)
                            intent.putExtra("PROFILE",user_profile)
                            intent.putExtra("SOCIAL",user_social)
                            intent.putExtra("TOKEN",user_token)
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        Log.e("onResponse", "실패 : " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(
                        "실패:", "Failed API call with call: " + call +
                                " + exception: " + t
                    )
                }

            })

    }


    override fun onStart() {
        super.onStart()
        val pref = getSharedPreferences("userEmail", 0)
        val savedEmail =
            pref.getString("email", "").toString() //1번째는 데이터 키 값이고 2번째는 키 값에 데이터가 존재하지않을때 대체 값

                retrofit = RetrofitClient.getInstance()
                retrofitService = retrofit.create(RetrofitService::class.java)
                Log.e(TAG, "shared " + savedEmail.toString())

                retrofitService.requestAutoLogin(savedEmail)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(
                            call: Call<String>,
                            response: Response<String>
                        ) {
                            if (response.isSuccessful) {
                                Log.d(TAG, "shared " + response.body() + response.message())
                                val jsonObject = JSONObject(response.body().toString())
                                val result = jsonObject.getBoolean("result")
                                Log.d(TAG, "shared " + result.toString())


                        if (result) {
                            val email = jsonObject.getString("email")
                            val nickname = jsonObject.getString("nickname")
                            val profile = jsonObject.getString("profile")
                            val social = jsonObject.getString("social")

                            user_info.loginUserEmail = email.toString()
                            user_info.loginUserNickname = nickname.toString()
                            user_info.loginUserProfile =profile.toString()
                            user_info.loginUserSocial =social.toString()

                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            return

                        } else {
                            auth = FirebaseAuth.getInstance()
                            gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        }


                    } else {
                        Log.e("onResponse", "실패 : " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(
                        "실패:", "Failed API call with call: " + call +
                                " + exception: " + t
                    )
                }
            })
    }

    class user_info{
        companion object {
            var loginUserEmail = ""
            var loginUserProfile = ""
            var loginUserNickname= ""
            var loginUserSocial= ""

        }
    }

    fun saveDate( loginEmail :String ){
        val pref =getSharedPreferences("userEmail", MODE_PRIVATE)
        val edit = pref.edit() // 수정모드
        edit.putString("email", loginEmail) // 값 넣기
        edit.putString("autoLogin", "1") // 값 넣기
        edit.apply() // 적용하기
    }
}