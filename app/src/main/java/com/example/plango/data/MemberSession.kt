package com.example.plango.data

import com.example.plango.model.NotificationSettings

object MemberSession {
    var currentMemberId: Long = -1L
    var email: String? = null
    var nickname: String? = null
    var profileImageUrl: String? = null

    // 🔹 프로필용 추가 필드
    var loginId: String? = null
    var loginType: String? = null   // "NORMAL" / "KAKAO" / null

    var accessToken: String? = null
    var refreshToken: String? = null

    // 🔹 알림 설정 캐시
    var notificationSettings: NotificationSettings? = null

    val isLoggedIn: Boolean
        get() = currentMemberId != -1L && !accessToken.isNullOrBlank()

    fun clear() {
        currentMemberId = -1L
        email = null
        nickname = null
        profileImageUrl = null

        loginId = null
        loginType = null

        accessToken = null
        refreshToken = null

        // 세션 클리어 시 알림 설정도 초기화할지 말지는 정책 따라 선택
        notificationSettings = null
    }

    // ✅ 전체 채팅방 알림 ON 여부
    fun isAllChatNotificationOn(): Boolean {
        return notificationSettings?.allChatRoomEnabled ?: true
    }

    // ✅ 여행 일정 리마인더 ON 여부
    fun isTripReminderOn(): Boolean {
        return notificationSettings?.tripReminderEnabled ?: false
    }

    // ✅ 친구 요청 알림 ON 여부
    fun isFriendRequestOn(): Boolean {
        return notificationSettings?.friendRequestEnabled ?: false
    }
}
