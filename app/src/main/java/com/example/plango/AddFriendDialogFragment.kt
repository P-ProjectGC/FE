package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.FriendSearchResultAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.MemberSearchData
import com.example.plango.data.MemberSession
import com.example.plango.databinding.DialogAddFriendBinding
import com.example.plango.model.Friend
import kotlinx.coroutines.launch

class AddFriendDialogFragment : DialogFragment() {

    private var _binding: DialogAddFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: FriendSearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initUi()
        initClickListeners()
        loadSentFriendRequests()
    }

    // 🔵 다이얼로그 켜질 때, 내가 보낸 친구 요청 목록 미리 로드
    private fun loadSentFriendRequests() {
        viewLifecycleOwner.lifecycleScope.launch {
            FriendRepository.refreshSentFriendRequests()
            // 실패해도 무시하고, hasSentRequestToNickname 결과만 사용
        }
    }

    private fun initRecyclerView() {
        searchAdapter = FriendSearchResultAdapter(
            onAddClick = { friend ->
                onFriendActionClicked(friend)
            },
            isRequested = { friend ->
                FriendRepository.hasSentRequestToNickname(friend.nickname)
            }
        )

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }

    private fun initUi() {
        binding.rvSearchResult.isVisible = false
        binding.tvEmptyResult.isVisible = false
    }

    private fun initClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.etNickname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    /**
     * 닉네임 검색 수행
     * - 서버 /api/members/search 사용
     */
    private fun performSearch() {
        val keyword = binding.etNickname.text.toString().trim()

        if (keyword.isEmpty()) {
            Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvEmptyResult.isVisible = false
        binding.rvSearchResult.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results: List<MemberSearchData> =
                    FriendRepository.searchMemberByNickname(keyword)

                if (results.isEmpty()) {
                    searchAdapter.submitList(emptyList())
                    binding.rvSearchResult.isVisible = false
                    binding.tvEmptyResult.isVisible = true
                } else {
                    val friendList: List<Friend> = results.map { mapToFriend(it) }
                    searchAdapter.submitList(friendList)

                    binding.tvEmptyResult.isVisible = false
                    binding.rvSearchResult.isVisible = true
                }
            } catch (e: Exception) {
                searchAdapter.submitList(emptyList())
                binding.rvSearchResult.isVisible = false
                binding.tvEmptyResult.isVisible = true

                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "검색 중 오류: ${e.message ?: "알 수 없는 오류"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** 버튼 클릭 시: 추가 / 취소 분기 */
    private fun onFriendActionClicked(friend: Friend) {
        val nickname = friend.nickname

        val isRequested = FriendRepository.hasSentRequestToNickname(nickname)

        if (isRequested) {
            // 이미 보낸 상태 → 취소 API
            cancelFriendRequest(friend)
        } else {
            // 아직 안 보낸 상태 → 친구 요청 보내기
            onAddFriendClicked(friend)
        }
    }

    /** 친구 요청 보내기 */
    private fun onAddFriendClicked(friend: Friend) {
        val targetNickname = friend.nickname

        // 이미 "보낸 친구 요청"인지 한 번 더 체크
        if (FriendRepository.hasSentRequestToNickname(targetNickname)) {
            Toast.makeText(
                requireContext(),
                "이미 친구 요청이 존재합니다.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 이미 친구인지 체크
        val currentFriends = FriendRepository.getFriends()
        if (currentFriends.any { it.nickname == targetNickname }) {
            Toast.makeText(requireContext(), "이미 친구인 사용자입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val myMemberId = MemberSession.currentMemberId

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FriendRepository.requestFriend(myMemberId, targetNickname)

            result.onSuccess {
                Toast.makeText(requireContext(), "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                // FriendRepository.requestFriend 안에서 sent 캐시를 추가했다고 가정
                searchAdapter.notifyDataSetChanged()
            }.onFailure { e ->
                val message = e.message ?: "친구 요청 처리 중 오류가 발생했습니다."
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** 친구 요청 취소 */
    private fun cancelFriendRequest(friend: Friend) {
        val nickname = friend.nickname

        // 내가 보낸 요청 중에서, 이 닉네임에게 보낸 요청 ID 찾기
        val requestId = FriendRepository.getSentRequestIdByNickname(nickname)
        if (requestId == null) {
            Toast.makeText(requireContext(), "친구 요청 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // ⬇️ 더 이상 myMemberId 안 넘김 (JWT 토큰으로 인증)
            val result = FriendRepository.cancelFriendRequest(requestId)

            result.onSuccess {
                Toast.makeText(requireContext(), "친구 요청을 취소했습니다.", Toast.LENGTH_SHORT).show()
                // 취소 성공 시 리스트 갱신
                searchAdapter.notifyDataSetChanged()
            }.onFailure { e ->
                val message = e.message ?: "친구 요청 취소 중 오류가 발생했습니다."
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * 서버 MemberSearchData -> UI에서 쓰는 Friend 로 변환
     */
    private fun mapToFriend(data: MemberSearchData): Friend {
        return Friend(
            memberId = data.memberId,         // ✅ 새로 추가된 필드
            nickname = data.nickname,
            realName = "",                    // 아직 실명 정보 없으면 빈 문자열로
            profileImageUrl = data.profileImageUrl,
            isKakaoUser = false               // 필요하면 나중에 data.loginType 보고 세팅
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddFriendDialog"
    }
}
