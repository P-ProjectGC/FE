package com.example.plango.data

import com.example.plango.model.Friend

object FriendRequestRepository {

    private val _requests = mutableListOf<Friend>()

    fun getRequests(): List<Friend> = _requests

    fun setRequests(newRequests: List<Friend>) {
        _requests.clear()
        _requests.addAll(newRequests)
    }

    /**
     * @return true  -> 요청 추가 성공
     *         false -> 이미 요청 목록에 있던 친구
     */
    fun addRequest(friend: Friend): Boolean {
        if (_requests.contains(friend)) {
            return false
        }
        _requests.add(friend)
        return true
    }

    fun removeRequest(friend: Friend) {
        _requests.remove(friend)
    }
}

