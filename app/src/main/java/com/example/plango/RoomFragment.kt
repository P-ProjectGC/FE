package com.example.plango

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.RoomAdapter
import com.example.plango.data.TravelRoomRepository
import com.example.plango.databinding.FragmentRoomBinding
import com.example.plango.model.TravelRoom

class RoomFragment : Fragment() {

    lateinit var binding: FragmentRoomBinding
    private lateinit var roomAdapter: RoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 어댑터 생성 (초기엔 빈 리스트)
        // 카드 클릭 시 해당 방 내부 화면으로 이동
        roomAdapter = RoomAdapter(emptyList()) { room: TravelRoom ->
            val intent = Intent(requireContext(), RoomScheduleTestActivity::class.java).apply {
                putExtra("ROOM_NAME", room.title)
                putExtra("ROOM_MEMO", room.memo)
                putExtra("START_DATE", room.startDate)   // "2025-08-03" 같은 형식
                putExtra("END_DATE", room.endDate)       // "2025-08-05"
                // 닉네임 리스트는 아직 없으니까 빈 리스트로 전달
                putStringArrayListExtra(
                    "MEMBER_NICKNAMES",
                    arrayListOf<String>()
                )
            }
            startActivity(intent)
        }

        // RecyclerView 세팅
        binding.rvRoomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }

        // 상단 "새 여행방" 버튼
        binding.btnNewRoom.setOnClickListener {
            navigateToCreateRoom()
        }

        // 빈 화면용 "첫 여행방 만들기" 버튼
        binding.btnCreateFirstRoom.setOnClickListener {
            navigateToCreateRoom()
        }

        // 더미 데이터 로드
        loadRooms()
    }

    // 여행방 생성 플로우로 진입
    private fun navigateToCreateRoom() {
        val intent = Intent(requireContext(), CreateRoomActivity::class.java)
        startActivity(intent)
    }

    //val rooms = TravelRoomRepository.getEmptyRooms() -> 빈 방 목록 시 확인용
    private fun loadRooms() {
        // 지금은 더미 데이터, 나중에 이 한 줄만 서버 코드로 교체
        val rooms: List<TravelRoom> = TravelRoomRepository.getRooms()
        // 빈 화면 테스트하고 싶으면:
        // val rooms = TravelRoomRepository.getEmptyRooms()

        if (rooms.isEmpty()) {
            binding.rvRoomList.visibility = View.GONE
            binding.layoutEmptyRoom.visibility = View.VISIBLE
        } else {
            binding.rvRoomList.visibility = View.VISIBLE
            binding.layoutEmptyRoom.visibility = View.GONE
            roomAdapter.submitList(rooms)
        }
    }

    override fun onResume() {
        super.onResume()
        // 여행방 화면에서는 알림 아이콘 숨김
        (activity as? MainActivity)?.showAlarmIcon(false)

        // 화면에 다시 돌아올 때마다 최신 리스트로 갱신
        loadRooms()
    }
}
