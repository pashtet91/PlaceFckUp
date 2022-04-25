package com.example.placefckup.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.placefckup.model.Bookmark
import com.example.placefckup.repository.BookmarkRepo
import com.example.placefckup.util.ImageUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class MapsViewModel(application: Application):
    AndroidViewModel(application){

    private val TAG = "MapsViewModel"

    private val bookmarkRepo:BookmarkRepo = BookmarkRepo(getApplication())

    private var bookmarks: LiveData<List<BookmarkView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap?){
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude?: 0.0
        bookmark.latitude = place.latLng?.latitude?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()

        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let{bookmark.setImage(it, getApplication())}
        Log.i(TAG, "New bookmark $newId added to the database.")

    }

    fun getBookmarkViews():
            LiveData<List<BookmarkView>>?{
        if(bookmarks == null){
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun mapBookmarksToBookmarkView(){
        bookmarks = Transformations.map(bookmarkRepo.allBookmark){
            repoBookmarks->
            repoBookmarks.map{bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark) =
        BookmarkView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone
        )

    data class BookmarkView(
        var id: Long? = null,
        val location: LatLng = LatLng(0.0, 0.0),
        val name: String = "",
        val phone: String = ""
    ){
        fun getImage(context: Context) = id?.let{
            ImageUtils.loadBitmapFromFile(context,
                Bookmark.generateImageFileName(it))
        }
    }

}