package com.harimi.singtogether.sing

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.harimi.singtogether.Data.DuetData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.SettingFragment
import com.harimi.singtogether.SingFragment
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * 듀엣 아이템 상세화면 프래그먼트
 */
class DetailDuetFragment : Fragment() {

    private var idx : Int? = null // duet 테이블 idx
    private var title : String? = null
    private var singer : String? = null
    private var cnt_play : String? = null
    private var cnt_reply : String? = null
    private var cnt_duet : String? = null
    private var nickname : String? = null
    private var duet_path : String? = null // 사용자 오디오/비디오
    private var mr_path : String? = null // mr
    private var extract_path : String? = null //
    private var profile : String? = null
    private var date : String? = null
    private var simpleExoPlayer: ExoPlayer?=null
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idx=it.getInt("idx")
            title=it.getString("title")
            singer=it.getString("singer")
            cnt_play=it.getString("cnt_play")
            cnt_reply=it.getString("cnt_reply")
            cnt_duet=it.getString("cnt_duet")
            nickname=it.getString("nickname")
            duet_path=it.getString("duet_path")
            mr_path=it.getString("mr_path")
            extract_path=it.getString("extract_path")
            profile=it.getString("profile")
            date=it.getString("date")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentDetailDuetBinding.inflate(inflater,container,false)
        Log.e("디테일프래그","idx"+idx)
        //듀엣참여 버튼 클릭
        binding.fragmentDetailDuetBtnJoin.setOnClickListener {
            // DuetActivity 로 이동
            val intent= Intent(context,DuetActivity::class.java)
            intent.putExtra("idx",idx)
            intent.putExtra("title",title)
            intent.putExtra("singer",singer)
            intent.putExtra("nickname",nickname)
            intent.putExtra("duet_path",duet_path)
            intent.putExtra("mr_path",mr_path)
            intent.putExtra("extract_path",extract_path)
            intent.putExtra("profile",profile)
            startActivity(intent)
        }

        binding.tvUploadUserNickName.text=nickname
        binding.tvSongTitle.text=title
        binding.tvSinger.text=singer
        binding.tvHits.text=cnt_play
        binding.tvReviewNumber.text=cnt_reply
        binding.tvUploadDate.text=date
        Glide.with(this).load("http://3.35.236.251/"+profile).into(binding.ivUploadUserProfile)
        Log.e("디테일프래그","duet_path"+duet_path)

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
        // 게시물 삭제
        binding.fragmentDetailDuetBtnDelete.setOnClickListener {
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

        return binding.root
    }

    fun deleteSong(){
        idx?.let {
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