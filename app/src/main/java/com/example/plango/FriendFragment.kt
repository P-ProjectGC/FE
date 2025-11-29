package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.AddFriendDialogFragment
import com.example.plango.adapter.FriendAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendRequestRepository
import com.example.plango.data.FriendSearchRepository
import com.example.plango.databinding.FragmentFriendBinding
import com.example.plango.model.Friend

class FriendFragment : Fragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var friendAdapter: FriendAdapter

    // 친구 목록 더미 데이터 ON/OFF
    private val USE_DUMMY_FRIENDS = true

    // 친구 요청 더미 데이터 ON/OFF
    private val USE_DUMMY_REQUESTS = true

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
        friendAdapter = FriendAdapter(emptyList())
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendAdapter
        }

        // 2) 앱 처음 실행 시 더미 친구 목록 세팅
        if (USE_DUMMY_FRIENDS) {
            val dummyFriends = listOf(
                Friend("음주헌터", "송헌재", null, true),
                Friend("디자인광", "남유정", null, false),
                Friend("인성파탄자", "곽주희", null, false),
                Friend("팬티사랑", "신진성", null, true),
                Friend("감성폐급", "이주엽", null, false),
                Friend("말이많아", "강태화", null, true),
                Friend("딸기좋아", "신하리", null, true)
            )
            // 친구 목록 더미 세팅
            FriendRepository.setFriends(dummyFriends)

            // 🔵 친구 검색용 더미 유저 세팅 (친구추가 다이얼로그에서 사용)
            val dummySearchUsers = dummyFriends + listOf(
                Friend("도시탐험가", "김태희", null, true),
                Friend("바다바다", "박재욱", null, false),
                Friend("도시탐험가2", "신진성", null, false)
            )
            FriendSearchRepository.setAllUsers(dummySearchUsers)
        } else {
            FriendRepository.setFriends(emptyList())
        }

        // 친구 추가 버튼 (상단, empty 화면 둘 다)
        binding.btnAddFriend.setOnClickListener {
            AddFriendDialogFragment().show(parentFragmentManager, "AddFriendDialog")
        }
        binding.btnAddFriendEmpty.setOnClickListener {
            AddFriendDialogFragment().show(parentFragmentManager, "AddFriendDialog")
        }

        // 🔍 3) 검색바 텍스트 변경 시마다 필터링
        binding.etSearch.addTextChangedListener { editable ->
            currentQuery = editable?.toString().orEmpty()
            filterFriends()
        }

        // 4) 화면에 친구 목록 표시 (현재 검색어 기준으로)
        refreshFriendList()

        // 5) 더미 친구 요청 리스트 세팅 (추후 백엔드 데이터로 교체)
        if (USE_DUMMY_REQUESTS) {
            val dummyRequests = listOf(
                Friend("술고래", "헌재송", null, true),
                Friend("팬티도둑", "진성신", null, false),
                Friend("로또엄마", "주희곽", null, false)
            )
            FriendRequestRepository.setRequests(dummyRequests)
        } else {
            FriendRequestRepository.setRequests(emptyList())
        }

        // 6) 알림 아이콘(🔔) 클릭 시 친구요청 팝업 띄우기
        (activity as? MainActivity)?.setOnAlarmClickListener {
            FriendRequestDialogFragment()
                .show(parentFragmentManager, "FriendRequestDialog")
        }

        // 7) 다이얼로그에서 수락/거절 처리 후 → 여기로 신호 오면 리스트 새로고침
        parentFragmentManager.setFragmentResultListener(
            "friend_request_handled",
            viewLifecycleOwner
        ) { _, _ ->
            // 친구 목록(Repository) 내용이 바뀌었으니 현재 검색어 기준으로 다시 필터
            refreshFriendList()

            // 요청 리스트도 바뀌었으니 알람 배지 갱신
            (activity as? MainActivity)?.updateAlarmBadge(
                FriendRequestRepository.getRequests().size
            )
        }

        // 🔴 화면 처음 들어올 때도 배지 숫자 세팅
        (activity as? MainActivity)?.updateAlarmBadge(
            FriendRequestRepository.getRequests().size
        )
    }

    override fun onResume() {
        super.onResume()
        // 친구 화면일 때만 알림 아이콘 보이게
        (activity as? MainActivity)?.showAlarmIcon(true)
    }

    /** Repository에서 다시 읽고, 현재 검색어 기준으로 필터링 */
    private fun refreshFriendList() {
        filterFriends()
    }

    /** 현재 검색어(currentQuery)로 친구 목록 필터링해서 UI 갱신 */
    private fun filterFriends() {
        val allFriends = FriendRepository.getFriends()

        // 검색어가 비었으면 전체, 있으면 닉네임 부분 일치 필터
        val filtered = if (currentQuery.isBlank()) {
            allFriends
        } else {
            allFriends.filter { friend ->
                friend.nickname.contains(currentQuery, ignoreCase = true)
            }
        }

        // 친구 수는 필터 결과 기준으로 표시
        binding.tvFriendCount.text = "친구 (${filtered.size})"

        // 빈 상태 처리
        if (allFriends.isEmpty() && currentQuery.isBlank()) {
            // 진짜 친구가 0명일 때만 empty 화면
            binding.layoutEmptyFriends.visibility = View.VISIBLE
            binding.rvFriends.visibility = View.GONE
        } else {
            binding.layoutEmptyFriends.visibility = View.GONE
            binding.rvFriends.visibility =
                if (filtered.isEmpty()) View.GONE else View.VISIBLE
        }

        // RecyclerView에 데이터 반영
        friendAdapter.submitList(filtered)
    }
}
