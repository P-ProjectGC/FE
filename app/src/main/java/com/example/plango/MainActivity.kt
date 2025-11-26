package com.example.plango

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.plango.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시스템 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initBottomNavigation()

        // 처음에는 알림 아이콘 숨겨두기 (홈 화면 기준)
        showAlarmIcon(false)

        // 초기 nav = Home
        binding.bottomNav.selectedItemId = R.id.menu_home
    }

    // 하단 네비게이션 탭 클릭 시 프래그먼트 전환
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

    //  알림 아이콘 보이기/숨기기 제어
    fun showAlarmIcon(show: Boolean) {
        val alarm = findViewById<ImageView>(R.id.iv_alarm)
        alarm.visibility = if (show) View.VISIBLE else View.GONE
    }
}
