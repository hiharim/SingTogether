package com.harimi.singtogether

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "8dea534e8ce2ecf01d213633642d513a")
    }
}