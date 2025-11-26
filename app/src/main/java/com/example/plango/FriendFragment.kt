package com.example.plango

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.FriendAdapter
import com.example.plango.databinding.FragmentFriendBinding
import com.example.plango.model.Friend


class FriendFragment : Fragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var friendAdapter: FriendAdapter

    //  더미데이터 ON/OFF 스위치 (원하면 true/false만 바꾸면 됨, false로 하면 더미데이터 없는 초기 친구추가 화면 확인 가능)
    private val USE_DUMMY_DATA = true

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

        // 1) Adapter 초기화
        friendAdapter = FriendAdapter(emptyList())

        // 2) RecyclerView 연결
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendAdapter
        }

        // 3) 더미 데이터 생성
        val dummyFriends = if (USE_DUMMY_DATA) {
            listOf(
                Friend("음주헌터", "송헌재", profileImageUrl = null, isKakaoUser = true),
                Friend("디자인광", "남유정", profileImageUrl = null, isKakaoUser = false),
                Friend("인성파탄자", "곽주희", profileImageUrl = null, isKakaoUser = false),
                Friend("팬티사랑", "신진성", profileImageUrl = null, isKakaoUser = true),
                Friend("감성폐급", "이주엽", profileImageUrl = null, isKakaoUser = false),
                Friend("말이많아", "강태화", profileImageUrl = null, isKakaoUser = true),
                Friend("딸기좋아", "신하리", profileImageUrl = null, isKakaoUser = true)
            )
        } else {
            emptyList()
        }
        binding.tvFriendCount.text = "친구 (${dummyFriends.size})" //친구 숫자 반영하게 함

        // 4) empty 화면 vs 친구 리스트 화면 토글
        if (dummyFriends.isEmpty()) {
            binding.layoutEmptyFriends.visibility = View.VISIBLE
            binding.rvFriends.visibility = View.GONE
        } else {
            binding.layoutEmptyFriends.visibility = View.GONE
            binding.rvFriends.visibility = View.VISIBLE
            friendAdapter.submitList(dummyFriends)
        }
    }

    override fun onResume() {
        super.onResume()
        // 친구 화면일 때만 알림 아이콘 보이게
        (activity as? MainActivity)?.showAlarmIcon(true)
    }
}
