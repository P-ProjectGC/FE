package com.example.plango

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.plango.data.FriendRepository
import com.example.plango.data.FriendRequestRepository
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 알람 아이콘 콜백
    private var alarmClickListener: (() -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        // 🔥 Splash 적용 — super.onCreate() 전에 실행
        val splashScreen = installSplashScreen()

        // 스플래쉬 유지시간 설정
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
        }, 800)  // 0.8초 유지

        // fade-out 애니메이션
        splashScreen.setOnExitAnimationListener { splashView ->
            splashView.view.animate()
                .alpha(0f)
                .setDuration(300L)
                .withEndAction { splashView.remove() }
                .start()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔔 Android 13(API 33) 이상 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        // 🔔 알림 채널 생성 (여기서 한 번만 호출해두면 됨)
        NotificationHelper.createChatNotificationChannel(this)
        NotificationHelper.createFriendRequestNotificationChannel(this)

        // 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // bottom 제거
            insets
        }


        initBottomNavigation()
        initAlarmIcon()
        initProfileButton()

        // 처음에는 알람 아이콘 숨기기
        showAlarmIcon(false)

        // 초기 화면 = Home
        binding.bottomNav.selectedItemId = R.id.menu_home

        // 상단 프로필 아이콘 초기 로딩
        loadProfileIcon()
    }

    // 메인으로 다시 돌아올 때(프로필 화면 뒤로가기 등) 최신 프로필 이미지 & 친구요청 알림 체크
    override fun onResume() {
        super.onResume()
        loadProfileIcon()
        checkNewFriendRequestsAndNotify()   // 앱이 메인으로 돌아올 때마다 친구 요청 체크
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBottomNavigation() {

        // 초기 화면 = HomeFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.menu_home -> {
                    showProfileButton(true)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_friends -> {
                    showProfileButton(true)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, FriendFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_rooms -> {
                    showProfileButton(true)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, RoomFragment())
                        .commitAllowingStateLoss()
                    true
                }

                else -> false
            }
        }
    }

    private fun initAlarmIcon() {
        val layoutAlarm = findViewById<FrameLayout>(R.id.layout_alarm)
        val ivAlarm = findViewById<ImageView>(R.id.iv_alarm)

        val listener = View.OnClickListener {
            alarmClickListener?.invoke()
        }

        layoutAlarm.setOnClickListener(listener)
        ivAlarm.setOnClickListener(listener)
    }

    fun setOnAlarmClickListener(listener: () -> Unit) {
        alarmClickListener = listener
    }

    fun showAlarmIcon(show: Boolean) {
        val layout = findViewById<FrameLayout>(R.id.layout_alarm)
        layout.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun updateAlarmBadge(count: Int) {
        val badge = findViewById<TextView>(R.id.tv_alarm_badge)

        if (count > 0) {
            badge.text = count.toString()
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.GONE
        }
    }

    fun showProfileButton(show: Boolean) {
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)
        ivProfile.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun initProfileButton() {
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)

        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)

            // 프로필 화면 들어갈 때는 버튼/알림/헤더 숨김
            showProfileButton(false)
            showAlarmIcon(false)
            showMainHeader(false)
        }
    }

    // 헤더 숨기기/보이기
    fun showMainHeader(show: Boolean) {
        val header =
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.layout_header)
        header.visibility = if (show) View.VISIBLE else View.GONE
    }

    // 🔹 상단 프로필 버튼에 현재 세션 프로필 이미지 적용
    private fun loadProfileIcon() {
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)
        val path = MemberSession.profileImageUrl

        if (path.isNullOrBlank()) {
            ivProfile.setImageResource(R.drawable.icon_profile)
            return
        }

        // 최종 이미지 URL(S3 or 서버)
        val imageUrl = if (path.startsWith("http")) {
            path
        } else {
            RetrofitClient.IMAGE_BASE_URL + path
        }

        android.util.Log.d("MAIN_PROFILE_ICON", "finalUrl=$imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .placeholder(R.drawable.icon_profile)
            .error(R.drawable.icon_profile)
            .into(ivProfile)
    }

    // 🔔 새 친구 요청 발생 여부 확인 + 알림 + 뱃지 갱신
    private fun checkNewFriendRequestsAndNotify() {
        // 로그인 안 되어 있으면 아무 것도 안 함
        if (!MemberSession.isLoggedIn) return

        lifecycleScope.launch {
            try {
                // 🔹 이전에 캐시에 있던 친구 요청 목록
                val oldList = FriendRequestRepository.getRequests()

                // 🔹 서버에서 최신 "받은 친구 요청 목록" 가져오기
                val result =
                    FriendRepository.fetchReceivedFriendRequests(MemberSession.currentMemberId)

                result.onSuccess { newList ->
                    // FriendRepository 안에서 FriendRequestRepository.setRequests(newList)는 이미 호출된 상태라고 가정

                    // 🔹 헤더 알림 뱃지 숫자 갱신
                    updateAlarmBadge(newList.size)

                    // 🔹 "새로 추가된 요청"만 골라내기
                    val newlyAdded = newList.filter { newItem ->
                        oldList.none { it.requestId == newItem.requestId }
                    }

                    // 새 요청이 없다면 알림도 안 띄움
                    if (newlyAdded.isEmpty()) return@onSuccess

                    // 🔔 새로 들어온 각 요청에 대해 알림 생성
                    for (item in newlyAdded) {
                        // 알림 눌렀을 때 열릴 화면: MainActivity
                        val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }

                        val pendingIntent = PendingIntent.getActivity(
                            this@MainActivity,
                            item.requestId.toInt(),   // 각 요청별로 다른 requestCode 사용
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        PendingIntent.FLAG_IMMUTABLE
                                    else 0
                        )

                        NotificationHelper.showFriendRequestNotification(
                            context = this@MainActivity,
                            notificationId = item.requestId.toInt(),
                            title = "새 친구 요청",
                            message = "${item.senderNickname}님이 친구 요청을 보냈어요.",
                            pendingIntent = pendingIntent
                        )
                    }
                }.onFailure {
                    // 조회 실패 시에는 조용히 패스
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 🔹 외부(Fragment)에서 호출할 수 있는 프로필 아이콘 새로고침 함수
    fun refreshProfileIcon() {
        loadProfileIcon()
    }
}
