package com.example.plango

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.plango.adapter.CalendarAdapter
import com.example.plango.databinding.FragmentHomeBinding
import com.example.plango.model.Calendar
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding

    private var isSnapped = false   // 스냅 중복 방지용

    private lateinit var adapter: CalendarAdapter
    private var selectedIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onResume() {
        super.onResume()
        // 친구 화면일 때만 알림 아이콘 보이게
        (activity as? MainActivity)?.showAlarmIcon(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupScrollBehavior()
    }

    private fun setupScrollBehavior() {
        val scroll = binding.homeScroll

        val title = binding.tvTitle
        val hint = binding.tvScrollHint
        val calendar = binding.calendarCard
        val search = binding.layoutSearch

        val maxTransition = 250f   // 자연스럽게 애니메이션 줄 길이

        scroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            val ratio = (scrollY / maxTransition).coerceIn(0f, 1f)

            // ⭐ 1) 인삿말 자연스러운 사라짐
            title.alpha = 1f - ratio
            title.translationY = -80f * ratio

            // ⭐ 2) 스크롤 힌트 자연스럽게 사라짐
            hint.alpha = 1f - ratio
            hint.translationY = -120f * ratio

            // ⭐ 3) 캘린더가 천천히 올라옴 (핵심!!)
            calendar.translationY = -140f * ratio

            val snapPoint = binding.layoutSearch.bottom   // 검색창 바로 아래

            if (!isSnapped && scrollY > maxTransition + 80) {  // 조금 더 내려야 발동되게 조정
                scroll.post {
                    scroll.smoothScrollTo(0, snapPoint)
                    isSnapped = true
                }
            }


            if (scrollY < 10) {
                isSnapped = false
            }
        }
    }

    private fun setupCalendar() {

        val year = 2024
        val month = 11

        val lastDay = LocalDate.of(year, month, 1).lengthOfMonth()

        val list = ArrayList<com.example.plango.model.Calendar>()

        for (i in 1..lastDay) {
            list.add(
                com.example.plango.model.Calendar(
                    day = i,
                    date = LocalDate.of(year, month, i),
                    hasSchedule = (i in listOf(28, 29, 30))
                )
            )
        }

        adapter = CalendarAdapter(
            requireContext(),
            list
        ) { selectedDay ->
            selectedIndex = selectedDay.day
        }

        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendar.adapter = adapter
    }
}