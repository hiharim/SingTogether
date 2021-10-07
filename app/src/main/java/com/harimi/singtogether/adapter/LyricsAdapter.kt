package com.harimi.singtogether.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.LyricsData
import com.harimi.singtogether.R


// 가사 어댑터
class LyricsAdapter(val lyricsList: ArrayList<LyricsData>, private var time: String): RecyclerView.Adapter<LyricsAdapter.LyricsViewHolder>() {


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

        Log.e("가사어댑터", "time 전 : " + "["+time+"]")


        if(holder.seconds.text.contains("["+time+"]")) {
            Log.e("가사어댑터", "time 후 : " + time)
            holder.line.setTextColor(Color.BLUE)

        }else {
            holder.line.setTextColor(Color.BLACK)
        }

//        lyricsList.forEach {
//            if(holder.line.text.contains("["+time+"]")) {
//                holder.line.setTextColor(Color.BLUE)
//                return@forEach
//            }
//        }


    }

    override fun getItemCount(): Int=lyricsList.size
}