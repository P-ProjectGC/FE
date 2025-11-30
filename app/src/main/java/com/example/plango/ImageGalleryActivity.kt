package com.example.plango

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImageGalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        recyclerView = findViewById(R.id.recyclerGallery)

        val uriStrings = intent.getStringArrayListExtra("IMAGE_URIS") ?: arrayListOf()
        val uris = uriStrings.map { Uri.parse(it) }

        adapter = ImageGalleryAdapter(uris)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        findViewById<ImageButton>(R.id.btnCloseGallery).setOnClickListener {
            finish()
        }
    }
}
