package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.databinding.ItemFriendSearchBinding
import com.example.plango.model.Friend

class FriendSearchResultAdapter(
    private val onAddClick: (Friend) -> Unit,
    private val isRequested: (Friend) -> Boolean
) : ListAdapter<Friend, FriendSearchResultAdapter.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Friend>() {
            override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
                // 아직 id 필드는 없으니까, 닉네임으로 비교
                return oldItem.nickname == newItem.nickname
            }

            override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(
        private val binding: ItemFriendSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) = with(binding) {
            tvNickname.text = friend.nickname
            tvRealName.text = friend.realName

            // 카카오 계정 뱃지
            ivKakaoBadge.isVisible = friend.isKakaoUser

            // 이미 보낸 친구 요청인지 여부
            val requested = isRequested(friend)

            if (requested) {
                // 🔹 취소 버튼 상태 (X 버튼)
                btnAdd.setBackgroundResource(R.drawable.bg_friend_action_cancel)
                ivAddIcon.setImageResource(R.drawable.x_button)
                tvAddLabel.text = "취소"
            } else {
                // 🔹 추가 버튼 상태 (+ 버튼)
                btnAdd.setBackgroundResource(R.drawable.bg_add_friend_button)
                ivAddIcon.setImageResource(R.drawable.icon_friend_add)
                tvAddLabel.text = "추가"
            }

            btnAdd.setOnClickListener {
                onAddClick(friend)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
