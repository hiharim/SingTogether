package com.harimi.singtogether

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.harimi.singtogether.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    var auth: FirebaseAuth? = null
    val GOOGLE_REQUEST_CODE = 99
    val TAG = "googleLogin"
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val context :Context=this;

        //todo :  jang 키해쉬값도 카카오에 등록하기
//        val keyHash= Utility.getKeyHash(this)
//        Log.d("키해쉬값",keyHash)


        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_client))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)


        //버튼 클릭시
//        googleSignInBtn.setOnClickListener {
//            signIn()
//        }

        // 카카오 로그인 버튼 클릭
        binding.activityLoginBtnLoginKakao.setOnClickListener {
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        }







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
//                }
//            }
//        }
//        else {
//            //로그인 필요
//        }


        // 사용자 정보 요청 (기본)
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
            }
        }

    }
    // 로그인 공통 callback 구성
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
                            "\n만료시간: ${tokenInfo.expiresIn} 초")

                }
            }
            // 프로필액티비티로 이동
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

///구글 로그인 클릭했을 때
    private fun signIn() {
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
                    loginSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    ///구글 로그인이 성공할 시
    private fun loginSuccess(){
//        val intent = Intent(this,LoginActivity::class.java)
//        startActivity(intent)
//        finish()
    }
}