package com.example.plango.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plango.R
import com.example.plango.model.Friend
import com.google.android.material.card.MaterialCardView

class FriendSelectAdapter_rm(
    private val onFriendClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendSelectAdapter_rm.FriendViewHolder>() {

    private val items = mutableListOf<Friend>()
    private val selectedNicknames = mutableSetOf<String>()

    fun submitList(newItems: List<Friend>, selectedSet: Set<String>) {
        items.clear()
        items.addAll(newItems)

        selectedNicknames.clear()
        selectedNicknames.addAll(selectedSet)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_select_rm, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = items[position]
        val isSelected = selectedNicknames.contains(friend.nickname)
        holder.bind(friend, isSelected)

        // 카드/체크박스 클릭 시 선택 토글
        holder.itemView.setOnClickListener { onFriendClick(friend) }
        holder.cbSelect.setOnClickListener { onFriendClick(friend) }
    }

    override fun getItemCount(): Int = items.size

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardRoot: MaterialCardView = view as MaterialCardView
        val cbSelect: CheckBox = view.findViewById(R.id.cb_friend_select)
        private val tvNickname: TextView = view.findViewById(R.id.tv_nickname)
        private val tvRealName: TextView = view.findViewById(R.id.tv_real_name)
        private val ivProfile: ImageView = view.findViewById(R.id.iv_profile)
        private val ivKakaoBadge: ImageView = view.findViewById(R.id.iv_kakao_badge)

        fun bind(friend: Friend, isSelected: Boolean) {
            // 텍스트
            tvNickname.text = friend.nickname
            tvRealName.text = friend.realName

            // 카카오 뱃지
            ivKakaoBadge.visibility =
                if (friend.isKakaoUser) View.VISIBLE else View.GONE

            // 프로필 이미지
            if (friend.profileImageUrl.isNullOrBlank()) {
                ivProfile.setImageResource(R.drawable.profile_basic)
            } else {
                Glide.with(itemView.context)
                    .load(friend.profileImageUrl)
                    .placeholder(R.drawable.profile_basic)
                    .error(R.drawable.profile_basic)
                    .centerCrop()
                    .into(ivProfile)
            }

            // 체크박스 상태
            cbSelect.isChecked = isSelected

            // ✅ 선택 여부에 따라 카드 배경 색 변경
            if (isSelected) {
                cardRoot.setCardBackgroundColor(Color.parseColor("#EDF8FD"))
            } else {
                cardRoot.setCardBackgroundColor(Color.WHITE)
            }
        }
    }
}
