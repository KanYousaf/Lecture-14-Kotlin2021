package com.example.lecture14

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kanwal_laptop.test.City
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapFragment: SupportMapFragment? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mLocationRequest: LocationRequest? = null
    private var mLocationCallback : LocationCallback? = null
    private var latLng: LatLng? = null

    private var pakistan_cities: String = ""
    private var pakistan_cities_lat: Double = 0.0
    private var pakistan_cities_long: Double = 0.0

    //list to add city name, latitude and longitute values
    private var city_list = ArrayList<City>()

    //list to save cities names for spinner
    private var city_names: MutableList<String> = mutableListOf()
    private lateinit var mAdapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        //read pk.txt file to get cities names for spinner
        readFile()
        mAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, city_names)
        spinner_cities.adapter = mAdapter

        spinner_cities.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                citySelected(position)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getLocationAccess()
    }

    fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_FINE_LOCATION
            )
        }
    }

    private fun getLocationUpdates() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.setInterval(5000) //5 seconds
        mLocationRequest!!.setFastestInterval(3000) //3 seconds
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        mLocationRequest!!.setSmallestDisplacement(0.1F); //1/10 meter

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        latLng = LatLng(location.latitude, location.longitude)
                        val markerOptions = MarkerOptions()
                        markerOptions.position(latLng!!)
                        markerOptions.title("Current Position")
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        mMap.addMarker(markerOptions)
                        //zoom to current position:
                        val cameraPosition = CameraPosition.Builder().target(latLng!!).zoom(16.0f).build()
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_FINE_LOCATION
            )
            return
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest!!, mLocationCallback!!, null)
    }


    fun readFile() {
        val scanner: Scanner = Scanner(resources.openRawResource(R.raw.pk))
        scanner.nextLine()
        while (scanner.hasNextLine()) {
            val line: String = scanner.nextLine()
            val pieces = line.split("\t")
            if (pieces.size >= 2) {
                pakistan_cities = pieces[0]
                pakistan_cities_lat = pieces[1].toDouble()
                pakistan_cities_long = pieces[2].toDouble()

                Log.i("MapException", pieces[0] + "   LAT   " + pieces[1] + "   LNG   " + pieces[2])

                city_names.add(pakistan_cities)
                val city = City(pakistan_cities, pakistan_cities_lat, pakistan_cities_long)
                city_list.add(city)
            }
        }
    }

    fun citySelected(index: Int) {
        if (index >= 0 && index < city_list.size) {
            val city_index = city_list[index]

            val cameraPosition =
                CameraPosition.Builder().target(LatLng(city_index.lat, city_index.long)).zoom(8.0f)
                    .build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(city_index.lat, city_index.long)))
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(city_index.lat, city_index.long))
                    .title(city_index.name)
            )
        }
    }

    fun onClearBtnPressed(view: View) {
        mMap.clear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("MapException", "Permission Granted")
                    getLocationAccess()
                } else {
                    Log.i("MapException", "Permission Denied")
                    finish()
                }
                return
            }
        }
    }

    companion object {
        private val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 111
    }
}