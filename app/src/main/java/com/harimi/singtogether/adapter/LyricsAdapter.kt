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
import kotlin.time.seconds


// 가사 어댑터
class LyricsAdapter(val lyricsList: ArrayList<LyricsData>, private var time: String): RecyclerView.Adapter<LyricsAdapter.LyricsViewHolder>() {

    var baseIndex = 0

    // 리스트 아이템에 있는 뷰 참조
    inner class LyricsViewHolder(v: View) : RecyclerView.ViewHolder(v){
        val seconds=v.findViewById<TextView>(R.id.rv_lyrics_tv_seconds)
        val next_seconds=v.findViewById<TextView>(R.id.rv_lyrics_tv_next_seconds)
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
        //holder.next_seconds.text=curData.next_seconds
        holder.line.text=curData.line
//        Log.e("가사어댑터", "time 전 : " + "[" + time + "]")

        val mTime=time.substring(3).toInt()
//        Log.e("가사어댑터", "mTime : " + mTime)
        val mSeconds=holder.seconds.text.substring(4,6).toInt()
//        Log.e("가사어댑터", "mSeconds : " + mSeconds)
//        val mNextSeconds=holder.next_seconds.text.substring(3).toInt()
//        Log.e("가사어댑터", "mNextSeconds : " + mNextSeconds)
        val mNextSeconds=holder.next_seconds.text.substring(3).toInt()
//        Log.e("가사어댑터", "mNextSeconds : " + mNextSeconds)


        var a=baseIndex
        var b=lyricsList.get(a)
        var c=b.seconds.substring(4,6).toInt()
        Log.e("가사어댑터", "index : " + "$c" )
        if(c <=mTime){

            Log.e("가사어댑터", "baseIndex : " + "$baseIndex")
            if (baseIndex.toString().equals(position.toString())) {
                baseIndex++
                Log.e("가사어댑터", "baseIndex : " + "$baseIndex")
                holder.line.setTextColor(Color.BLUE)
            }


        }else {
            holder.line.setTextColor(Color.BLACK)
        }

//        if(holder.seconds.text.contains(time)){
//            Log.e("가사어댑터", "time 후 : " + time)
//            //holder.line.setTextColor(Color.BLUE)
//            if(mTime<mNextSeconds)
//        }



//        if(holder.seconds.text.contains(time)) {
//            Log.e("가사어댑터", "time 후 : " + time)
//            //holder.line.setTextColor(Color.BLUE)
//            while (mTime<mNextSeconds) {
//                holder.line.setTextColor(Color.BLUE)
//                if(mTime>mNextSeconds){
//                    break
//                }
//            }
//
//        }else {
//            holder.line.setTextColor(Color.BLACK)
//        }


//        for (i in lyricsList){
//            if(holder.seconds.text.contains(time)){
//                while (holder.seconds.text.substring(4,6).toInt()<holder.next_seconds.text.substring(3).toInt()){
//                    holder.line.setTextColor(Color.BLUE)
//                }
//            }else{
//                holder.line.setTextColor(Color.BLACK)
//            }
//        }


//        for(i in lyricsList){
//            while (holder.seconds.text.substring(4,6).toInt()<holder.next_seconds.text.substring(3).toInt()){
//                if(holder.seconds.text.contains(time)){
//                    holder.line.setTextColor(Color.BLUE)
//                }
//            }
//        }



//        for ((index,value) in lyricsList.withIndex()) {
//                val a=baseIndex
//                val b=lyricsList.get(a)
//                val c=b.seconds.substring(4,6).toInt()
////            Log.e("가사어댑터", "index : " + "$index" )
//                Log.e("가사어댑터", "c : " + c )
//                Log.e("가사어댑터", "mTime : " + mTime)
//                if(c <=mTime){
//                    if (baseIndex == index){
//                        Log.e("가사어댑터", "index : " + "$index" )
//                        holder.line.setTextColor(Color.BLUE)
//                        baseIndex++
//                        Log.e("가사어댑터", "baseIndex : " + "$baseIndex")
//                    }else{
//
//                    }
//                }else{
//                    Log.e("가사어댑터", "else " )
////                holder.line.setTextColor(Color.BLACK)
//                }
//



//            val a=index+1
//            Log.e("가사어댑터", "index+1 : " + a)
//            if(lyricsList.size==a){
//                break
//            }else{

//                    val b=lyricsList.get(a)
//                    Log.e("가사어댑터", "lyricsList.size-index : " + b)
//                    val c=b.seconds.substring(4,6).toInt()
//                    Log.e("가사어댑터", "c : " + c)
//                    if (mSeconds <= mTime  && mTime < c){
//                        holder.line.setTextColor(Color.BLUE)
//                    }
//                    else if(mTime<mSeconds && mTime>c){
//                        holder.line.setTextColor(Color.BLACK)
//                    }else{
////                    lyricsList.get(index).line
//                        holder.line.setTextColor(Color.BLACK)
//                    }

//            }

//            Log.e("가사어댑터", "value.seconds : " + value.seconds)


//        }


//
//        if(holder.seconds.text.contains(time)){
//            while(mTime<=mSeconds){
//                holder.line.setTextColor(Color.BLUE)
//
//                if (mTime>mSeconds){
//                    continue
//                }
//            }
//        }else{
//            holder.line.setTextColor(Color.BLACK)
//        }



//        for (i in lyricsList){
//            while (holder.seconds.text==holder.next_seconds.text){
//                if(holder.seconds.text.contains(time)) {
//                        Log.e("가사어댑터", "time 후 : " + time)
//                        holder.line.setTextColor(Color.BLUE)
//
//                }else {
//                    holder.line.setTextColor(Color.BLACK)
//                }
//            }
//        }

//        lyricsList.forEach {
//            if (holder.seconds.text.contains(time)) {
//                Log.e("가사어댑터", "time 후 : " + time)
//                holder.line.setTextColor(Color.BLUE)
//
//            } else {
//                holder.line.setTextColor(Color.BLACK)
//            }
//        }


//        for (i in lyricsList.indices) {
//            if(holder.seconds.text.contains(time)) {
//                Log.e("가사어댑터", "time 후 : " + time)
//                //holder.line.setTextColor(Color.BLUE)
//                while (holder.seconds.text.equals(holder.next_seconds.text)){
//                    holder.line.setTextColor(Color.RED)
//                    if(holder.seconds.text!=holder.next_seconds.text){
//                     continue
//                    }
//                }
//
//            }else {
//                holder.line.setTextColor(Color.BLACK)
//            }
//
//        }




    }

    override fun getItemCount(): Int=lyricsList.size
}