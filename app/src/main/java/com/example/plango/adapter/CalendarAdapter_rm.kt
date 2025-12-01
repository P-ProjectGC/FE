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

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    fun submitList(list: List<CalendarDay_rm>) {
        days = list
        notifyDataSetChanged()
    }

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

        tv.text = date.dayOfMonth.toString()

        if (item.isCurrentMonth) {
            tv.alpha = 1f
            tv.setTextColor(Color.parseColor("#333333"))
        } else {
            tv.alpha = 0.3f
            tv.setTextColor(Color.parseColor("#999999"))
        }
    }
}