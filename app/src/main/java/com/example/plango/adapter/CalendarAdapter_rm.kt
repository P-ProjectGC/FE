package com.example.plango.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.CalendarDay_rm
import com.example.plango.model.RoomRangeType
import java.time.LocalDate

class CalendarAdapter_rm(
    private val onDayClick: (CalendarDay_rm) -> Unit
) : RecyclerView.Adapter<CalendarAdapter_rm.DayViewHolder>() {

    private var days: List<CalendarDay_rm> = emptyList()

    // 👉 홈화면에서는 사실 안 쓰지만, 다른 화면 재사용 가능성을 생각해서 남겨둠
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    fun submitList(list: List<CalendarDay_rm>) {
        days = list
        notifyDataSetChanged()
    }

    /** 프래그먼트에서 선택 범위를 넘겨줄 때 사용 (홈에서는 사실상 단일 선택용) */
    fun setRange(start: LocalDate?, end: LocalDate?) {
        startDate = start
        endDate = end
        notifyDataSetChanged()
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tv_day_rm)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val day = days[pos]
                    onDayClick(day)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day_rm, parent, false)
        return DayViewHolder(view)
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = days[position]
        val tv = holder.tvDay
        val date = item.date
        val container = holder.itemView as ViewGroup  // FrameLayout 전체

        // 0. 날짜 숫자
        tv.text = date.dayOfMonth.toString()

        // 1. 기본 색 / 투명도 초기화
        if (item.isCurrentMonth) {
            tv.alpha = 1f
            tv.setTextColor(Color.parseColor("#333333"))
        } else {
            tv.alpha = 0.3f
            tv.setTextColor(Color.parseColor("#999999"))
        }

        // 🔹 배경/foreground 초기화
        tv.background = null
        container.background = null
        container.foreground = null   // ⭐ 이거 중요! 재활용 방지

        // 2. 🔵 여행 기간 배경 (#B2DEF2) → TextView 쪽만
        if (item.isCurrentMonth) {
            when (item.roomRangeType) {
                RoomRangeType.SINGLE -> tv.setBackgroundResource(R.drawable.bg_room_single)
                RoomRangeType.START  -> tv.setBackgroundResource(R.drawable.bg_room_start)
                RoomRangeType.MIDDLE -> tv.setBackgroundResource(R.drawable.bg_room_middle)
                RoomRangeType.END    -> tv.setBackgroundResource(R.drawable.bg_room_end)
                RoomRangeType.NONE   -> { /* 배경 없음 */ }
            }
        }

        // 3. 🔽 오늘 날짜 회색 테두리 처리 (foreground)

        val today = LocalDate.now()

        if (item.isCurrentMonth && date == today) {
            // 파란 배경이 있든 없든, 셀 전체 위에 테두리를 얹음
            container.foreground =
                androidx.core.content.ContextCompat.getDrawable(
                    container.context,
                    R.drawable.bg_today_light_gray   // ← 위에 만든 xml 이름
                )
            tv.setTextColor(Color.parseColor("#111111"))
        }
    }


}

