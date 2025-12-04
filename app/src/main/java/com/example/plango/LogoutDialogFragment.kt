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
import com.example.plango.databinding.DialogLogoutConfirmBinding
import kotlinx.coroutines.launch

class LogoutDialogFragment : DialogFragment() {

    private var _binding: DialogLogoutConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLogoutConfirmBinding.inflate(LayoutInflater.from(context))

        // 닫기(X)
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 취소
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            // 서버 로그아웃 + 로컬 정리
            requestLogout()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    /**
     * ✅ 서버에 로그아웃 요청 + 로컬 토큰/세션 정리
     */
    private fun requestLogout() {
        lifecycleScope.launch {
            try {
                // 서버는 no-op 이지만, 향후를 위해 호출해 둠
                val response = RetrofitClient.authService.logout()
                // 굳이 code 체크 안 해도 되지만, 해 두면 좋음
                if (response.isSuccessful) {
                    // 응답 body?.code 가 0 이 아닐 가능성 거의 없지만, 그냥 무시하고 로컬 정리 진행
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 에러가 나도, 로컬 세션은 정리해서 강제 로그아웃하는 게 UX 상 더 낫다고 가정
            } finally {
                performLocalLogout()
            }
        }
    }

    /**
     * ✅ 로컬 로그아웃 처리
     *  - TokenManager 의 토큰 삭제
     *  - MemberSession 초기화
     *  - 로그인 화면으로 이동 (기존 액티비티 스택 날리기)
     */
    private fun performLocalLogout() {
        val context = requireContext()
        val appContext = context.applicationContext

        // TokenManager 에 clearTokens() 같은 함수가 있다고 가정
        val tokenManager = TokenManager(appContext)
        tokenManager.clearTokens()    // accessToken / refreshToken 모두 제거

        MemberSession.clear()

        Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

        // 🔹 로그인 화면으로 이동
        val intent = Intent(appContext, LoginActivity::class.java).apply {
            // TODO: LoginActivity 이름이 다르면 여기 클래스명만 바꿔주면 됨
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
        fun newInstance(): LogoutDialogFragment {
            return LogoutDialogFragment()
        }
    }
}
