package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.databinding.ItemFriendSearchBinding
import com.example.plango.model.Friend

class FriendSearchResultAdapter(
    private val onAddClick: (Friend) -> Unit
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

            // 프로필 이미지 (지금은 기본 이미지만 사용)
            // friend.profileImageUrl 생기면 여기서 Glide/Picasso로 로드하면 됨

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
