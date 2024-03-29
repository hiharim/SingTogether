package com.harimi.singtogether

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.Data.PostReviewData
import com.harimi.singtogether.Network.*
import com.harimi.singtogether.adapter.PostFragmentReviewAdapter
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import com.harimi.singtogether.databinding.FragmentPostBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.log

/**
 * 홈 포스트아이템 클릭하면 나오는 디테일 프래그먼트
 */

class PostFragment : Fragment() {
    var TAG :String = "PostFragment "
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    private lateinit var binding: FragmentPostBinding

    private var idx : Int? = null
    private var mr_idx : Int? = null // mr 테이블 idx
    private var total_like : String? = null
    private var title : String? = null
    private var singer : String? = null
    private var cnt_play : String? = null
    private var cnt_reply : String? = null
    private var cnt_like : String? = null
    private var nickname : String? = null
    private var email : String? = null
    private var collabo_email : String? = null
    private var collaboration_nickname : String? = null
    private var song_path : String? = null
    private var profile : String? = null
    private var collaboration_profile : String? = null
    private var date : String? = null
    private var kinds : String? = null
    private var token : String? = null // 게시물 올린 사용자 토큰
    private var col_token : String? = null // 콜라보한 사용자 토큰
    private var isLiked : String? = null
    private var thumbnail : String? = null
    private var simpleExoPlayer: ExoPlayer?=null
    private var isBadge: String? = null
    private var isBadgeCollabo : String? = null
    private var userLeaveCheck: String? = null
    private var collaborationLeaveCheck: String? = null
    private val postReviewDataList: ArrayList<PostReviewData> = ArrayList()

    private lateinit var postReviewAdapter: PostFragmentReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idx=it.getInt("idx")
            mr_idx=it.getInt("mr_idx")
            title=it.getString("title")
            singer=it.getString("singer")
            cnt_play=it.getString("cnt_play")
            cnt_reply=it.getString("cnt_reply")
            cnt_like=it.getString("cnt_like")
            nickname=it.getString("nickname")
            email=it.getString("email")
            collabo_email=it.getString("collabo_email")
            collaboration_nickname=it.getString("collaboration_nickname")
            song_path=it.getString("song_path")
            profile=it.getString("profile")
            collaboration_profile=it.getString("collaboration_profile")
            date=it.getString("date")
            kinds=it.getString("kinds")
            token=it.getString("token")
            col_token=it.getString("col_token")
            isLiked=it.getString("isLike")
            thumbnail=it.getString("thumbnail")
            isBadge=it.getString("isBadge")
            isBadgeCollabo=it.getString("isBadgeCollabo")
            userLeaveCheck=it.getString("userLeaveCheck")
            collaborationLeaveCheck=it.getString("collaborationLeaveCheck")

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding= FragmentPostBinding.inflate(inflater,container,false)

//        if(col_token==null){
//            col_token=token
//        }
        binding.tvUploadUserNickName.text=nickname
        binding.tvUploadCollaboNickName.text=collaboration_nickname
        binding.tvSongTitle.text=title
        binding.tvSinger.text=singer
        binding.tvHits.text=cnt_play
        binding.tvReviewNumber.text=cnt_reply
        binding.tvUploadDate.text=date
        Glide.with(this).load("http://3.35.236.251/"+profile).into(binding.ivUploadUserProfile)
        Glide.with(this).load("http://3.35.236.251/"+collaboration_profile).into(binding.ivUploadCollaboProfile)

        //뱃지
        if(isBadge.equals("true")){
            binding.postBadge.visibility=View.VISIBLE
        }else {
            binding.postBadge.visibility=View.GONE
        }
        if(isBadgeCollabo.equals("true")){
            binding.postBadgeCollabo.visibility=View.VISIBLE
        }else {
            binding.postBadgeCollabo.visibility=View.GONE
        }

        // 솔로일때
        if(binding.tvUploadUserNickName.text.equals(binding.tvUploadCollaboNickName.text)){
            binding.tvUploadCollaboNickName.visibility=View.GONE
            binding.ivUploadCollaboProfile.visibility=View.GONE
            binding.postBadgeCollabo.visibility=View.GONE
        }

        if(kinds.equals("녹음")){
            Glide.with(this).load(thumbnail).into(binding.imageViewThumb)
            binding.imageViewThumb.visibility=View.VISIBLE
        }

        // 빌드 시 context 가 필요하기 때문에 context 를 null 체크 해준 뒤 빌드
        context?.let{
            simpleExoPlayer= SimpleExoPlayer.Builder(it).build()
        }
        binding.pv.player = simpleExoPlayer
        val factory: DataSource.Factory = DefaultDataSourceFactory(
            requireContext(),
            "ExoPlayer"
        )
        var mediaItem = MediaItem.fromUri(Uri.parse(song_path))
        val progressiveMediaSource = ProgressiveMediaSource.Factory(factory)
            .createMediaSource(mediaItem)
        simpleExoPlayer!!.setMediaSource(progressiveMediaSource)
        simpleExoPlayer!!.prepare()
        simpleExoPlayer!!.play()

        binding.fragmentPostRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fragmentPostRecyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        postReviewAdapter = PostFragmentReviewAdapter(postReviewDataList, requireContext())
        binding.fragmentPostRecyclerView.adapter = postReviewAdapter

        val login_user= LoginActivity.user_info.loginUserEmail
        if(userLeaveCheck.equals("1")){
            binding.fragmentPostBtnDelete.visibility=View.GONE
        }else{
            if(email.equals(login_user)) {
                binding.fragmentPostBtnDelete.visibility=View.VISIBLE
                // 게시물 삭제
                binding.fragmentPostBtnDelete.setOnClickListener {
                    val popupMenu = PopupMenu(context, it)
                    popupMenu.inflate(R.menu.delete_menu)
                    popupMenu.show()
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.delete -> {
                                val builder = AlertDialog.Builder(requireContext())
                                builder.setTitle("삭제하기")
                                builder.setMessage("삭제 하시겠습니까? ")
                                builder.setPositiveButton("네") { dialogInterface: DialogInterface, i: Int ->
                                    deletePost()
                                }
                                builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->

                                }
                                builder.show()
                            }
                        }
                        false
                    }
                }
            }
        }

        // 뒤로가기버튼 클릭
        binding.fragmentPostBtnBack.setOnClickListener {
            val totalFragment = TotalFragment()
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.remove(this)
                ?.replace(R.id.activity_main_frame,totalFragment)
                ?.commit()
        }

        postReviewLoad(binding.fragmentPostRecyclerView)

        ////프로필 액티비티로 넘어가기
        binding.ivUploadUserProfile.setOnClickListener{
            if (userLeaveCheck.equals("1")){
                Toast.makeText(requireContext(), "탈퇴한 회원입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            goToLookAtProfileActivity(email!!,nickname!!,profile!!)
            Log.d(TAG, email)
        }

        binding.ivUploadCollaboProfile.setOnClickListener {
            if (collaborationLeaveCheck.equals("1")){
                Toast.makeText(requireContext(), "탈퇴한 회원입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            goToLookAtProfileActivity(collabo_email!!,collaboration_nickname!!,collaboration_profile!!)
            Log.d(TAG, collabo_email)
        }

        binding.ivUploadReview.setOnClickListener {
            var uploadReview = binding.etWriteReview.text.toString()
            if (uploadReview.equals("")){

                Toast.makeText(requireContext(), "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                var uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                retrofit= RetrofitClient.getInstance()
                retrofitService=retrofit.create(RetrofitService::class.java)
                retrofitService.requestWritePostReview(
                    idx.toString()!!,
                    LoginActivity.user_info.loginUserEmail,
                    LoginActivity.user_info.loginUserProfile,
                    LoginActivity.user_info.loginUserNickname,
                    uploadReview,
                    uploadDate
                )
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val body = response.body().toString()
                                Log.d(TAG, body)
                                var jsonObject = JSONObject(response.body().toString())
                                var result = jsonObject.getBoolean("result")
                                Log.e(TAG,"댓글달기 col_token: $col_token")
                                if (result) {
                                    binding.etWriteReview.setText("")
                                    var getIdx = jsonObject.getString("idx")
                                    val postReviewData = PostReviewData(
                                        getIdx,
                                        LoginActivity.user_info.loginUserEmail,
                                        LoginActivity.user_info.loginUserNickname,
                                        LoginActivity.user_info.loginUserProfile,
                                        uploadReview,
                                        uploadDate,
                                        idx.toString()!!
                                    )
                                    postReviewDataList.add(postReviewData)
                                    postReviewAdapter.notifyDataSetChanged()
                                    binding.fragmentPostRecyclerView.scrollToPosition(
                                        postReviewDataList.size - 1
                                    )
                                }
                                //FCM 보내기
                                if (LoginActivity.user_info.loginUserEmail.equals(email) || userLeaveCheck.equals("1")) {
                                } else {
                                    PushSongPostNotification(
                                        SongPostNotificationData(
                                            "SingTogether",
                                            nickname + "님의 포스팅에 " + LoginActivity.user_info.loginUserNickname + " 님이 댓글을 남겼습니다.",
                                            idx!!,
                                            thumbnail!!,
                                            title!!,
                                            singer!!,
                                            cnt_play!!,
                                            cnt_reply!!,
                                            cnt_like!!,
                                            nickname!!,
                                            email!!,
                                            profile!!,
                                            song_path!!,
                                            collaboration_nickname!!,
                                            collabo_email!!,
                                            collaboration_profile!!,
                                            date!!,
                                            kinds!!,
                                            mr_idx!!,
                                            token!!,
                                            col_token!!,
                                            isLiked!!,
                                            "송포스트"
                                        ),
                                        token.toString()
                                    ).also {
                                        sendNotification(it)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                        }
                    })
            }
        }


        binding.fragmentPostTvLike.text=cnt_like
        // 사용자가 좋아요 눌렀는지 안눌렀는지 확인
        val user_email=LoginActivity.user_info.loginUserEmail
        if(isLiked.equals(user_email)){
            binding.fragmentPostIvLike.background=ContextCompat.getDrawable(requireContext(),R.drawable.like)
        }else{
            binding.fragmentPostIvLike.background=ContextCompat.getDrawable(requireContext(),R.drawable.non_like)
        }

        // 좋아요 클릭
        binding.fragmentPostIvLike.setOnClickListener {
            clickLike()
        }


        return binding.root
    }

    fun deletePost() {
        idx?.let {
            retrofitService.deleteMyPost(it).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        Log.e("DetailDuetFragment", "deleteSong() 통신 성공: ${response.body().toString()}")
                        val jsonObject = JSONObject(response.body().toString())
                        val result= jsonObject.getString("result")
                        if(result.equals("true")){
                            val builder = AlertDialog.Builder(requireContext())
                            builder.setMessage("삭제되었습니다 ")
                            builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
                                // 이전화면으로 이동
                                val totalFragment = TotalFragment()
                                requireActivity().supportFragmentManager.beginTransaction().replace(
                                    R.id.activity_main_frame,totalFragment).addToBackStack(null).commit()
                            }
                            builder.show()
                        }

                    } else {
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                        Log.e("DetailDuetFragment", "deleteSong() 응답 문제" + response.code())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("DetailDuetFragment", "deleteSong()  통신 실패" + t.message)
                }


            })
        }
    }

    fun goToLookAtProfileActivity(getEmail : String,getNickname : String,getProfile : String){
        if (LoginActivity.user_info.loginUserEmail.equals(getEmail)){
            Toast.makeText(requireContext(), "회원님의 프로필 입니다. 마이페이지에서 확인해주세요", Toast.LENGTH_SHORT).show()
            return
        }else{
            retrofit= RetrofitClient.getInstance()
            retrofitService=retrofit.create(RetrofitService::class.java)
            retrofitService.requestLookAtUserProfile(
                getEmail,
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
                                var isBadge2 = jsonObject.getBoolean("isBadge")
                                val intent = Intent(
                                    context,
                                    LookAtUserProfileActivity::class.java
                                )
                                intent.putExtra("otherUserEmail", getEmail)
                                Log.d(TAG, getEmail)
                                intent.putExtra("nickname", getNickname)
                                intent.putExtra("profile", getProfile)
                                intent.putExtra("followingUserNumber", followingUserNumber)
                                intent.putExtra("followUserNumber", followUserNumber)
                                intent.putExtra("isFollow", isFollow)
                                intent.putExtra("isBadge", isBadge2)
                                startActivity(intent)
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })


        }
    }

    ////fcm send 메세지 && 코루틴 launch
    private fun sendNotification(notification: PushSongPostNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postSongPostNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: 성공")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun clickLike() {
        val userEmail=LoginActivity.user_info.loginUserEmail
        idx?.let {
            userEmail.let { it1 ->
                retrofitService.requestSongPostLike(it, it1,email!!).enqueue(object : Callback<String> {
                    // 통신에 성공한 경우
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val jsonObject = JSONObject(response.body().toString())
                            total_like =jsonObject.getString("cnt_like")
                            val isLike = jsonObject.getString("isLike")
                            Log.e(TAG,"clickLike() total_like: $total_like")
                            Log.e(TAG,"clickLike() isLike: $isLike")
                            Log.e(TAG,"clickLike() col_token: $col_token")
                            binding.fragmentPostTvLike.text=total_like
                            if(isLike.equals("true")) {
                                binding.fragmentPostIvLike.background=ContextCompat.getDrawable(requireContext(),R.drawable.like)
                            }else{
                                binding.fragmentPostIvLike.background=ContextCompat.getDrawable(requireContext(),R.drawable.non_like)
                            }
                            //FCM 보내기
                            if (LoginActivity.user_info.loginUserEmail.equals(email) || userLeaveCheck.equals("1")) {
                            } else if(isLike.equals("true")){
                                PushSongPostNotification(
                                    SongPostNotificationData(
                                        "SingTogether",
                                        nickname + "님의 포스팅에 " + LoginActivity.user_info.loginUserNickname + " 님이 좋아요를 누르셨습니다.",
                                        idx!!,
                                        thumbnail!!,
                                        title!!,
                                        singer!!,
                                        cnt_play!!,
                                        cnt_reply!!,
                                        total_like!!,
                                        nickname!!,
                                        email!!,
                                        profile!!,
                                        song_path!!,
                                        collaboration_nickname!!,
                                        collabo_email!!,
                                        collaboration_profile!!,
                                        date!!,
                                        kinds!!,
                                        mr_idx!!,
                                        token!!,
                                        col_token!!,
                                        isLiked!!,
                                        "송포스트"
                                    ),
                                    token.toString()
                                ).also {
                                    sendNotification(it)
                                }
                            }

                        } else {
                            // 통신은 성공했지만 응답에 문제가 있는 경우
                            Log.e(TAG, "clickLike() 응답 문제" + response.code())
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e(TAG, "clickLike() 통신 실패" + t.message)
                    }


                })
            }
        }
    }

    fun postReviewLoad(recyclerview : RecyclerView){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetMainPostReview(idx.toString())
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        postReviewDataList.clear()

                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val idx = jsonObject.getString("idx")
                            val uploadUserEmail = jsonObject.getString("uploadUserEmail")
                            val uploadUserProfile = jsonObject.getString("uploadUserProfile")
                            val uploadUserNickname = jsonObject.getString("uploadUserNickname")
                            val review = jsonObject.getString("review")
                            val uploadDate = jsonObject.getString("uploadDate")
                            val postIdx = jsonObject.getString("postIdx")


                            val postReviewData = PostReviewData(
                                idx,
                                uploadUserEmail,
                                uploadUserNickname,
                                uploadUserProfile,
                                review,
                                uploadDate,
                                postIdx
                            )

                            postReviewDataList.add(postReviewData)
                            postReviewAdapter.notifyDataSetChanged()
                            recyclerview.scrollToPosition(
                                postReviewDataList.size - 1
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.release()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PostFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}