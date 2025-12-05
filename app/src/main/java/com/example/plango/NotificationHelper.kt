package com.example.plango

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.plango.data.MemberSession

object NotificationHelper {

    private const val CHANNEL_ID_CHAT = "chat_channel"
    private const val CHANNEL_NAME_CHAT = "채팅 알림"



    // ✅ 친구 요청 알림용 채널
    private const val CHANNEL_ID_FRIEND = "friend_request_channel"
    private const val CHANNEL_NAME_FRIEND = "친구 요청 알림"


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
        // ✅ 1) 프로필 전체 채팅 알림이 꺼져 있으면 바로 리턴
        if (!MemberSession.isAllChatNotificationOn()) {
            return
        }

        // ✅ 2) 이 방에서 알림 꺼져 있으면 리턴
        if (!NotificationPrefs.isChatNotificationEnabled(context, roomId)) {
            return
        }

        // 알림 눌렀을 때 들어갈 화면: RoomScheduleTestActivity
        val intent = Intent(context, RoomScheduleTestActivity::class.java).apply {
            putExtra("ROOM_ID", roomId)
            putExtra("ROOM_NAME", roomName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val requestCode = roomId.toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CHAT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(roomName)
            .setContentText(messagePreview)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(requestCode, builder.build())
        }
    }

    fun createFriendRequestNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID_FRIEND,
                CHANNEL_NAME_FRIEND,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "친구 요청 및 친구 관련 알림 채널"
            }

            manager.createNotificationChannel(channel)
        }
    }

    fun showFriendRequestNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        pendingIntent: PendingIntent? = null
    ) {
        // ✅ 프로필에서 친구 요청 알림 OFF면 바로 리턴
        if (!MemberSession.isFriendRequestOn()) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FRIEND)
            .setSmallIcon(R.mipmap.ic_launcher)  // 필요하면 나중에 전용 아이콘으로 교체
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }





}
