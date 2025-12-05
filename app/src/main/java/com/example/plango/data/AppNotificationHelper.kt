// AppNotificationHelper.kt
package com.example.plango.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.plango.R
import com.example.plango.RoomScheduleTestActivity
import com.example.plango.model.TravelRoom
import java.time.LocalDate

object AppNotificationHelper {

    private const val CHANNEL_TRIP_REMINDER_ID = "trip_reminder_channel"
    private const val CHANNEL_TRIP_REMINDER_NAME = "여행 리마인드"
    private const val PREF_TRIP_REMINDER = "trip_reminder_prefs"

    /**
     * 채널 생성 (Android 8.0+)
     */
    private fun createTripReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_TRIP_REMINDER_ID,
                CHANNEL_TRIP_REMINDER_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "여행 하루 전 리마인드 알림 채널"
            }

            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 이미 오늘 이 방에 대해 알림을 띄운 적 있으면 건너뛰고,
     * 아니면 알림을 띄우고 "오늘 이 방 알림 보냈다" 표시.
     */
    fun showTripReminderIfNeeded(context: Context, room: TravelRoom, date: LocalDate) {
        // ✅ 프로필에서 리마인더 OFF면 아예 안 보내기
        if (!MemberSession.isTripReminderOn()) return

        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREF_TRIP_REMINDER, Context.MODE_PRIVATE)

        // ex) room_21_2025-12-06
        val key = "room_${room.id}_${date}"

        // 이미 오늘 이 방에 대해 알림을 보냈으면 패스
        if (prefs.getBoolean(key, false)) {
            return
        }

        // 실제 알림 띄우기
        showTripReminderNotification(appContext, room)

        // 오늘 이 방에 대해 알림 보냈다고 저장
        prefs.edit().putBoolean(key, true).apply()
    }

    /**
     * 실제 알림 하나 띄우는 부분
     */
    private fun showTripReminderNotification(context: Context, room: TravelRoom) {
        // Android 13+ 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                return
            }
        }

        createTripReminderChannel(context)

        // 알림 눌렀을 때 해당 방으로 진입
        val intent = Intent(context, RoomScheduleTestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ROOM_ID", room.id)
            putExtra("ROOM_NAME", room.title)
            putExtra("ROOM_MEMO", room.memo)
            putExtra("START_DATE", room.startDate)
            putExtra("END_DATE", room.endDate)
            putStringArrayListExtra(
                "MEMBER_NICKNAMES",
                ArrayList(room.memberNicknames)
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            room.id.toInt(),   // 방마다 다른 requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TRIP_REMINDER_ID)
            .setSmallIcon(R.drawable.icon_alarm_p)
            .setContentTitle("내일 여행이에요! ✈️")
            .setContentText("${room.title} (${room.dateText})")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(room.id.toInt(), builder.build())
        }
    }
}
