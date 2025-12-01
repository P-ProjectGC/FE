package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.databinding.ItemRoomListBinding
import com.example.plango.model.TravelRoom
import android.graphics.Color
import com.example.plango.R
class RoomAdapter(
    private var items: List<TravelRoom>,
    private val usePopupStyle: Boolean = false,
    private val onClick: (TravelRoom) -> Unit
)
    : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(
        private val binding: ItemRoomListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TravelRoom) {
            binding.tvRoomTitle.text = item.title
            binding.tvRoomDate.text = item.dateText
            binding.tvRoomMemo.text = item.memo
            binding.tvMemberCount.text = "${item.memberCount}명"

            if (usePopupStyle) {
                // 팝업 스타일
                val ctx = binding.root.context
                binding.layoutRoomCard.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_room_card_popup)

                binding.tvRoomTitle.setTextColor(Color.WHITE)
                binding.tvRoomDate.setTextColor(Color.WHITE)
                binding.tvRoomMemo.setTextColor(Color.WHITE)
                binding.tvMemberCount.setTextColor(Color.WHITE)

                ImageViewCompat.setImageTintList(
                    binding.ivDateIcon,
                    ContextCompat.getColorStateList(ctx, android.R.color.white)
                )
                ImageViewCompat.setImageTintList(
                    binding.ivMemoIcon,
                    ContextCompat.getColorStateList(ctx, android.R.color.white)
                )
                ImageViewCompat.setImageTintList(
                    binding.ivMemberIcon,
                    ContextCompat.getColorStateList(ctx, android.R.color.white)
                )

            } else {
                // 기본 리스트 스타일
                val ctx = binding.root.context
                binding.layoutRoomCard.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_room_card_normal)

                binding.tvRoomTitle.setTextColor(Color.BLACK)
                binding.tvRoomDate.setTextColor(Color.parseColor("#50555C"))
                binding.tvRoomMemo.setTextColor(Color.parseColor("#50555C"))
                binding.tvMemberCount.setTextColor(Color.parseColor("#50555C"))

                ImageViewCompat.setImageTintList(
                    binding.ivDateIcon,
                    ContextCompat.getColorStateList(ctx, R.color.gray_icon)
                )
                ImageViewCompat.setImageTintList(
                    binding.ivMemoIcon,
                    ContextCompat.getColorStateList(ctx, R.color.gray_icon)
                )
                ImageViewCompat.setImageTintList(
                    binding.ivMemberIcon,
                    ContextCompat.getColorStateList(ctx, R.color.gray_icon)
                )
            }

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    // 🔹 이 위치가 맞음: 어댑터 클래스의 멤버 함수
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
