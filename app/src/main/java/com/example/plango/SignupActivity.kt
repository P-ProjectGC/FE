package com.example.plango

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plango.data.RetrofitClient
import com.example.plango.data.signup_api.SignupRepository
import com.example.plango.data.signup_api.SignupViewModel
import com.example.plango.data.signup_api.SignupViewModelFactory
import com.example.plango.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private lateinit var signupViewModel: SignupViewModel

    // 정규식 상수들
    private val ID_REGEX = "^[a-z0-9]{4,16}$".toRegex()
    private val EMAIL_REGEX =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    private val PW_REGEX =
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*]{6,20}$".toRegex()

    // 중복확인 여부 플래그
    private var isNicknameChecked = false
    private var isIdChecked = false
    private var isEmailChecked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signupViewModel = SignupViewModelFactory(
            SignupRepository(RetrofitClient.signupApiService)
        ).create(SignupViewModel::class.java)

        signupViewModel.signupResult.observe(this) { result ->
            Log.d("SIGNUP_DEBUG", "response = $result")
            if (result != null && result.code == 0) {
                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }

        signupViewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                binding.signUpLoading.visibility = View.VISIBLE
                binding.signUpBtn.isEnabled = false
                binding.signUpBtn.alpha = 0.5f
            } else {
                binding.signUpLoading.visibility = View.GONE
                binding.signUpBtn.isEnabled = true
                binding.signUpBtn.alpha = 1f
            }
        }


        setupTextWatchers()
        setupPasswordToggle()
        setupClickListeners()
        observeDuplicateChecks()
    }

    // ---------------------------
    // TextWatcher
    // ---------------------------
    private fun setupTextWatchers() = with(binding) {

        signUpNameEt.addTextChangedListener(watcher { validateName() })
        signUpNicknameEt.addTextChangedListener(watcher { validateNickname() })
        signUpIdEt.addTextChangedListener(watcher { validateId() })
        signUpEmailEt.addTextChangedListener(watcher { validateEmail() })
        signUpPwEt.addTextChangedListener(watcher {
            validatePassword()
            validatePasswordCheck()
        })
        signUpPwCheckEt.addTextChangedListener(watcher { validatePasswordCheck() })

        signUpAgreeCb.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }
    }

    // ---------------------------
    // 버튼 클릭
    // ---------------------------
    private fun setupClickListeners() = with(binding) {

        // 뒤로가기 → 로그인 화면 이동
        btnBack.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }

        // 회원가입 버튼
        signUpBtn.setOnClickListener {

            if (!isNicknameChecked) {
                showError(signUpNicknameErrorTv, "✗ 닉네임 중복확인을 해주세요.")
                return@setOnClickListener
            }

            if (!isIdChecked) {
                showError(signUpIdErrorTv, "✗ 아이디 중복확인을 해주세요.")
                return@setOnClickListener
            }

            if (!isEmailChecked) {
                showError(signUpEmailErrorTv, "✗ 이메일 중복확인을 해주세요.")
                return@setOnClickListener
            }

            val name = signUpNameEt.text.toString()
            val nickname = signUpNicknameEt.text.toString()
            val loginId = signUpIdEt.text.toString()
            val password = signUpPwEt.text.toString()
            val email = signUpEmailEt.text.toString()

            // 🚀 회원가입 API 호출
            signupViewModel.signup(name, nickname, loginId, password, email)
        }

        // 닉네임 중복확인
        binding.btnNicknameCheck.setOnClickListener {
            val nickname = binding.signUpNicknameEt.text.toString().trim()

            if (!validateNickname()) return@setOnClickListener

            signupViewModel.checkNickname(nickname)
        }

        // 아이디 중복확인
        binding.btnIdCheck.setOnClickListener {
            val id = binding.signUpIdEt.text.toString().trim()

            if (!validateId()) return@setOnClickListener

            signupViewModel.checkLoginId(id)
        }

        // 이메일 중복확인
        binding.btnEmailCheck.setOnClickListener {
            val email = binding.signUpEmailEt.text.toString().trim()

            if (!validateEmail()) return@setOnClickListener

            signupViewModel.checkEmail(email)
        }

        // 약관 전체보기
        signUpTermsDetailTv.setOnClickListener {
            showTermsDialog()
            // TODO: 약관 전체보기 추가
        }

        // 카카오 가입
        btnKakao.setOnClickListener {
            Toast.makeText(this@SignUpActivity, "카카오 회원가입 기능 준비 중입니다.", Toast.LENGTH_SHORT).show()
            // TODO: API 연동
        }
    }

    // ---------------------------
    // 중복확인 API 결과 감지
    // ---------------------------
    private fun observeDuplicateChecks() {

        // ✔ 아이디 중복확인
        signupViewModel.loginIdCheckState.observe(this) { result ->
            result.onSuccess { available ->
                if (available) {
                    isIdChecked = true
                    showSuccess(binding.signUpIdErrorTv, "✓ 사용 가능한 아이디입니다.")
                } else {
                    isIdChecked = false
                    showError(binding.signUpIdErrorTv, "✗ 이미 사용 중인 아이디입니다.")
                }
                updateButtonState()
            }
            result.onFailure {
                showError(binding.signUpIdErrorTv, "✗ 아이디 확인 실패")
            }
        }

        // ✔ 이메일 중복확인
        signupViewModel.emailCheckState.observe(this) { result ->
            result.onSuccess { available ->
                if (available) {
                    isEmailChecked = true
                    showSuccess(binding.signUpEmailErrorTv, "✓ 사용 가능한 이메일입니다.")
                } else {
                    isEmailChecked = false
                    showError(binding.signUpEmailErrorTv, "✗ 이미 사용 중인 이메일입니다.")
                }
                updateButtonState()
            }
            result.onFailure {
                showError(binding.signUpEmailErrorTv, "✗ 이메일 확인 실패")
            }
        }

        // ✔ 닉네임 중복확인
        signupViewModel.nicknameCheckState.observe(this) { result ->
            result.onSuccess { available ->
                if (available) {
                    isNicknameChecked = true
                    showSuccess(binding.signUpNicknameErrorTv, "✓ 사용 가능한 닉네임입니다.")
                } else {
                    isNicknameChecked = false
                    showError(binding.signUpNicknameErrorTv, "✗ 이미 사용 중인 닉네임입니다.")
                }
                updateButtonState()
            }
            result.onFailure {
                showError(binding.signUpNicknameErrorTv, "✗ 닉네임 확인 실패")
            }
        }
    }

    // ---------------------------
    // 유효성 검사 함수들
    // ---------------------------
    private fun validateName(): Boolean = with(binding) {
        val name = signUpNameEt.text.toString().trim()

        return if (name.length < 2) {
            showError(signUpNameErrorTv, "✗ 이름은 2글자 이상 입력해주세요.")
            false
        } else {
            hideError(signUpNameErrorTv)
            true
        }.also { updateButtonState() }
    }

    private fun validateNickname(): Boolean = with(binding) {
        val nickname = signUpNicknameEt.text.toString().trim()

        return if (nickname.length !in 2..10) {
            showError(signUpNicknameErrorTv, "✗ 닉네임은 2~10자로 입력해주세요.")
            false
        } else {
            hideError(signUpNicknameErrorTv)
            true
        }.also { updateButtonState() }
    }

    private fun validateId(): Boolean = with(binding) {
        val id = signUpIdEt.text.toString().trim()

        return if (!ID_REGEX.matches(id)) {
            showError(signUpIdErrorTv, "✗ 아이디는 영문 소문자+숫자 4~16자입니다.")
            false
        } else {
            hideError(signUpIdErrorTv)
            true
        }.also { updateButtonState() }
    }

    private fun validateEmail(): Boolean = with(binding) {
        val email = signUpEmailEt.text.toString().trim()

        return if (!EMAIL_REGEX.matches(email)) {
            showError(signUpEmailErrorTv, "✗ 이메일 형식이 올바르지 않습니다.")
            false
        } else {
            hideError(signUpEmailErrorTv)
            true
        }.also { updateButtonState() }
    }

    private fun validatePassword(): Boolean = with(binding) {
        val pw = signUpPwEt.text.toString()

        return if (!PW_REGEX.matches(pw)) {
            showError(signUpPwErrorTv, "✗ 영문+숫자 포함 6~20자로 입력해주세요.")
            false
        } else {
            hideError(signUpPwErrorTv)
            true
        }.also { updateButtonState() }
    }

    private fun validatePasswordCheck(): Boolean = with(binding) {
        val pw = signUpPwEt.text.toString()
        val pwCheck = signUpPwCheckEt.text.toString()

        return if (pwCheck.isEmpty()) {
            showError(signUpPwCheckErrorTv, "✗ 비밀번호 확인을 입력해주세요.")
            false
        } else if (pw != pwCheck) {
            showError(signUpPwCheckErrorTv, "✗ 비밀번호가 일치하지 않습니다.")
            false
        } else {
            hideError(signUpPwCheckErrorTv)
            true
        }.also { updateButtonState() }
    }

    private fun validateNameOnly() =
        binding.signUpNameEt.text.toString().trim().length >= 2

    private fun validateNicknameOnly() =
        binding.signUpNicknameEt.text.toString().trim().length in 2..10

    private fun validateIdOnly() =
        ID_REGEX.matches(binding.signUpIdEt.text.toString().trim())

    private fun validateEmailOnly() =
        EMAIL_REGEX.matches(binding.signUpEmailEt.text.toString().trim())

    private fun validatePasswordOnly() =
        PW_REGEX.matches(binding.signUpPwEt.text.toString())

    private fun validatePasswordCheckOnly(): Boolean {
        val pw = binding.signUpPwEt.text.toString()
        val pwCheck = binding.signUpPwCheckEt.text.toString()
        return pw.isNotEmpty() && pw == pwCheck
    }

    // ---------------------------
    // 버튼 활성화 로직
    // ---------------------------
    private fun updateButtonState() = with(binding) {

        val allValid =
            validateNameOnly() &&
                    validateNicknameOnly() &&
                    validateIdOnly() &&
                    validateEmailOnly() &&
                    validatePasswordOnly() &&
                    validatePasswordCheckOnly() &&
                    signUpAgreeCb.isChecked

        signUpBtn.isEnabled = allValid
        signUpBtn.alpha = if (allValid) 1f else 0.5f
    }

    // ---------------------------
    // 비밀번호 보기/숨기기
    // ---------------------------
    private fun setupPasswordToggle() = with(binding) {

        btnPwToggle.setOnClickListener {
            togglePasswordVisibility(signUpPwEt, btnPwToggle)
        }

        btnPwCheckToggle.setOnClickListener {
            togglePasswordVisibility(signUpPwCheckEt, btnPwCheckToggle)
        }
    }

    private fun togglePasswordVisibility(field: android.widget.EditText, button: android.widget.ImageView) {
        val isVisible = field.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        if (isVisible) {
            field.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ic_eye_off)
        } else {
            field.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.ic_eye_on)
        }

        field.setSelection(field.text.length)
    }

    // ---------------------------
    // UI 메시지(오류/성공)
    // ---------------------------
    private fun showError(target: View, msg: String) {
        if (target is android.widget.TextView) {
            target.text = msg
            target.setTextColor(getColor(R.color.error_red))
            target.visibility = View.VISIBLE
        }
    }

    private fun showSuccess(target: View, msg: String) {
        if (target is android.widget.TextView) {
            target.text = msg
            target.setTextColor(getColor(R.color.success_green))
            target.visibility = View.VISIBLE
        }
    }

    private fun hideError(target: View) {
        target.visibility = View.GONE
    }

    // ---------------------------
    // 텍스트 변경 감지
    // ---------------------------
    private fun watcher(onChanged: () -> Unit) = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChanged()
        }
    }

    // ---------------------------
    // 이용약관
    // ---------------------------
    private fun showTermsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.terms_dialog, null)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 닫기 버튼
        val closeBtn = dialogView.findViewById<Button>(R.id.btn_terms_close)
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}