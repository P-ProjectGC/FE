package com.example.plango.data

import android.util.Log
import com.example.plango.model.Friend
import com.example.plango.model.AcceptedFriendship
import com.example.plango.model.FriendRequest
import com.example.plango.model.CreatedFriendRequest
import com.example.plango.model.ApiResponse
// RetrofitClient가 FriendApiService 인스턴스를 제공하므로 import 필요
import com.example.plango.data.RetrofitClient
import com.example.plango.data.RetrofitClient.friendApiService
import com.example.plango.model.FriendRequestItem
import com.example.plango.model.SentFriendRequestItem

// 🟢 RetrofitClient에서 API Service 인스턴스를 직접 가져와 사용합니다.
private val apiService: FriendApiService = RetrofitClient.friendApiService
//  보낸 친구 요청 목록 캐시
private val sentFriendRequests = mutableListOf<SentFriendRequestItem>()
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
                memberId = myId,
                request = FriendRequest(targetNickname = targetNickname)
            )

            // 🔥🔥 바로 여기!! 디버그 로그 추가 🔥🔥
            println(
                ">>> requestFriend url=${response.raw().request.url} " +
                        "method=${response.raw().request.method} code=${response.code()}"
            )
            println(">>> errorBody = ${response.errorBody()?.string()}")

            // 🔥🔥 여기까지가 우리가 진짜 보고 싶은 서버의 "정답" 🔥🔥
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "친구 요청 실패: ${response.code()}"))
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
                memberId = myId,
                friendId = requestId
            )

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "친구 수락 실패: ${response.code()}"))
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
                memberId = myId,
                friendId = requestId
            )

            // API는 Void(null)를 반환하지만, 200번대 성공 코드를 확인
            if (response.isSuccessful) {
                Result.success(Unit) // 거절 성공
            } else {
                // 응답 본문에서 에러 메시지 추출 시도
                val errorMessage = response.body()?.message ?: "친구 요청 거절 실패 (HTTP Code: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // FriendRepository.kt

    suspend fun fetchFriendsFromServer(memberId: Long, nickname: String? = null): Result<List<Friend>> {
        return try {
            val response = apiService.getFriendList(
                memberId = memberId,
                nickname = nickname
            )

            // 🔥 서버 응답 상태 로그 찍기
            Log.d("FRIEND_API", "HTTP CODE = ${response.code()}")
            Log.d("FRIEND_API", "isSuccessful = ${response.isSuccessful}")

            // 🔥 응답 body 문자열로 찍기
            try {
                Log.d("FRIEND_API", "RAW_BODY = ${response.errorBody()?.string()}")
            } catch (e: Exception) {
                Log.d("FRIEND_API", "Error parsing raw body: ${e.message}")
            }

            val body = response.body()
            Log.d("FRIEND_API", "BODY = $body")



            if (response.isSuccessful && response.body()?.data != null) {

                val list = response.body()!!.data!!

                // 서버 DTO → 앱 Friend 모델로 변환
                val converted = list.map { api ->
                    Friend(
                        memberId = api.memberId,              // 멤버아읻

                        nickname = api.nickname,
                        realName = api.nickname,   // realName 없음 → nickname 재사용
                        profileImageUrl = api.profileImageUrl,
                        isKakaoUser = api.loginType == "KAKAO"   // 🔥 여기
                    )
                }

                setFriends(converted)

                Log.d("FRIEND_API", "SUCCESS size=${converted.size}")

                Result.success(converted)
            } else {
                Log.e("FRIEND_API", "FAIL: message=${body?.message}, http=${response.code()}")

                Result.failure(
                    Exception(body?.message ?: "친구 목록 조회 실패 (HTTP ${response.code()})")
                )
            }
        } catch (e: Exception) {
            Log.e("FRIEND_API", "EXCEPTION: ${e.message}", e)
            Result.failure(e)
        }
    }


   //친구요청조회
// com.example.plango.data.FriendRepository

    suspend fun fetchReceivedFriendRequests(memberId: Long): Result<List<FriendRequestItem>> {
        return try {
            val response = apiService.getReceivedFriendRequests(memberId)

            // 디버깅용 로그 (원하면 import android.util.Log)
            // Log.d("FRIEND_REQ_API", "HTTP=${response.code()}, success=${response.isSuccessful}")

            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                val converted = body.data!!.map { api ->
                    android.util.Log.d("FRIEND_REQ_API", "nickname=${api.nickname}, loginType=${api.loginType}")
                    FriendRequestItem(
                        requestId = api.friendId,
                        senderNickname = api.nickname,
                        senderMemberId = api.memberId,
                        requestedAt = api.createdAt,
                        isKakaoUser = api.loginType?.contains("KAKAO", ignoreCase = true) == true
                    )
                }

                // 🔥 로컬 저장소에 반영
                FriendRequestRepository.setRequests(converted)

                Result.success(converted)
            } else {
                Result.failure(
                    Exception(body?.message ?: "친구 요청 조회 실패 (HTTP ${response.code()})")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

   //친구검색(추가위해)
   suspend fun searchMemberByNickname(keyword: String): List<MemberSearchData> {
       if (keyword.isBlank()) return emptyList()

       val memberId = MemberSession.currentMemberId

       val response = friendApiService.searchMember(
           memberId = memberId,
           nickname = keyword
       )

       if (response.code != "0") {
           // 실패면 빈 리스트 반환 (또는 예외 던지는 패턴으로 바꿔도 됨)
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

            val response = apiService.getSentFriendRequests(memberId)

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

    suspend fun cancelFriendRequest(memberId: Long, friendId: Long): Result<Unit> {
        return try {
            val response = apiService.cancelFriendRequest(memberId, friendId)

            if (response.isSuccessful) {
                // 캐시에서 제거
                sentFriendRequests.removeAll { it.friendId == friendId }
                Result.success(Unit)
            } else {
                Result.failure(Exception("친구 요청 취소 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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



