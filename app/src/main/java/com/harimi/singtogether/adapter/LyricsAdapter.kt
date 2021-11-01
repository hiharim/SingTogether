package com.harimi.singtogether.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.LyricsData
import com.harimi.singtogether.R
import com.harimi.singtogether.sing.MergeAudioActivity
import com.harimi.singtogether.sing.RecordActivity
import com.harimi.singtogether.sing.Video2Activity
import kotlin.time.seconds


// 가사 어댑터
class LyricsAdapter(val lyricsList: ArrayList<LyricsData>): RecyclerView.Adapter<LyricsAdapter.LyricsViewHolder>() {

    var time="";
    var baseIndex = 0

    // 리스트 아이템에 있는 뷰 참조
    inner class LyricsViewHolder(v: View) : RecyclerView.ViewHolder(v){
        val seconds=v.findViewById<TextView>(R.id.rv_lyrics_tv_seconds)
        val line=v.findViewById<TextView>(R.id.rv_lyrics_tv_line)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LyricsAdapter.LyricsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_lyrics, parent, false)
        return LyricsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LyricsAdapter.LyricsViewHolder, position: Int) {
        val curData = lyricsList[position]
        holder.seconds.text=curData.seconds
        holder.line.text=curData.line
        val mSeconds=holder.seconds.text.substring(1,5)

        var mTime=mSeconds.toFloat()
        time= RecordActivity.time_info.pTime
        var now_Time=time.toFloat()


        if(mTime<=now_Time) {
            //holder.line.setTextColor(Color.parseColor("#fc9d9a")) // 핑크
            holder.line.setTextColor(Color.parseColor("#fe4365")) // 보라색
        }else{
            //holder.line.setTextColor(Color.parseColor("#a3a1a1")) //그레이

            if(holder.seconds.text.contains("ww")){
                holder.line.setTextColor(Color.parseColor("#ffe4e1")) // 연핑크
            }else if(holder.seconds.text.contains("mm")){
                holder.line.setTextColor(Color.parseColor("#add8e6")) // 파랑
            }else if(holder.seconds.text.contains("gg")){
                holder.line.setTextColor(Color.parseColor("#a3a1a1")) //그레이
            }

        }

    }



    override fun getItemCount(): Int=lyricsList.size
}