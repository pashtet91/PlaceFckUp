package com.example.placefckup.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.placefckup.R
import com.example.placefckup.databinding.ActivityBookmarkDetailsBinding
import com.example.placefckup.viewmodel.BookmarDetailsViewModel

class BookmarkDetailsActivity : AppCompatActivity() {

    private lateinit var databinding:ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarDetailsViewModel>()
    private var bookmarkDetailsView:
            BookmarDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this,
        R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =

        when(item.itemId){
            R.id.action_save->{
                saveChanges()
                true
            }
            else->super.onOptionsItemSelected(item)
        }


    private fun populateImageView(){
        bookmarkDetailsView?.let{
            bookmarkView -> val placeImage = bookmarkView.getImage(this)
            placeImage?.let{
                databinding.imageViewPlace.setImageBitmap(placeImage)
            }
        }
    }

    private fun getIntentData(){
        val bookmarkId = intent.getLongExtra(
            MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)

        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this,
            {it?.let{
                bookmarkDetailsView = it
                databinding.bookmarkDetailsView = it
                populateImageView()
            }}
        )
    }

    private fun saveChanges(){
        val name = databinding.editTextName.text.toString()

        if(name.isEmpty())
            return

        bookmarkDetailsView?.let{
            bookmarkView ->
            bookmarkView.name= databinding.editTextName.text.toString()
            bookmarkView.notes =
                databinding.editTextNotes.text.toString()
            bookmarkView.address =
                databinding.editTextAddress.text.toString()
            bookmarkView.phone =
                databinding.editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    private fun setupToolbar(){
        setSupportActionBar(databinding.toolbar)
    }
}