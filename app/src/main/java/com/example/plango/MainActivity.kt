package com.example.plango

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // edge-to-edge 적용 (루트 레이아웃 id: main)
        val root: View = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // BottomNavigationView 설정
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_friends -> {
                    Toast.makeText(this, "친구 목록 탭 클릭", Toast.LENGTH_SHORT).show()
                    // TODO: 친구 목록 화면으로 전환
                    true
                }
                R.id.menu_home -> {
                    Toast.makeText(this, "홈 탭 클릭", Toast.LENGTH_SHORT).show()
                    // TODO: 홈 화면으로 전환
                    true
                }
                R.id.menu_rooms -> {
                    Toast.makeText(this, "방 목록 탭 클릭", Toast.LENGTH_SHORT).show()
                    // TODO: 방 목록 화면으로 전환
                    true
                }
                else -> false
            }
        }

        // 앱 시작 시 기본으로 선택될 탭 (홈)
        bottomNav.selectedItemId = R.id.menu_home
    }
}
