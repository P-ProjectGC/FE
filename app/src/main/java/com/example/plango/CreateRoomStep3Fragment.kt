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
            if (btnComplete.isEnabled) {
                val roomName = etRoomName.text.toString().trim()
                val roomMemo = etRoomMemo.text.toString().trim()

                // TODO: 여기서 실제 방 생성/서버 전송 로직 호출
                // 예: (activity as? CreateRoomActivity)?.createRoom(roomName, roomMemo)

                // 일단은 액티비티 종료로 마무리 (임시)
                activity?.finish()
            }
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
