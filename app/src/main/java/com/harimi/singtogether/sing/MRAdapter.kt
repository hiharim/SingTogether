package com.harimi.singtogether.sing

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.R


class MRAdapter(val mrList: ArrayList<MRData>) : RecyclerView.Adapter<MRAdapter.MRViewHolder>(), Filterable {

    val unFilteredList = mrList //⑴ 필터 전 리스트
    var filteredList = mrList //⑵ 필터 중인 리스트

    // 리스트 아이템에 있는 뷰 참조
    inner class MRViewHolder(v: View) : RecyclerView.ViewHolder(v){
        val idx=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_idx)
        val title=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_title)
        val singer=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_signer)
        val song_path=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_song_path)
        val genre=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_genre)
        val lyrics=v.findViewById<TextView>(R.id.rv_fragment_m_r_tv_lyrics)
    }

    // 필터
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
//                if (charString.isEmpty()) {
//                    filteredList = mrList
//                } else {
//                    var filteringList = ArrayList<MRData>()
//                    for (item in mrList) {
//                        if (item.genre.contains(charString)) {
//                            filteringList.add(item)
//                            Log.e("mr어댑터","charString:" +charString)
//                        }
//                    }
//                    filteredList =filteringList
//                }

                filteredList = if (charString.isEmpty()) { //⑶
                    unFilteredList
                } else {
                    var filteringList = ArrayList<MRData>()
                    for (item in unFilteredList) {
                        if (item.genre == charString) filteringList.add(item)
                        Log.e("mr어댑터","charString:" +charString)
                    }
                    filteringList
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            //리사이클러뷰를 업데이트 해주는 작업
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as ArrayList<MRData>
                notifyDataSetChanged()
            }
        }
    }

    //뷰홀더가 처음 생성될때
   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MRViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(
            R.layout.rv_fragment_m_r,
            parent,
            false
        )
        return MRViewHolder(view)

    }

    //재활용해주는곳 및 값을 넣어주는 곳
    override fun onBindViewHolder(holder: MRViewHolder, position: Int) {
        //val curData = mrList[position]
        val curData = filteredList[position]
        holder.idx.text= curData.idx.toString()
        holder.title.text=curData.title
        holder.singer.text=curData.singer
        holder.song_path.text=curData.song_path
        holder.genre.text=curData.genre
        holder.lyrics.text=curData.lyrics

        holder.itemView.setOnClickListener { v->
            // BeforeSingActivity 로 이동
            val intent= Intent(v.context, BeforeSingActivity::class.java)
            intent.putExtra("IDX", curData.idx)
            intent.putExtra("TITLE", curData.title)
            intent.putExtra("SINGER", curData.singer)
            intent.putExtra("SONG_PATH", curData.song_path)
            intent.putExtra("GENRE", curData.genre)
            intent.putExtra("LYRICS", curData.lyrics)
            ContextCompat.startActivity(v.context, intent, null)
        }


    }

    // 리스트 개수 반환
    override fun getItemCount(): Int =
        filteredList.size
        //mrList.size


}