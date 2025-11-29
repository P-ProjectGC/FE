package com.example.plango

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.adapter.CalendarAdapter_rm
import com.example.plango.model.CalendarDay_rm
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.graphics.Color
class CreateRoomStep1Fragment : Fragment(R.layout.fragment_create_room_step1) {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvMonthTitle: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var btnNext: Button

    // 날짜 정보 박스
    private lateinit var layoutDateInfo: LinearLayout
    private lateinit var tvDepartureDate: TextView
    private lateinit var tvArrivalDate: TextView

    private lateinit var calendarAdapter: CalendarAdapter_rm
    private var currentYearMonth: YearMonth = YearMonth.now()

    // 출발/도착
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? CreateRoomActivity)?.setStep(1)

        initViews(view)
        setupCalendar()
        setupButtons()
    }

    private fun initViews(view: View) {
        rvCalendar = view.findViewById(R.id.rv_calendar_rm)
        tvMonthTitle = view.findViewById(R.id.tv_month_title)
        btnPrevMonth = view.findViewById(R.id.btn_prev_month)
        btnNextMonth = view.findViewById(R.id.btn_next_month)
        btnNext = view.findViewById(R.id.btn_next_step1)

        layoutDateInfo = view.findViewById(R.id.layout_date_info)
        tvDepartureDate = view.findViewById(R.id.tv_departure_date)
        tvArrivalDate = view.findViewById(R.id.tv_arrival_date)
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter_rm { day ->
            // 이전/다음 달 날짜는 선택 안 되게 막고 싶으면 여기서 필터링
            if (!day.isCurrentMonth) return@CalendarAdapter_rm

            handleDateClick(day.date)
        }

        rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }

        refreshCalendar()

        btnPrevMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            refreshCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            refreshCalendar()
        }
    }

    private fun refreshCalendar() {
        tvMonthTitle.text = formatYearMonth(currentYearMonth)
        val days = generateCalendarDays(currentYearMonth)
        calendarAdapter.submitList(days)
        // 월이 바뀌어도 선택 범위는 유지
        calendarAdapter.setRange(startDate, endDate)
    }

    private fun setupButtons() {
        updateNextButtonState()

        btnNext.setOnClickListener {
            // TODO: 나중에 2단계로 넘어갈 때 startDate / endDate 넘겨주기
            // (activity as? CreateRoomActivity)?.goToStep2(startDate, endDate)
        }
    }

    /**
     * 날짜 하나 탭했을 때 출발/도착 선택 로직
     */
    private fun handleDateClick(date: LocalDate) {
        when {
            // 아직 아무것도 안 찍었거나, 둘 다 이미 선택된 상태면 → 새로 시작 (출발만)
            startDate == null || (startDate != null && endDate != null) -> {
                startDate = date
                endDate = null
            }

            // 출발만 선택된 상태에서 다시 찍기
            startDate != null && endDate == null -> {
                when {
                    date.isBefore(startDate) -> {
                        // 더 이른 날짜를 찍으면 → 그게 새로운 출발
                        startDate = date
                    }
                    date.isEqual(startDate) -> {
                        // 같은 날 다시 찍으면 그냥 유지 (원하면 리셋 로직으로 바꿀 수 있음)
                        endDate = null
                    }
                    else -> {
                        // 출발 이후 날짜 찍으면 → 도착
                        endDate = date
                    }
                }
            }
        }

        // 화면 반영
        updateDateInfoBox()
        calendarAdapter.setRange(startDate, endDate)
        updateNextButtonState()
    }

    private fun updateDateInfoBox() {
        if (startDate == null && endDate == null) {
            layoutDateInfo.visibility = View.GONE
            return
        }

        layoutDateInfo.visibility = View.VISIBLE
        tvDepartureDate.text = startDate?.format(dateFormatter) ?: "-"
        tvArrivalDate.text = endDate?.format(dateFormatter) ?: "-"
    }

    private fun updateNextButtonState() {
        // 기간 선택이 끝난 경우에만 다음 버튼 활성
        val enabled = startDate != null && endDate != null
        btnNext.isEnabled = enabled

        if (enabled) {
            btnNext.setBackgroundResource(R.drawable.bg_btn_next_enabled)
            btnNext.backgroundTintList = null
            btnNext.setTextColor(Color.WHITE)
        } else {
            btnNext.setBackgroundResource(R.drawable.bg_btn_next_disabled)
            btnNext.backgroundTintList = null
            btnNext.setTextColor(Color.parseColor("#B3FFFFFF"))
        }
    }

    private fun formatYearMonth(yearMonth: YearMonth): String {
        val monthText = yearMonth.month.toString()
            .lowercase()
            .replaceFirstChar { it.uppercase() }
        return "$monthText ${yearMonth.year}"
    }

    // 6x7 = 42칸 캘린더 생성
    private fun generateCalendarDays(yearMonth: YearMonth): List<CalendarDay_rm> {
        val firstOfMonth = yearMonth.atDay(1)
        val firstDayOfWeekIndex = firstOfMonth.dayOfWeek.value % 7  // 일요일 0 기준

        val startDate = firstOfMonth.minusDays(firstDayOfWeekIndex.toLong())

        val days = mutableListOf<CalendarDay_rm>()
        for (i in 0 until 42) {
            val date = startDate.plusDays(i.toLong())
            val isCurrentMonth = (date.month == yearMonth.month)
            days.add(CalendarDay_rm(date = date, isCurrentMonth = isCurrentMonth))
        }
        return days
    }
}
