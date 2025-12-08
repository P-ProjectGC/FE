package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.AddFriendDialogFragment
import com.example.plango.adapter.FriendAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendRequestRepository
import com.example.plango.data.MemberSession
import com.example.plango.databinding.FragmentFriendBinding
import kotlinx.coroutines.launch

class FriendFragment : Fragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var friendAdapter: FriendAdapter

    // 현재 검색어 (화면에서 유지)
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) RecyclerView + Adapter 초기화
        friendAdapter = FriendAdapter(mutableListOf())

        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendAdapter
        }

        // 🔥 삭제 버튼 클릭 시 → 다이얼로그 띄우기
        friendAdapter.onDeleteClick = { friend, position ->
            showDeleteDialog(friend.friendId, position) // ✅ 올바른 값
        }

        lifecycleScope.launch {
            val result = FriendRepository.fetchReceivedFriendRequests(
                MemberSession.currentMemberId
            )

            result.onSuccess { list ->
                // 친구 요청 뱃지 갱신
                (activity as? MainActivity)?.updateAlarmBadge(list.size)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "친구 요청 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 2) 더미 완전 제거 → 처음엔 빈 상태로 시작
        FriendRepository.setFriends(emptyList())
        FriendRequestRepository.setRequests(emptyList())

        // 친구 추가 버튼 (상단, empty 화면 둘 다)
        binding.btnAddFriend.setOnClickListener {
            AddFriendDialogFragment().show(parentFragmentManager, "AddFriendDialog")
        }
        binding.btnAddFriendEmpty.setOnClickListener {
            AddFriendDialogFragment().show(parentFragmentManager, "AddFriendDialog")
        }

        // 🔍 검색바 텍스트 변경 시마다 필터링 로직 적용
        binding.etSearch.addTextChangedListener { editable ->
            currentQuery = editable?.toString().orEmpty()
            filterFriends()
        }

        // 🔥 서버에서 친구 목록 처음 로드
        loadFriendsFromServer()

        // 알림 아이콘 눌렀을 때 팝업 열기
        (activity as? MainActivity)?.setOnAlarmClickListener {
            FriendRequestDialogFragment().show(parentFragmentManager, "FriendRequestDialog")
        }

        // 요청 수락/거절 후 → 리스트 재갱신
        parentFragmentManager.setFragmentResultListener(
            "friend_request_handled",
            viewLifecycleOwner
        ) { _, _ ->
            refreshFriendList()

            (activity as? MainActivity)?.updateAlarmBadge(
                FriendRequestRepository.getRequests().size
            )
        }

        // 초기 알람 배지 세팅 (요청 로직 연동 전이라 0)
        (activity as? MainActivity)?.updateAlarmBadge(
            FriendRequestRepository.getRequests().size
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.apply {
            showMainHeader(true)
            showAlarmIcon(true)      // 친구 화면에서는 알람 ON
            showProfileButton(true)
        }
    }


    /** 서버에서 친구 목록 불러오기 */
    private fun loadFriendsFromServer() {
        val memberId = MemberSession.currentMemberId

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FriendRepository.fetchFriendsFromServer(
                memberId = memberId,
                nickname = null // 전체 조회
            )

            result.onSuccess {
                refreshFriendList()
            }.onFailure { e ->
                Toast.makeText(
                    requireContext(),
                    e.message ?: "친구 목록 조회 실패",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /** 친구 목록 갱신 */
    private fun refreshFriendList() {
        filterFriends()
    }

    /** 현재 검색어(currentQuery)로 친구 목록 필터링해서 UI 갱신 */
    private fun filterFriends() {
        val allFriends = FriendRepository.getFriends()

        val filtered = if (currentQuery.isBlank()) {
            allFriends
        } else {
            allFriends.filter { friend ->
                friend.nickname.contains(currentQuery, ignoreCase = true)
            }
        }

        binding.tvFriendCount.text = "친구 (${filtered.size})"

        if (allFriends.isEmpty() && currentQuery.isBlank()) {
            binding.layoutEmptyFriends.visibility = View.VISIBLE
            binding.rvFriends.visibility = View.GONE
        } else {
            binding.layoutEmptyFriends.visibility = View.GONE
            binding.rvFriends.visibility =
                if (filtered.isEmpty()) View.GONE else View.VISIBLE
        }

        friendAdapter.submitList(filtered)
    }

    /** 친구 삭제 확인 다이얼로그 */
    private fun showDeleteDialog(friendId: Long, position: Int) {
        val dialog = FriendDeleteDialogFragment(
            onConfirmDelete = {
                deleteFriend(friendId, position)
            }
        )
        dialog.show(parentFragmentManager, "FriendDeleteDialog")
    }


    /** 실제 친구 삭제 처리 (API 호출 + 리스트 갱신) */
    private fun deleteFriend(friendId: Long, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = FriendRepository.deleteFriend(friendId)

            if (result.isSuccess) {
                val updatedList = FriendRepository.getFriends()
                    .filterNot { it.friendId == friendId }   // 🔥 FIXED
                FriendRepository.setFriends(updatedList)

                refreshFriendList()
                Toast.makeText(requireContext(), "친구가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    result.exceptionOrNull()?.message ?: "친구 삭제에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
