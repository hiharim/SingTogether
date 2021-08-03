package com.harimi.singtogether

import android.app.Application
import android.content.Context
import com.kakao.sdk.common.KakaoSdk


class GlobalApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "8dea534e8ce2ecf01d213633642d513a")
    }
}