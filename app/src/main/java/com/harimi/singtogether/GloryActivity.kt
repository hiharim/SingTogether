package com.harimi.singtogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GloryActivity : AppCompatActivity() {

    private lateinit var iv_backArrow : ImageButton
    private lateinit var tv_year : TextView
    private lateinit var iv_forwardArrow : ImageButton
    private lateinit var rv_bestDuet : RecyclerView
    private lateinit var rv_bestSolo : RecyclerView
    private lateinit var tv_duetText : TextView
    private lateinit var tv_soloText : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glory)

        iv_backArrow = findViewById(R.id.iv_backArrow)
        tv_year = findViewById(R.id.tv_year)
        iv_forwardArrow = findViewById(R.id.iv_forwardArrow)
        rv_bestDuet = findViewById(R.id.rv_bestDuet)
        rv_bestSolo = findViewById(R.id.rv_bestSolo)
        tv_duetText = findViewById(R.id.tv_duetText)
        tv_soloText = findViewById(R.id.tv_soloText)


    }
}