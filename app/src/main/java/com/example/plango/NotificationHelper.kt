package com.example.plango

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID_CHAT = "chat_channel"
    private const val CHANNEL_NAME_CHAT = "채팅 알림"

    // 앱 시작 시 한 번만 호출해두면 됨 (여러 번 호출해도 괜찮음)
    fun createChatNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID_CHAT,
                CHANNEL_NAME_CHAT,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "여행방 채팅 알림 채널"
            }

            manager.createNotificationChannel(channel)
        }
    }

    fun showChatNotification(
        context: Context,
        roomId: Long,
        roomName: String,
        messagePreview: String
    ) {

        // 🔒 이 방에서 알림 꺼져 있으면 그냥 리턴
        if (!NotificationPrefs.isChatNotificationEnabled(context, roomId)) {
            return
        }

        // 알림 눌렀을 때 들어갈 화면: RoomScheduleTestActivity
        val intent = Intent(context, RoomScheduleTestActivity::class.java).apply {
            putExtra("ROOM_ID", roomId)
            putExtra("ROOM_NAME", roomName)
            // 필요하면 START_DATE, END_DATE도 같이 넣어도 됨
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val requestCode = roomId.toInt()  // 방별로 알림 ID를 다르게 쓰고 싶을 때

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        // 아이콘은 나중에 채팅 관련 아이콘으로 바꾸면 됨
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CHAT)
            .setSmallIcon(R.mipmap.ic_launcher)  // TODO: 나중에 R.drawable.ic_chat_notification 같은 걸로 교체
            .setContentTitle(roomName)
            .setContentText(messagePreview)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            // roomId를 그대로 노티 ID로 사용 (방마다 알림 묶이게)
            notify(requestCode, builder.build())
        }
    }
}
