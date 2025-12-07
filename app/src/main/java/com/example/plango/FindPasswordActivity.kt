package com.example.plango.ui.findpw

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.plango.R
import com.example.plango.data.RetrofitClient
import com.example.plango.model.findpassword.CheckLoginIdRequest
import kotlinx.coroutines.launch

class FindPasswordActivity : AppCompatActivity() {

    private lateinit var btnBack: LinearLayout

    private lateinit var etLoginId: EditText
    private lateinit var btnFindPw: Button
    private lateinit var tvFindPwError: TextView
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_password)

        btnBack = findViewById(R.id.btn_back)
        etLoginId = findViewById(R.id.et_login_id)
        btnFindPw = findViewById(R.id.btn_find_pw)
        tvFindPwError = findViewById(R.id.tv_find_pw_error)
        loading = findViewById(R.id.findPwLoading)

        btnBack.setOnClickListener { finish() }

        btnFindPw.setOnClickListener {
            tvFindPwError.visibility = View.GONE
            val loginId = etLoginId.text.toString().trim()

            if (loginId.isEmpty()) {
                Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkLoginId(loginId)
        }
    }

    private fun showLoading(show: Boolean) {
        loading.visibility = if (show) View.VISIBLE else View.GONE
        btnFindPw.isEnabled = !show
        btnFindPw.alpha = if (show) 0.5f else 1f
    }

    private fun checkLoginId(loginId: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val response = RetrofitClient.authService.checkLoginIdForPassword(
                    CheckLoginIdRequest(loginId)
                )

                Log.d("FIND_PW", "checkLoginId httpCode = ${response.code()}")

                if (!response.isSuccessful) {
                    when (response.code()) {
                        404 -> {
                            tvFindPwError.text = "일치하는 아이디가 없습니다."
                            tvFindPwError.visibility = View.VISIBLE
                        }
                        400 -> {
                            tvFindPwError.text = "해당 아이디는 비밀번호를 변경할 수 없습니다."
                            tvFindPwError.visibility = View.VISIBLE
                        }
                        else -> {
                            Toast.makeText(
                                this@FindPasswordActivity,
                                "아이디 확인 중 오류가 발생했습니다. (${response.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    showLoading(false)
                    return@launch
                }

                val body = response.body()
                Log.d("FIND_PW", "checkLoginId body = $body")

                if (body == null || body.code != 0) {
                    tvFindPwError.text = body?.message ?: "아이디 검증에 실패했습니다."
                    tvFindPwError.visibility = View.VISIBLE
                    showLoading(false)
                    return@launch
                }

                // ✅ 아이디 검증 성공 → 새 비밀번호 설정 화면으로 이동
                val intent = Intent(this@FindPasswordActivity, ResetPasswordActivity::class.java)
                intent.putExtra("loginId", loginId)
                startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@FindPasswordActivity,
                    "네트워크 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
}
