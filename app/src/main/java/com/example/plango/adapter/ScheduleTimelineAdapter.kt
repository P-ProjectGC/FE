package com.example.plango

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.model.TravelScheduleItem

/**
 * 일정 하나 = RecyclerView 한 줄
 * - 왼쪽: 시작 시간 + 위/아래 연결 라인
 * - 오른쪽: 카드(장소, 시간 범위, 주소)
 * - 중간 빈 시간대는 "줄 자체가 없음" → 자연스럽게 점프
 */
class ScheduleTimelineAdapter(
    private val onItemClick: (TravelScheduleItem) -> Unit,          // 카드 전체 클릭 (지도 포커스)
    private val onItemEditClick: (TravelScheduleItem) -> Unit       // 연필 아이콘 클릭 (편집)
) : RecyclerView.Adapter<ScheduleTimelineAdapter.ScheduleViewHolder>() {

    private val items = mutableListOf<TravelScheduleItem>()

    /** 편집 모드 여부 (기본 false) */
    var isEditMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()   // 모드 바뀌면 연필 아이콘 show/hide 갱신
        }

    /** 외부에서 day별 일정 리스트 넘겨줄 때 호출 */
    fun submitList(list: List<TravelScheduleItem>) {
        items.clear()
        // timeLabel = "HH:mm" 형식 가정. 파싱해서 정렬하면 더 안전.
        items.addAll(list.sortedBy { it.timeLabel })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_timeline, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(items[position], position, items.size, isEditMode, onItemClick, onItemEditClick)
    }

    override fun getItemCount(): Int = items.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 왼쪽 타임라인
        private val textTimeHour: TextView = itemView.findViewById(R.id.textTimeHour)
        private val viewLineTop: View = itemView.findViewById(R.id.viewLineTop)
        private val viewLineBottom: View = itemView.findViewById(R.id.viewLineBottom)
        private val viewDot: View = itemView.findViewById(R.id.viewDot)

        // 카드 내용
        private val textPlaceName: TextView = itemView.findViewById(R.id.textPlaceName)
        private val textTimeRange: TextView = itemView.findViewById(R.id.textTimeRange)
        private val textAddress: TextView = itemView.findViewById(R.id.textAddress)

        // 카드 전체 레이아웃
        private val cardContainer: View = itemView.findViewById(R.id.layoutCardSchedule)

        // 🔵 새로 추가: 카드 안의 연필 아이콘
        private val imageEdit: View = itemView.findViewById(R.id.imageEditSchedule)

        fun bind(
            item: TravelScheduleItem,
            position: Int,
            totalCount: Int,
            isEditMode: Boolean,
            onItemClick: (TravelScheduleItem) -> Unit,
            onItemEditClick: (TravelScheduleItem) -> Unit
        ) {
            // 1) 왼쪽 시간 텍스트: 시작 시간만 표시
            textTimeHour.text = item.timeLabel

            // 2) 위/아래 라인 연결
            viewLineTop.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            viewLineBottom.visibility =
                if (position == totalCount - 1) View.INVISIBLE else View.VISIBLE

            // 3) 카드 채우기
            textPlaceName.text = item.placeName
            textTimeRange.text = item.timeRange   // "14:00 ~ 16:00"
            textAddress.text = item.address

            // 4) 편집 모드에 따라 연필 아이콘 show/hide
            imageEdit.visibility = if (isEditMode) View.VISIBLE else View.GONE

            // 5) 카드 전체 클릭 → 항상 지도 포커스
            cardContainer.setOnClickListener {
                onItemClick(item)
            }

            // 6) 연필 아이콘 클릭 → 편집 동작
            imageEdit.setOnClickListener {
                onItemEditClick(item)
            }
        }
    }
}
