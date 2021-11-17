package com.harimi.singtogether

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.BestDuetData
import com.harimi.singtogether.Data.BestSoloData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.adapter.BestDuetAdapter
import com.harimi.singtogether.adapter.BestSoloAdapter
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GloryActivity : AppCompatActivity() {
    private var TAG :String = "GloryActivity_"
    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    private lateinit var iv_backArrow : ImageView
    private lateinit var tv_year : TextView
    private lateinit var iv_forwardArrow : ImageView
    private lateinit var rv_bestDuet : RecyclerView
    private lateinit var rv_bestSolo : RecyclerView
    private lateinit var tv_duetText : TextView
    private lateinit var tv_soloText : TextView
    private lateinit var ib_back : ImageButton


    private lateinit var currentYear : String
    private lateinit var getYear : String
    private lateinit var getMinYear : String
    private  var choiceGetYear : String ?= ""


    private val bestDuetList: ArrayList<BestDuetData> = ArrayList()
    private lateinit var bestDuetAdapter: BestDuetAdapter

    private val bestSoloList: ArrayList<BestSoloData> = ArrayList()
    private lateinit var bestSoloAdapter: BestSoloAdapter

    override fun onResume() {
        super.onResume()

        bestDuetList.clear()
        bestSoloList.clear()
        bestSoloAdapter.notifyDataSetChanged()
        bestDuetAdapter.notifyDataSetChanged()
        loadGloryPost(getYear)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glory)

        initView()

    }


    private fun initView(){

        iv_backArrow = findViewById(R.id.iv_backArrow)
        tv_year = findViewById(R.id.tv_year)
        iv_forwardArrow = findViewById(R.id.iv_forwardArrow)
        rv_bestDuet = findViewById(R.id.rv_bestDuet)
        rv_bestSolo = findViewById(R.id.rv_bestSolo)
        tv_duetText = findViewById(R.id.tv_duetText)
        tv_soloText = findViewById(R.id.tv_soloText)
        ib_back = findViewById(R.id.ib_back)

        rv_bestDuet.layoutManager = LinearLayoutManager(this)
//        rv_bestDuet.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        bestDuetAdapter = BestDuetAdapter(bestDuetList, this)
        rv_bestDuet.adapter = bestDuetAdapter

        rv_bestSolo.layoutManager = LinearLayoutManager(this)
//        rv_bestSolo.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        bestSoloAdapter = BestSoloAdapter(bestSoloList, this)
        rv_bestSolo.adapter = bestSoloAdapter

        var nowYear : String = SimpleDateFormat("yyyy-MM-dd").format(Date())
//        var nowYear = "2022-1-1"
        var timeArr = nowYear.split("-")
        var year = timeArr[0].toInt() -1
        ////최대 년도 비교군 .
        getYear = year.toString()
        currentYear = getYear.toString()

        Log.d(TAG,  getYear)
        Log.d(TAG,  currentYear)
        //처음 액티비티볼때 forwardArrow  색깔 변경해주기
        iv_forwardArrow.setColorFilter(Color.parseColor("#88000000"))

        //뒤로가기 화살표 눌렀을때
        iv_backArrow.setOnClickListener {
            if (currentYear.equals(getMinYear)){
                Toast.makeText(this,"뒤의 게시물이 없습니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var getCurrentYear : Int = currentYear.toInt()-1
            currentYear = getCurrentYear.toString()
            Log.d(TAG, "iv_backArrow: "+ currentYear)
            if (currentYear.equals(getMinYear)){
                iv_backArrow.setColorFilter(Color.parseColor("#88000000"))
                iv_forwardArrow.setColorFilter(Color.parseColor("#000000"))
            }else{
                iv_backArrow.setColorFilter(Color.parseColor("#000000"))
                iv_forwardArrow.setColorFilter(Color.parseColor("#000000"))
            }

            bestDuetList.clear()
            bestSoloList.clear()
            bestDuetAdapter.notifyDataSetChanged()
            bestSoloAdapter.notifyDataSetChanged()
            loadGloryPost(getCurrentYear.toString())

            tv_year.setText(getCurrentYear.toString())
            tv_duetText.setText(getCurrentYear.toString()+"년도 싱투게더 베스트 듀엣")
            tv_soloText.setText(getCurrentYear.toString()+"년도 싱투게더 베스트 솔로")
        }

        //앞으로가기 화살표 눌렀을 때
        iv_forwardArrow.setOnClickListener {
            if (currentYear.equals(getYear)){
                Toast.makeText(this,"앞의 게시물이 없습니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var getCurrentYear : Int = currentYear.toInt()+1
            currentYear = getCurrentYear.toString()
            Log.d(TAG, "iv_backArrow: "+ currentYear)
            if (currentYear.equals(getYear)){
                iv_backArrow.setColorFilter(Color.parseColor("#88000000"))
                iv_backArrow.setColorFilter(Color.parseColor("#000000"))
            }else{
                iv_forwardArrow.setColorFilter(Color.parseColor("#000000"))
                iv_backArrow.setColorFilter(Color.parseColor("#000000"))
            }

            bestDuetList.clear()
            bestSoloList.clear()
            bestDuetAdapter.notifyDataSetChanged()
            bestSoloAdapter.notifyDataSetChanged()
            loadGloryPost(getCurrentYear.toString())

            tv_year.setText(getCurrentYear.toString())
            tv_duetText.setText(getCurrentYear.toString()+"년도 싱투게더 베스트 듀엣")
            tv_soloText.setText(getCurrentYear.toString()+"년도 싱투게더 베스트 솔로")
        }


        tv_year.setText(getYear)
        tv_duetText.setText(getYear.toString()+"년도 싱투게더 베스트 듀엣")
        tv_soloText.setText(getYear.toString()+"년도 싱투게더 베스트 솔로")
        //작년의 명예의전당 게시물들 올려주기
        loadGloryPost(getYear)


        ib_back.setOnClickListener {
            finish()
        }

        tv_year.setOnClickListener {
            alertDialog()
        }
    }

    private fun alertDialog(){
        ////dialog에 들어갈 리스트 셋해주기 , 동적사이즈와 value
        var list_one = ArrayList<String>()
        var plusInt : Int = 0
        for (i in getMinYear.toInt() .. getYear.toInt()){
            var plusYear = getMinYear.toInt()+plusInt
            list_one.add(plusYear.toString())
//            Log.d(TAG,  plusYear.toString())
            plusInt ++
        }
        var items = Array(list_one.size, {item->""})

        for (i in 0 until list_one.size) {
            items[i] = list_one.get(i)
        }

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("년도 선택")
        dialog.setCancelable(false)
        dialog.setSingleChoiceItems(items, -1) { dialog, which ->
            choiceGetYear = items[which]
        }

        dialog.setPositiveButton("선택"){dialog, which ->
            Log.d(TAG,  getYear)
            Log.d(TAG,  choiceGetYear)

            if (choiceGetYear.equals(getMinYear)){
                iv_backArrow.setColorFilter(Color.parseColor("#88000000"))
                iv_forwardArrow.setColorFilter(Color.parseColor("#000000"));
            }else if(choiceGetYear.equals(getYear)){
                iv_forwardArrow.setColorFilter(Color.parseColor("#88000000"));
                iv_backArrow.setColorFilter(Color.parseColor("#000000"));
            }else{
                iv_forwardArrow.setColorFilter(Color.parseColor("#000000"))
                iv_backArrow.setColorFilter(Color.parseColor("#000000"))
            }
            currentYear = choiceGetYear.toString()


            bestDuetList.clear()
            bestSoloList.clear()
            bestDuetAdapter.notifyDataSetChanged()
            bestSoloAdapter.notifyDataSetChanged()
            loadGloryPost(choiceGetYear!!)
            tv_year.setText(choiceGetYear)
            tv_duetText.setText(choiceGetYear+"년도 싱투게더 베스트 듀엣")
            tv_soloText.setText(choiceGetYear+"년도 싱투게더 베스트 솔로")
        }
        dialog.setNeutralButton("취소") {dialog, which ->

            dialog.dismiss()
        }
        dialog.show()
    }
    private fun loadGloryPost(choiceYear : String){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestLoadGloryPost(choiceYear, LoginActivity.user_info.loginUserEmail)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        bestDuetList.clear()
                        bestSoloList.clear()

                        val jsonObject = JSONObject(body)
                        val duetDataList = jsonObject.getString("DuetDataList")
                        val SoloDataList = jsonObject.getString("SoloDataList")
                        getMinYear = jsonObject.getString("getMinYear")


                        val duetDataArray = JSONArray(duetDataList)
                        for (i in 0..duetDataArray.length() - 1) {
                            val duetDataObject = duetDataArray.getJSONObject(i)
                            val idx = duetDataObject.getInt("idx")
                            val thumbnail = duetDataObject.getString("thumbnail")
                            val cnt_play = duetDataObject.getString("cnt_play")
                            val cnt_reply = duetDataObject.getString("cnt_reply")
                            val cnt_like = duetDataObject.getString("cnt_like")
                            val nickname = duetDataObject.getString("nickname")
                            val email = duetDataObject.getString("email")
                            val song_path = duetDataObject.getString("song_path")
                            val date = duetDataObject.getString("date")
                            val collaboration = duetDataObject.getString("collaboration")
                            val mr_idx = duetDataObject.getInt("mr_idx")
                            val title = duetDataObject.getString("title")
                            val singer = duetDataObject.getString("singer")
                            val lyrics = duetDataObject.getString("lyrics")
                            val profile = duetDataObject.getString("profile")
                            val collaboration_profile = duetDataObject.getString("col_profile")
                            val collabo_email = duetDataObject.getString("collabo_email")
                            val kinds = duetDataObject.getString("kinds")
                            val token = duetDataObject.getString("token")
                            val col_token = duetDataObject.getString("col_token")
                            val isLike = duetDataObject.getString("isLike")
                            val userLeaveCheck = duetDataObject.getString("userLeaveCheck")
                            val collaborationLeaveCheck = duetDataObject.getString("collaborationLeaveCheck")

                            val bestDuetData = BestDuetData(
                                idx,
                                thumbnail,
                                title,
                                singer,
                                lyrics,
                                cnt_play,
                                cnt_reply,
                                cnt_like,
                                nickname,
                                email,
                                profile,
                                song_path,
                                collaboration,
                                collabo_email,
                                collaboration_profile,
                                date,
                                kinds,
                                mr_idx,
                                token,
                                col_token,
                                isLike,
                                true,
                                true,
                                userLeaveCheck,
                                collaborationLeaveCheck
                            )
                            bestDuetList.add(0, bestDuetData)
                            bestDuetAdapter.notifyDataSetChanged()
                        }

                        val SoloDataArray = JSONArray(SoloDataList)
                        for (i in 0..SoloDataArray.length() - 1) {
                            val SoloDataObject = SoloDataArray.getJSONObject(i)
                            val idx = SoloDataObject.getInt("idx")
                            val thumbnail = SoloDataObject.getString("thumbnail")
                            val cnt_play = SoloDataObject.getString("cnt_play")
                            val cnt_reply = SoloDataObject.getString("cnt_reply")
                            val cnt_like = SoloDataObject.getString("cnt_like")
                            val nickname = SoloDataObject.getString("nickname")
                            val email = SoloDataObject.getString("email")
                            val song_path = SoloDataObject.getString("song_path")
                            val date = SoloDataObject.getString("date")
                            val collaboration = SoloDataObject.getString("collaboration")
                            val mr_idx = SoloDataObject.getInt("mr_idx")
                            val title = SoloDataObject.getString("title")
                            val singer = SoloDataObject.getString("singer")
                            val lyrics = SoloDataObject.getString("lyrics")
                            val profile = SoloDataObject.getString("profile")
                            val collaboration_profile = SoloDataObject.getString("col_profile")
                            val collabo_email = SoloDataObject.getString("collabo_email")
                            val kinds = SoloDataObject.getString("kinds")
                            val token = SoloDataObject.getString("token")
                            val col_token = SoloDataObject.getString("col_token")
                            val isLike = SoloDataObject.getString("isLike")
                            val userLeaveCheck = SoloDataObject.getString("userLeaveCheck")
                            val collaborationLeaveCheck =SoloDataObject.getString("collaborationLeaveCheck")

                            val bestSoloData = BestSoloData(
                                idx,
                                thumbnail,
                                title,
                                singer,
                                lyrics,
                                cnt_play,
                                cnt_reply,
                                cnt_like,
                                nickname,
                                email,
                                profile,
                                song_path,
                                collaboration,
                                collabo_email,
                                collaboration_profile,
                                date,
                                kinds,
                                mr_idx,
                                token,
                                col_token,
                                isLike,
                                true,
                                true,userLeaveCheck, collaborationLeaveCheck

                            )
                            bestSoloList.add(0, bestSoloData)
                            bestSoloAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
}