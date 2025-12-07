package com.example.plango.ui.findid

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.plango.R
import com.example.plango.data.RetrofitClient
import com.example.plango.model.findid.SendFindIdCodeRequest
import com.example.plango.model.findid.VerifyFindIdCodeRequest
import kotlinx.coroutines.launch

class VerifyFindIdCodeActivity : AppCompatActivity() {

    private lateinit var btnBack: View
    private lateinit var tvDesc: TextView
    private lateinit var etCode: EditText
    private lateinit var tvError: TextView
    private lateinit var tvResend: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnVerify: Button
    private lateinit var loading: ProgressBar

    private var email: String = ""
    private var maskedEmail: String = ""

    private var timer: CountDownTimer? = null
    private var isExpired: Boolean = false   // 5분 지나면 true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_find_id_code)

        // 인텐트 값
        email = intent.getStringExtra("email") ?: ""
        maskedEmail = intent.getStringExtra("maskedEmail") ?: email

        if (email.isEmpty()) {
            Toast.makeText(this, "이메일 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 뷰 바인딩
        btnBack = findViewById(R.id.btn_back)
        tvDesc = findViewById(R.id.tv_verify_desc)
        etCode = findViewById(R.id.et_verify_code)
        tvError = findViewById(R.id.tv_verify_error)
        tvResend = findViewById(R.id.tv_resend)
        tvTimer = findViewById(R.id.tv_timer)
        btnVerify = findViewById(R.id.btn_verify)
        loading = findViewById(R.id.verifyLoading)

        // 설명 텍스트에 마스킹된 이메일 넣기
        tvDesc.text = "${maskedEmail} 으로 발송된\n6자리 인증번호를 입력해주세요."

        // 뒤로가기
        btnBack.setOnClickListener { finish() }

        // 타이머 시작 (5분)
        startTimer()

        // 인증번호 확인 클릭
        btnVerify.setOnClickListener {
            tvError.visibility = View.GONE
            val code = etCode.text.toString().trim()

            if (code.length != 6) {
                tvError.text = "인증번호 6자리를 입력해주세요."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (isExpired) {
                tvError.text = "인증번호가 만료되었습니다. 재전송을 눌러 새 번호를 받아주세요."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            verifyCode(email, code)
        }

        // 재전송 클릭
        tvResend.setOnClickListener {
            resendCode(email)
        }
    }

    private fun showLoading(show: Boolean) {
        loading.visibility = if (show) View.VISIBLE else View.GONE
        btnVerify.isEnabled = !show
        tvResend.isEnabled = !show
        btnVerify.alpha = if (show) 0.5f else 1f
    }

    // 5분 타이머 시작/리셋
    private fun startTimer() {
        timer?.cancel()
        isExpired = false

        timer = object : CountDownTimer(5 * 60 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSec = millisUntilFinished / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                tvTimer.text = String.format("남은 시간: %d:%02d", min, sec)
            }

            override fun onFinish() {
                isExpired = true
                tvTimer.text = "남은 시간: 0:00"
                tvError.text = "인증번호가 만료되었습니다. 재전송을 눌러 새 번호를 받아주세요."
                tvError.visibility = View.VISIBLE
            }
        }.start()
    }

    // 인증번호 검증 API: /api/auth/find-id/verify-code
    private fun verifyCode(email: String, code: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val response = RetrofitClient.authService.verifyFindIdCode(
                    VerifyFindIdCodeRequest(
                        email = email,
                        code = code
                    )
                )

                Log.d("VERIFY_ID", "verifyCode httpCode = ${response.code()}")

                if (!response.isSuccessful) {
                    if (response.code() == 400 || response.code() == 404) {
                        tvError.text = "인증번호가 올바르지 않거나 만료되었습니다."
                        tvError.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@VerifyFindIdCodeActivity,
                            "인증번호 확인 중 오류가 발생했습니다. (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val body = response.body()
                Log.d("VERIFY_ID", "verifyCode body = $body")

                if (body == null || body.code != 0 || body.data == null) {
                    tvError.text = body?.message ?: "인증번호 검증에 실패했습니다."
                    tvError.visibility = View.VISIBLE
                    return@launch
                }

                // 🎉 성공 - 전체 로그인 ID 획득
                val loginId = body.data.loginId

                // ➡ 전체 아이디 보여주는 화면으로 이동
                val intent = Intent(
                    this@VerifyFindIdCodeActivity,
                    FindIdFullResultActivity::class.java
                ).apply {
                    putExtra("loginId", loginId)
                }
                startActivity(intent)

                // 이 화면은 닫기
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@VerifyFindIdCodeActivity,
                    "네트워크 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    // 인증번호 재발송: /api/auth/find-id/send-code
    private fun resendCode(email: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                tvError.visibility = View.GONE

                val response = RetrofitClient.authService.sendFindIdCode(
                    SendFindIdCodeRequest(email = email)
                )

                Log.d("VERIFY_ID", "resendCode httpCode = ${response.code()}")

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@VerifyFindIdCodeActivity,
                        "인증번호 재전송 중 오류가 발생했습니다. (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val body = response.body()
                Log.d("VERIFY_ID", "resendCode body = $body")

                if (body == null || body.code != 0 || body.data == null) {
                    Toast.makeText(
                        this@VerifyFindIdCodeActivity,
                        body?.message ?: "인증번호 재전송에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // 새 인증번호 발송 성공
                Toast.makeText(
                    this@VerifyFindIdCodeActivity,
                    "새 인증번호를 이메일로 발송했어요.",
                    Toast.LENGTH_SHORT
                ).show()

                // 타이머 리셋
                startTimer()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@VerifyFindIdCodeActivity,
                    "네트워크 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
