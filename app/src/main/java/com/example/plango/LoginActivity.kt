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
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient


class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val keyHash = Utility.getKeyHash(this)
        Log.d("KAKAO_KEY_HASH", "keyHash = $keyHash")

        tokenManager = TokenManager(this)

        // TODO: 프로필에서 로그아웃 기능 구현 후 자동 로그인 활성화
       // val savedToken = tokenManager.getAccessToken()
        //Log.d("TOKEN_TEST", "자동 로그인 체크 - 저장된 토큰 = $savedToken")
        //if (!savedToken.isNullOrEmpty()) {
          // startActivity(Intent(this, MainActivity::class.java))
           // finish()
            //return
       //}

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

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {

            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (token != null) {
                    sendKakaoTokenToServer(token)
                } else {
                    loginWithKakaoAccount()
                }
            }

        } else {
            loginWithKakaoAccount()
        }
    }

    // ------------------------------
    //  카카오 계정 로그인
    // ------------------------------
    private fun loginWithKakaoAccount() {

        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->

            if (error != null) {
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                return@loginWithKakaoAccount
            }

            if (token != null) {
                sendKakaoTokenToServer(token)
            }
        }
    }


    // ------------------------------
    //  카카오 토큰을 BE로 전달하는 핵심 함수
    // ------------------------------
    private fun sendKakaoTokenToServer(token: OAuthToken) {

        val access = token.accessToken
        val id = token.idToken ?: ""

        Log.d("KAKAO_LOGIN", "accessToken: $access")
        Log.d("KAKAO_LOGIN", "idToken: $id")

        authViewModel.loginKakao(access, id)
    }

    // ------------------------------
    //  카카오 로그인 결과 처리
    // ------------------------------
    // TODO: 닉네임 저장 API 연동 후에는 지우고 아래 코드 써야함
    private fun observeKakaoLogin() {
        authViewModel.kakaoLoginState.observe(this) { result ->
            result.onSuccess { data ->

                // 토큰 저장
                tokenManager.saveAccessToken(data.accessToken)
                tokenManager.saveRefreshToken(data.refreshToken)

                if (data.newMember) {
                    // 신규 회원 → 닉네임 설정으로 이동
                    val intent = Intent(this, KakaoNicknameActivity::class.java)
                    intent.putExtra("email", data.email)
                    intent.putExtra("profileImageUrl", data.profileImageUrl)
                    startActivity(intent)
                    finish()
                } else {
                    // 기존 회원 → 바로 메인
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

            result.onFailure {
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun observeKakaoLogin() {
//        authViewModel.kakaoLoginResult.observe(this) { result ->
//            result.onSuccess { data ->
//
//                tokenManager.saveAccessToken(data.accessToken)
//                tokenManager.saveRefreshToken(data.refreshToken)
//
//                if (data.newMember) {
//                    // 닉네임 설정 화면으로 이동
//                    val intent = Intent(this, KakaoNicknameActivity::class.java)
//                    intent.putExtra("email", data.email)
//                    intent.putExtra("profileImageUrl", data.profileImageUrl)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // 기존 회원 → 바로 메인 이동
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
//            }
//        }
//    }


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
            startKakaoLogin()
        }

        // 회원가입
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 아이디/비밀번호 찾기
        binding.tvFindId.setOnClickListener {
            Toast.makeText(this, "아이디 찾기 화면으로 이동", Toast.LENGTH_SHORT).show()
        }
        binding.tvFindPw.setOnClickListener {
            Toast.makeText(this, "비밀번호 찾기 화면으로 이동", Toast.LENGTH_SHORT).show()
        }
    }
}
