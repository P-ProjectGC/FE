package com.example.plango.data

import com.example.plango.model.Friend

object FriendSearchRepository {

    private val _allUsers = mutableListOf<Friend>()

    fun setAllUsers(users: List<Friend>) {
        _allUsers.clear()
        _allUsers.addAll(users)
    }

    // 부분 일치 검색 → 여러 명 결과
    fun searchByNickname(nickname: String): List<Friend> {
        val query = nickname.trim()
        if (query.isEmpty()) return emptyList()

        return _allUsers.filter { friend ->
            friend.nickname.contains(query)
        }
    }
}
