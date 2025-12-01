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
        tv.background = null   // 배경 초기화

        // 2. 🔵 여행 기간 배경(#B2DEF2) 적용
        if (item.isCurrentMonth) {
            when (item.roomRangeType) {
                RoomRangeType.SINGLE -> {
                    tv.setBackgroundResource(R.drawable.bg_room_single)
                }
                RoomRangeType.START -> {
                    tv.setBackgroundResource(R.drawable.bg_room_start)
                }
                RoomRangeType.MIDDLE -> {
                    tv.setBackgroundResource(R.drawable.bg_room_middle)
                }
                RoomRangeType.END -> {
                    tv.setBackgroundResource(R.drawable.bg_room_end)
                }
                RoomRangeType.NONE -> {
                    // 여행 없는 날은 배경 없음
                }
            }
        }

        // ❌ 여기서부터 있던 "선택 범위(검은 동그라미)" 로직은 전부 제거함
        //    홈화면은 그냥 날짜 탭 → 아래 안내/방 카드만 보여주면 되니까,
        //    캘린더 셀에는 따로 선택 스타일을 주지 않는다.
        //
        // 만약 나중에 "선택된 날짜만 살짝 스타일" 주고 싶으면,
        // 아래처럼 단일 선택만 처리하는 코드를 추가하면 됨:
        //
        // val isSelected = startDate != null && date == startDate
        // if (isSelected && item.isCurrentMonth) {
        //     tv.setTypeface(tv.typeface, Typeface.BOLD)
        //     tv.setTextColor(Color.parseColor("#1A1A1A"))
        // }
    }
}
