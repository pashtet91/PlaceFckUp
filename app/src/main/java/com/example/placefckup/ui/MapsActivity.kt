package com.example.placefckup.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.placefckup.R
import com.example.placefckup.adapter.BookmarkInfoWindowAdapter
import com.example.placefckup.adapter.BookmarkListAdapter

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.placefckup.databinding.ActivityMapsBinding
import com.example.placefckup.viewmodel.MapsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val mapsViewModel by viewModels<MapsViewModel>()

    private lateinit var mMap: GoogleMap
    private lateinit var fLProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupToolbar()
        setupPlacesClient()

        setupNavigationDrawer()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupMapListeners()
        createBookmarkObserver()
        getCurrentLocation()

    }

    private fun setupToolbar(){
        setSupportActionBar(binding.mainMapView.toolbar)

        var toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainMapView.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        toggle.syncState()
    }

    private fun setupNavigationDrawer(){
        val layoutManager = LinearLayoutManager(this)
        binding.drawerViewMaps.bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        binding.drawerViewMaps.bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    private fun setupMapListeners(){
        mMap.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        mMap.setOnPoiClickListener{
            //Toast.makeText(this, it.name, Toast.LENGTH_LONG).show()
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }

    private fun setupPlacesClient(){
        Places.initialize(applicationContext,
        getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupLocationClient(){
        fLProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getCurrentLocation(){
        if(ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            requestLocationPermissions()
        } else {



            mMap.isMyLocationEnabled = true

            fLProviderClient.lastLocation.addOnCompleteListener{
                val location = it.result
                if(location != null){
                    val latLng = LatLng(location.latitude, location.longitude)

                    val update = CameraUpdateFactory.newLatLngZoom(latLng,
                    16.0f)

                    mMap.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest){
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placesId = pointOfInterest.placeId

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest
            .builder(placesId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
//                Toast.makeText(
//                    this,
//                    "${place.name}," +
//                            place.phoneNumber,
//                    Toast.LENGTH_LONG
//                ).show()
                displayPoiGetPhotoStep(place)
            }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode
                    )
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place){
        val photoMetadata = place
            .getPhotoMetadatas()?.get(0)
            if(photoMetadata == null) {
                displayPoiDisplayStep(place, null)
                return
            }

        val photoRequest = FetchPhotoRequest
            .builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(
                R.dimen.default_image_width
            ))
            .setMaxHeight(resources.getDimensionPixelSize(
                R.dimen.default_image_height
            ))
            .build()

        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponce->
                val bitmap = fetchPhotoResponce.bitmap
                displayPoiDisplayStep(place, bitmap)
            }

            .addOnFailureListener { exception ->
                if (exception is ApiException){
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                    "Place not found: " +
                    exception.message + ", " +
                    "statusCode: " + statusCode)
                }
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?){

        val marker = mMap.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceInfo(place, photo)//photo
        marker?.showInfoWindow()
    }

    private fun handleInfoWindowClick(marker: Marker){
//        val placeInfo = (marker.tag as PlaceInfo)
//        if(placeInfo.place != null){
//            GlobalScope.launch {
//                mapsViewModel.addBookmarkFromPlace(
//                    placeInfo.place,
//                    placeInfo.image
//                )
//            }
//        }
//        marker.remove()
        when(marker.tag){
            is PlaceInfo-> {
                val placeInfo=(marker.tag as PlaceInfo)
                if(placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(
                            placeInfo.place,
                            placeInfo.image
                        )
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkView->{
                val bookmarkMarkerView = (marker.tag as
                        MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let{
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun addPlaceMarker(bookmark:MapsViewModel.BookmarkView): Marker? {
        val marker = mMap.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE
            ))
            .alpha(0.8f))
        if (marker != null) {
            marker.tag = bookmark
        }
        return  marker
    }

    private fun displayAllBookmarks(bookmarks
                                    : List<MapsViewModel.BookmarkView>){
        bookmarks.forEach{addPlaceMarker(it)}
    }

    private fun startBookmarkDetails(bookmarkId: Long){
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun createBookmarkObserver(){
        mapsViewModel.getBookmarkViews()?.observe(
            this, {
                mMap.clear()
                it?.let{
                    displayAllBookmarks(it)
                    bookmarkListAdapter.setBookmarkData(it)
                }
            }
        )
    }

    private fun requestLocationPermissions(){
        ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        REQUEST_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_LOCATION){
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getCurrentLocation()
            else
                Log.e(TAG, "Location permission denied")
        }
    }

    companion object{
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        const val EXTRA_BOOKMARK_ID =
            "com.example.placefckup.EXTRA_BOOKMARK_ID"
    }

    class PlaceInfo(val place: Place? = null,
                    val image: Bitmap? = null)
}