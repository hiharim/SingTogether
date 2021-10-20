package com.harimi.singtogether

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.harimi.singtogether.Data.PostReviewData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.PostFragmentReviewAdapter
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import com.harimi.singtogether.databinding.FragmentPostBinding
import de.hdodenhof.circleimageview.CircleImageView
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

    private var idx : Int? = null // duet 테이블 idx
    private var mr_idx : Int? = null // mr 테이블 idx
    private var title : String? = null
    private var singer : String? = null
    private var cnt_play : String? = null
    private var cnt_reply : String? = null
    private var cnt_like : String? = null
    private var nickname : String? = null
    private var collaboration_nickname : String? = null
    private var song_path : String? = null
    private var profile : String? = null
    private var collaboration_profile : String? = null
    private var date : String? = null
    private var simpleExoPlayer: ExoPlayer?=null

    private val postReviewDataList: ArrayList<PostReviewData> = ArrayList()
//    private lateinit var rv_detailReplayReview : RecyclerView
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
            collaboration_nickname=it.getString("collaboration_nickname")
            song_path=it.getString("song_path")
            profile=it.getString("profile")
            collaboration_profile=it.getString("collaboration_profile")
            date=it.getString("date")

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding= FragmentPostBinding.inflate(inflater,container,false)

        binding.tvUploadUserNickName.text=nickname
        binding.tvUploadCollaboNickName.text=collaboration_nickname
        binding.tvSongTitle.text=title
        binding.tvSinger.text=singer
        binding.tvHits.text=cnt_play
        binding.tvReviewNumber.text=cnt_reply
        binding.tvUploadDate.text=date
        Glide.with(this).load("http://3.35.236.251/"+profile).into(binding.ivUploadUserProfile)
        Glide.with(this).load("http://3.35.236.251/"+collaboration_profile).into(binding.ivUploadCollaboProfile)
        Log.e("디테일프래그","duet_path"+song_path)


        binding.fragmentPostRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fragmentPostRecyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        postReviewAdapter = PostFragmentReviewAdapter(postReviewDataList, requireContext())
        binding.fragmentPostRecyclerView.adapter = postReviewAdapter


        postReviewLoad(binding.fragmentPostRecyclerView)

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
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                        }
                    })
            }
        }

        return binding.root
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