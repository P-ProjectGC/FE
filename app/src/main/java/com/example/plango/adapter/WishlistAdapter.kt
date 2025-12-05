package com.example.plango

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class WishlistAdapter(
    private val items: MutableList<WishlistPlaceItem>,
    private var isHost: Boolean,
    private val onConfirmClick: (WishlistPlaceItem) -> Unit,
    private val onDeleteClick: (WishlistPlaceItem) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPlaceName: TextView = itemView.findViewById(R.id.textPlaceNameWishlist)
        val textAddress: TextView = itemView.findViewById(R.id.textAddressWishlist)
        val textAddedBy: TextView = itemView.findViewById(R.id.textAddedBy)
        val btnConfirm: TextView = itemView.findViewById(R.id.btnConfirmSchedule)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDeleteWishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist_place, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val item = items[position]

        holder.textPlaceName.text = item.placeName
        holder.textAddress.text = item.address
        holder.textAddedBy.text = "추가: ${item.addedBy}"

        // ✅ 일정 확정 버튼
        holder.btnConfirm.setOnClickListener {
            if (!isHost) {
                Toast.makeText(
                    holder.itemView.context,
                    "방장만 일정 확정이 가능해요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            onConfirmClick(item)
        }

        // ✅ 삭제 버튼
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // 전체 갱신
    fun refresh() {
        notifyDataSetChanged()
    }

    // 개별 삭제
    fun removeItem(item: WishlistPlaceItem) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    // ⭐ 방장 여부를 나중에 서버 기준으로 다시 넣어주는 함수
    fun updateHost(isHost: Boolean) {
        this.isHost = isHost
        notifyDataSetChanged()
    }
}
