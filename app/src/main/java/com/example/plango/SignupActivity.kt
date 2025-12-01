package com.example.plango

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plango.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    // 정규식 상수들
    private val ID_REGEX = "^[a-z0-9]{4,16}$".toRegex()                  // 아이디: 영문소문자+숫자 4~16자
    private val EMAIL_REGEX =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()   // 이메일 형식
    private val PW_REGEX =
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*]{6,20}$".toRegex() // 비밀번호: 영문+숫자 6~20자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextWatchers()
        setupPasswordToggle()
        setupClickListeners()
    }

    // 각 EditText에 TextWatcher 달아서 실시간 검증
    private fun setupTextWatchers() = with(binding) {

        signUpNameEt.addTextChangedListener(simpleWatcher { validateName() })
        signUpNicknameEt.addTextChangedListener(simpleWatcher { validateNickname() })
        signUpIdEt.addTextChangedListener(simpleWatcher { validateId() })
        signUpEmailEt.addTextChangedListener(simpleWatcher { validateEmail() })
        signUpPwEt.addTextChangedListener(simpleWatcher {
            validatePassword()
            validatePasswordCheck()  // 비번 바뀌면 확인도 다시 검사
        })
        signUpPwCheckEt.addTextChangedListener(simpleWatcher { validatePasswordCheck() })

        signUpAgreeCb.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }
    }

    // 버튼/하단 텍스트 클릭 처리
    private fun setupClickListeners() = with(binding) {

        // 회원가입 버튼
        signUpBtn.setOnClickListener {
            Toast.makeText(this@SignUpActivity, "회원가입 완료!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 닉네임 중복확인
        btnNicknameCheck.setOnClickListener {
            val nickname = signUpNicknameEt.text.toString()
            if (nickname.length in 2..10) {
                Toast.makeText(this@SignUpActivity, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SignUpActivity, "닉네임 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 아이디 중복확인
        btnIdCheck.setOnClickListener {
            val id = signUpIdEt.text.toString()
            if (ID_REGEX.matches(id)) {
                Toast.makeText(this@SignUpActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SignUpActivity, "아이디 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 이메일 중복확인
        btnEmailCheck.setOnClickListener {
            val email = signUpEmailEt.text.toString()
            if (EMAIL_REGEX.matches(email)) {
                Toast.makeText(this@SignUpActivity, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SignUpActivity, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 카카오 회원가입 (디자인 버튼)
        btnKakao.setOnClickListener {
            Toast.makeText(this@SignUpActivity, "카카오 회원가입 기능 준비 중", Toast.LENGTH_SHORT).show()
        }
    }

    // 이름 검증: 2글자 이상이면 통과 (한글/영어 모두 허용)
    private fun validateName(): Boolean = with(binding) {
        val name = signUpNameEt.text.toString().trim()

        return if (name.length < 2) {
            showError(signUpNameErrorTv, "이름은 2글자 이상 입력해주세요.")
            false
        } else {
            hideError(signUpNameErrorTv)
            true
        }.also { updateButtonState() }
    }

    // 닉네임 검증: 2~10자 (한글/영문/숫자 정도 허용)
    private fun validateNickname(): Boolean = with(binding) {
        val nickname = signUpNicknameEt.text.toString().trim()

        return if (nickname.length < 2 || nickname.length > 10) {
            showError(signUpNicknameErrorTv, "닉네임은 2~10자로 입력해주세요.")
            false
        } else {
            hideError(signUpNicknameErrorTv)
            true
        }.also { updateButtonState() }
    }

    // 아이디 검증: 영문소문자+숫자 4~16자
    private fun validateId(): Boolean = with(binding) {
        val id = signUpIdEt.text.toString().trim()

        return if (!ID_REGEX.matches(id)) {
            showError(signUpIdErrorTv, "아이디는 영문 소문자+숫자 4~16자로 입력해주세요.")
            false
        } else {
            hideError(signUpIdErrorTv)
            true
        }.also { updateButtonState() }
    }

    // 이메일 형식 검증
    private fun validateEmail(): Boolean = with(binding) {
        val email = signUpEmailEt.text.toString().trim()

        return if (!EMAIL_REGEX.matches(email)) {
            showError(signUpEmailErrorTv, "이메일 형식이 올바르지 않습니다.")
            false
        } else {
            hideError(signUpEmailErrorTv)
            true
        }.also { updateButtonState() }
    }

    // 비밀번호 규칙 검증
    private fun validatePassword(): Boolean = with(binding) {
        val pw = signUpPwEt.text.toString()

        return if (!PW_REGEX.matches(pw)) {
            showError(signUpPwErrorTv, "비밀번호는 영문+숫자 포함 6~20자로 입력해주세요.")
            false
        } else {
            hideError(signUpPwErrorTv)
            true
        }.also { updateButtonState() }
    }

    // 비밀번호 확인 일치 여부
    private fun validatePasswordCheck(): Boolean = with(binding) {
        val pw = signUpPwEt.text.toString()
        val pwCheck = signUpPwCheckEt.text.toString()

        return if (pw.isNotEmpty() && pwCheck.isNotEmpty() && pw != pwCheck) {
            showError(signUpPwCheckErrorTv, "비밀번호가 일치하지 않습니다.")
            false
        } else if (pwCheck.isEmpty()) {
            showError(signUpPwCheckErrorTv, "비밀번호 확인을 입력해주세요.")
            false
        } else {
            hideError(signUpPwCheckErrorTv)
            true
        }.also { updateButtonState() }
    }

    // 모든 값이 유효하고 약관 동의까지 되어 있을 때만 버튼 활성화
    private fun updateButtonState() = with(binding) {

        val allValid = validateNameOnly() &&
                validateNicknameOnly() &&
                validateIdOnly() &&
                validateEmailOnly() &&
                validatePasswordOnly() &&
                validatePasswordCheckOnly() &&
                signUpAgreeCb.isChecked

        // 로그인 버튼과 똑같이
        signUpBtn.isEnabled = allValid
        signUpBtn.alpha = if (allValid) 1f else 0.5f
    }

    // updateButtonState에서 에러 메시지를 다시 띄우지 않도록 "조용한" 버전
    private fun validateNameOnly(): Boolean {
        val name = binding.signUpNameEt.text.toString().trim()
        return name.length >= 2
    }

    private fun validateNicknameOnly(): Boolean {
        val nickname = binding.signUpNicknameEt.text.toString().trim()
        return nickname.length in 2..10
    }

    private fun validateIdOnly(): Boolean {
        val id = binding.signUpIdEt.text.toString().trim()
        return ID_REGEX.matches(id)
    }

    private fun validateEmailOnly(): Boolean {
        val email = binding.signUpEmailEt.text.toString().trim()
        return EMAIL_REGEX.matches(email)
    }

    private fun validatePasswordOnly(): Boolean {
        val pw = binding.signUpPwEt.text.toString()
        return PW_REGEX.matches(pw)
    }

    private fun validatePasswordCheckOnly(): Boolean {
        val pw = binding.signUpPwEt.text.toString()
        val pwCheck = binding.signUpPwCheckEt.text.toString()
        return pw.isNotEmpty() && pw == pwCheck
    }

    private fun setupPasswordToggle() = with(binding) {

        // 비밀번호 보기/숨기기
        btnPwToggle.setOnClickListener {
            val isVisible = signUpPwEt.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            if (isVisible) {
                signUpPwEt.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnPwToggle.setImageResource(R.drawable.ic_eye_off)
            } else {
                signUpPwEt.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnPwToggle.setImageResource(R.drawable.ic_eye_on)
            }

            signUpPwEt.setSelection(signUpPwEt.text.length)
        }

        // 비밀번호 확인 보기/숨기기
        btnPwCheckToggle.setOnClickListener {
            val isVisible = signUpPwCheckEt.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            if (isVisible) {
                signUpPwCheckEt.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnPwCheckToggle.setImageResource(R.drawable.ic_eye_off)
            } else {
                signUpPwCheckEt.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnPwCheckToggle.setImageResource(R.drawable.ic_eye_on)
            }

            signUpPwCheckEt.setSelection(signUpPwCheckEt.text.length)
        }
    }

    // 공통 에러 표시/숨기기
    private fun showError(target: View, msg: String) {
        if (target is android.widget.TextView) {
            target.text = msg
            target.visibility = View.VISIBLE
        }
    }

    private fun hideError(target: View) {
        target.visibility = View.GONE
    }

    // TextWatcher 반복 코드 줄이기용 헬퍼
    private fun simpleWatcher(onChanged: () -> Unit) = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChanged()
        }
    }
}