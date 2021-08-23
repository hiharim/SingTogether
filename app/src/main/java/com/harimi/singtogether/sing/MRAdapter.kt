package com.harimi.singtogether.sing

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.R
import org.w3c.dom.Text


class MRAdapter(val mrList:ArrayList<MRData>) : RecyclerView.Adapter<MRAdapter.MRViewHolder>() {

    // 리스트 아이템에 있는 뷰 참조
    inner class MRViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val idx=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_idx)
        val title=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_signer)
        val song_path=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_song_path)
        val genre=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_genre)
        val lyrics=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_lyrics)
    }

    //뷰홀더가 처음 생성될때
   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MRViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_m_r,parent,false)

        return MRViewHolder(view)
//        return MRViewHolder(view).apply {
//            itemView.setOnClickListener{
//                // 클릭한 뷰의 순서값
//                val curPos : Int = adapterPosition
//                // 객체 형태로 번호 맞게 가져오기
//                val idx : MRData= mrList.get(curPos)
//                val title : MRData = mrList.get(curPos)
//                val singer : MRData = mrList.get(curPos)
//                val song_path : MRData = mrList.get(curPos)
//                val genre : MRData = mrList.get(curPos)
//                val intent= Intent(itemView?.context,BeforeSingActivity::class.java)
//                ContextCompat.startActivity(itemView?.context,intent,null)
//            }
//        }
    }

    //재활용해주는곳 및 값을 넣어주는 곳
    override fun onBindViewHolder(holder: MRViewHolder, position: Int) {
        val curData = mrList[position]
        holder.idx.text= curData.idx.toString()
        holder.title.text=curData.title
        holder.singer.text=curData.singer
        holder.song_path.text=curData.song_path
        holder.genre.text=curData.genre
        holder.lyrics.text=curData.lyrics

        holder.itemView.setOnClickListener { v->
            // BeforeSingActivity 로 이동
            val intent= Intent(v.context,BeforeSingActivity::class.java)
            intent.putExtra("IDX",curData.idx)
            intent.putExtra("TITLE",curData.title)
            intent.putExtra("SINGER",curData.singer)
            intent.putExtra("SONG_PATH",curData.song_path)
            intent.putExtra("GENRE",curData.genre)
            intent.putExtra("LYRICS",curData.lyrics)
            ContextCompat.startActivity(v.context,intent,null)
        }


    }

    // 리스트 개수 반환
    override fun getItemCount(): Int = mrList.size

}