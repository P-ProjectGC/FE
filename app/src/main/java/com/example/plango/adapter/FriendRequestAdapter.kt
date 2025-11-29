package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.Friend

class FriendRequestAdapter(
    private var items: List<Friend>,
    private var requestedAtTexts: List<String>,   // "3시간 전" / "1일 전" 같은 요청 시간
    private val onAcceptClick: (Friend) -> Unit,  // 수락 클릭 콜백
    private val onRejectClick: (Friend) -> Unit   // 거절 클릭 콜백
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

        // 이름들
        holder.tvNickname.text = item.nickname
        holder.tvRealName.text = item.realName

        // 카카오 유저 뱃지 표시
        holder.ivKakaoBadge.visibility =
            if (item.isKakaoUser) View.VISIBLE else View.GONE

        // 프로필 이미지 (지금은 기본 아이콘만 사용)
        // item.profileImageUrl 사용 시 Glide/Picasso로 교체 가능

        // 요청 시간 텍스트
        holder.tvRequestedAt.text =
            if (position < requestedAtTexts.size) requestedAtTexts[position]
            else requestedAtTexts.lastOrNull() ?: ""

        // --- 버튼 콜백 설정 ---
        holder.btnAccept.setOnClickListener {
            onAcceptClick(item)
        }

        holder.btnReject.setOnClickListener {
            onRejectClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<Friend>, newRequestedAt: List<String>) {
        items = newList
        requestedAtTexts = newRequestedAt
        notifyDataSetChanged()
    }
}
