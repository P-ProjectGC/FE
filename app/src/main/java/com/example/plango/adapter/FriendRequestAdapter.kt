package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.FriendRequestItem

class FriendRequestAdapter(
    private var items: List<FriendRequestItem>,
    private val onAcceptClick: (FriendRequestItem) -> Unit,
    private val onRejectClick: (FriendRequestItem) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {

    inner class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.iv_profile)
        val ivKakaoBadge: ImageView = itemView.findViewById(R.id.iv_kakao_badge)
        val tvNickname: TextView = itemView.findViewById(R.id.tv_nickname)
        val tvRealName: TextView = itemView.findViewById(R.id.tv_real_name)
        val tvRequestedAt: TextView = itemView.findViewById(R.id.tv_requested_at)
        val btnAccept: TextView = itemView.findViewById(R.id.btn_accept)
        val btnReject: TextView = itemView.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val item = items[position]

        // 닉네임 / 실제 이름
        holder.tvNickname.text = item.senderNickname
        holder.tvRealName.text = item.senderNickname   // 서버에 realName 없으면 닉네임 재사용

        // 요청 시간 (서버에서 받은 createdAt 그대로 사용)
        holder.tvRequestedAt.text = item.requestedAt

        // 카카오 뱃지 노출 여부
        holder.ivKakaoBadge.visibility =
            if (item.isKakaoUser) View.VISIBLE else View.GONE

        // 버튼 콜백
        holder.btnAccept.setOnClickListener { onAcceptClick(item) }
        holder.btnReject.setOnClickListener { onRejectClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<FriendRequestItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
