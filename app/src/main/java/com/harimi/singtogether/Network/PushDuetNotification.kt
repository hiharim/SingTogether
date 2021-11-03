package com.harimi.singtogether.Network

data class PushDuetNotification(
    val data: DuetNotificationData,
    val to: String
)
