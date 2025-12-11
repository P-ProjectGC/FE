package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.databinding.ItemNoticeBinding
import com.example.plango.model.Notice

class NoticeAdapter :
    ListAdapter<Notice, NoticeAdapter.NoticeViewHolder>(diffCallback) {

    // ⭐ 반드시 있어야 함 — NoticeListActivity에서 onItemClick 사용하기 위해
    var onItemClick: ((Notice) -> Unit)? = null

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Notice>() {
            override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean =
                oldItem == newItem
        }
    }

    inner class NoticeViewHolder(
        private val binding: ItemNoticeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notice) = with(binding) {
            tvTitle.text = item.title
            tvContent.text = item.content
            tvDate.text = formatDate(item.createdAt)

            // 타입 텍스트 매핑
            val typeLabel = when (item.type) {
                "UPDATE" -> "업데이트"
                "EMERGENCY" -> "긴급"
                "ERROR" -> "오류"
                else -> item.type
            }
            tvType.text = typeLabel

            // 타입별 배경색 적용
            val bgRes = when (item.type) {
                "UPDATE" -> R.drawable.bg_notice_type_update
                "EMERGENCY" -> R.drawable.bg_notice_type_urgent
                "ERROR" -> R.drawable.bg_notice_type_error
                else -> R.drawable.bg_notice_type_update
            }
            tvType.setBackgroundResource(bgRes)

            // ⭐ 카드 클릭 → Activity로 전달
            root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }

        private fun formatDate(iso: String?): String {
            if (iso == null) return ""
            return try {
                iso.substring(0, 10).replace("-", ".")
            } catch (e: Exception) {
                iso
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}