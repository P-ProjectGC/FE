package com.example.plango

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.plango.data.TravelRoomRepository
import com.example.plango.model.TravelRoom

class CreateRoomStep3Fragment : Fragment(R.layout.fragment_create_room_step3) {

    private lateinit var etRoomName: EditText
    private lateinit var etRoomMemo: EditText
    private lateinit var tvMemoCount: TextView
    private lateinit var btnComplete: Button

    private val memoMaxLength = 500

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 헤더를 3단계 상태로
        (activity as? CreateRoomActivity)?.setStep(3)

        initViews(view)
        setupMemoLimit()
        setupTextWatchers()
        setupButton()
    }

    private fun initViews(view: View) {
        etRoomName = view.findViewById(R.id.et_room_name)
        etRoomMemo = view.findViewById(R.id.et_room_memo)
        tvMemoCount = view.findViewById(R.id.tv_memo_count)
        btnComplete = view.findViewById(R.id.btn_complete_step3)
    }

    private fun setupMemoLimit() {
        etRoomMemo.filters = arrayOf(InputFilter.LengthFilter(memoMaxLength))
        updateMemoCount()
    }

    private fun setupTextWatchers() {
        etRoomName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCompleteButtonState()
            }
        })

        etRoomMemo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateMemoCount()
            }
        })
    }

    private fun setupButton() {
        updateCompleteButtonState()

        btnComplete.setOnClickListener {
            if (!btnComplete.isEnabled) return@setOnClickListener

            val roomName = etRoomName.text.toString().trim()
            val roomMemo = etRoomMemo.text.toString().trim()

            // ⭐ Activity에서 날짜 + 친구 닉네임 가져오기
            val activity = activity as? CreateRoomActivity ?: return@setOnClickListener
            val start = activity.startDate
            val end = activity.endDate
            val selectedNicknames = activity.selectedFriendNicknames

            // 안전 방어 (정상 플로우면 안 걸림)
            if (start == null || end == null) {
                // TODO: 필요하면 토스트 띄우기
                return@setOnClickListener
            }

            // 📅 리스트에 보여줄 날짜 텍스트 (더미 데이터 스타일 맞추기)
            // 예: "8월 3일 - 8월 5일"
            val dateText = "${start.monthValue}월 ${start.dayOfMonth}일 - " +
                    "${end.monthValue}월 ${end.dayOfMonth}일"

            // 👥 인원 수 (나중에 실제 멤버 수로 바꿔도 됨)
            val memberCount = selectedNicknames.size.takeIf { it > 0 } ?: 1

            // ✅ 1) 새 TravelRoom 객체 생성
            val newRoom = TravelRoom(
                id = System.currentTimeMillis(),
                title = roomName,
                startDate = start.toString(),   // "2025-11-29"
                endDate = end.toString(),       // "2025-12-02"
                dateText = dateText,            // "11월 29일 - 12월 2일"
                memo = roomMemo,
                memberCount = memberCount
            )


            // ✅ 2) Repository에 방 추가 -> 방 목록에서 사용할 데이터
            TravelRoomRepository.addRoom(newRoom)

            // ✅ 3) 일정/지도 화면으로 이동 (지금까지 쓰던 테스트용 플로우 유지)
            val intent = Intent(requireContext(), RoomScheduleTestActivity::class.java).apply {
                putExtra("ROOM_NAME", roomName)
                putExtra("ROOM_MEMO", roomMemo)
                putExtra("START_DATE", start.toString())   // "2025-11-29" 형식
                putExtra("END_DATE", end.toString())
                putStringArrayListExtra(
                    "MEMBER_NICKNAMES",
                    ArrayList(selectedNicknames)           // ⭐ 닉네임 리스트 전달
                )
            }
            startActivity(intent)

            // CreateRoomActivity는 스택에서 제거 (뒤로가기 시 방 목록으로)
            requireActivity().finish()
        }
    }

    private fun updateMemoCount() {
        val length = etRoomMemo.text?.length ?: 0
        tvMemoCount.text = "${length}/${memoMaxLength}자"
    }

    private fun updateCompleteButtonState() {
        val enabled = etRoomName.text?.toString()?.trim().isNullOrEmpty().not()
        btnComplete.isEnabled = enabled

        if (enabled) {
            btnComplete.setBackgroundResource(R.drawable.bg_btn_next_enabled)
            btnComplete.backgroundTintList = null
            btnComplete.setTextColor(Color.WHITE)
        } else {
            btnComplete.setBackgroundResource(R.drawable.bg_btn_next_disabled)
            btnComplete.backgroundTintList = null
            btnComplete.setTextColor(Color.parseColor("#B3FFFFFF"))
        }
    }
}
