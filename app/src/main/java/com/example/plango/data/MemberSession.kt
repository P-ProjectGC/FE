package com.example.plango.data

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
    }
}
