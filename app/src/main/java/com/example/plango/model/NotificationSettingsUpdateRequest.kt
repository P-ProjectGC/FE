package com.example.plango.model

data class NotificationSettingsUpdateRequest(
    val allChatRoomEnabled: Boolean,
    val tripReminderEnabled: Boolean,
    val friendRequestEnabled: Boolean
)