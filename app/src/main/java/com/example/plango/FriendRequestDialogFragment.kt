package com.example.plango

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.adapter.FriendRequestAdapter
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendRequestRepository
import com.example.plango.data.MemberSession
import com.example.plango.model.Friend
import com.example.plango.model.FriendRequestItem
import kotlinx.coroutines.launch

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

        // 뷰 초기화
        tvTitle = view.findViewById(R.id.tvTitleFriendRequest)
        tvEmpty = view.findViewById(R.id.tvEmptyFriendRequest)
        ivEmptyIcon = view.findViewById(R.id.ivEmptyIcon)
        rvFriendRequests = view.findViewById(R.id.rvFriendRequests)
        val ivClose = view.findViewById<ImageView>(R.id.ivClose)

        // 로컬 초기 데이터
        val initialRequests: List<FriendRequestItem> = FriendRequestRepository.getRequests()
        val initialCount = initialRequests.size

        // 어댑터 생성
        adapter = FriendRequestAdapter(
            items = initialRequests,
            onAcceptClick = { item -> handleAccept(item) },
            onRejectClick = { item -> handleReject(item) }
        )

        rvFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        rvFriendRequests.adapter = adapter

        updateUI(initialCount)
        ivClose.setOnClickListener { dismiss() }

        // 서버에서 받은 친구 요청 목록 불러오기
        lifecycleScope.launch {
            val result = FriendRepository.fetchReceivedFriendRequests(
                MemberSession.currentMemberId
            )

            result.onSuccess { requestList ->
                // FriendRequestRepository.setRequests(...) 는 Repository 내부에서 처리된다고 가정
                adapter.submitList(requestList)
                updateUI(requestList.size)

                // 알림 배지 갱신
                (activity as? MainActivity)?.updateAlarmBadge(requestList.size)
            }.onFailure { e ->
                Toast.makeText(
                    requireContext(),
                    "친구 요청 조회 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    /** 수락 눌렀을 때 동작 */
    private fun handleAccept(item: FriendRequestItem) {
        val myMemberId = MemberSession.currentMemberId
        val requestId = item.requestId

        lifecycleScope.launch {
            val result = FriendRepository.acceptFriendRequest(myMemberId, requestId)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "${item.senderNickname}님과 친구가 되었습니다!",
                    Toast.LENGTH_SHORT
                ).show()

                // 친구 목록에 추가
                val newFriend = Friend(
                    nickname = item.senderNickname,
                    realName = item.senderNickname,
                    profileImageUrl = null,
                    isKakaoUser = item.isKakaoUser
                )
                FriendRepository.addFriend(newFriend)

                // 요청 목록에서 제거 + UI 갱신
                removeFromRequests(item)

                // FriendFragment에게 "갱신해" 신호 전달
                parentFragmentManager.setFragmentResult(
                    "friend_request_handled",
                    Bundle().apply { putString("action", "accept") }
                )
            }.onFailure { e ->
                val message = e.message ?: "친구 요청 수락에 실패했습니다."
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** 거절 눌렀을 때 동작 */
    private fun handleReject(item: FriendRequestItem) {
        val myMemberId = MemberSession.currentMemberId
        val requestId = item.requestId

        lifecycleScope.launch {
            val result = FriendRepository.rejectFriendRequest(myMemberId, requestId)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "${item.senderNickname}님의 요청을 거절했습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                removeFromRequests(item)

                parentFragmentManager.setFragmentResult(
                    "friend_request_handled",
                    Bundle().apply { putString("action", "reject") }
                )
            }.onFailure { e ->
                val message = e.message ?: "친구 요청 거절 처리 중 알 수 없는 오류가 발생했습니다."
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** 요청 리스트에서 제거 + 어댑터/타이틀/빈화면 갱신 */
    private fun removeFromRequests(item: FriendRequestItem) {
        FriendRequestRepository.removeRequest(item)

        val updatedList: List<FriendRequestItem> = FriendRequestRepository.getRequests()
        val newCount = updatedList.size

        adapter.submitList(updatedList)
        updateUI(newCount)

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
