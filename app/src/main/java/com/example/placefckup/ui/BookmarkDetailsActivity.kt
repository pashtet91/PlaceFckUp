package com.example.placefckup.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.placefckup.R
import com.example.placefckup.databinding.ActivityBookmarkDetailsBinding
import com.example.placefckup.util.ImageUtils
import com.example.placefckup.viewmodel.BookmarDetailsViewModel
import java.io.File

class BookmarkDetailsActivity : AppCompatActivity(),
        PhotoOptionDialogFragment.PhotoOptionDialogListener{

    private lateinit var databinding:ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarDetailsViewModel>()
    private var bookmarkDetailsView:
            BookmarDetailsViewModel.BookmarkDetailsView? = null

    private var photoFile: File? = null

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

    override fun onCaptureClick() {
        photoFile = null

        try{
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException){
            return
        }

        photoFile?.let{
            photoFile ->
            val photoUri = FileProvider.getUriForFile(
                this,
            "com.example.placefckup.fileprovider",
                photoFile)

            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map{it.activityInfo.packageName}
                .forEach{grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)}

            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

        }
    }

    override fun onPickClick(){
        Toast.makeText(this, "Gallery Pick",
        Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == android.app.Activity.RESULT_OK){
            when(requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    val photoFile = photoFile ?: return

                    val uri = FileProvider.getUriForFile(this,
                        "com.example.placefckup.fileprovider",
                    photoFile)

                    revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    var image = getImageWithPath(photoFile.absolutePath)

                    val bitmap = ImageUtils.rotateImageIfRequired(this,
                    image, uri)

                    updateImage(bitmap)
                }
            }
        }
    }

    private fun populateImageView(){
        bookmarkDetailsView?.let{
            bookmarkView -> val placeImage = bookmarkView.getImage(this)
            placeImage?.let{
                databinding.imageViewPlace.setImageBitmap(placeImage)
            }

            databinding.imageViewPlace.setOnClickListener{
                replaceImage()
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

    private fun replaceImage(){
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun updateImage(image: Bitmap){
        bookmarkDetailsView?.let{
            databinding.imageViewPlace.setImageBitmap(image)
            it.setImage(this, image)
        }
    }

    private fun getImageWithPath(filePath: String) =
        ImageUtils.decodeFileToSize(
            filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height)
        )

    private fun setupToolbar(){
        setSupportActionBar(databinding.toolbar)
    }

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
    }
}