package com.example.plango

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.adapter.FriendSelectAdapter_rm
import com.example.plango.adapter.SelectedFriendChipAdapter_rm
import com.example.plango.data.FriendRepository
import com.example.plango.model.Friend

class CreateRoomStep2Fragment : Fragment(R.layout.fragment_create_room_step2) {

    private lateinit var etSearchNickname: EditText
    private lateinit var rvFriendList: RecyclerView
    private lateinit var btnNext: Button

    // 선택 요약 뷰
    private lateinit var tvSelectedCount: TextView
    private lateinit var rvSelectedFriends: RecyclerView

    private lateinit var friendAdapter: FriendSelectAdapter_rm
    private lateinit var chipAdapter: SelectedFriendChipAdapter_rm

    // 전체 친구 목록 (검색 전 원본)
    private var allFriends: List<Friend> = emptyList()

    // 선택된 친구 (닉네임 기준으로 관리)
    private val selectedNicknames = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 헤더를 2단계 상태로
        (activity as? CreateRoomActivity)?.setStep(2)

        initViews(view)
        setupFriendList()
        setupSelectedChips()
        setupButtons()
        setupSearch()

        loadFriends()
    }

    private fun initViews(view: View) {
        etSearchNickname = view.findViewById(R.id.et_search_nickname)
        rvFriendList = view.findViewById(R.id.rv_friend_list)
        btnNext = view.findViewById(R.id.btn_next_step2)

        tvSelectedCount = view.findViewById(R.id.tv_selected_count)
        rvSelectedFriends = view.findViewById(R.id.rv_selected_friends)
    }

    private fun setupFriendList() {
        friendAdapter = FriendSelectAdapter_rm { friend ->
            toggleFriendSelection(friend)
        }

        rvFriendList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendAdapter
        }

        updateNextButtonState()
    }

    private fun setupSelectedChips() {
        chipAdapter = SelectedFriendChipAdapter_rm { friend ->
            // 칩 눌렀을 때도 선택 해제
            if (selectedNicknames.remove(friend.nickname)) {
                applyFilter(etSearchNickname.text?.toString()?.trim().orEmpty())
                updateSelectedSummary()
                updateNextButtonState()
            }
        }

        rvSelectedFriends.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = chipAdapter
        }
    }

    private fun setupButtons() {
        btnNext.setOnClickListener {
            if (selectedNicknames.isNotEmpty()) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fcv_create_room_container,
                        CreateRoomStep3Fragment()
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setupSearch() {
        etSearchNickname.addTextChangedListener(object : TextWatcher {
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
            ) { }

            override fun afterTextChanged(s: Editable?) {
                val keyword = s?.toString()?.trim().orEmpty()
                applyFilter(keyword)
            }
        })
    }

    private fun loadFriends() {
        allFriends = FriendRepository.getFriends()
        applyFilter(etSearchNickname.text?.toString()?.trim().orEmpty())
        updateSelectedSummary()
    }

    // 검색: 내 친구 목록에서 닉네임/실명으로 필터
    private fun applyFilter(keyword: String) {
        val filtered = if (keyword.isBlank()) {
            allFriends
        } else {
            allFriends.filter { friend ->
                friend.nickname.contains(keyword, ignoreCase = true) ||
                        friend.realName.contains(keyword, ignoreCase = true)
            }
        }

        // 선택 상태 유지한 채로 리스트 갱신
        friendAdapter.submitList(filtered, selectedNicknames)
    }

    private fun toggleFriendSelection(friend: Friend) {
        if (selectedNicknames.contains(friend.nickname)) {
            selectedNicknames.remove(friend.nickname)
        } else {
            selectedNicknames.add(friend.nickname)
        }

        applyFilter(etSearchNickname.text?.toString()?.trim().orEmpty())
        updateSelectedSummary()
        updateNextButtonState()
    }

    private fun updateSelectedSummary() {
        val count = selectedNicknames.size
        tvSelectedCount.text = "${count}명 선택됨"

        val selectedFriends = allFriends.filter { it.nickname in selectedNicknames }
        chipAdapter.submitList(selectedFriends)
    }

    private fun updateNextButtonState() {
        val enabled = selectedNicknames.isNotEmpty()
        btnNext.isEnabled = enabled

        if (enabled) {
            btnNext.setBackgroundResource(R.drawable.bg_btn_next_enabled)
            btnNext.backgroundTintList = null
            btnNext.setTextColor(Color.WHITE)
        } else {
            btnNext.setBackgroundResource(R.drawable.bg_btn_next_disabled)
            btnNext.backgroundTintList = null
            btnNext.setTextColor(Color.parseColor("#B3FFFFFF"))
        }
    }
}
