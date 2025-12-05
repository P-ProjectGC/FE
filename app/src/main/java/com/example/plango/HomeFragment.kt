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
import android.content.Intent
import com.example.plango.data.TravelRoomRepository
import com.example.plango.model.RoomRangeType
import com.example.plango.model.TravelRoom
import java.time.temporal.ChronoUnit
import androidx.lifecycle.lifecycleScope
import com.example.plango.data.AppNotificationHelper
import com.example.plango.data.MemberSession
import com.example.plango.data.RetrofitClient
import com.example.plango.model.NotificationSettings
import kotlinx.coroutines.launch

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


    private val displayDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy년 M월 d일")

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.apply {
            showMainHeader(true)
            showAlarmIcon(false)
            showProfileButton(true)
        }

        // 🔹 홈에 돌아올 때마다 방 목록 & 리마인드 체크
        viewLifecycleOwner.lifecycleScope.launch {
            // 1) 여행방 목록 서버에서 한번 가져오기 (캘린더 색칠 문제도 동시에 해결됨)
            try {
                val success = TravelRoomRepository.fetchRoomsFromServer(keyword = null)
                if (success) {
                    refreshCalendar()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2) 알림 설정 조회해서 tripReminderEnabled 가 true 인지 확인
            try {
                val response = RetrofitClient.memberApiService.getNotificationSettings()
                if (response.isSuccessful) {
                    val body = response.body()
                    val settings: NotificationSettings? = body?.data

                    if (body?.code == 0 && settings != null) {
                        MemberSession.applyNotificationSettings(settings)
                        if (settings.tripReminderEnabled) {
                            // 3) 리마인드 알림 체크
                            checkTomorrowTripsAndNotify()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



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
        // 🔹 홈 화면 "새로운 여행 만들기" 버튼 → 방 생성 플로우 진입
        binding.btnCreateTrip.setOnClickListener {
            val intent = Intent(requireContext(), CreateRoomActivity::class.java)
            startActivity(intent)
        }
        // 🔹 혹시 모를 상황 대비: 로그인 된 상태에서만 호출
        if (MemberSession.isLoggedIn) {
            loadRoomsAndRefreshCalendar()
        }
    }

    private fun setupUi() {
        // 🔹 세션의 닉네임 사용 (없으면 기본 문구)
        val nickname = MemberSession.nickname ?: "여행자"
        binding.tvTitle.text = "“$nickname”의\nPlanGo"

        // 🔹 검색창 클릭시 팝업 띄우기
        val searchClick: (View) -> Unit = {
            RoomSearchDialogFragment().show(parentFragmentManager, "RoomSearchDialog")
        }
        binding.layoutSearch.setOnClickListener(searchClick)   // 첫 화면 검색바
        binding.layoutSearch2.setOnClickListener(searchClick)  // 캘린더 화면 검색바

        // 🔹 “아래로 스크롤하세요” 안내 → 캘린더로 스크롤
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
            // ✅ 여기만 바뀜 (핵심!)
            onDateSelected(day.date)
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

        val rangeMap = buildRoomRangeMap()   // 🔵 여행 기간 정보 계산
        val days = generateCalendarDays(ym, rangeMap)

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
        if (selectedDate == null) {
            binding.layoutSelectedRoom.visibility = View.GONE
            binding.tvSelectedDate.visibility = View.VISIBLE
            binding.tvSelectedDate.text = "날짜를 선택해 보세요!"
        }
    }



    private fun generateCalendarDays(
        yearMonth: YearMonth,
        rangeMap: Map<LocalDate, RoomRangeType>
    ): List<CalendarDay_rm> {
        val firstOfMonth = yearMonth.atDay(1)
        val firstDayOfWeekIndex = firstOfMonth.dayOfWeek.value % 7  // 일요일 0 기준
        val startDate = firstOfMonth.minusDays(firstDayOfWeekIndex.toLong())

        val days = mutableListOf<CalendarDay_rm>()
        for (i in 0 until 42) {
            val date = startDate.plusDays(i.toLong())
            val isCurrentMonth = (date.month == yearMonth.month)

            val type = rangeMap[date] ?: RoomRangeType.NONE

            days.add(
                CalendarDay_rm(
                    date = date,
                    isCurrentMonth = isCurrentMonth,
                    roomRangeType = type
                )
            )
        }
        return days
    }


    /** 🔹 날짜 하나 탭했을 때 */
    private fun onDateSelected(date: LocalDate) {
        selectedDate = date

        val allRooms = TravelRoomRepository.getRooms()

        val matched = allRooms.filter { room ->
            isDateInRoom(date, room)
        }

        val dateText = date.format(displayDateFormatter)

        if (matched.isEmpty()) {
            // 상태 2 — 날짜 선택 O + 방 없음
            binding.layoutSelectedRoom.visibility = View.GONE
            binding.tvSelectedDate.visibility = View.VISIBLE
            binding.tvSelectedDate.text = "선택한 날짜에 일정이 없습니다\n$dateText"

        } else {
            // 상태 3 — 날짜 선택 O + 방 있음
            val room = matched.first()

            binding.layoutSelectedRoom.visibility = View.VISIBLE
            binding.tvSelectedDate.visibility = View.GONE  // ⭐ 추가!

            binding.tvHomeRoomTitle.text = room.title
            binding.tvHomeRoomDate.text = room.dateText
            binding.tvHomeRoomMemo.text = room.memo
            binding.tvHomeRoomMemberCount.text = "${room.memberCount}명"

            // 카드 눌렀을 때 해당 방으로 진입
            binding.layoutSelectedRoom.setOnClickListener {
                val intent = Intent(requireContext(), RoomScheduleTestActivity::class.java).apply {
                    putExtra("ROOM_ID", room.id)
                    putExtra("ROOM_NAME", room.title)
                    putExtra("ROOM_MEMO", room.memo)
                    putExtra("START_DATE", room.startDate)
                    putExtra("END_DATE", room.endDate)
                    putStringArrayListExtra(
                        "MEMBER_NICKNAMES",
                        ArrayList(room.memberNicknames)
                    )
                }
                startActivity(intent)
            }
        }
    }

    //날짜파싱
    private fun parseToLocalDate(text: String): LocalDate? {
        return try {
            when {
                text.contains(".") -> {
                    // 예: "25.10.28" 또는 "2025.10.28"
                    val parts = text.split(".")
                    return when (parts.size) {
                        3 -> {
                            val year = if (parts[0].length == 2) "20${parts[0]}" else parts[0]
                            LocalDate.of(year.toInt(), parts[1].toInt(), parts[2].toInt())
                        }
                        else -> null
                    }
                }

                text.contains("-") -> {
                    // 예: "2025-10-28"
                    LocalDate.parse(text)
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }


    // HomeFragment 안에 추가
    private fun buildRoomRangeMap(): Map<LocalDate, RoomRangeType> {
        val result = mutableMapOf<LocalDate, RoomRangeType>()
        val rooms = TravelRoomRepository.getRooms()

        for (room in rooms) {
            val start = parseToLocalDate(room.startDate) ?: continue
            val end = parseToLocalDate(room.endDate) ?: continue

            // start > end 인 경우 방어
            if (end.isBefore(start)) continue

            val days = ChronoUnit.DAYS.between(start, end) + 1

            if (days == 1L) {
                // 1일짜리 여행
                result[start] = RoomRangeType.SINGLE
            } else {
                var cur = start
                while (!cur.isAfter(end)) {
                    val type = when {
                        cur == start -> RoomRangeType.START
                        cur == end -> RoomRangeType.END
                        else -> RoomRangeType.MIDDLE
                    }

                    // 이미 다른 방이 칠해져 있다면 덮어쓸지 말지는 취향대로
                    result[cur] = type
                    cur = cur.plusDays(1)
                }
            }
        }

        return result
    }






    /** 🔹 date 가 room의 [startDate ~ endDate] 사이인지 체크 */
    private fun isDateInRoom(date: LocalDate, room: TravelRoom): Boolean {
        val start = parseToLocalDate(room.startDate)
        val end = parseToLocalDate(room.endDate)

        if (start == null || end == null) return false

        return !date.isBefore(start) && !date.isAfter(end)
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

        var downY = 0f
        var isDragging = false
        val touchSlop = android.view.ViewConfiguration.get(requireContext()).scaledTouchSlop

        scroll.setOnTouchListener { _, event ->
            if (pageHeight <= 0f) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    isDragging = false
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = kotlin.math.abs(event.y - downY)
                    if (dy > touchSlop) {
                        isDragging = true
                    }
                    false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 드래그가 아니면(탭이면) 스냅 X
                    if (!isDragging || isSnapping) {
                        isDragging = false
                        return@setOnTouchListener false
                    }

                    val currentY = scroll.scrollY
                    val pageTop2 = pageHeight.toInt()
                    val snapBottomLimit = (pageHeight * 1.2f).toInt() // 이거 넘으면 스냅 안 함

                    // 👉 2페이지 안쪽으로 충분히 내려왔으면(버튼 있는 위치) 스냅하지 않음
                    if (currentY > snapBottomLimit) {
                        isDragging = false
                        return@setOnTouchListener false
                    }

                    val targetY = when {
                        // 1페이지~2페이지 사이 구간 → 가까운 페이지로 스냅
                        currentY < pageTop2 -> {
                            val mid = pageHeight / 2f
                            if (currentY < mid) 0 else pageTop2
                        }
                        // 2페이지 상단 근처 → 2페이지 맨 위로 스냅
                        currentY in pageTop2..snapBottomLimit -> {
                            pageTop2
                        }
                        else -> {
                            // 이 케이스는 위 if에서 이미 걸러져서 거의 안 옴
                            currentY
                        }
                    }

                    isSnapping = true
                    scroll.post {
                        scroll.smoothScrollTo(0, targetY)

                        if (targetY == pageTop2 && !isCalendarVisible) {
                            // 두 번째 화면으로 넘어갈 때
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
                            // 첫 화면으로 돌아갈 때
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

                        scroll.postDelayed({
                            isSnapping = false
                            isDragging = false
                        }, 260)
                    }

                    false
                }

                else -> false
            }
        }
    }

    // 🔹 홈에서 방 목록 로딩 + 캘린더 갱신
    private fun loadRoomsAndRefreshCalendar() {
        // 🔹 로그인 안 되어 있으면 아무 것도 안 함 (토큰 없는 상태 방어)
        if (!MemberSession.isLoggedIn) return

        viewLifecycleOwner.lifecycleScope.launch {
            // 이미 방 목록이 있으면 서버는 안 타도 됨
            if (TravelRoomRepository.getRooms().isEmpty()) {
                TravelRoomRepository.fetchRoomsFromServer()
            }
            // 서버에서 rooms 채워졌다고 가정하고 캘린더 갱신
            refreshCalendar()
        }
    }

    /**
     * 🔔 내일 출발하는 여행방이 있으면 로컬 알림 띄우기
     */
    private fun checkTomorrowTripsAndNotify() {
        val rooms = TravelRoomRepository.getRooms()
        if (rooms.isEmpty()) {
            println("👉 [TripReminder] rooms empty, skip")
            return
        }

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        println("👉 [TripReminder] today=$today, tomorrow=$tomorrow")

        val tomorrowRooms = rooms.filter { room ->
            val start = parseToLocalDate(room.startDate).also {
                println("   room=${room.title}, raw='${room.startDate}', parsed=$it")
            }
            start == tomorrow
        }

        println("👉 [TripReminder] tomorrowRooms size=${tomorrowRooms.size}")

        if (tomorrowRooms.isEmpty()) return

        for (room in tomorrowRooms) {
            println("👉 [TripReminder] notify room=${room.title}")
            AppNotificationHelper.showTripReminderIfNeeded(
                requireContext(),
                room,
                today
            )
        }
    }






}
