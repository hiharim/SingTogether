package com.harimi.singtogether

import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.FragmentDetailDuetBinding
import com.harimi.singtogether.databinding.FragmentPostBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.log

/**
 * 홈 포스트아이템 클릭하면 나오는 디테일 프래그먼트
 */

class PostFragment : Fragment() {
    var TAG :String = "PostFragment "

    private var idx : Int? = null // duet 테이블 idx
    private var mr_idx : Int? = null // mr 테이블 idx
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
    private var thumbnail : String? = null
    private var simpleExoPlayer: ExoPlayer?=null

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
            thumbnail=it.getString("thumbnail")

        }
    }

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

        if(binding.tvUploadUserNickName.text.equals(binding.tvUploadCollaboNickName.text)){
            binding.tvUploadCollaboNickName.visibility=View.GONE
            binding.ivUploadCollaboProfile.visibility=View.GONE
            binding.collaboCardView.visibility=View.GONE
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

        return binding.root
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