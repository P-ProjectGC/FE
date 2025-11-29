package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plango.R
import com.example.plango.model.Friend

class SelectedFriendChipAdapter_rm(
    private val onChipClick: (Friend) -> Unit
) : RecyclerView.Adapter<SelectedFriendChipAdapter_rm.ChipViewHolder>() {

    private val items = mutableListOf<Friend>()

    fun submitList(newItems: List<Friend>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_friend_chip_rm, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val friend = items[position]
        holder.bind(friend)
        holder.itemView.setOnClickListener { onChipClick(friend) }
    }

    override fun getItemCount(): Int = items.size

    inner class ChipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile: ImageView = view.findViewById(R.id.iv_chip_profile)
        private val tvNickname: TextView = view.findViewById(R.id.tv_chip_nickname)

        fun bind(friend: Friend) {
            tvNickname.text = friend.nickname

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
        }
    }
}
