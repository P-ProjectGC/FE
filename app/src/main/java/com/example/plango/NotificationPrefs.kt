package com.example.plango

import android.content.Context

object NotificationPrefs {

    private const val PREF_NAME = "plango_notification_prefs"

    // 방별 채팅 알림 on/off 저장 키
    private fun keyForRoom(roomId: Long): String = "chat_notification_room_$roomId"

    fun isChatNotificationEnabled(context: Context, roomId: Long): Boolean {
        if (roomId == -1L) return true  // 혹시라도 ROOM_ID 없으면 막지 말고 허용
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // 기본값 true = 기본은 "알림 켜짐"
        return prefs.getBoolean(keyForRoom(roomId), true)
    }

    fun setChatNotificationEnabled(context: Context, roomId: Long, enabled: Boolean) {
        if (roomId == -1L) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(keyForRoom(roomId), enabled)
            .apply()
    }
}
