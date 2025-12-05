package com.example.plango.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.databinding.ItemRoomListBinding
import com.example.plango.model.TravelRoom

class RoomAdapter(
    private var items: List<TravelRoom>,
    private val usePopupStyle: Boolean = false,
    private val onClick: (TravelRoom) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(
        private val binding: ItemRoomListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TravelRoom) {
            binding.tvRoomTitle.text = item.title
            binding.tvRoomDate.text = item.dateText
            binding.tvRoomMemo.text = item.memo

            // ✅ 멤버 수 계산 로직
            // 1순위: memberNicknames (상세조회로 채워진 경우)
            // 2순위: memberCount (목록 API에서 내려온 숫자)
            // 3순위: 최소 1명
            val displayMemberCount = when {
                item.memberNicknames.isNotEmpty() -> item.memberNicknames.size
                item.memberCount > 0 -> item.memberCount
                else -> 1
            }
            binding.tvMemberCount.text = "${displayMemberCount}명"

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
