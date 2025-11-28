package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.FriendAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendRequestRepository
import com.example.plango.databinding.FragmentFriendBinding
import com.example.plango.model.Friend

class FriendFragment : Fragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var friendAdapter: FriendAdapter

    // 친구 목록 더미 데이터 ON/OFF
    private val USE_DUMMY_FRIENDS = true

    // 친구 요청 더미 데이터 ON/OFF
    private val USE_DUMMY_REQUESTS = true

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

        // 2) 앱 처음 실행 시 더미 친구 목록을 FriendRepository에 넣을지 여부
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
            FriendRepository.setFriends(dummyFriends)
        }

        // 3) 화면에 친구 목록 표시
        refreshFriendList()

        // 4) 더미 친구 요청 리스트 세팅 (추후 백엔드 데이터로 교체)
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

        // 5) 알림 아이콘(🔔) 클릭 시 친구요청 팝업 띄우기
        (activity as? MainActivity)?.setOnAlarmClickListener {
            FriendRequestDialogFragment()
                .show(parentFragmentManager, "FriendRequestDialog")
        }

        // 6) 다이얼로그에서 수락/거절 처리 후 → 여기로 신호 오면 리스트 새로고침
        parentFragmentManager.setFragmentResultListener(
            "friend_request_handled",
            viewLifecycleOwner
        ) { _, _ ->
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

    /** UI 갱신 함수: 친구 목록을 Repository에서 다시 읽어서 반영 */
    private fun refreshFriendList() {
        val friends = FriendRepository.getFriends()

        // 상단 친구 개수
        binding.tvFriendCount.text = "친구 (${friends.size})"

        if (friends.isEmpty()) {
            binding.layoutEmptyFriends.visibility = View.VISIBLE
            binding.rvFriends.visibility = View.GONE
        } else {
            binding.layoutEmptyFriends.visibility = View.GONE
            binding.rvFriends.visibility = View.VISIBLE
            friendAdapter.submitList(friends)
        }
    }
}
