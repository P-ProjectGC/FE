// 파일: com/example/plango/model/FriendResponse.kt
package com.example.plango.model

data class FriendResponse(
    val friendId: Long,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val loginType: String        // "NORMAL" 또는 "KAKAO"
)
