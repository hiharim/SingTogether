package com.harimi.singtogether.sing

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent

import android.net.Uri
import android.os.Build
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
import com.google.android.gms.common.api.Api
import com.harimi.singtogether.*
import com.harimi.singtogether.Data.DetailDuetReviewData
import com.harimi.singtogether.Network.*
import com.harimi.singtogether.adapter.DetailDuetReviewAdapter
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import com.harimi.singtogether.databinding.FragmentDuetBinding
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
import java.util.*
import kotlin.collections.ArrayList


/**
 * 듀엣 아이템 상세화면 프래그먼트
 */
class DetailDuetFragment : Fragment() {

    var TAG :String = "DetailDuetFragment "
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit
    private lateinit var binding: FragmentDetailDuetBinding
    private var duet_idx : Int? = null // duet 테이블 idx
    private var mr_idx : Int? = null // mr 테이블 idx

    private var idx : Int? = null // duet 테이블 idx
    private var thumbnail: String? = null
    private var title : String? = null
    private var singer : String? = null
    private var cnt_play : String? = null
    private var cnt_reply : String? = null
    private var cnt_duet : String? = null
    private var nickname : String? = null
    private var email : String? = null
    private var duet_path : String? = null // 사용자 오디오/비디오
    private var mr_path : String? = null // mr
    private var extract_path : String? = null //
    private var profile : String? = null
    private var date : String? = null
    private var kinds : String? = null
    private var lyrics : String? = null
    private var token : String? = null
    private var isBadge : String? = null
    private var simpleExoPlayer: ExoPlayer?=null

    private val detailDuetReviewList: ArrayList<DetailDuetReviewData> = ArrayList()
    private lateinit var detailDuetReviewAdapter: DetailDuetReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            duet_idx=it.getInt("duet_idx")
            mr_idx=it.getInt("mr_idx")
            thumbnail=it.getString("thumbnail")
            title=it.getString("title")
            singer=it.getString("singer")
            cnt_play=it.getString("cnt_play")
            cnt_reply=it.getString("cnt_reply")
            cnt_duet=it.getString("cnt_duet")
            nickname=it.getString("nickname")
            email=it.getString("email")
            duet_path=it.getString("duet_path")
            mr_path=it.getString("mr_path")
            extract_path=it.getString("extract_path")
            profile=it.getString("profile")
            date=it.getString("date")
            kinds=it.getString("kinds")
            lyrics=it.getString("lyrics")
            token=it.getString("token")
            isBadge=it.getString("isBadge")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentDetailDuetBinding.inflate(inflater,container,false)

        //듀엣참여 버튼 클릭
        binding.fragmentDetailDuetBtnJoin.setOnClickListener {
            // DuetActivity 로 이동
            val intent= Intent(context,DuetActivity::class.java)
            intent.putExtra("duet_idx",duet_idx)
            intent.putExtra("mr_idx",mr_idx)
            intent.putExtra("title",title)
            intent.putExtra("singer",singer)
            intent.putExtra("nickname",nickname)
            intent.putExtra("email",email)
            intent.putExtra("duet_path",duet_path)
            intent.putExtra("mr_path",mr_path)
            intent.putExtra("extract_path",extract_path)
            intent.putExtra("profile",profile)
            intent.putExtra("kinds",kinds)
            intent.putExtra("lyrics",lyrics)
            startActivity(intent)
        }

        // 듀엣 완성 포스팅 text
        val count=cnt_duet+"개"

        if(cnt_duet.equals("0")){
            binding.fragmentDetailDuetTvComplete.text="듀엣 완성 포스팅 보기"
        }else{
            binding.fragmentDetailDuetTvComplete.text=count+"의 듀엣 완성 포스팅 보기"
        }

        // 듀엣 완성 포스팅 보기
        binding.fragmentDetailDuetTvComplete.setOnClickListener {
            if(cnt_duet.equals("0")){
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("현재 포스팅 중인 듀엣 완성곡이 없습니다. 듀엣 참여하시고 포스팅 해보세요!")
                builder.setPositiveButton("닫기") { dialogInterface: DialogInterface, i: Int ->
                }
                builder.show()
            }else{
                val completeDuetFragment =CompleteDuetFragment()
                var bundle =Bundle()
                bundle.putInt("duet_idx", duet_idx!!)
                bundle.putString("cnt_duet",cnt_duet!!)
                completeDuetFragment.arguments=bundle
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.activity_main_frame,completeDuetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

            binding.tvUploadUserNickName.text=nickname
            binding.tvSongTitle.text=title
            binding.tvSinger.text=singer
            binding.tvHits.text=cnt_play
            //조회수 처리
//            var plus_cnt_play= cnt_play!!.toInt()
//            plus_cnt_play++
//            binding.tvHits.text= plus_cnt_play.toString()

            binding.tvUploadDate.text=date
            //프로필
            Glide.with(this).load("http://3.35.236.251/" + profile).into(binding.ivUploadUserProfile)

            // 뱃지
            if(isBadge.equals("true")){
                binding.duetBadge.visibility=View.VISIBLE
            }else{
                binding.duetBadge.visibility=View.GONE
            }

            if(kinds.equals("녹음")){
                Glide.with(this).load(thumbnail).into(binding.imageViewThumb)
                binding.imageViewThumb.visibility=View.VISIBLE
            }

            binding.fragmentDetailDuetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.fragmentDetailDuetRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            detailDuetReviewAdapter = DetailDuetReviewAdapter(detailDuetReviewList, requireContext())
            binding.fragmentDetailDuetRecyclerView.adapter = detailDuetReviewAdapter

            detailDuetReviewLoad(binding.fragmentDetailDuetRecyclerView)

            // 빌드 시 context 가 필요하기 때문에 context 를 null 체크 해준 뒤 빌드
            context?.let{
                simpleExoPlayer= SimpleExoPlayer.Builder(it).build()
            }
            binding.pv.player = simpleExoPlayer
            val factory: DataSource.Factory = DefaultDataSourceFactory(
                requireContext(),
                "ExoPlayer"
            )
            var mediaItem = MediaItem.fromUri(Uri.parse(duet_path))
            val progressiveMediaSource = ProgressiveMediaSource.Factory(factory)
                .createMediaSource(mediaItem)
            simpleExoPlayer!!.setMediaSource(progressiveMediaSource)
            simpleExoPlayer!!.prepare()
            simpleExoPlayer!!.play()

            initRetrofit()
            val login_user= LoginActivity.user_info.loginUserEmail
            if(email.equals(login_user)) {
                binding.fragmentDetailDuetBtnDelete.visibility=View.VISIBLE
                // 게시물 삭제
                binding.fragmentDetailDuetBtnDelete.setOnClickListener {
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
                                    deleteSong()
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

            // 뒤로가기버튼 클릭
            binding.fragmentDetailDuetBtnBack.setOnClickListener {
                val singFragment = SingFragment()
                activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.remove(this)
                    ?.replace(R.id.activity_main_frame,singFragment)
                    ?.commit()
            }

            //프로필 액티비티로 넘어가기
            binding.ivUploadUserProfile.setOnClickListener{
                if (LoginActivity.user_info.loginUserEmail.equals(email)){
//                val activity =it!!.context as AppCompatActivity
//                val MyPageFragment = MyPageFragment()
//                var bundle =Bundle()
//                activity.supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.activity_main_frame,MyPageFragment)
//                    .commit()
                    Toast.makeText(requireContext(), "회원님의 프로필 입니다. 마이페이지에서 확인해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }else{
                    retrofit= RetrofitClient.getInstance()
                    retrofitService=retrofit.create(RetrofitService::class.java)
                    retrofitService.requestLookAtUserProfile(
                        email!!,
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
                                        var isBadge = jsonObject.getBoolean("isBadge")

                                        val intent = Intent(
                                            context,
                                            LookAtUserProfileActivity::class.java
                                        )
                                        intent.putExtra("otherUserEmail", email)
                                        Log.d(TAG, email)
                                        intent.putExtra("nickname", nickname)
                                        intent.putExtra("profile", profile)
                                        intent.putExtra("followingUserNumber", followingUserNumber)
                                        intent.putExtra("followUserNumber", followUserNumber)
                                        intent.putExtra("isFollow", isFollow)
                                        intent.putExtra("isBadge", isBadge)
                                        startActivity(intent)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                            }
                        })


                }
            }


            ///댓글달기
            binding.ivUploadReview.setOnClickListener {
                var uploadReview = binding.etWriteReview.text.toString()
                if (uploadReview.equals("")){

                    Toast.makeText(requireContext(), "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }else{
                    var uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                    retrofit= RetrofitClient.getInstance()
                    retrofitService=retrofit.create(RetrofitService::class.java)
                    retrofitService.requestWriteDetailDuetReview(
                        duet_idx.toString()!!,
                        LoginActivity.user_info.loginUserEmail,
                        LoginActivity.user_info.loginUserProfile,
                        LoginActivity.user_info.loginUserNickname,
                        uploadReview,
                        uploadDate
                    )
                        .enqueue(object : Callback<String> {
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {
                                if (response.isSuccessful) {
                                    val body = response.body().toString()
                                    Log.d(TAG, body)
                                    var jsonObject = JSONObject(response.body().toString())
                                    var result = jsonObject.getBoolean("result")
                                    if (result) {
                                        binding.etWriteReview.setText("")
                                        var getIdx = jsonObject.getString("idx")
                                        val detailDuetReviewData = DetailDuetReviewData(
                                            getIdx,
                                            LoginActivity.user_info.loginUserEmail,
                                            LoginActivity.user_info.loginUserNickname,
                                            LoginActivity.user_info.loginUserProfile,
                                            uploadReview,
                                            uploadDate,
                                            duet_idx.toString()!!
                                        )
                                        detailDuetReviewList.add(detailDuetReviewData)
                                        detailDuetReviewAdapter.notifyDataSetChanged()
                                        binding.fragmentDetailDuetRecyclerView.scrollToPosition(
                                            detailDuetReviewList.size - 1
                                        )
                                    }

                                    //FCM 보내기
                                    if (LoginActivity.user_info.loginUserEmail.equals(email)) {

                                    } else {
                                        PushDuetNotification(
                                            DuetNotificationData(
                                                "SingTogether",
                                                nickname + "님의 듀엣초대 게시물에 " + LoginActivity.user_info.loginUserNickname + " 님이 댓글을 남겼습니다.",
                                                duet_idx!!,
                                                mr_idx!!,
                                                thumbnail!!,
                                                title!!,
                                                singer!!,
                                                cnt_play!!,
                                                cnt_reply!!,
                                                cnt_duet!!,
                                                email!!,
                                                nickname!!,
                                                profile!!,
                                                duet_path!!,
                                                date!!,
                                                mr_path!!,
                                                extract_path!!,
                                                kinds!!,
                                                lyrics!!,
                                                token!!,
                                                "듀엣"
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


            return binding.root
    }


    ////fcm send 메세지 && 코루틴 launch
    private fun sendNotification(notification: PushDuetNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postDuetNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: 성공")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun detailDuetReviewLoad(recyclerview: RecyclerView) {
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetDetailDuetReview(duet_idx.toString())
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        detailDuetReviewList.clear()

                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val idx = jsonObject.getString("idx")
                            val uploadUserEmail = jsonObject.getString("uploadUserEmail")
                            val uploadUserProfile = jsonObject.getString("uploadUserProfile")
                            val uploadUserNickname = jsonObject.getString("uploadUserNickname")
                            val review = jsonObject.getString("review")
                            val uploadDate = jsonObject.getString("uploadDate")
                            val detailDuetIdx = jsonObject.getString("detailDuetIdx")


                            val detailDuetReviewData = DetailDuetReviewData(
                                idx,
                                uploadUserEmail,
                                uploadUserNickname,
                                uploadUserProfile,
                                review,
                                uploadDate,
                                detailDuetIdx
                            )
                            detailDuetReviewList.add(detailDuetReviewData)
                            detailDuetReviewAdapter.notifyDataSetChanged()
                            recyclerview.scrollToPosition(
                                detailDuetReviewList.size - 1
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }

    fun deleteSong() {
        duet_idx?.let {
            retrofitService.deleteMySong(it).enqueue(object : Callback<String> {
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
                                val singFragment = SingFragment()
                                requireActivity().supportFragmentManager.beginTransaction().replace(
                                    R.id.activity_main_frame,singFragment).addToBackStack(null).commit()
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

    private fun initRetrofit() {
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }


    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.release()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailDuetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}