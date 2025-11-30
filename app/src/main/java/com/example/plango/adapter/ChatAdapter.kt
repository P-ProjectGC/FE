package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.ChatMessage

class ChatAdapter(
    private val items: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MINE = 1
        private const val VIEW_TYPE_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isMe) VIEW_TYPE_MINE else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_MINE) {
            val view = inflater.inflate(R.layout.item_chat_message_mine, parent, false)
            MineViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_message_other, parent, false)
            OtherViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        when (holder) {
            is MineViewHolder -> holder.bind(message)
            is OtherViewHolder -> holder.bind(message)
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

    // ===== ViewHolders =====

    private class MineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessageMine)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeMine)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.message
            tvTime.text = message.timeText
        }
    }

    private class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessageOther)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimeOther)

        fun bind(message: ChatMessage) {
            tvSenderName.text = message.senderName
            tvMessage.text = message.message
            tvTime.text = message.timeText
        }
    }
}
