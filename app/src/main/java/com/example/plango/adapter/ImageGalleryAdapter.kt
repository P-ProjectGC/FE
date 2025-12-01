package com.example.plango

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageGalleryAdapter(
    private val uris: List<Uri>
) : RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivGalleryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = uris[position]

        try {
            Glide.with(holder.itemView.context)
                .load(uri)
                .into(holder.imageView)
        } catch (e: SecurityException) {
            // 권한 없으면 이 아이템은 빈칸처럼 보여도 됨
            holder.imageView.setImageDrawable(null)
        }
    }


    override fun getItemCount(): Int = uris.size
}
