package com.techtown.matchingservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.collections.MarkerManager
import com.techtown.matchingservice.model.ContentDTO
import java.util.*

class Search : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap : GoogleMap
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    val db = FirebaseFirestore.getInstance()
    var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
    var contentUidList: ArrayList<String> = arrayListOf()
    val geocoder = Geocoder(this, Locale.getDefault())
    //private lateinit var clusterManager: ClusterManager<LatLngData>
    //var clusterRenderer : com.techtown.matchingservice.ClusterRenderer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        db.collection("images")
            .get()
            .addOnSuccessListener { result ->
                contentDTOs.clear()
                for(document in result){
                    val cor = geocoder.getFromLocationName(document["place"] as String?, 1)
                    val id = document["id"]
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
        /*val search = findViewById<ImageButton>(R.id.imageButton4)
        search.setOnClickListener {
            val cor = geocoder.getFromLocationName(input.text.toString(), 1)
            Toast.makeText(this, "좌표 : ${cor[0].latitude}, ${cor[0].longitude}", Toast.LENGTH_LONG).show()
            var location = LatLng(cor[0].latitude, cor[0].longitude)
            //val markerOptions = MarkerOptions().position(location).title("Here!")
            //mMap.addMarker(markerOptions)
           // Toast.makeText(this, "3",Toast.LENGTH_LONG).show()
            try{
                val cameraPosition = CameraPosition.Builder().target(location).zoom(15.0f).build()
                val drawable2 = getDrawable(R.drawable.blue_pin)
                val bitmapDrawable2 = drawable2 as BitmapDrawable
                val bitmap2 = bitmapDrawable2.bitmap
                val markerOption = MarkerOptions().position(location).title("여기").icon(BitmapDescriptorFactory.fromBitmap(bitmap2))
                Toast.makeText(applicationContext, "1",Toast.LENGTH_LONG).show()
                mMap.addMarker(markerOption)
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            } catch(e:Exception){

            }
        }*/

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
        //clusterManager = ClusterManager(this, mMap)
        //mMap.setOnCameraIdleListener(clusterManager)
        //mMap.setOnMarkerClickListener(clusterManager)
        //markeradd()
        //updateLocation()
        //clusterItemClick(mMap)
    }
    /*private fun clusterItemClick(mMap : GoogleMap){
        clusterManager.setOnClusterClickListener { p0 ->
            val center : CameraUpdate = CameraUpdateFactory.newLatLng(p0.position)
            mMap!!.animateCamera(center)
            Toast.makeText(applicationContext, "1", Toast.LENGTH_LONG).show()
            true

        }
    }
    fun markeradd(){
        var i : Long =0
        mMap.setOnMarkerClickListener(clusterManager.markerManager)
        val normalMarkerCollection : MarkerManager.Collection = clusterManager.markerManager.newCollection()
        val click : MarkerOptions = MarkerOptions()
        db.collection("images")
            .get()
            .addOnSuccessListener { result ->
                contentDTOs.clear()
                for(document in result){
                    val cor = geocoder.getFromLocationName(document["place"] as String, 1)
                    val id = document["id"].toString()
                    val lat : Double = cor[0].latitude
                    val lng : Double = cor[0].longitude
                    mMap.clear()
                    val drawable2 = getDrawable(R.drawable.blue_pin)
                    val bitmapDrawable2 = drawable2 as BitmapDrawable
                    val bitmap2 = bitmapDrawable2.bitmap
                    clusterManager?.addItem(
                        LatLngData(
                            LatLng(lat, lng),
                            BitmapDescriptorFactory.fromBitmap(bitmap2),id
                        )
                    )
                    //clusterManager.setOnClusterClickListener {  }
                }
            }
            .addOnFailureListener{ exception ->
                Log.w("MainActivity", "Error getting documents: $exception")
            }
    }
    @SuppressLint("MissingPermission")
    fun updateLocation(){
        val locationRequest = LocationRequest.create()
        //clusterRenderer = ClusterRenderer(this, mMap, clusterManager)
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
        val drawable = getDrawable(R.drawable.pin)
        val bitmapDrawable = drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        val markerOptions = MarkerOptions().position(LATLNG).title("현재 위치").icon(BitmapDescriptorFactory.fromBitmap(bitmap))
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
    }*/


}