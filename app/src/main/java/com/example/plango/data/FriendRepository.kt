package com.example.plango.data

import com.example.plango.model.Friend

object FriendRepository {

    // 내부에서 관리하는 친구 리스트
    private val _friends = mutableListOf<Friend>()

    // 외부에서 읽을 때는 읽기 전용 List
    fun getFriends(): List<Friend> = _friends

    // 처음 더미 데이터 세팅할 때 사용
    fun setFriends(newFriends: List<Friend>) {
        _friends.clear()
        _friends.addAll(newFriends)
    }

    // 친구 한 명 추가 (친구 요청 수락 시 여기로)
    fun addFriend(friend: Friend) {
        if (!_friends.contains(friend)) {
            _friends.add(friend)
        }
    }
}
