package com.example.plango

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.RoomAdapter
import com.example.plango.data.TravelRoomRepository
import com.example.plango.databinding.FragmentRoomBinding
import com.example.plango.model.TravelRoom
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.plango.data.MemberSession

class RoomFragment : Fragment() {

    private lateinit var binding: FragmentRoomBinding
    private lateinit var roomAdapter: RoomAdapter

    // 전체 여행방 목록 (검색용 원본 리스트)
    private var allRooms: List<TravelRoom> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 어댑터 생성 (초기엔 빈 리스트) - 기본 리스트 스타일
        roomAdapter = RoomAdapter(
            emptyList(),
            usePopupStyle = false
        ) { room: TravelRoom ->
            val intent = Intent(requireContext(), RoomScheduleTestActivity::class.java).apply {
                putExtra("ROOM_ID", room.id)
                putExtra("ROOM_NAME", room.title)
                putExtra("ROOM_MEMO", room.memo)
                putExtra("START_DATE", room.startDate)
                putExtra("END_DATE", room.endDate)
                putStringArrayListExtra(
                    "MEMBER_NICKNAMES",
                    ArrayList(room.memberNicknames)
                )
                putExtra("IS_HOST", room.isHost)   // ⭐ 서버 기준 방장 여부 같이 전달
            }
            startActivity(intent)
        }

        // RecyclerView 세팅
        binding.rvRoomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
            // 🔹 NestedScrollView 안에서는 이거 꼭!
            isNestedScrollingEnabled = false
        }

        // 상단 "새 여행방" 버튼
        binding.btnNewRoom.setOnClickListener {
            navigateToCreateRoom()
        }

        // 빈 화면용 "첫 여행방 만들기" 버튼
        binding.btnCreateFirstRoom.setOnClickListener {
            navigateToCreateRoom()
        }

        // 검색바 텍스트 감지
        binding.etSearchRoom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) { }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val query = s?.toString().orEmpty()
                filterRooms(query)
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        // 초기 데이터 로드
        loadRooms()
    }

    // 여행방 생성 플로우로 진입
    private fun navigateToCreateRoom() {
        val intent = Intent(requireContext(), CreateRoomActivity::class.java)
        startActivity(intent)
    }

    private fun loadRooms() {
        // 코루틴으로 서버 호출
        viewLifecycleOwner.lifecycleScope.launch {
            // TODO: 로그인 붙으면 실제 로그인된 멤버 ID로 교체
            val memberId = MemberSession.currentMemberId

            val success = TravelRoomRepository.fetchRoomsFromServer(memberId)

            if (!success) {
                // 서버 실패 시 → Repository 내부에서 기본 더미 1개 넣어둔 상태
                Toast.makeText(
                    requireContext(),
                    "서버에서 여행방 목록을 불러오지 못했어요.\n기본 데이터를 표시합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // 항상 Repository에서 현재 rooms 가져오기
            allRooms = TravelRoomRepository.getRooms()

            if (allRooms.isEmpty()) {
                // 실제로 방이 하나도 없을 때만 "아직 여행방이 없어요" 표시
                binding.rvRoomList.visibility = View.GONE
                binding.layoutEmptyRoom.visibility = View.VISIBLE
                roomAdapter.submitList(emptyList())
            } else {
                binding.rvRoomList.visibility = View.VISIBLE
                binding.layoutEmptyRoom.visibility = View.GONE

                // 현재 검색어 유지한 채로 갱신
                val currentQuery = binding.etSearchRoom.text?.toString().orEmpty()
                if (currentQuery.isBlank()) {
                    roomAdapter.submitList(allRooms)
                } else {
                    filterRooms(currentQuery)
                }
            }
        }
    }


    // 검색어로 방 필터링
    private fun filterRooms(query: String) {
        if (allRooms.isEmpty()) {
            roomAdapter.submitList(emptyList())
            return
        }

        if (query.isBlank()) {
            roomAdapter.submitList(allRooms)
            return
        }

        val lowerQuery = query.lowercase()
        val filtered = allRooms.filter { room ->
            room.title.lowercase().contains(lowerQuery) ||
                    (room.memo ?: "").lowercase().contains(lowerQuery)
        }

        roomAdapter.submitList(filtered)
    }


    override fun onResume() {
        super.onResume()
        // 여행방 화면에서는 알림 아이콘 숨김
        (activity as? MainActivity)?.showAlarmIcon(false)

        // 화면에 다시 돌아올 때마다 최신 리스트로 갱신
        loadRooms()
    }
}
