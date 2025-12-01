package com.example.plango

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.plango.adapter.CalendarAdapter_rm
import com.example.plango.databinding.FragmentHomeBinding
import com.example.plango.model.CalendarDay_rm
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.view.animation.DecelerateInterpolator

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    // 페이지 스냅용
    private var pageHeight = 0f
    private var isSnapping = false

    // 캘린더 관련
    private lateinit var calendarAdapter: CalendarAdapter_rm
    private var currentYearMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
    private var isCalendarVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupCalendar()
        setupPageSnapAndFade()
    }

    private fun setupUi() {
        val nickname = "남유정" // TODO: 로그인 정보에서 받아오기
        binding.tvTitle.text = "“$nickname”의\nPlanGo"

        // 🔹 검색바 공통 클릭 동작
        val searchClick: (View) -> Unit = {
            // TODO: 여행방 검색 화면으로 이동
            // val intent = Intent(requireContext(), SearchRoomActivity::class.java)
            // startActivity(intent)
        }
        binding.layoutSearch.setOnClickListener(searchClick)   // 첫 번째 화면 검색바
        binding.layoutSearch2.setOnClickListener(searchClick)  // 두 번째 화면 검색바

        // 🔹 “아래로 스크롤하세요” / 화살표 누르면 캘린더 화면으로 스크롤
        val scrollToCalendar: (View) -> Unit = {
            if (pageHeight > 0f) {
                binding.homeScroll.smoothScrollTo(0, pageHeight.toInt())
            }
        }
        binding.tvScrollHint.setOnClickListener(scrollToCalendar)
        binding.tvScrollArrow.setOnClickListener(scrollToCalendar)
    }

    /** 🔵 rm 캘린더 사용 */
    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter_rm { day ->
            if (!day.isCurrentMonth) return@CalendarAdapter_rm
            handleDateClick(day.date)
        }

        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }

        refreshCalendar()

        binding.btnPrevMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            refreshCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            refreshCalendar()
        }
    }

    private fun refreshCalendar() {
        val ym = currentYearMonth
        binding.tvMonth.text = "${ym.year}년 ${ym.monthValue}월"

        val days = generateCalendarDays(ym)
        calendarAdapter.submitList(days)
        calendarAdapter.setRange(selectedDate, selectedDate)
        updateSelectedDateText()
    }

    private fun handleDateClick(date: LocalDate) {
        selectedDate = date
        calendarAdapter.setRange(selectedDate, selectedDate)
        updateSelectedDateText()
    }

    private fun updateSelectedDateText() {
        binding.tvSelectedDate.text = selectedDate?.let {
            it.format(dateFormatter) + " 일정 보기"
        } ?: "날짜를 선택해주세요"
    }

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

    /** 🔵 1페이지 ↔ 2페이지 스냅 + 캘린더 사르르 페이드인 */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupPageSnapAndFade() {
        val scroll = binding.homeScroll
        val card = binding.calendarCard
        val search2 = binding.layoutSearch2

        // 처음에는 둘 다 안 보이게
        card.alpha = 0f
        search2.alpha = 0f
        search2.visibility = View.INVISIBLE

        scroll.post {
            pageHeight = binding.topPanel.height.toFloat()
        }

        scroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (pageHeight <= 0f) return@setOnScrollChangeListener
            val ratio = (scrollY / pageHeight).coerceIn(0f, 1f)
            binding.tvScrollHint.alpha = 1f - ratio
            binding.tvScrollArrow.alpha = 1f - ratio
        }

        scroll.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP ||
                event.action == MotionEvent.ACTION_CANCEL
            ) {
                if (pageHeight <= 0f || isSnapping) return@setOnTouchListener false

                val currentY = scroll.scrollY
                val mid = pageHeight / 2f
                val targetY = if (currentY < mid) 0 else pageHeight.toInt()

                isSnapping = true
                scroll.post {
                    scroll.smoothScrollTo(0, targetY)

                    if (targetY == pageHeight.toInt() && !isCalendarVisible) {
                        // 🔵 두 번째 화면으로 넘어갈 때 → 검색바2 + 캘린더 사르르 등장
                        card.alpha = 0f
                        card.translationY = 40f
                        search2.alpha = 0f
                        search2.translationY = 20f
                        search2.visibility = View.VISIBLE

                        card.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(350)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        search2.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .setInterpolator(DecelerateInterpolator())
                            .withEndAction { isCalendarVisible = true }
                            .start()

                    } else if (targetY == 0 && isCalendarVisible) {
                        // 🔵 첫 화면으로 돌아갈 때 → 둘 다 사르르 사라짐
                        card.animate()
                            .alpha(0f)
                            .translationY(40f)
                            .setDuration(250)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        search2.animate()
                            .alpha(0f)
                            .translationY(20f)
                            .setDuration(220)
                            .setInterpolator(DecelerateInterpolator())
                            .withEndAction {
                                search2.visibility = View.INVISIBLE
                                isCalendarVisible = false
                            }
                            .start()
                    }

                    scroll.postDelayed({ isSnapping = false }, 260)
                }
            }
            false
        }
    }

}
