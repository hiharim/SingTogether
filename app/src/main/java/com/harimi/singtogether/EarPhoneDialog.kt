package com.harimi.singtogether

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button

class EarPhoneDialog(context: Context) {
    private val dialog = Dialog(context)

    fun myDig() {
        dialog.setContentView(R.layout.earphone_dialog)

        //Dialog 크기 설정
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        val btnCancel=dialog.findViewById<Button>(R.id.btn_dialog)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}