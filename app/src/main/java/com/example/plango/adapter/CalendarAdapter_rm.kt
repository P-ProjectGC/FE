package com.example.plango.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.CalendarDay_rm
import java.time.LocalDate

class CalendarAdapter_rm(
    private val onDayClick: (CalendarDay_rm) -> Unit
) : RecyclerView.Adapter<CalendarAdapter_rm.DayViewHolder>() {

    private var days: List<CalendarDay_rm> = emptyList()

    // 선택 범위 (출발/도착)
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    fun submitList(list: List<CalendarDay_rm>) {
        days = list
        notifyDataSetChanged()
    }

    /** 프래그먼트에서 선택 범위를 넘겨줄 때 사용 */
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

        // 기본 숫자
        tv.text = date.dayOfMonth.toString()

        // 이번 달 / 이전/다음 달 색
        if (item.isCurrentMonth) {
            tv.alpha = 1f
            tv.setTextColor(Color.parseColor("#333333"))
        } else {
            tv.alpha = 0.3f
            tv.setTextColor(Color.parseColor("#999999"))
        }
        tv.background = null

        // 선택 범위 처리
        val isStart = startDate != null && date == startDate
        val isEnd = endDate != null && date == endDate
        val isInRange =
            startDate != null && endDate != null &&
                    (date.isAfter(startDate) && date.isBefore(endDate))

        when {
            isStart || isEnd -> {
                // 출발/도착 날짜 (동그란 진한 배경)
                tv.setBackgroundResource(R.drawable.bg_calendar_selected_day)
                tv.setTextColor(Color.WHITE)
            }

            isInRange && item.isCurrentMonth -> {
                // 출발~도착 사이 날짜들 (텍스트 색만 포인트 컬러)
                tv.setTextColor(Color.parseColor("#4F46E5"))
            }
        }
    }
}
