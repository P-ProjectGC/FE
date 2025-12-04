package com.example.plango.model

data class Friend(
    val memberId: Long,          // 🔥 실제 멤버 ID (방 생성 시 서버로 보낼 값)
    val nickname: String,        // 카드에 크게 보이는 이름
    val realName: String,        // 아래에 작게 보이는 실제 이름
    val profileImageUrl: String? = null,
    val isKakaoUser: Boolean = false
)

