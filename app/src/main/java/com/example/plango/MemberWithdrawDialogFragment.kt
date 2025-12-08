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

                    android.util.Log.d(
                        "Withdraw",
                        "HTTP=${response.code()}, bodyCode=${body?.code}, msg=${body?.message}"
                    )

                    // ✅ 성공 코드 유연하게 처리 (0 또는 200 둘 다 성공으로 인정)
                    val isSuccessCode = (body?.code == 0 || body?.code == 200)

                    if (isSuccessCode) {
                        Toast.makeText(
                            requireContext(),
                            "회원탈퇴가 완료되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ 탈퇴 성공 시 공통 로그아웃 처리
                        performLocalSignOut()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "회원탈퇴에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "회원탈퇴 실패 (${response.code()})",
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
     * ✅ 로컬 토큰/세션 정리 + 로그인 화면으로 이동
     */
    private fun performLocalSignOut() {
        val activity = requireActivity()
        val appContext = activity.applicationContext

        // 1) 토큰 삭제
        val tokenManager = TokenManager(appContext)
        tokenManager.clearTokens()

        // 2) 세션 초기화
        MemberSession.clear()

        // 3) 로그인 화면으로 이동 (Task 초기화)
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // 4) 현재 액티비티 종료
        activity.finish()

        // 5) 다이얼로그 닫기
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
