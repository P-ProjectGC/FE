package com.example.plango

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class WishlistAdapter(
    private val items: MutableList<WishlistPlaceItem>,
    private val onConfirmClick: (WishlistPlaceItem) -> Unit,
    private val isHost: Boolean
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPlaceName: TextView = itemView.findViewById(R.id.textPlaceNameWishlist)
        val textAddress: TextView = itemView.findViewById(R.id.textAddressWishlist)
        val textAddedBy: TextView = itemView.findViewById(R.id.textAddedBy)
        val btnConfirm: TextView = itemView.findViewById(R.id.btnConfirmSchedule)
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

        // 👇 버튼 모양은 그대로 두고, 동작만 권한으로 막기
        holder.btnConfirm.setOnClickListener {
            if (!isHost) {
                Toast.makeText(
                    holder.itemView.context,
                    "방장만 일정 확정이 가능해요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 방장일 때만 실제 일정 확정 로직 실행
            onConfirmClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun refresh() {
        notifyDataSetChanged()
    }
}
