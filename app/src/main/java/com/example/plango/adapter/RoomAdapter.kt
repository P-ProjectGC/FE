package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.TravelRoom

class RoomAdapter(
    private var roomList: List<TravelRoom>,
    private val onItemClick: (TravelRoom) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRoomTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvRoomDate)
        private val tvMemo: TextView = itemView.findViewById(R.id.tvRoomMemo)
        private val tvMemberCount: TextView = itemView.findViewById(R.id.tvMemberCount)

        fun bind(room: TravelRoom) {
            tvTitle.text = room.title
            tvDate.text = room.dateText
            tvMemo.text = room.memo
            tvMemberCount.text = "${room.memberCount}명"

            itemView.setOnClickListener {
                onItemClick(room)   // 나중에 방 상세로 이동할 때 여기서 처리
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_list, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int = roomList.size

    // 나중에 서버에서 새 리스트 받아오면 이 함수로 교체해서 사용하면 됨
    fun submitList(newList: List<TravelRoom>) {
        roomList = newList
        notifyDataSetChanged()
    }
}
