// app/src/main/java/com/example/plango/model/RoomMemberDetail.kt
package com.example.plango.model

data class RoomMemberDetail(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val host: Boolean
)
