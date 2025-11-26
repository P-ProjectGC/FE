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

/**
 * 친구 목록을 표시하는 RecyclerView Adapter
 * - Friend 모델 데이터를 item_friend 레이아웃에 매핑
 * - 프로필 이미지: URL 있으면 로드, 없으면 기본 이미지 사용
 * - 카카오 유저 뱃지 표시/숨김 처리
 */
class FriendAdapter(
    private var items: List<Friend>
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    /**
     * ViewHolder
     * item_friend.xml 내부 뷰들을 캐싱해두는 역할
     */
    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNickname: TextView = view.findViewById(R.id.tv_nickname)        // 닉네임
        val tvRealName: TextView = view.findViewById(R.id.tv_real_name)       // 실명
        val ivProfile: ImageView = view.findViewById(R.id.iv_profile)         // 프로필 이미지
        val ivKakaoBadge: ImageView = view.findViewById(R.id.iv_kakao_badge)  // 카카오 뱃지
    }

    /**
     * ViewHolder 생성
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    /**
     * 데이터 바인딩
     */
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = items[position]

        // 닉네임 / 실명
        holder.tvNickname.text = friend.nickname
        holder.tvRealName.text = friend.realName

        // 카카오 유저이면 뱃지 보이기
        holder.ivKakaoBadge.visibility =
            if (friend.isKakaoUser) View.VISIBLE else View.GONE

        //  프로필 이미지 처리 (URL 있으면 Glide, 없으면 기본 아이콘)
        if (friend.profileImageUrl.isNullOrBlank()) {
            // 이미지가 없으면 기본 이미지
            holder.ivProfile.setImageResource(R.drawable.profile_basic)
        } else {
            // Glide로 URL 이미지 로드
            Glide.with(holder.itemView.context)
                .load(friend.profileImageUrl)
                .placeholder(R.drawable.profile_basic)
                .error(R.drawable.profile_basic)
                .centerCrop()
                .into(holder.ivProfile)
        }
    }

    /**
     * 리스트 개수
     */
    override fun getItemCount(): Int = items.size

    /**
     * RecyclerView 갱신 함수
     */
    fun submitList(newItems: List<Friend>) {
        items = newItems
        notifyDataSetChanged()
    }
}
