package com.example.placefckup.adapter

import android.app.Activity
import android.view.View
import com.example.placefckup.databinding.ContentBookmarkInfoBinding
import com.example.placefckup.ui.MapsActivity
import com.example.placefckup.viewmodel.MapsViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookmarkInfoWindowAdapter(val context: Activity):
    GoogleMap.InfoWindowAdapter {

    private val binding = ContentBookmarkInfoBinding.inflate(
        context.layoutInflater
    )

    override fun getInfoContents(marker: Marker): View? {
        val imageView = binding.photo
        when(marker.tag){
            is MapsActivity.PlaceInfo->{
                imageView.setImageBitmap(
                    (marker.tag as MapsActivity.PlaceInfo).image
                )
            }
            is MapsViewModel.BookmarkView->{
                val bookMarkview = marker.tag as
                        MapsViewModel.BookmarkView
//                imageView.setImageBitmap((marker.tag as //Bitmap))
//                        MapsActivity.PlaceInfo).image)
                imageView.setImageBitmap(bookMarkview.getImage(context))
            }
        }

        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        return binding.root
    }
}