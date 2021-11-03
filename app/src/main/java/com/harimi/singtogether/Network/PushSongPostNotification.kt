package com.harimi.singtogether.Network

data class PushSongPostNotification(
    val data: SongPostNotificationData,
    val to: String
)
