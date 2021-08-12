package com.harimi.singtogether

import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment


/**
    마이페이지 프래그먼트
 */
class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val settingFragment =  inflater.inflate(R.layout.fragment_setting, container, false)
        val fragment_setting_tv_logout =settingFragment.findViewById<TextView>(R.id.fragment_setting_tv_logout)
        val fragment_setting_tv_out =settingFragment.findViewById<TextView>(R.id.fragment_setting_tv_out)

        fragment_setting_tv_logout.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("로그아웃")
            builder.setMessage("로그아웃을 하시겠습니까? ")
            builder.setPositiveButton("네") { dialogInterface: DialogInterface, i: Int ->

                val pref = requireActivity().getSharedPreferences("userEmail", MODE_PRIVATE)
                val edit = pref.edit() // 수정모드
                edit.putString("email", "") // 값 넣기
                edit.apply() // 적용하기

                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
            }
            builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->

            }

            builder.show()

        }
        return  settingFragment
    }

    companion object {

        fun newInstance(param1: String, param2: String) =
                SettingFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}