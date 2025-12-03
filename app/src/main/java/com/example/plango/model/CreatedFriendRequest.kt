package com.example.plango.model

/**
 * API 응답의 'data' 필드 구조 (친구 요청 생성 성공 시 서버가 반환하는 정보)
 */
data class CreatedFriendRequest( // 🔴 최종 명칭 수정
    val requestId: Long,
    val status: String // 예: "pending"
    // 서버 응답에 따라 필드 추가 가능
)
