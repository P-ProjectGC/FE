package com.example.plango

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.databinding.DialogNicknameEditBinding
import com.example.plango.model.ProfileUpdateRequest
import kotlinx.coroutines.launch

class NicknameEditDialogFragment : DialogFragment() {

    interface OnNicknameSavedListener {
        fun onNicknameSaved(newNickname: String)
    }

    private var _binding: DialogNicknameEditBinding? = null
    private val binding get() = _binding!!

    // ✅ 중복확인 완료 + 사용 가능 여부
    private var isNicknameAvailable: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogNicknameEditBinding.inflate(LayoutInflater.from(context))

        val currentNickname = arguments?.getString(ARG_CURRENT_NICKNAME).orEmpty()
        binding.etNickname.setText(currentNickname)

        // ✅ 저장 버튼은 항상 클릭 가능하게 둔다
        binding.btnSave.isEnabled = true

        // ✅ 입력이 바뀌면 다시 중복확인 필요
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 닉네임이 조금이라도 바뀌면 다시 중복확인해야 함
                isNicknameAvailable = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔹 닫기 버튼 (X)
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 🔹 취소 버튼
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 🔹 중복확인 버튼
        binding.btnCheck.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()

            //  길이 검증은 "중복확인" 시점에만 수행
            if (nickname.length < 2) {
                Toast.makeText(requireContext(), "닉네임은 2~10자로 입력해주세요ㅠㅠ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nickname.length > 10) {
                Toast.makeText(requireContext(), "닉네임은 2~10자로 입력해주세요ㅠㅠ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥특수문자 포함 여부 체크
            val regex = "^[a-zA-Z0-9가-힣]+$".toRegex()
            if (!regex.matches(nickname)) {
                Toast.makeText(requireContext(), "닉네임에는 특수문자를 사용할 수 없어요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkNicknameAvailable(nickname)
        }

        // 🔹 저장 버튼
        binding.btnSave.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()

            // ✅ 1순위: 중복확인 안 했으면 무조건 여기서 막는다
            if (!isNicknameAvailable) {
                Toast.makeText(requireContext(), "먼저 중복확인을 해주세요!!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⚠️ 여기까지 왔다는 건:
            // - 길이는 이미 중복확인 시점에서 검증 통과했음
            // - 이후에 닉네임을 수정하면 isNicknameAvailable=false로 다시 떨어지기 때문에
            //   "유효하지 않은 길이 + 중복확인 되어 있음" 상태는 원천적으로 안 생김

            updateNicknameOnServer(nickname)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    /**
     * ✅ 닉네임 중복확인 API 호출
     * GET /api/auth/check/nickname?nickname=...
     */
    private fun checkNicknameAvailable(nickname: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.checkNickname(nickname)

                if (response.isSuccessful) {
                    val body = response.body()
                    val available = body?.data?.available == true

                    if (available) {
                        isNicknameAvailable = true
                        Toast.makeText(requireContext(), "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        isNicknameAvailable = false
                        Toast.makeText(requireContext(), "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "중복확인 실패 (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ✅ 닉네임 변경 API 호출 (PATCH /api/members/{memberId})
     */
    private fun updateNicknameOnServer(newNickname: String) {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdateRequest(
            nickname = newNickname,
            profileImageUrl = MemberSession.profileImageUrl  // 기존 이미지 유지
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.updateProfile(memberId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    // ApiResponse<Any> 기준: code == 0 이면 성공
                    if (body?.code == 0) {
                        // 세션에 반영
                        MemberSession.nickname = newNickname

                        // 부모 프래그먼트에 전달
                        (parentFragment as? OnNicknameSavedListener)?.onNicknameSaved(newNickname)

                        Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "닉네임 변경 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "닉네임 변경 실패 (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CURRENT_NICKNAME = "current_nickname"

        fun newInstance(currentNickname: String): NicknameEditDialogFragment {
            return NicknameEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CURRENT_NICKNAME, currentNickname)
                }
            }
        }
    }
}
