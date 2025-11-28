package com.example.plango.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.Calendar

class CalendarAdapter(
    private val context: Context,
    private val dayList: List<Calendar>,
    private val onDayClicked: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = dayList[position]
        val tvDay = holder.tvDay

        // 날짜 숫자 출력
        tvDay.text = day.day.toString()

        // 🔥🔥🔥 여기! 날짜 상태 UI 적용
        when {
            day.isToday -> {
                tvDay.background = ContextCompat.getDrawable(context, R.drawable.bg_day_today)
                tvDay.setTextColor(Color.BLACK)
            }
            day.hasSchedule -> {
                tvDay.background = ContextCompat.getDrawable(context, R.drawable.bg_day_schedule)
                tvDay.setTextColor(Color.WHITE)
            }
            day.isSelected -> {
                tvDay.background = ContextCompat.getDrawable(context, R.drawable.bg_day_selected)
                tvDay.setTextColor(Color.WHITE)
            }
            else -> {
                tvDay.background = ContextCompat.getDrawable(context, R.drawable.bg_day_default)
                tvDay.setTextColor(Color.parseColor("#222222"))
            }
        }

        // 날짜 클릭 이벤트
        holder.itemView.setOnClickListener {
            onDayClicked(day)
        }
    }

    override fun getItemCount(): Int = dayList.size
}
