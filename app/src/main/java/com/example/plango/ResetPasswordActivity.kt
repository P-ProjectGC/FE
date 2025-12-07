package com.example.plango.ui.findpw

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.plango.LoginActivity
import com.example.plango.R
import com.example.plango.data.RetrofitClient
import com.example.plango.model.findpassword.ResetPasswordRequest
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var btnBack: LinearLayout
    private lateinit var etNewPassword: EditText
    private lateinit var etNewPasswordConfirm: EditText
    private lateinit var tvPwError: TextView
    private lateinit var btnChangePw: Button
    private lateinit var loading: ProgressBar

    private var loginId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        loginId = intent.getStringExtra("loginId")
        if (loginId.isNullOrEmpty()) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnBack = findViewById(R.id.btn_back)
        etNewPassword = findViewById(R.id.et_new_password)
        etNewPasswordConfirm = findViewById(R.id.et_new_password_confirm)
        tvPwError = findViewById(R.id.tv_pw_error)
        btnChangePw = findViewById(R.id.btn_change_pw)
        loading = findViewById(R.id.resetPwLoading)

        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        btnChangePw.setOnClickListener {
            tvPwError.visibility = View.GONE
            val newPw = etNewPassword.text.toString()
            val newPwConfirm = etNewPasswordConfirm.text.toString()
            resetPassword(newPw, newPwConfirm)
        }
    }

    private fun showLoading(show: Boolean) {
        loading.visibility = if (show) View.VISIBLE else View.GONE
        btnChangePw.isEnabled = !show
        btnChangePw.alpha = if (show) 0.5f else 1f
    }

    private fun resetPassword(newPw: String, newPwConfirm: String) {
        val id = loginId ?: run {
            Toast.makeText(this, "아이디 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPw.isEmpty() || newPwConfirm.isEmpty()) {
            tvPwError.text = "새 비밀번호와 확인란을 모두 입력하세요."
            tvPwError.visibility = View.VISIBLE
            return
        }

        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$")
        if (!regex.matches(newPw)) {
            tvPwError.text = "비밀번호는 영문+숫자 조합 6~20자여야 합니다."
            tvPwError.visibility = View.VISIBLE
            return
        }

        if (newPw != newPwConfirm) {
            tvPwError.text = "새 비밀번호와 확인 값이 일치하지 않습니다."
            tvPwError.visibility = View.VISIBLE
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true)

                val response = RetrofitClient.authService.resetPassword(
                    ResetPasswordRequest(
                        loginId = id,
                        newPassword = newPw,
                        newPasswordConfirm = newPwConfirm
                    )
                )

                Log.d("RESET_PW", "resetPassword httpCode = ${response.code()}")

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "비밀번호 변경 중 오류가 발생했습니다. (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                    return@launch
                }

                val body = response.body()
                Log.d("RESET_PW", "resetPassword body = $body")

                if (body == null || body.code != 0) {
                    tvPwError.text = body?.message ?: "비밀번호 변경에 실패했습니다."
                    tvPwError.visibility = View.VISIBLE
                    showLoading(false)
                    return@launch
                }

                Toast.makeText(
                    this@ResetPasswordActivity,
                    body.message.ifEmpty { "비밀번호 변경에 성공했습니다." },
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java).apply {
                    // 기존 스택(FindPasswordActivity, ResetPasswordActivity) 다 날리고
                    // LoginActivity만 남기기
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()


            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "네트워크 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
}
