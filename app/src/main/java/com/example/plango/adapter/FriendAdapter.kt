package com.example.plango.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plango.R
import com.example.plango.model.Friend

/**
 * 친구 목록을 표시하는 RecyclerView Adapter
 * - Friend 모델 데이터를 item_friend 레이아웃에 매핑
 * - 리스트 갱신을 위한 submitList() 제공
 */
class FriendAdapter(
    private var items: List<Friend>
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    /**
     * ViewHolder
     * - item_friend.xml 내부 뷰들을 캐싱해두는 역할
     */
    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNickname: TextView = view.findViewById(R.id.tv_nickname)        // 친구 닉네임
        val tvRealName: TextView = view.findViewById(R.id.tv_real_name)       // 친구 실명
        val ivProfile: ImageView = view.findViewById(R.id.iv_profile)         // 프로필 이미지
        val ivKakaoBadge: ImageView = view.findViewById(R.id.iv_kakao_badge)  // 카카오 유저 뱃지
    }

    /**
     * ViewHolder 생성
     * - item_friend.xml 을 inflate하여 FriendViewHolder 로 감싸서 반환
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    /**
     * ViewHolder에 실제 데이터 바인딩
     * - position에 해당하는 Friend 데이터를 UI에 반영
     */
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = items[position]

        // 텍스트 데이터 바인딩
        holder.tvNickname.text = friend.nickname
        holder.tvRealName.text = friend.realName

        // 카카오 유저라면 뱃지 표시, 아니면 숨김
        holder.ivKakaoBadge.visibility =
            if (friend.isKakaoUser) View.VISIBLE else View.GONE

        // 추후 Glide 적용 예정
        // 현재는 기본 프로필 아이콘을 그대로 사용
    }

    /**
     * 리스트 개수 반환
     */
    override fun getItemCount(): Int = items.size

    /**
     * RecyclerView 데이터 갱신용 메서드
     * - 외부에서 새 리스트를 넘겨주면 전체 새로고침
     * - 나중에 DiffUtil 로 최적화 가능
     */
    fun submitList(newItems: List<Friend>) {
        items = newItems
        notifyDataSetChanged()
    }
}
