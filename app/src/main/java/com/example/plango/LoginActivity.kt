package com.example.plango

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.plango.databinding.ActivityLoginBinding

class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextWatchers()
        setupButtonListeners()
    }

    /** ---------------------------
     *  EditText 입력 감지 → 버튼 활성화
     * -------------------------- */
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etId.addTextChangedListener(watcher)
        binding.etPw.addTextChangedListener(watcher)
    }

    /** 로그인 버튼 활성/비활성 제어 */
    private fun updateLoginButtonState() {
        val id = binding.etId.text.toString()
        val pw = binding.etPw.text.toString()

        val enabled = id.isNotEmpty() && pw.isNotEmpty()

        binding.btnLogin.isEnabled = enabled
        binding.btnLogin.alpha = if (enabled) 1f else 0.5f
    }

    /** ---------------------------
     *  버튼 클릭 리스너
     * -------------------------- */
    private fun setupButtonListeners() {

        /** LOGIN 버튼 */
        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPw.text.toString()

            if (id == "test" && pw == "1234") {
                binding.tvError.visibility = android.view.View.GONE
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                binding.tvError.visibility = android.view.View.VISIBLE
            }
        }

        /** 아이디 찾기 */
        binding.tvFindId.setOnClickListener {
            Toast.makeText(this, "아이디 찾기 화면으로 이동", Toast.LENGTH_SHORT).show()
        }

        /** 비밀번호 찾기 */
        binding.tvFindPw.setOnClickListener {
            Toast.makeText(this, "비밀번호 찾기 화면으로 이동", Toast.LENGTH_SHORT).show()
        }

        /** 카카오 로그인 */
        binding.btnKakao.setOnClickListener {
            Toast.makeText(this, "카카오 로그인 기능 준비 중", Toast.LENGTH_SHORT).show()
        }

        /** 회원가입 */
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}