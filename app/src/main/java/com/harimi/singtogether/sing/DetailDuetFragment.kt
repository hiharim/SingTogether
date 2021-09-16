package com.harimi.singtogether.sing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import de.hdodenhof.circleimageview.CircleImageView


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

        return binding.root
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