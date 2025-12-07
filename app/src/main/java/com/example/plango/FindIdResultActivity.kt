package com.example.plango.ui.findid

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plango.LoginActivity
import com.example.plango.R

class FindIdResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id_result)

        // 👇 여기서 바로 로컬 변수로 뷰 얻기 (lateinit 안 씀)
        val btnBack = findViewById<android.view.View>(R.id.btn_back)
        val tvResult = findViewById<TextView>(R.id.tv_find_id_result)
        val tvInfo = findViewById<TextView>(R.id.tv_find_id_info)
        val btnGoVerify = findViewById<Button>(R.id.btn_go_verify)
        val btnGoLogin = findViewById<Button>(R.id.btn_go_login)

        // 인텐트 값
        val maskedLoginId = intent.getStringExtra("maskedLoginId") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val maskedEmail = intent.getStringExtra("maskedEmail") ?: email

        if (maskedLoginId.isEmpty()) {
            Toast.makeText(this, "아이디 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 아이디 문구 스타일 적용
        val fullText = "회원님의 아이디는 $maskedLoginId 입니다."
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(maskedLoginId)
        val end = start + maskedLoginId.length
        if (start >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(0xFF51BDEB.toInt()),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        tvResult.text = spannable

        tvInfo.text =
            "정보 보호를 위해 아이디의 일부만 보입니다.\n" +
                    "가려지지 않은 전체 아이디는 $maskedEmail 으로 발송해드린\n" +
                    "인증번호 입력 시 확인할 수 있습니다."

        // 🔙 뒤로가기 (이메일 입력 화면으로)
        btnBack.setOnClickListener { finish() }

        // ➡ 추가 인증하러 가기 (인증번호 입력 화면)
        btnGoVerify.setOnClickListener {
            val intent = Intent(this, VerifyFindIdCodeActivity::class.java).apply {
                putExtra("email", email)
                putExtra("maskedEmail", maskedEmail)
            }
            startActivity(intent)
        }

        // 🔐 로그인 하러 가기
        btnGoLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
