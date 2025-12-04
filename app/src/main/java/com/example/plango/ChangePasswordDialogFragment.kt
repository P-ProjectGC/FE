package com.example.plango

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.databinding.DialogChangePasswordBinding
import com.example.plango.model.ChangePasswordRequest
import kotlinx.coroutines.launch

class ChangePasswordDialogFragment : DialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChangePasswordBinding.inflate(LayoutInflater.from(context))

        // 🔹 닫기 버튼 (X)
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 🔹 취소 버튼
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 🔹 변경 버튼
        binding.btnSave.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val newPasswordConfirm = binding.etNewPasswordConfirm.text.toString()

            // 0) 필수값 체크 (우선순위: 현재 비밀번호 관련)
            if (currentPassword.isBlank()) {
                Toast.makeText(requireContext(), "현재 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword.isBlank()) {
                Toast.makeText(requireContext(), "새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPasswordConfirm.isBlank()) {
                Toast.makeText(requireContext(), "새 비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1) (클라이언트에서 할 수 있는 부분까지) 새 비밀번호 길이 체크
            //if (newPassword.length < 8) {
                //Toast.makeText(requireContext(), "새 비밀번호는 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
               // return@setOnClickListener
           // }

            // 2) 새 비밀번호와 현재 비밀번호 동일 여부
            if (currentPassword == newPassword) {
                Toast.makeText(requireContext(), "새 비밀번호는 현재 비밀번호와 달라야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3) 새 비밀번호 & 확인 일치 여부
            if (newPassword != newPasswordConfirm) {
                Toast.makeText(requireContext(), "새 비밀번호가 서로 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 여기까지 통과하면 서버에 비밀번호 변경 요청
            changePasswordOnServer(currentPassword, newPassword, newPasswordConfirm)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    /**
     * ✅ 비밀번호 변경 API 호출 (PATCH /api/members/{memberId}/password)
     */
    private fun changePasswordOnServer(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String
    ) {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ChangePasswordRequest(
            currentPassword = currentPassword,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.changePassword(memberId, request)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.code == 0) {
                        // 🔹 성공
                        Toast.makeText(requireContext(), "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        // 🔹 서버가 준 에러 메시지 사용
                        // (예: "현재 비밀번호가 일치하지 않습니다." 같은 메시지)
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "비밀번호 변경에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // 🔹 HTTP 에러 (토큰 문제 / 서버 오류 등)
                    Toast.makeText(
                        requireContext(),
                        "비밀번호 변경 실패 (${response.code()})",
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
        fun newInstance(): ChangePasswordDialogFragment {
            return ChangePasswordDialogFragment()
        }
    }
}
