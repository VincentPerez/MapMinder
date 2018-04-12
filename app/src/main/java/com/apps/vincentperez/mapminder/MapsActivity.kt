package com.apps.vincentperez.mapminder

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.stetho.Stetho
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map : GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private  var markers : ArrayList<Marker> = ArrayList()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
        private const val ADD_REMINDER_REQUEST = 4
        private const val MODIFY_REMINDER_REQUEST = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Stetho.initializeWithDefaults(this)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
            }
        }
        createLocationRequest()

        val fabbtn = findViewById<FloatingActionButton>(R.id.fab)
        fabbtn.setOnClickListener {
            loadPlacePicker()
        }

        val addbtn = findViewById<FloatingActionButton>(R.id.add)
        addbtn.setOnClickListener {
            addReminder()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        /*val gumbo = LatLng(48.877, 2.369999)
        map.addMarker(MarkerOptions().position(gumbo).title("Gumbo Yaya"))
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(gumbo, 15.0f))*/
        map.getUiSettings().setZoomControlsEnabled(true)
        map!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                markerClick(marker)
                return false
            }
        })
        setUpMap()
    }

    private fun addReminder() {
        val intent = Intent(this, AddReminderActivity::class.java)
        intent.putExtra("EXTRA_LAT", lastLocation.latitude.toString())
        intent.putExtra("EXTRA_LON", lastLocation.longitude.toString())
        intent.putExtra("EXTRA_ADDRESS", getAddress(LatLng(lastLocation.latitude, lastLocation.longitude)))
        startActivityForResult(intent, ADD_REMINDER_REQUEST)
    }

    private fun modifyReminder(marker: Marker) {
        val intent = Intent(this, AddReminderActivity::class.java)
        val DB:DatabaseHandler = DatabaseHandler(this)

        val reminder = DB.FetchMarker(marker.tag as Long)
        intent.putExtra("EXTRA_TITLE", reminder?.Title)
        intent.putExtra("EXTRA_CONTENT", reminder?.Content)
        intent.putExtra("EXTRA_ADDRESS", reminder?.Address)
        intent.putExtra("EXTRA_TAG", marker.tag.toString())
        startActivityForResult(intent, MODIFY_REMINDER_REQUEST)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                //map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,12.0f))
            }
        }
        populateMap()
    }

    private fun populateMap() {
        val DB:DatabaseHandler = DatabaseHandler(this)
        val arraylist : ArrayList<Reminder> = DB.FetchMarkers("%")
        for (reminder in arraylist) {
            placeMarkerOnMap(LatLng(reminder.Latitude!!, reminder.Longitude!!), reminder.Title, reminder.conID)
        }
    }

    private fun placeMarkerOnMap(location: LatLng, title: String?, tag: Long?) {
        val markerOptions = MarkerOptions().position(location)
        var id: Long? = tag
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
        //        BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))
        if (title == null || title == "") {
            markerOptions.title(getAddress(location))
        } else {
            markerOptions.title(title)
        }
        if (tag == null) {
            val values = ContentValues()
            values.put("title", markerOptions.title)
            values.put("address", getAddress(location))
            values.put("lat", location.latitude)
            values.put("lon", location.longitude)
            values.put("content", "")

            val DB:DatabaseHandler = DatabaseHandler(this);
            id = DB.AddReminder(values)
        }
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker())

        val marker = map.addMarker(markerOptions)
        marker.setTag(id)
        markers.add(marker)
    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex + 1) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }
        return addressText
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {

        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private fun markerClick(marker: Marker) {
        val DB:DatabaseHandler = DatabaseHandler(this)
        AlertDialog.Builder(this)
                .setTitle(marker.title.toString())
                //get message from marker content
                .setMessage(DB.fetchContent(marker.tag as Long))
                .setPositiveButton(R.string.delete_alert, { _, _ ->

                    val ret: Int = DB.RemoveMarker(marker.tag as Long)
                    if (ret > 0) {
                        marker.remove()
                    }
                })
                .setNeutralButton(R.string.modify_alert, { _, _ ->
                    //activity add reminder with values filled
                    modifyReminder(marker)
                })
                .setNegativeButton(R.string.cancel_alert, {
                    dialog, _ -> dialog.cancel()
                })
                .create()
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng, null, null)
            }
        }
        if (requestCode == ADD_REMINDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                placeMarkerOnMap(LatLng(data?.getStringExtra("EXTRA_LAT")!!.toDouble(), data?.getStringExtra("EXTRA_LON")!!.toDouble()),
                        data.getStringExtra("EXTRA_TITLE"), data.getStringExtra("EXTRA_TAG").toLong())
            }
        }

        if (requestCode == MODIFY_REMINDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                //modify marker title
                for (marker in markers) {
                    if (marker.tag as Long == data?.getStringExtra("EXTRA_TAG")?.toLong()) {
                        marker.setTitle(data?.getStringExtra("EXTRA_TITLE"))
                        marker.showInfoWindow()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
               startLocationUpdates()
        }
    }

}
