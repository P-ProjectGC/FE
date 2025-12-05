package com.example.plango

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.data.token.TokenManager
import com.example.plango.databinding.DialogMemberWithdrawBinding
import kotlinx.coroutines.launch

class MemberWithdrawDialogFragment : DialogFragment() {

    private var _binding: DialogMemberWithdrawBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogMemberWithdrawBinding.inflate(LayoutInflater.from(context))

        // 닫기(X)
        binding.btnClose.setOnClickListener { dismiss() }

        // 취소
        binding.btnCancel.setOnClickListener { dismiss() }

        // 탈퇴 버튼
        binding.btnWithdraw.setOnClickListener {
            requestWithdraw()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    /**
     * ✅ 서버에 회원탈퇴 요청 (DELETE /api/members/{memberId})
     */
    private fun requestWithdraw() {
        val memberId = MemberSession.currentMemberId
        if (memberId == -1L) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.withdrawMember(memberId)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.code == 200) {
                        // 🔹 탈퇴 성공
                        Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        // ✅ 1) 로컬 토큰/세션 정리
                        val appContext = requireContext().applicationContext
                        val tokenManager = TokenManager(appContext)
                        tokenManager.clearTokens()      // access / refresh 토큰 삭제
                        MemberSession.clear()           // 세션 초기화

                        // ✅ 2) 로그인 화면으로 이동 (기존 액티비티 스택 제거)
                        val intent = Intent(appContext, LoginActivity::class.java).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                            )
                        }
                        startActivity(intent)

                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "회원탈퇴에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // ... (HTTP 에러 처리 기존 그대로) ...
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * ✅ 로컬 토큰/세션 정리 + 로그인 화면으로 이동
     */
    private fun performLocalSignOut() {
        val context = requireContext()
        val appContext = context.applicationContext

        // 토큰 삭제
        val tokenManager = TokenManager(appContext)
        tokenManager.clearTokens()

        // 세션 초기화
        MemberSession.clear()

        // 로그인 화면으로 이동
        val intent = Intent(appContext, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MemberWithdrawDialogFragment {
            return MemberWithdrawDialogFragment()
        }
    }
}
