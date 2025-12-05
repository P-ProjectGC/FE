package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plango.R
import com.example.plango.data.RetrofitClient
import com.example.plango.model.ChatContentType
import com.example.plango.model.ChatMessage

class ChatAdapter(
    private val items: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TEXT_MINE = 1
        private const val VIEW_TYPE_TEXT_OTHER = 2
        private const val VIEW_TYPE_IMAGE_MINE = 3
        private const val VIEW_TYPE_IMAGE_OTHER = 4
    }

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        return when (msg.type) {
            ChatContentType.TEXT -> {
                if (msg.isMe) VIEW_TYPE_TEXT_MINE else VIEW_TYPE_TEXT_OTHER
            }

            ChatContentType.IMAGE -> {
                if (msg.isMe) VIEW_TYPE_IMAGE_MINE else VIEW_TYPE_IMAGE_OTHER
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT_MINE -> {
                val view = inflater.inflate(R.layout.item_chat_message_mine, parent, false)
                MineTextViewHolder(view)
            }

            VIEW_TYPE_TEXT_OTHER -> {
                val view = inflater.inflate(R.layout.item_chat_message_other, parent, false)
                OtherTextViewHolder(view)
            }

            VIEW_TYPE_IMAGE_MINE -> {
                val view = inflater.inflate(R.layout.item_chat_image_mine, parent, false)
                MineImageViewHolder(view)
            }

            VIEW_TYPE_IMAGE_OTHER -> {
                val view = inflater.inflate(R.layout.item_chat_image_other, parent, false)
                OtherImageViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_chat_message_mine, parent, false)
                MineTextViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        when (holder) {
            is MineTextViewHolder -> holder.bind(message)
            is OtherTextViewHolder -> holder.bind(message)
            is MineImageViewHolder -> holder.bind(message)
            is OtherImageViewHolder -> holder.bind(message)
        }
    }

    fun submitList(newItems: List<ChatMessage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }

    // ===================== ViewHolders =====================

    private class MineTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessageMine)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeMine)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.message ?: ""
            tvTime.text = message.timeText
        }
    }

    private class OtherTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessageOther)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeOther)
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivSenderProfile)

        fun bind(message: ChatMessage) {
            tvSenderName.text = message.senderName
            tvMessage.text = message.message ?: ""
            tvTime.text = message.timeText

            // 🔹 프로필 이미지 로딩 (profileImageUrl 사용!)
            val raw = message.profileImageUrl

            if (raw.isNullOrBlank()) {
                ivProfile.setImageResource(R.drawable.profile_basic)
            } else {
                // 절대/상대 경로 둘 다 처리
                val finalUrl =
                    if (raw.startsWith("http")) {
                        raw
                    } else {
                        val base = RetrofitClient.IMAGE_BASE_URL.trimEnd('/')
                        val cleaned = raw.trimStart('/')
                        "$base/$cleaned"
                    }

                Glide.with(itemView.context)
                    .load(finalUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_basic)
                    .error(R.drawable.profile_basic)
                    .into(ivProfile)
            }
        }
    }

    private class MineImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImageMine)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeImageMine)

        fun bind(message: ChatMessage) {
            val uri = message.imageUri
            if (uri != null) {
                try {
                    ivImage.setImageURI(uri)
                } catch (e: SecurityException) {
                    ivImage.setImageDrawable(null)
                }
            } else {
                ivImage.setImageDrawable(null)
            }
            tvTime.text = message.timeText
        }
    }

    private class OtherImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderNameImage)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImageOther)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeImageOther)
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivSenderProfile)

        fun bind(message: ChatMessage) {
            tvSenderName.text = message.senderName

            // 🔹 이미지 말풍선 안의 사진
            val uri = message.imageUri
            if (uri != null) {
                try {
                    ivImage.setImageURI(uri)
                } catch (e: SecurityException) {
                    ivImage.setImageDrawable(null)
                }
            } else {
                ivImage.setImageDrawable(null)
            }
            tvTime.text = message.timeText

            // 🔹 프로필 이미지 로딩 (profileImageUrl 사용!)
            val raw = message.profileImageUrl

            if (raw.isNullOrBlank()) {
                ivProfile.setImageResource(R.drawable.profile_basic)
            } else {
                val finalUrl =
                    if (raw.startsWith("http")) {
                        raw
                    } else {
                        val base = RetrofitClient.IMAGE_BASE_URL.trimEnd('/')
                        val cleaned = raw.trimStart('/')
                        "$base/$cleaned"
                    }

                Glide.with(itemView.context)
                    .load(finalUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_basic)
                    .error(R.drawable.profile_basic)
                    .into(ivProfile)
            }
        }
    }
}
