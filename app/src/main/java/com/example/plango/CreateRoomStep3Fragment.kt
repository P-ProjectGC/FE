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
import com.example.plango.data.MemberSession
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

            // 서버까지 다녀온 뒤에 로컬 저장 + 화면 이동을 한 번에 처리
            createRoomOnServerAndNavigate(
                roomName = roomName,
                roomMemo = roomMemo,
                start = start,
                end = end,
                selectedNicknames = selectedNicknames
            )
        }
    }

    // 서버 호출 + 로컬 저장 + 다음 화면 이동까지 담당
    private fun createRoomOnServerAndNavigate(
        roomName: String,
        roomMemo: String,
        start: LocalDate,
        end: LocalDate,
        selectedNicknames: List<String>
    ) {
        // 1) 헤더에 들어갈 내 memberId
        val memberIdHeader = MemberSession.currentMemberId

        // 2) Step2에서 선택한 친구들의 memberId 리스트 가져오기
        //    (CreateRoomActivity에 우리가 미리 저장해둔 값)
        val parentActivity = activity as? CreateRoomActivity
        val selectedFriendIds: List<Long> = parentActivity?.selectedFriendIds ?: emptyList()

        // 3) 서버에 보낼 memberIds:
        //    - 친구가 있으면: 그 친구들 memberId
        //    - 친구를 안 골랐다면: 나 혼자 여행 → 내 memberId만
        val memberIdsBody: List<Long> =
            if (selectedFriendIds.isNotEmpty()) {
                selectedFriendIds
            } else {
                listOf(memberIdHeader)
            }

        val request = CreateRoomRequest(
            roomName = roomName,
            memo = roomMemo.ifBlank { null },
            startDate = start.toString(),   // "yyyy-MM-dd"
            endDate = end.toString(),
            memberIds = memberIdsBody       // ✅ 실제 멤버 ID 리스트로 교체 완료
        )

        Log.d("CreateRoomFinal", "start=$start / end=$end")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.roomApiService
                    .createRoom( request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("CreateRoomAPI", "response body = $body")

                    if (body?.code == 0) {
                        val roomDto = body.data

                        if (roomDto == null) {
                            Toast.makeText(
                                requireContext(),
                                "서버에서 방 정보를 받지 못했어요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        Toast.makeText(
                            requireContext(),
                            "서버 방 생성 성공! (roomId=${roomDto.roomId})",
                            Toast.LENGTH_SHORT
                        ).show()

                        val dateText = "${start.monthValue}월 ${start.dayOfMonth}일 - " +
                                "${end.monthValue}월 ${end.dayOfMonth}일"
                        val memberCount = selectedNicknames.size.takeIf { it > 0 } ?: 1

                        val newRoom = TravelRoom(
                            id = roomDto.roomId,
                            title = roomName,
                            startDate = start.toString(),
                            endDate = end.toString(),
                            dateText = dateText,
                            memo = roomMemo.ifBlank { null },
                            memberCount = memberCount,
                            memberNicknames = selectedNicknames.ifEmpty { listOf("나") },
                            isHost = true   // 생성자는 방장 확정
                        )

                        // 로컬 저장
                        TravelRoomRepository.addRoom(newRoom)

                        // 방 내부 화면으로 이동
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
                            putExtra("IS_HOST", true)
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
