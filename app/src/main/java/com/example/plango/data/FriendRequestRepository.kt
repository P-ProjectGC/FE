package com.example.plango.data

import com.example.plango.model.Friend

// 친구 요청 목록을 전역에서 관리하는 저장소
object FriendRequestRepository {

    // 내부 저장용 리스트
    private val _requests = mutableListOf<Friend>()

    // 외부에서 읽을 때는 읽기 전용 List로
    fun getRequests(): List<Friend> = _requests

    // 전체 교체 (나중에 백엔드에서 받아온 데이터로 한 번에 세팅)
    fun setRequests(newRequests: List<Friend>) {
        _requests.clear()
        _requests.addAll(newRequests)
    }

    // 한 명 추가 (나중에 "친구 요청 보내기" 시에 사용 가능)
    fun addRequest(friend: Friend) {
        _requests.add(friend)
    }

    // 한 명 제거 (수락/거절 시에 index로 제거 or 객체로 제거)
    fun removeRequest(friend: Friend) {
        _requests.remove(friend)
    }

    // 필요하면 나중에 id 기반 로직이 생기면 Friend에 id 필드 추가해서 removeById도 만들면 됨
}
