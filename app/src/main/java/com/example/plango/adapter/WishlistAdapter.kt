package com.example.plango

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WishlistAdapter(
    private val items: MutableList<WishlistPlaceItem>,
    private val onConfirmClick: (WishlistPlaceItem) -> Unit
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

        holder.btnConfirm.setOnClickListener {
            onConfirmClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun refresh() {
        notifyDataSetChanged()
    }
}
