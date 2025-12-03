package com.example.plango

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.plango.data.RetrofitClient
import com.example.plango.data.login_api.AuthRepository
import com.example.plango.data.login_api.AuthService
import com.example.plango.data.login_api.AuthViewModel
import com.example.plango.data.login_api.AuthViewModelFactory
import com.example.plango.data.token.TokenManager
import com.example.plango.databinding.ActivityLoginBinding

class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val authService = RetrofitClient.authService
    private val authRepository = AuthRepository(authService)

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(authRepository)
    }

    private lateinit var tokenManager: TokenManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        // 🔥 반드시 super.onCreate() 전에 실행해야 Splash가 뜨고 유지됨!
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // TODO: 프로필에서 로그아웃 기능 구현하고 활성화하기
//        // 🔥 자동 로그인
//        val savedToken = tokenManager.getAccessToken()
//        // TODO: 토큰 테스트 코드
//        Log.d("TOKEN_TEST", "자동 로그인 체크 - 저장된 토큰 = $savedToken")
//        if (!savedToken.isNullOrEmpty()) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//            return
//        }

        setupTextWatchers()
        setupButtonListeners()
        observeLogin()
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

    // 로그인 결과 관찰
    private fun observeLogin() {

        authViewModel.loginResult.observe(this) { result ->

            binding.tvError.visibility = View.GONE

            result.onSuccess { data ->

                tokenManager.saveAccessToken(data.accessToken)
//                tokenManager.saveRefreshToken(data.refreshToken)

                Log.d("TOKEN_TEST", "access = ${tokenManager.getAccessToken()}")
                Log.d("TOKEN_TEST", "refresh = ${tokenManager.getRefreshToken()}")

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            result.onFailure {
                // 로그인 실패
                binding.tvError.text = "로그인 실패: 아이디 또는 비밀번호를 확인하세요."
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }


    /** ---------------------------
     *  버튼 클릭 리스너
     * -------------------------- */
    private fun setupButtonListeners()  {

        /** LOGIN 버튼 */
        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPw.text.toString()

            // 일반 로그인 실행
            authViewModel.loginNormal(id, pw)
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