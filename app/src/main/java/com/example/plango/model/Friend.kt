package com.example.plango.model
// 친구목록 더미 데이터. 나중에 백엔드랑 협업 시 실제 데이터로 교체할 예정
data class Friend(
    val nickname: String,   // 카드에 크게 보이는 이름
    val realName: String,    // 아래에 작게 보이는 실제 이름
    val profileImageUrl: String? = null, // 나중에 서버에서 URL 넣어줄 예정
    val isKakaoUser: Boolean = false      // 카카오 계정 사용자면 true
)
