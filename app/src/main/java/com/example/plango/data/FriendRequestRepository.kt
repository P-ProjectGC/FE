package com.example.plango.data

import com.example.plango.model.FriendRequestItem // 🔴 새로운 모델 import

object FriendRequestRepository {

    // 🔴 내부 저장소 타입을 FriendRequestItem으로 변경
    private val _requests = mutableListOf<FriendRequestItem>()

    // 🔴 반환 타입을 FriendRequestItem으로 변경
    fun getRequests(): List<FriendRequestItem> = _requests

    // 🔴 인자 타입을 FriendRequestItem으로 변경
    fun setRequests(newRequests: List<FriendRequestItem>) {
        _requests.clear()
        _requests.addAll(newRequests)
    }

    /**
     * @return true  -> 요청 추가 성공
     * false -> 이미 요청 목록에 있던 친구
     */
    // 🔴 인자 타입을 FriendRequestItem으로 변경
    fun addRequest(item: FriendRequestItem): Boolean {
        if (_requests.contains(item)) {
            return false
        }
        _requests.add(item)
        return true
    }

    // 🔴 인자 타입을 FriendRequestItem으로 변경
    fun removeRequest(item: FriendRequestItem) {
        _requests.remove(item)
    }
}