package com.example.plango

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.RetrofitClient
import com.example.plango.data.TravelRoomRepository
import com.example.plango.model.CreateRoomRequest
import com.example.plango.model.TravelRoom
import kotlinx.coroutines.launch
import java.time.LocalDate

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

            val activity = activity as? CreateRoomActivity ?: return@setOnClickListener
            val start = activity.startDate
            val end = activity.endDate
            val selectedNicknames = activity.selectedFriendNicknames

            if (start == null || end == null) {
                Toast.makeText(requireContext(), "날짜 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 서버까지 다녀온 뒤에 로컬 저장 + 화면 이동을 한 번에 처리
            createRoomOnServerAndNavigate(
                roomName = roomName,
                roomMemo = roomMemo,
                start = start,
                end = end,
                selectedNicknames = selectedNicknames
            )
        }
    }

    // ✅ 서버 호출 + 로컬 저장 + 다음 화면 이동까지 담당
    private fun createRoomOnServerAndNavigate(
        roomName: String,
        roomMemo: String,
        start: LocalDate,
        end: LocalDate,
        selectedNicknames: List<String>
    ) {
        val memberIdHeader = 1L            // TODO: 나중에 로그인된 멤버 ID로 교체
        val memberIdsBody = listOf(2L)     // TODO: 실제 멤버 ID 리스트로 교체

        val request = CreateRoomRequest(
            roomName = roomName,
            memo = roomMemo.ifBlank { null },
            startDate = start.toString(),   // "yyyy-MM-dd"
            endDate = end.toString(),
            memberIds = memberIdsBody
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.roomApiService
                    .createRoom(memberIdHeader, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("CreateRoomAPI", "response body = $body")

                    if (body?.code == 0) {
                        Toast.makeText(
                            requireContext(),
                            "서버 방 생성 성공 (roomId=${body.data?.roomId})",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ 여기서 로컬 TravelRoom 생성 + Repository에 저장
                        val dateText = "${start.monthValue}월 ${start.dayOfMonth}일 - " +
                                "${end.monthValue}월 ${end.dayOfMonth}일"
                        val memberCount = selectedNicknames.size.takeIf { it > 0 } ?: 1

                        val newRoom = TravelRoom(
                            id = System.currentTimeMillis(),
                            title = roomName,
                            startDate = start.toString(),
                            endDate = end.toString(),
                            dateText = dateText,
                            memo = roomMemo,
                            memberCount = memberCount
                        )

                        TravelRoomRepository.addRoom(newRoom)

                        // ✅ 그리고 나서 화면 이동
                        val intent = Intent(requireContext(), RoomScheduleTestActivity::class.java).apply {
                            putExtra("ROOM_ID", newRoom.id)
                            putExtra("ROOM_NAME", roomName)
                            putExtra("ROOM_MEMO", roomMemo)
                            putExtra("START_DATE", start.toString())
                            putExtra("END_DATE", end.toString())
                            putStringArrayListExtra(
                                "MEMBER_NICKNAMES",
                                ArrayList(selectedNicknames)
                            )
                        }
                        startActivity(intent)
                        requireActivity().finish()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "서버 응답 실패: ${body?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "HTTP 오류: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CreateRoomAPI", "error", e)
                Toast.makeText(
                    requireContext(),
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
