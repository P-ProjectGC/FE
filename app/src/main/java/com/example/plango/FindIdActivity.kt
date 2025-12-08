package com.example.plango.ui.findid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.plango.LoginActivity
import com.example.plango.R
import com.example.plango.data.RetrofitClient
import com.example.plango.model.findid.FindIdRequest
import com.example.plango.model.findid.SendFindIdCodeRequest
import kotlinx.coroutines.launch

class FindIdActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnFindId: Button
    private lateinit var tvError: TextView
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        etEmail = findViewById(R.id.et_email)
        btnFindId = findViewById(R.id.btn_find_id)
        tvError = findViewById(R.id.tv_find_id_error)
        loading = findViewById(R.id.findIdLoading)

        // 🔥🔥🔥 여기 바로 아래에 뒤로가기 버튼 코드 넣으면 됨!!
        val btnBack = findViewById<View>(R.id.btn_back)
        btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnFindId.setOnClickListener {
            tvError.visibility = View.GONE   // 버튼 누를 때마다 에러 초기화
            val email = etEmail.text.toString().trim()

            // 1) 기본 검증
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2) 서버에 아이디 찾기 요청 → 이메일 존재 여부 확인
            findIdAndSendCode(email)
        }
    }

    private fun showLoading(show: Boolean) {
        loading.visibility = if (show) View.VISIBLE else View.GONE
        btnFindId.isEnabled = !show
        btnFindId.alpha = if (show) 0.5f else 1f
    }

    // 🔹 1단계: /api/auth/find-id 호출 → 이메일 있는지 확인
    // 🔹 2단계: 성공 시 /api/auth/find-id/send-code 호출 → 인증번호 발송
    private fun findIdAndSendCode(email: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // 1) /api/auth/find-id
                val findIdResponse = RetrofitClient.authService.findId(
                    FindIdRequest(email = email)
                )

                Log.d("FIND_ID", "findId httpCode = ${findIdResponse.code()}")

                if (!findIdResponse.isSuccessful) {
                    // HTTP 404 등
                    if (findIdResponse.code() == 404) {
                        tvError.text = "입력하신 정보와 일치하는 아이디를 찾을 수 없습니다."
                        tvError.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@FindIdActivity,
                            "아이디 찾기 중 오류가 발생했습니다. (${findIdResponse.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showLoading(false)
                    return@launch
                }

                val body = findIdResponse.body()
                Log.d("FIND_ID", "findId body = $body")
                Log.d("FIND_ID", "findId apiCode = ${body?.code}")
                Log.d("FIND_ID", "findId data = ${body?.data}")

                if (body == null || body.code != 0 || body.data == null) {
                    // 서버에서 code != 0 이거나 data null인 경우
                    tvError.text = body?.message ?: "아이디 찾기에 실패했습니다."
                    tvError.visibility = View.VISIBLE
                    showLoading(false)
                    return@launch
                }

                // 여기까지 왔다 = 이메일 존재 + maskedLoginId 도착
                val maskedLoginId = body.data.maskedLoginId
                Log.d("FIND_ID", "maskedLoginId = $maskedLoginId")

//                // 2) 존재하는 이메일이면 인증번호 발송 API 호출
//                val sendCodeResponse = RetrofitClient.authService.sendFindIdCode(
//                    SendFindIdCodeRequest(email = email)
//                )
//
//                Log.d("FIND_ID", "sendCode httpCode = ${sendCodeResponse.code()}")
//
//                if (!sendCodeResponse.isSuccessful) {
//                    Toast.makeText(
//                        this@FindIdActivity,
//                        "인증번호 발송 중 오류가 발생했습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    showLoading(false)
//                    return@launch
//                }
//
//                val sendCodeBody = sendCodeResponse.body()
//                Log.d("FIND_ID", "sendCode body = $sendCodeBody")
//                Log.d("FIND_ID", "sendCode apiCode = ${sendCodeBody?.code}")
//                Log.d("FIND_ID", "sendCode data = ${sendCodeBody?.data}")
//                Log.d(
//                    "FIND_ID",
//                    "sendCode verificationCode = ${sendCodeBody?.data?.verificationCode}"
//                )
//
//                if (sendCodeBody == null || sendCodeBody.code != 0 || sendCodeBody.data == null) {
//                    Toast.makeText(
//                        this@FindIdActivity,
//                        sendCodeBody?.message ?: "인증번호 발송에 실패했습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    showLoading(false)
//                    return@launch
//                }
//
//                // ✅ 여기서 maskedEmail 뽑아옴
//                val maskedEmail = sendCodeBody.data.maskedEmail
//
//                // 성공: 이메일로 인증번호 발송 완료
//                tvError.visibility = View.GONE
//                Toast.makeText(
//                    this@FindIdActivity,
//                    "입력하신 이메일로 인증번호를 발송했어요.",
//                    Toast.LENGTH_SHORT
//                ).show()

//                // ✅ 마스킹 아이디 결과 화면으로 이동
//                val intent = Intent(this@FindIdActivity, FindIdResultActivity::class.java).apply {
//                    putExtra("maskedLoginId", maskedLoginId)   // /find-id 에서 받은 값
//                    putExtra("email", email)                   // 원본 이메일
//                    putExtra("maskedEmail", maskedEmail)       // 화면 안내용 마스킹 이메일
//                }
//                startActivity(intent)
//                finish()   // 뒤로 가기 눌렀을 때 다시 이메일 입력 화면 안 보이게

            val intent = Intent(this@FindIdActivity, FindIdResultActivity::class.java).apply {
                putExtra("maskedLoginId", maskedLoginId)
                putExtra("email", email)
            }
            startActivity(intent)
            finish()


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("FIND_ID", "findIdAndSendCode Exception: ${e.message}", e)
                Toast.makeText(
                    this@FindIdActivity,
                    "네트워크 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
}
