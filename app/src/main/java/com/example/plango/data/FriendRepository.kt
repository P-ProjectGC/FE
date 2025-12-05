package com.example.plango.data

import android.util.Log
import com.example.plango.model.Friend
import com.example.plango.model.AcceptedFriendship
import com.example.plango.model.FriendRequest
import com.example.plango.model.CreatedFriendRequest
import com.example.plango.model.ApiResponse
import com.example.plango.data.RetrofitClient.friendApiService
import com.example.plango.model.FriendRequestItem
import com.example.plango.model.SentFriendRequestItem

// 🟢 RetrofitClient에서 API Service 인스턴스를 직접 가져와 사용합니다.
private val apiService: FriendApiService = RetrofitClient.friendApiService

// 보낸 친구 요청 목록 캐시
private val sentFriendRequests = mutableListOf<SentFriendRequestItem>()

private var isLoaded = false

/**
 * 로컬 메모리 관리와 실제 API 통신을 수행하는 싱글톤 Repository입니다.
 */
object FriendRepository {

    // 내부에서 관리하는 친구 리스트 (로컬 메모리)
    private val _friends = mutableListOf<Friend>()

    // 외부에서 읽을 때는 읽기 전용 List
    fun getFriends(): List<Friend> = _friends

    // 처음 더미 데이터 세팅할 때 사용
    fun setFriends(newFriends: List<Friend>) {
        _friends.clear()
        _friends.addAll(newFriends)
        isLoaded = true          // ✅ 서버에서 한 번 이상 제대로 받은 상태
    }

    // 친구 한 명 추가 (친구 요청 수락 시 로컬 목록에 추가)
    fun addFriend(friend: Friend) {
        if (!_friends.contains(friend)) {
            _friends.add(friend)
        }
    }

    // =========================================================
    // 🟢 서버 통신 로직: 친구 요청 생성 (POST /api/friends)
    // =========================================================
    suspend fun requestFriend(myId: Long, targetNickname: String): Result<CreatedFriendRequest> {
        return try {
            val response = apiService.sendFriendRequest(
                request = FriendRequest(targetNickname = targetNickname)
            )

            println(
                ">>> requestFriend url=${response.raw().request.url} " +
                        "method=${response.raw().request.method} code=${response.code()}"
            )
            println(">>> errorBody = ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "친구 요청 실패: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // =========================================================
    // 🟢 서버 통신 로직: 친구 요청 수락 (POST /api/friends/{friendId}/accept)
    // =========================================================
    suspend fun acceptFriendRequest(myId: Long, requestId: Long): Result<AcceptedFriendship> {
        return try {
            val response = apiService.acceptFriendRequest(
                friendId = requestId
            )

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "친구 수락 실패: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // =========================================================
    // 🟢 서버 통신 로직: 친구 요청 거절 (POST /api/friends/{friendId}/reject)
    // =========================================================
    suspend fun rejectFriendRequest(myId: Long, requestId: Long): Result<Unit> {
        return try {
            val response = apiService.rejectFriendRequest(
                friendId = requestId
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage =
                    response.body()?.message
                        ?: "친구 요청 거절 실패 (HTTP Code: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // =========================================================
    // 🟢 친구 목록 조회 (+ 프로필 이미지 풀 URL 변환하는 핵심 부분)
    // =========================================================
    suspend fun fetchFriendsFromServer(
        memberId: Long,
        nickname: String? = null
    ): Result<List<Friend>> {
        return try {
            val response = apiService.getFriendList(
                nickname = nickname
            )

            Log.d("FRIEND_API", "HTTP CODE = ${response.code()}")
            Log.d("FRIEND_API", "isSuccessful = ${response.isSuccessful}")

            try {
                Log.d("FRIEND_API", "RAW_BODY = ${response.errorBody()?.string()}")
            } catch (e: Exception) {
                Log.d("FRIEND_API", "Error parsing raw body: ${e.message}")
            }

            val body = response.body()
            Log.d("FRIEND_API", "BODY = $body")

            if (response.isSuccessful && body?.data != null) {

                val list = body.data!!

                // 서버 DTO → 앱 Friend 모델로 변환
                val converted = list.map { api ->

                    Log.d(
                        "FRIEND_API_PROFILE",
                        "nickname=${api.nickname}, rawUrl=${api.profileImageUrl}"
                    )

                    // 🔵 원본 URL
                    val rawUrl = api.profileImageUrl

                    // 🔵 최종 이미지 URL (내 프로필과 동일 규칙)
                    val fullUrl = if (rawUrl.isNullOrBlank()) {
                        null
                    } else if (rawUrl.startsWith("http")) {
                        rawUrl
                    } else {
                        RetrofitClient.IMAGE_BASE_URL + rawUrl
                    }

                    Friend(
                        memberId = api.memberId,
                        nickname = api.nickname,
                        realName = api.name,              // realName 없음 → nickname 재사용
                        profileImageUrl = fullUrl,            // 🔥 여기!
                        isKakaoUser = api.loginType == "KAKAO"
                    )
                }

                setFriends(converted)

                Log.d("FRIEND_API", "SUCCESS size=${converted.size}")

                Result.success(converted)
            } else {
                Log.e(
                    "FRIEND_API",
                    "FAIL: message=${body?.message}, http=${response.code()}"
                )

                Result.failure(
                    Exception(
                        body?.message
                            ?: "친구 목록 조회 실패 (HTTP ${response.code()})"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("FRIEND_API", "EXCEPTION: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================
    // 🟢 친구 요청(받은 것) 조회
    // =========================================================
    suspend fun fetchReceivedFriendRequests(memberId: Long): Result<List<FriendRequestItem>> {
        return try {
            val response = apiService.getReceivedFriendRequests()
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                val converted = body.data!!.map { api ->
                    android.util.Log.d(
                        "FRIEND_REQ_API",
                        "nickname=${api.nickname}, loginType=${api.loginType}"
                    )
                    FriendRequestItem(
                        requestId = api.friendId,
                        senderNickname = api.nickname,
                        senderMemberId = api.memberId,
                        requestedAt = api.createdAt,
                        isKakaoUser = api.loginType?.contains("KAKAO", ignoreCase = true) == true
                    )
                }

                FriendRequestRepository.setRequests(converted)

                Result.success(converted)
            } else {
                Result.failure(
                    Exception(
                        body?.message
                            ?: "친구 요청 조회 실패 (HTTP ${response.code()})"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // =========================================================
    // 🟢 친구 검색 (추가용)
    // =========================================================
    suspend fun searchMemberByNickname(keyword: String): List<MemberSearchData> {
        if (keyword.isBlank()) return emptyList()

        val memberId = MemberSession.currentMemberId

        val response = friendApiService.searchMember(
            nickname = keyword
        )

        if (response.code != "0") {
            return emptyList()
        }

        return response.data ?: emptyList()
    }

    /**
     * 서버에서 "보낸 친구 요청 목록"을 새로 가져와서 캐시에 저장
     */
    suspend fun refreshSentFriendRequests(): Result<List<SentFriendRequestItem>> {
        return try {
            val memberId = MemberSession.currentMemberId

            val response = apiService.getSentFriendRequests()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 0) {
                    val list = body.data ?: emptyList()
                    sentFriendRequests.clear()
                    sentFriendRequests.addAll(list)
                    Result.success(list)
                } else {
                    Result.failure(Exception(body?.message ?: "보낸 친구 요청 조회 실패"))
                }
            } else {
                Result.failure(Exception("보낸 친구 요청 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun cancelFriendRequest(friendId: Long): Result<Unit> {
        return try {
            val response = apiService.cancelFriendRequest(friendId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("친구 요청 취소 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureFriendsLoaded(memberId: Long): Boolean {
        // 이미 한 번 불러왔고, 리스트도 비어있지 않다면 그냥 true
        if (isLoaded && _friends.isNotEmpty()) {
            Log.d("FRIEND_API", "ensureFriendsLoaded: already loaded, size=${_friends.size}")
            return true
        }

        // 아직 안 불러온 상태 → 서버 호출
        val result = fetchFriendsFromServer(memberId)

        val success = result.isSuccess
        Log.d(
            "FRIEND_API",
            "ensureFriendsLoaded: fetch result = $success, size=${_friends.size}"
        )
        return success
    }

    /**
     * 현재 캐시에 기준해서 "이미 이 닉네임으로 보낸 요청이 있는지" 확인
     */
    fun hasSentRequestToNickname(nickname: String): Boolean {
        return sentFriendRequests.any { it.nickname == nickname }
    }

    fun getSentRequestIdByNickname(nickname: String): Long? {
        return sentFriendRequests.firstOrNull { it.nickname == nickname }?.friendId
    }
}
