package com.example.plango

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
import android.content.Intent

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
            if (start == null || end == null || selectedNicknames.isEmpty()) {
                // TODO: 필요하면 토스트 띄워도 됨
                return@setOnClickListener
            }

            // ✅ 여행방 생성 후, 일정/지도 화면으로 이동
            val intent = Intent(requireContext(), RoomScheduleTestActivity::class.java).apply {
                putExtra("ROOM_NAME", roomName)
                putExtra("ROOM_MEMO", roomMemo)
                putExtra("START_DATE", start.toString())   // "2025-11-29" 이런 형식
                putExtra("END_DATE", end.toString())
                putStringArrayListExtra(
                    "MEMBER_NICKNAMES",
                    ArrayList(selectedNicknames)           // ⭐ 닉네임 리스트만 전달
                )
            }
            startActivity(intent)

            // CreateRoomActivity는 스택에서 제거 (뒤로가기 시 다시 안 보이게)
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
