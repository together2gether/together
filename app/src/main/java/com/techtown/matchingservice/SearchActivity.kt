package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Camera
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.techtown.matchingservice.model.ContentDTO
import java.lang.Exception
import java.util.*

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap : GoogleMap
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    val db = FirebaseFirestore.getInstance()
    var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
    var contentUidList: ArrayList<String> = arrayListOf()
    val geocoder = Geocoder(this, Locale.getDefault())
    private lateinit var clusterManager: ClusterManager<LatLngData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        db.collection("images")
            .get()
            .addOnSuccessListener { result ->
                contentDTOs.clear()
                for(document in result){
                    val cor = geocoder.getFromLocationName(document["place"] as String, 1)
                    var location = LatLng(cor[0].latitude, cor[0].longitude)
                    try{
                        var markerOptions = MarkerOptions().title("여기에요").position(location)
                        val cameraPosition = CameraPosition.Builder().target(location).zoom(15.0f).build()
                        mMap.addMarker(markerOptions)
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    } catch(e:Exception){

                    }
                }
            }
            .addOnFailureListener{ exception ->
                Log.w("MainActivity", "Error getting documents: $exception")
            }

        /*var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()


        val geocoder = Geocoder(this, Locale.getDefault())*/
        val button = findViewById<Button>(R.id.button44)
        button.setOnClickListener {
            finish()
        }
        val input = findViewById<EditText>(R.id.editTextTextPersonName18)
        val search = findViewById<ImageButton>(R.id.imageButton4)
        search.setOnClickListener {
            val cor = geocoder.getFromLocationName(input.text.toString(), 1)
            Toast.makeText(this, "좌표 : ${cor[0].latitude}, ${cor[0].longitude}", Toast.LENGTH_LONG).show()
            var location = LatLng(cor[0].latitude, cor[0].longitude)
            //val markerOptions = MarkerOptions().position(location).title("Here!")
            //mMap.addMarker(markerOptions)
           // Toast.makeText(this, "3",Toast.LENGTH_LONG).show()
            try{
                var markerOptions = MarkerOptions().title("여기에요").position(location)
                val cameraPosition = CameraPosition.Builder().target(location).zoom(15.0f).build()
                mMap.addMarker(markerOptions)
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            } catch(e:Exception){

            }
        }

        val permission = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        requirePermissions(permission, 999)
    }
    fun startProcess(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }
    fun requirePermissions(permissions : Array<String>, requestCode : Int){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            permissionGranted(requestCode)
        } else{
            val isAllPermissionsGranted = permissions.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            if(isAllPermissionsGranted) {
                permissionGranted(requestCode)
            } else{
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.all{
            it == PackageManager.PERMISSION_GRANTED
            }){
            permissionGranted(requestCode)
        } else{
            permissionDenied(requestCode)
        }
    }
    fun permissionGranted(requestCode: Int){
        startProcess()
    }
    fun permissionDenied(requestCode: Int){
        Toast.makeText(this, "권한 승인이 필요합니다.", Toast.LENGTH_LONG).show()
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        clusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        markeradd()
        updateLocation()
    }
    fun markeradd(){
        var i : Long =0
        db.collection("images")
            .get()
            .addOnSuccessListener { result ->
                contentDTOs.clear()
                for(document in result){
                    val cor = geocoder.getFromLocationName(document["place"] as String, 1)
                    val lat : Double = cor[0].latitude
                    val lng : Double = cor[0].longitude
                    mMap.clear()
                    clusterManager.addItem(LatLngData(LatLng(lat, lng)))

                }
            }
            .addOnFailureListener{ exception ->
                Log.w("MainActivity", "Error getting documents: $exception")
            }
    }
    @SuppressLint("MissingPermission")
    fun updateLocation(){
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                p0?.let{
                    for(location in it.locations){
                        Log.d("Location", "${location.latitude}, ${location.longitude}")
                        removeLocationListener()
                        setLastLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }
    fun setLastLocation(lastLocation : Location){
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        val markerOptions = MarkerOptions().position(LATLNG).title("Here!")
        val cameraPosition = CameraPosition.Builder().target(LATLNG).zoom(15.0f).build()
        //mMap.clear()
        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
    private fun moveCamera(map:GoogleMap, marker:Marker){
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    marker.position.latitude,
                    marker.position.longitude
                ), 16f
            )
        )
        marker.showInfoWindow()
    }

    private fun removeLocationListener(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}