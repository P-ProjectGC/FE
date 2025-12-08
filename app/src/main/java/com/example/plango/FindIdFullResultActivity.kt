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

class FindIdFullResultActivity : AppCompatActivity() {

    private lateinit var tvFullResult: TextView
    private lateinit var tvInfo: TextView
    private lateinit var btnGoLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id_full_result)

        tvFullResult = findViewById(R.id.tv_full_id_result)
        tvInfo = findViewById(R.id.tv_full_id_info)
        btnGoLogin = findViewById(R.id.btn_full_go_login)

        val loginId = intent.getStringExtra("loginId") ?: ""

        if (loginId.isEmpty()) {
            Toast.makeText(this, "아이디 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setResultText(loginId)

        // 로그인 하기 → 스택 비우고 로그인 화면으로
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

    private fun setResultText(loginId: String) {
        // 이름이 아직 없으니까 일단 "회원님의 아이디는 ~ 입니다."
        val fullText = "회원님의 아이디는\n$loginId 입니다."
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf(loginId)
        val end = start + loginId.length
        if (start >= 0) {
            // 아이디 부분만 파란색 + 볼드
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
        tvFullResult.text = spannable
    }
}
