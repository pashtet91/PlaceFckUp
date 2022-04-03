package com.example.placefckup.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import com.example.placefckup.databinding.ContentBookmarkInfoBinding
import com.example.placefckup.ui.MapsActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookmarkInfoWindowAdapter(context: Activity):
    GoogleMap.InfoWindowAdapter {

    private val binding = ContentBookmarkInfoBinding.inflate(
        context.layoutInflater
    )

    override fun getInfoContents(marker: Marker): View? {
        // This function is required, but can return null if
        // not replacing the entire info window
        val imageView = binding.photo
        imageView.setImageBitmap((marker.tag as //Bitmap))
                MapsActivity.PlaceInfo).image)
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        return binding.root
    }
}