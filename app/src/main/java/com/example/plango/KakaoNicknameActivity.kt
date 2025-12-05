package com.example.plango

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.plango.data.RetrofitClient
import com.example.plango.data.login_api.AuthRepository
import com.example.plango.data.signup_api.SignupRepository
import com.example.plango.data.signup_api.SignupViewModel
import com.example.plango.data.signup_api.SignupViewModelFactory
import com.example.plango.data.token.TokenManager
import com.example.plango.databinding.ActivityKakaoNicknameBinding

class KakaoNicknameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKakaoNicknameBinding

    private val authService = RetrofitClient.authService
    private val authRepository = AuthRepository(authService)
    private val viewModel: SignupViewModel by viewModels {
        SignupViewModelFactory(SignupRepository(RetrofitClient.signupApiService))
    }

    private lateinit var tokenManager: TokenManager

    private var email: String? = null
    private var profileImageUrl: String? = null

    private var isNicknameValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKakaoNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        email = intent.getStringExtra("email")
        profileImageUrl = intent.getStringExtra("profileImageUrl")

        setupTextWatcher()
        setupButtonListeners()
        observeNicknameCheck()

        // ❌ BE API 미완성 → 임시로 주석 처리
        // observeKakaoSignup()
    }

    private fun setupTextWatcher() {
        binding.signUpNicknameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tvNicknameStatus.text = ""
                isNicknameValid = false
                binding.btnSignup.alpha = 0.5f
                binding.btnSignup.isEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupButtonListeners() {

        // 닉네임 중복확인
        binding.btnNicknameCheck.setOnClickListener {
            val nickname = binding.signUpNicknameEt.text.toString().trim()

            if (nickname.isBlank()) {
                Toast.makeText(this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkNickname(nickname)
        }

        // 카카오 회원가입 완료 버튼 (현재 API 없음 → 임시 비활성)
        binding.btnSignup.setOnClickListener {

            if (!isNicknameValid) {
                Toast.makeText(this, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "BE API 준비 중입니다.", Toast.LENGTH_SHORT).show()

            // ❌ BE API 없는 부분 임시 삭제
            /*
            val nickname = binding.signUpNicknameEt.text.toString().trim()

            viewModel.signupKakao(
                nickname = nickname,
                email = email ?: "",
                profileImageUrl = profileImageUrl
            )
            */
        }
    }

    private fun observeNicknameCheck() {
        viewModel.nicknameCheckState.observe(this) { result ->
            result.onSuccess { available ->
                if (available) {
                    binding.tvNicknameStatus.setTextColor(Color.parseColor("#51BDEB"))
                    binding.tvNicknameStatus.text = "사용 가능한 닉네임입니다."
                    isNicknameValid = true

                    binding.btnSignup.alpha = 1f
                    binding.btnSignup.isEnabled = true

                } else {
                    binding.tvNicknameStatus.setTextColor(Color.parseColor("#FF4C4C"))
                    binding.tvNicknameStatus.text = "이미 사용 중인 닉네임입니다."
                    isNicknameValid = false

                    binding.btnSignup.alpha = 0.5f
                    binding.btnSignup.isEnabled = false
                }
            }

            result.onFailure {
                binding.tvNicknameStatus.setTextColor(Color.parseColor("#FF4C4C"))
                binding.tvNicknameStatus.text = "닉네임 확인 실패"
                isNicknameValid = false
            }
        }
    }

    // ❌ 아직 API 없음 → 임시 주석
    /*
    private fun observeKakaoSignup() {
        viewModel.kakaoSignupState.observe(this) { result ->
            result.onSuccess { data ->

                tokenManager.saveAccessToken(data.accessToken)
                tokenManager.saveRefreshToken(data.refreshToken)

                MemberSession.currentMemberId = data.memberId.toLong()
                MemberSession.email = data.email
                MemberSession.nickname = data.nickname
                MemberSession.profileImageUrl = data.profileImageUrl

                Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            result.onFailure {
                Toast.makeText(this, "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    */
}