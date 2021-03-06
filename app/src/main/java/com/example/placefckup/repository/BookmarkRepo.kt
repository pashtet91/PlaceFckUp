package com.example.placefckup.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.placefckup.db.BookmarkDao
import com.example.placefckup.db.PlaceBookDatabase
import com.example.placefckup.model.Bookmark

class BookmarkRepo(context: Context) {
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()

    fun addBookmark(bookmark: Bookmark): Long?{
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark{
        return Bookmark()
    }

    fun getBookmark(bookmarkId: Long): Bookmark{
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> =
        bookmarkDao.loadLiveBookmark(bookmarkId)

    fun updateBookmark(bookmark:Bookmark) =
        bookmarkDao.updateBookmark(bookmark)

    val allBookmark: LiveData<List<Bookmark>>
    get(){
        return bookmarkDao.loadAll()
    }
}