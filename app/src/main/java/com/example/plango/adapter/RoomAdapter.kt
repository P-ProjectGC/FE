package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.databinding.ItemRoomListBinding
import com.example.plango.model.TravelRoom

class RoomAdapter(
    private var items: List<TravelRoom>,
    private val onClick: (TravelRoom) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(
        private val binding: ItemRoomListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TravelRoom) {
            binding.tvRoomTitle.text = item.title
            binding.tvRoomDate.text = item.dateText
            binding.tvRoomMemo.text = item.memo
            binding.tvMemberCount.text = "${item.memberCount}명"

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRoomListBinding.inflate(inflater, parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<TravelRoom>) {
        items = newItems
        notifyDataSetChanged()
    }
}
