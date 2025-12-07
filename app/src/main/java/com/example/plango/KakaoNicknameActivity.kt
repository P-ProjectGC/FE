package com.example.plango

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.data.signup_api.SignupRepository
import com.example.plango.data.signup_api.SignupViewModel
import com.example.plango.data.signup_api.SignupViewModelFactory
import com.example.plango.databinding.ActivityKakaoNicknameBinding
import com.example.plango.model.ProfileUpdateRequest
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class KakaoNicknameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKakaoNicknameBinding

    private val viewModel: SignupViewModel by viewModels {
        SignupViewModelFactory(SignupRepository(RetrofitClient.signupApiService))
    }

    private var isNicknameValid = false
    private var profileImageUrl: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKakaoNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        profileImageUrl = intent.getStringExtra("profileImageUrl")

        setupTextWatcher()
        setupButtonListeners()
        observeNicknameCheck()
        setupBackButton()
    }

    // -----------------------------
    // 🔙 뒤로가기 버튼 처리
    // -----------------------------
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {

            // 카카오 SDK 로그아웃
            UserApiClient.instance.logout { error ->

                // 로그인 화면 복귀
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

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

        binding.btnNicknameCheck.setOnClickListener {
            val nickname = binding.signUpNicknameEt.text.toString().trim()

            if (nickname.isBlank()) {
                Toast.makeText(this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkNickname(nickname)
        }

        binding.btnSignup.setOnClickListener {
            if (!isNicknameValid) {
                Toast.makeText(this, "닉네임 중복 확인을 해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nickname = binding.signUpNicknameEt.text.toString().trim()
            saveKakaoNickname(nickname)
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

    // -----------------------------
    // 🔥 닉네임 저장 (로딩 포함)
    // -----------------------------
    private fun saveKakaoNickname(newNickname: String) {

        val memberId = intent.getIntExtra("memberId", -1).toLong()

        if (memberId == -1L) {
            Toast.makeText(this, "memberId가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdateRequest(
            nickname = newNickname,
            profileImageUrl = profileImageUrl
        )

        // 🔥 로딩 시작
        binding.loadingLayout.visibility = View.VISIBLE
        binding.btnSignup.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApiService.updateProfile(memberId, request)

                if (response.isSuccessful && response.body()?.code == 0) {

                    MemberSession.currentMemberId = memberId
                    MemberSession.nickname = newNickname
                    MemberSession.email = email
                    MemberSession.profileImageUrl = profileImageUrl

                    Toast.makeText(this@KakaoNicknameActivity, "닉네임 설정 완료!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@KakaoNicknameActivity, MainActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(
                        this@KakaoNicknameActivity,
                        "닉네임 저장 실패: ${response.body()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@KakaoNicknameActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            } finally {
                // 🔥 로딩 종료
                binding.loadingLayout.visibility = View.GONE
                binding.btnSignup.isEnabled = true
            }
        }
    }
}