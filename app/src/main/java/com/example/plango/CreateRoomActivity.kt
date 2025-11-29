package com.example.plango

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.plango.adapter.CalendarAdapter_rm
import com.example.plango.model.CalendarDay_rm
import com.example.plango.data.FriendRepository
import com.example.plango.model.Friend
import java.time.LocalDate

class CreateRoomActivity : AppCompatActivity() {

    // 헤더 뷰들
    private lateinit var btnBack: LinearLayout

    private lateinit var tvSubtitle: TextView

    private lateinit var tvStep1Circle: TextView
    private lateinit var tvStep1Label: TextView
    private lateinit var tvStep2Circle: TextView
    private lateinit var tvStep2Label: TextView
    private lateinit var tvStep3Circle: TextView
    private lateinit var tvStep3Label: TextView
    // ⭐ [여행방 생성에 필요한 상태들 저장용 필드]
    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
    var selectedFriendNicknames: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        // 🔹 친구 더미 데이터 세팅 (한 번만)
        if (FriendRepository.getFriends().isEmpty()) {
            FriendRepository.setFriends(
                listOf(
                    Friend("음주헌터", "송현재", null, false),
                    Friend("디자인광", "남유정", null, true),
                    Friend("팬티헌터", "신진성", null, true),
                    Friend("로또누나", "곽주희", null, false),
                    Friend("개폐급쓰레기","강석환",null,false),
                    Friend("디자인싫어","헌재송",null,true)
                )
            )
        }

        initHeaderViews()
        setStep(1)   // 처음 진입은 1단계

        btnBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_create_room_container, CreateRoomStep1Fragment())
                .commit()
        }
    }

    private fun initHeaderViews() {
        // btn_back 는 LinearLayout(아이콘 + "뒤로" 텍스트 감싸는 레이아웃) 기준
        btnBack = findViewById(R.id.btn_back)
        tvSubtitle = findViewById(R.id.tv_subtitle_create)

        tvStep1Circle = findViewById(R.id.tv_step1_circle)
        tvStep1Label = findViewById(R.id.tv_step1_label)
        tvStep2Circle = findViewById(R.id.tv_step2_circle)
        tvStep2Label = findViewById(R.id.tv_step2_label)
        tvStep3Circle = findViewById(R.id.tv_step3_circle)
        tvStep3Label = findViewById(R.id.tv_step3_label)
    }

    fun setStep(step: Int) {
        when (step) {
            1 -> {
                tvSubtitle.text = "여행 날짜를 선택해주세요"

                applyStepSelected(tvStep1Circle, tvStep1Label)
                applyStepUnselected(tvStep2Circle, tvStep2Label)
                applyStepUnselected(tvStep3Circle, tvStep3Label)

                tvStep1Circle.text = "1"
                tvStep2Circle.text = "2"
                tvStep3Circle.text = "3"
            }

            2 -> {
                tvSubtitle.text = "함께 여행할 친구를 선택해주세요"

                applyStepCompleted(tvStep1Circle, tvStep1Label)
                applyStepSelected(tvStep2Circle, tvStep2Label)
                applyStepUnselected(tvStep3Circle, tvStep3Label)

                tvStep2Circle.text = "2"
                tvStep3Circle.text = "3"
            }

            3 -> {
                tvSubtitle.text = "여행방 이름과 메모를 작성해주세요"

                applyStepCompleted(tvStep1Circle, tvStep1Label)
                applyStepCompleted(tvStep2Circle, tvStep2Label)
                applyStepSelected(tvStep3Circle, tvStep3Label)

                tvStep3Circle.text = "3"
            }
        }
    }

    private fun applyStepSelected(circle: TextView, label: TextView) {
        circle.setBackgroundResource(R.drawable.bg_step_circle_selected)
        circle.setTextColor(Color.WHITE)
        label.setTextColor(Color.parseColor("#007AFF"))
    }

    private fun applyStepUnselected(circle: TextView, label: TextView) {
        circle.setBackgroundResource(R.drawable.bg_step_circle_unselected)
        circle.setTextColor(Color.parseColor("#999999"))
        label.setTextColor(Color.parseColor("#999999"))
    }

    private fun applyStepCompleted(circle: TextView, label: TextView) {
        circle.setBackgroundResource(R.drawable.bg_step_circle_selected)
        circle.text = "✓"
        circle.setTextColor(Color.WHITE)
        label.setTextColor(Color.parseColor("#007AFF"))
    }
}
