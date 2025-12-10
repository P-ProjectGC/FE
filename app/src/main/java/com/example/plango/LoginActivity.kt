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
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.data.login_api.AuthRepository
import com.example.plango.data.login_api.AuthViewModel
import com.example.plango.data.login_api.AuthViewModelFactory
import com.example.plango.data.token.TokenManager
import com.example.plango.databinding.ActivityLoginBinding
import com.example.plango.ui.findid.FindIdActivity
import com.example.plango.ui.findpw.FindPasswordActivity
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding

    // 🔹 앱 실행 속도 측정용 변수
    private var startMs = 0L

    // RetrofitClient에서 AuthService 가져오기
    private val authService = RetrofitClient.authService
    private val authRepository = AuthRepository(authService)

    // Hilt를 안 쓰는 경우 → Factory 필요
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(authRepository)
    }

    private lateinit var tokenManager: TokenManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        // 🔥 SplashScreen 은 super.onCreate() 전에 호출
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 🔹 앱 시작 시간 기록 (실제 런치 타임 측정 시작 지점)
        startMs = System.currentTimeMillis()
        Log.d("PERF", "APP_LAUNCH_START=$startMs")

        authViewModel.clearState()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val keyHash = Utility.getKeyHash(this)
        Log.d("KAKAO_KEY_HASH", "keyHash = $keyHash")

        tokenManager = TokenManager(this)

        // TODO: 프로필에서 로그아웃 기능 구현 후 자동 로그인 활성화
//        val savedToken = tokenManager.getAccessToken()
//        Log.d("TOKEN_TEST", "자동 로그인 체크 - 저장된 토큰 = $savedToken")
//        if (!savedToken.isNullOrEmpty()) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//            return
//        }

        authViewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loginLoading.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                binding.btnLogin.alpha = 0.5f
            } else {
                binding.loginLoading.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.btnLogin.alpha = 1f
            }
        }

        setupTextWatchers()       // 입력 감지 → 로그인 버튼 활성화
        setupButtonListeners()    // 버튼 클릭 이벤트 설정
        observeLogin()            // 일반 로그인 결과 관찰
        observeKakaoLogin()       // 카카오 로그인 결과 관찰
    }

    // 🔹 첫 화면이 완전히 그려졌을 때 실행 시간 측정
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val end = System.currentTimeMillis()
            Log.d("PERF", "APP_LAUNCH_DURATION=${end - startMs}ms")
        }
    }

    // ------------------------------
    //  EditText 변경 → 로그인 버튼 활성화
    // ------------------------------
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateLoginButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etId.addTextChangedListener(watcher)
        binding.etPw.addTextChangedListener(watcher)
    }

    private fun updateLoginButtonState() {
        val enabled = binding.etId.text.isNotEmpty() && binding.etPw.text.isNotEmpty()
        binding.btnLogin.isEnabled = enabled
        binding.btnLogin.alpha = if (enabled) 1f else 0.5f
    }

    // ------------------------------
    //  일반 로그인 결과 처리
    // ------------------------------
    private fun observeLogin() {
        authViewModel.normalLoginResult.observe(this) { result ->

            binding.tvError.visibility = View.GONE

            if (result == null) return@observe

            result.onSuccess { data ->

                // 1) 서버에서 받은 토큰 저장 (Interceptor에서 사용)
                tokenManager.saveAccessToken(data.accessToken)
                tokenManager.saveRefreshToken(data.refreshToken)

                // 2) 로그인 성공 시 세션 저장 (앱 내부에서 사용자 정보 사용)
                MemberSession.currentMemberId = data.memberId.toLong()
                MemberSession.email = data.email
                MemberSession.nickname = data.nickname
                MemberSession.profileImageUrl = data.profileImageUrl
                MemberSession.accessToken = data.accessToken
                MemberSession.refreshToken = data.refreshToken

                // 디버그 로그
                Log.d("TOKEN_TEST", "access = ${tokenManager.getAccessToken()}")
                Log.d("TOKEN_TEST", "refresh = ${tokenManager.getRefreshToken()}")
                Log.d(
                    "LOGIN_INFO",
                    "memberId=${MemberSession.currentMemberId}, nickname=${MemberSession.nickname}"
                )

                // 메인 화면 이동
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            result.onFailure {
                binding.tvError.text = "로그인 실패: 아이디 또는 비밀번호를 확인하세요."
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }

    // ------------------------------
    //  카카오 로그인 진입 함수
    // ------------------------------
    private fun startKakaoLogin() {
        Log.d("KAKAO_FLOW", "1️⃣ startKakaoLogin() 호출됨")
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e("KAKAO", "로그인 실패: $error")
            } else if (token != null) {

                val accessToken = token.accessToken
                val idToken = token.idToken   // ⭐ 서버 요구값

                Log.d("KAKAO_FLOW", "1️⃣ SDK 로그인 성공 → access=$accessToken | idToken=$idToken")

                // 서버로 전달
                authViewModel.loginKakao(accessToken, idToken)
            }
        }
    }

    // ------------------------------
    //  카카오 로그인 결과 처리
    // ------------------------------
    // TODO: 닉네임 저장 API 연동 후에는 지우고 아래 코드 써야함
    private fun observeKakaoLogin() {
        authViewModel.kakaoLoginState.observe(this) { result ->

            if (result == null) return@observe

            result.onSuccess { data ->

                binding.loginLoading.visibility = View.VISIBLE  // 🔥 로딩 시작

                Log.d("KAKAO_FLOW", "3️⃣ observeKakaoLogin 성공 → newMember=${data.newMember}")

                // 1) 토큰 저장
                tokenManager.saveAccessToken(data.accessToken)
                tokenManager.saveRefreshToken(data.refreshToken)

                // 2) 회원 타입 분기
                if (data.newMember || data.nickname.isNullOrBlank()) {

                    // 신규 회원 또는 닉네임 없는 회원 → 닉네임 설정 화면 이동
                    // 🔥 0.4초 로딩 후 화면 이동 (사용자 경험 ↑)
                    binding.loginLoading.postDelayed({

                        val intent = Intent(this, KakaoNicknameActivity::class.java)
                        intent.putExtra("memberId", data.memberId)
                        intent.putExtra("email", data.email)
                        intent.putExtra("profileImageUrl", data.profileImageUrl)

                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                    }, 400)

                } else {
                    // ⭐ 기존 회원도 FE 세션 저장해야 함 (여기가 핵심!!!)
                    MemberSession.currentMemberId = data.memberId.toLong()
                    MemberSession.email = data.email
                    MemberSession.nickname = data.nickname
                    MemberSession.profileImageUrl = data.profileImageUrl
                    MemberSession.accessToken = data.accessToken
                    MemberSession.refreshToken = data.refreshToken

                    // 기존 회원 → 메인 화면
                    binding.loginLoading.postDelayed({
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }, 400)
                }
            }

            result.onFailure {
                binding.loginLoading.visibility = View.GONE
                Log.e("KAKAO_FLOW", "3️⃣ observeKakaoLogin 실패", it)
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------------------
    //  버튼 클릭 이벤트
    // ------------------------------
    private fun setupButtonListeners() {

        // 일반 로그인
        binding.btnLogin.setOnClickListener {
            authViewModel.loginNormal(
                binding.etId.text.toString(),
                binding.etPw.text.toString()
            )
        }

        // 카카오 로그인 시작
        binding.btnKakao.setOnClickListener {
            Log.d("KAKAO_FLOW", "0️⃣ 카카오 버튼 클릭됨")
            startKakaoLogin()
        }

        // 회원가입
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 아이디/비밀번호 찾기
        binding.tvFindId.setOnClickListener {
            startActivity(Intent(this, FindIdActivity::class.java))
        }

        binding.tvFindPw.setOnClickListener {
            startActivity(Intent(this, FindPasswordActivity::class.java))
        }
    }
}
