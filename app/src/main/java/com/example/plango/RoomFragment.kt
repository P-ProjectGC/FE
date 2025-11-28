package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.RoomAdapter
import com.example.plango.data.TravelRoomRepository
import com.example.plango.model.TravelRoom
import com.example.plango.databinding.FragmentRoomBinding

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
        roomAdapter = RoomAdapter(emptyList()) { room: TravelRoom ->
            // TODO: 나중에 여기서 해당 여행방 상세 화면으로 이동
        }

        // RecyclerView 세팅
        binding.rvRoomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }

        // 버튼 클릭 리스너 (지금은 TODO)
        binding.btnNewRoom.setOnClickListener {
            // TODO: 여행방 생성 플로우로 이동
        }

        binding.btnCreateFirstRoom.setOnClickListener {
            // TODO: 여행방 생성 플로우로 이동
        }

        // 더미 데이터 로드
        loadRooms()
    }
    //val rooms = TravelRoomRepository.getEmptyRooms()->빈 방 목록 시 확인
    private fun loadRooms() {
        // 지금은 더미 데이터, 나중에 이 한 줄만 서버 코드로 교체
        val rooms: List<TravelRoom> = TravelRoomRepository.getRooms()
        // 빈 화면 테스트하고 싶으면:
         //val rooms = TravelRoomRepository.getEmptyRooms()

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
        (activity as? MainActivity)?.showAlarmIcon(false)
    }
}
