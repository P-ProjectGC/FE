package com.example.plango.data

import com.example.plango.model.FriendRequestItem

object FriendRequestRepository {

    // 내부 저장소: 실제로 관리하는 리스트
    private val _requests = mutableListOf<FriendRequestItem>()

    // ✅ 항상 "복사본"을 내보낸다 (외부에서 _requests 직접 못 건드리게)
    fun getRequests(): List<FriendRequestItem> = _requests.toList()

    // 서버나 초기 로딩 시 전체 교체
    fun setRequests(newRequests: List<FriendRequestItem>) {
        _requests.clear()
        _requests.addAll(newRequests)
    }

    /**
     * @return true  -> 요청 추가 성공
     *         false -> 이미 요청 목록에 있던 친구
     */
    fun addRequest(item: FriendRequestItem): Boolean {
        if (_requests.contains(item)) {
            return false
        }
        _requests.add(item)
        return true
    }

    fun removeRequest(item: FriendRequestItem) {
        _requests.remove(item)
    }
}
