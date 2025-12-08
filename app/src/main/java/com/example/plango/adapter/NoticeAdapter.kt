package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.databinding.ItemNoticeBinding
import com.example.plango.model.Notice

class NoticeAdapter :
    ListAdapter<Notice, NoticeAdapter.NoticeViewHolder>(diffCallback) {

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

            // 타입: UPDATE / EMERGENCY / ERROR → 한글로만 바꿔줌
            tvType.text = when (item.type) {
                "UPDATE" -> "업데이트"
                "EMERGENCY" -> "긴급"
                "ERROR" -> "오류"
                else -> item.type
            }
        }

        private fun formatDate(iso: String?): String {
            if (iso == null) return ""
            // "2025-12-08T12:34:56" → "2025.12.08"
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
