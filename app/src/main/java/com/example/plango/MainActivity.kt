package com.example.plango

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.plango.databinding.ActivityMainBinding
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


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

        // 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initBottomNavigation()
        initAlarmIcon()

        // 처음에는 알람 아이콘 숨기기
        showAlarmIcon(false)

        // 초기 화면 = Home
        binding.bottomNav.selectedItemId = R.id.menu_home
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
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_friends -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, FriendFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_rooms -> {
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
}
