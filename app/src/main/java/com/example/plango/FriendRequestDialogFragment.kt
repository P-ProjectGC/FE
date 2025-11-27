package com.example.plango

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.adapter.FriendRequestAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendRequestRepository
import com.example.plango.model.Friend

class FriendRequestDialogFragment : DialogFragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var ivEmptyIcon: ImageView
    private lateinit var rvFriendRequests: RecyclerView
    private lateinit var adapter: FriendRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.dialog_friend_request,
            container,
            false
        )

        tvTitle = view.findViewById(R.id.tvTitleFriendRequest)
        tvEmpty = view.findViewById(R.id.tvEmptyFriendRequest)
        ivEmptyIcon = view.findViewById(R.id.ivEmptyIcon)
        rvFriendRequests = view.findViewById(R.id.rvFriendRequests)
        val ivClose = view.findViewById<ImageView>(R.id.ivClose)

        // 초기 데이터
        val initialRequests: List<Friend> = FriendRequestRepository.getRequests()
        val initialCount = initialRequests.size

        // 요청 시간 더미 텍스트 (개수에 맞춰 생성)
        fun buildRequestedAtTexts(size: Int): List<String> =
            List(size) { index ->
                when (index) {
                    0 -> "3시간 전"
                    1 -> "1일 전"
                    2 -> "2일 전"
                    else -> "방금 전"
                }
            }

        // 어댑터 생성 (수락 / 거절 따로 콜백 연결)
        adapter = FriendRequestAdapter(
            items = initialRequests,
            requestedAtTexts = buildRequestedAtTexts(initialCount),
            onAcceptClick = { friend ->
                handleAccept(friend)
            },
            onRejectClick = { friend ->
                handleReject(friend)
            }
        )

        rvFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        rvFriendRequests.adapter = adapter

        // 처음 UI 상태 세팅
        updateUI(initialCount)

        ivClose.setOnClickListener { dismiss() }

        return view
    }

    /** 수락 눌렀을 때 동작 */
    private fun handleAccept(friend: Friend) {
        // 1) 친구 목록에 추가
        FriendRepository.addFriend(friend)
        // 2) 요청 목록에서 제거 + UI 갱신
        removeFromRequests(friend)

        // FriendFragment에게 "갱신해" 신호 전달
        parentFragmentManager.setFragmentResult(
            "friend_request_handled",
            Bundle().apply { putString("action", "accept") }
        )
    }

    /** 거절 눌렀을 때 동작 */
    private fun handleReject(friend: Friend) {
        // 1) 요청 목록에서 제거 + UI 갱신
        removeFromRequests(friend)

        parentFragmentManager.setFragmentResult(
            "friend_request_handled",
            Bundle().apply { putString("action", "reject") }
        )
    }

    /** 공통: 요청 리스트에서 제거 + 어댑터/타이틀/빈화면 갱신 */
    private fun removeFromRequests(friend: Friend) {
        // 1) 저장소에서 제거
        FriendRequestRepository.removeRequest(friend)

        // 2) 최신 리스트 가져오기
        val updatedList = FriendRequestRepository.getRequests()
        val newCount = updatedList.size

        // 3) 어댑터에 새로운 리스트 넣기
        adapter.submitList(
            updatedList,
            List(newCount) { index ->
                when (index) {
                    0 -> "3시간 전"
                    1 -> "1일 전"
                    2 -> "2일 전"
                    else -> "방금 전"
                }
            }
        )

        // 4) 타이틀/빈화면/리스트 표시 상태 갱신
        updateUI(newCount)

        // 5) 🔴 알림 배지 숫자도 갱신
        (activity as? MainActivity)?.updateAlarmBadge(newCount)
    }

    /** 개수에 따라 타이틀 텍스트와 빈 화면/리스트 토글 */
    private fun updateUI(count: Int) {
        tvTitle.text = "친구 요청 ($count)"

        if (count == 0) {
            rvFriendRequests.visibility = View.GONE
            ivEmptyIcon.visibility = View.VISIBLE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvFriendRequests.visibility = View.VISIBLE
            ivEmptyIcon.visibility = View.GONE
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.setCanceledOnTouchOutside(false)

        val heightPx = (400 * resources.displayMetrics.density).toInt()

        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85f).toInt(),
                heightPx
            )
        }
    }
}
