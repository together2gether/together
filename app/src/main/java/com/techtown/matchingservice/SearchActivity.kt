package com.techtown.matchingservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.techtown.matchingservice.databinding.ProductItemBinding
import com.techtown.matchingservice.model.ContentDTO
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : AppCompatActivity(), OnMapReadyCallback{
    private val ZoomLevel : Int = 12
    private lateinit var mClusterManager : ClusterManager<LatLngData>
    private lateinit var clusterRenderer: DefaultClusterRenderer<LatLngData>
    private val builder : LatLngBounds.Builder = LatLngBounds.builder()
    private lateinit var mMap : GoogleMap
    val db = FirebaseFirestore.getInstance()
    var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
    val geocoder = Geocoder(this, Locale.getDefault())
    private var mLastClickTime : Long =0
    private var size_check : Int =0
    var firestore:FirebaseFirestore? = null
    lateinit var recyclerView : RecyclerView
    companion object {
        var selectedMarker : Marker? = null
    }
    lateinit var uid: String
    var product :ArrayList<ContentDTO> = arrayListOf()
    var contentUidList: ArrayList<String?> = arrayListOf()
    var productUid : ArrayList<String> = arrayListOf()
    var productsList : ArrayList<ProductData> = arrayListOf<ProductData>()
    lateinit var adapter :ProductListAdapter
    lateinit var mRecyclerView: RecyclerView

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        adapter = ProductListAdapter(productsList)
        adapter.setItemClickListener(object :
        ProductListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val item = productsList[position]
                Intent(applicationContext, Product::class.java).apply {
                    putExtra("product", productsList[position].name)
                    putExtra("imageUrl", productsList[position].imageUri)
                    putExtra("price", productsList[position].price)
                    putExtra("totalNumber", productsList[position].totalNumber.toString())
                    putExtra("cycle", productsList[position].cycle.toString())
                    putExtra("unit", productsList[position].unit.toString())
                    putExtra("URL", productsList[position].url)
                    putExtra("place", productsList[position].place)
                    putExtra("timestamp", productsList[position].timestamp.toString())
                    putExtra("participationCount", productsList[position].participationCount)
                    putExtra(
                        "uidkey",
                        productsList[position].uidKey
                    )
                    putExtra(
                        "participationTotal",
                        productsList[position].participationTotal
                    )
                    putExtra("id", productsList[position].Listid)
                    putExtra("Uid", productsList[position].uid)

                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {applicationContext?.startActivity(this)}
                adapter.notifyDataSetChanged()
            }
        })
        mRecyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        mRecyclerView.adapter = adapter
        firestore = FirebaseFirestore.getInstance()
        val finish = findViewById<Button>(R.id.button44)
        finish.setOnClickListener {
            finish()
        }
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        requirePermissions(permissions, 999)
    }
    fun requirePermissions(permissions:Array<String>,requestCode : Int){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            permissionGranted(requestCode)
        } else{
            val isAllPermissionsGranted = permissions.all{
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            if(isAllPermissionsGranted){
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
        if(grantResults.all {it == PackageManager.PERMISSION_GRANTED}){
            permissionGranted(requestCode)
        } else{
            permissionDenied(requestCode)
        }

    }
    fun permissionGranted(requestCode: Int){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    fun permissionDenied(requestCode: Int){
        Toast.makeText(this, "권한 승인이 필요합니다.", Toast.LENGTH_LONG).show()
    }

        override fun onMapReady(p0: GoogleMap) {
        try{
            mMap = p0
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            updateLocation()
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true

            mClusterManager = ClusterManager<LatLngData>(this, mMap)
            clusterRenderer = MarkerClusterRenderer(
                this, mMap, mClusterManager
            )
            getExtra()
            mClusterManager.renderer = clusterRenderer

            mMap.setOnCameraIdleListener(mClusterManager)
            mMap.setOnMarkerClickListener(mClusterManager)

            mMap.setOnInfoWindowClickListener {  }
            mMap.setOnMapClickListener{
                //card_view.visibility = View.GONE
                productsList.clear()
                adapter.notifyDataSetChanged()
                updateLocation()
            }

            clusterItemClick(mMap)
            clusterClick(mMap)
            var i:Int =0
            db.collection("images")
                .get()
                .addOnSuccessListener { result ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    for(document in result){
                        val cor = geocoder.getFromLocationName(document["place"] as String, 1)
                        val id = document["id"] as String?
                        var item = document.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(document.id)
                        var location = LatLng(cor[0].latitude, cor[0].longitude)
                        addLatLngData(i,id, location)
                        i++
                    }
                }
            boundmap()
            updateLocation()

        } catch (e:Exception){

        }
    }

    @SuppressLint("MissingPermission")
    fun updateLocation(){
        val locationRequest = com.google.android.gms.location.LocationRequest.create()
        locationRequest.run {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval  = 1000
        }
        locationCallback = object: LocationCallback(){
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
        val resources : Resources = this!!.resources
        val bitmap2 = BitmapFactory.decodeResource(resources,R.drawable.red_pin)
        val markerOptions = MarkerOptions().position(LATLNG).title("현재 위치").icon(BitmapDescriptorFactory.fromBitmap(bitmap2))

        val cameraPosition = CameraPosition.Builder().target(LATLNG).zoom(15.0f).build()
        //mMap.clear()
        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
    fun cameraInit(){
        if(size_check < 100){
            boundmap()
        }

    }
    private fun moveCamera(map:GoogleMap, marker:Marker){
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    marker.position.latitude,
                    marker.position.longitude
                ), 15.0f
            )
        )
        marker.showInfoWindow()
    }
    private fun removeLocationListener(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    /*private fun changeRenderer(item:LatLngData){
        val drawable1 = getDrawable(R.drawable.pin)
        val bitmapDrawable2 = drawable1 as BitmapDrawable
        val bitmap2 = bitmapDrawable2.bitmap
        val drawable2 = getDrawable(R.drawable.checked_bluepin)
        val bitmapDrawable3 = drawable2 as BitmapDrawable
        val bitmap3 = bitmapDrawable3.bitmap
        if(selectedMarker != clusterRenderer.getMarker(item)){
            if(selectedMarker != null){
                selectedMarker!!.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap2))
            }
        }
        if(clusterRenderer.getMarker(item) != null){
            clusterRenderer.getMarker(item).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap3))
        }
        selectedMarker = clusterRenderer.getMarker(item)

    }*/
    fun boundmap(){
        val bounds : LatLngBounds = builder.build()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, ZoomLevel))
        val zoom : Float = mMap.cameraPosition.zoom - 0.5f
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom))
    }
    fun addLatLngData(index : Int, id : String?, latlng : LatLng){
        val data = LatLngData(index, id, latlng)
        if(size_check ==0){
            Handler(Looper.getMainLooper()).post{mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(data.latlng,12F))}
        }
        mClusterManager.addItem(data)
        builder.include(data.latlng)
        size_check++
        if(size_check == 100){
            Handler(Looper.getMainLooper()).post{boundmap()}
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    fun getExtra(){
    }
    private fun clusterClick(mMap : GoogleMap){
        mClusterManager.setOnClusterClickListener { cluster ->
            val builder_c :LatLngBounds.Builder = LatLngBounds.builder()
            productsList.clear()
            for(item in cluster.items){
                if(item != null){
                    builder_c.include(item.position)
                    var id = contentDTOs[item.index].userId as String
                    var name = contentDTOs[item.index].product as String
                    var place = contentDTOs[item.index].place as String
                    var image = contentDTOs[item.index].imageUrl as String
                    var participation = "현재 " + contentDTOs[item.index].ParticipationCount.toString() + " / " + contentDTOs[item.index].ParticipationTotal.toString()
                    var price = contentDTOs[item.index].price.toString()
                    var totalNumber = contentDTOs[item.index].totalNumber.toString()
                    var cycle = contentDTOs[item.index].cycle.toString()
                    var unit = contentDTOs[item.index].unit.toString()
                    var url = contentDTOs[item.index].url as String
                    var uid = contentDTOs[item.index].uid as String
                    var timestamp = contentDTOs[item.index].timestamp.toString()
                    var participationCount = contentDTOs[item.index].ParticipationCount.toString()
                    var uidkey = contentDTOs[item.index].Participation.containsKey(uid).toString()
                    var participationTotal = contentDTOs[item.index].ParticipationTotal.toString()
                    var Listid = contentUidList[item.index] as String
                    var product = ProductData(id, name, place, image,participation , price, totalNumber, cycle, unit, url, uid, timestamp, participationCount, uidkey, participationTotal,Listid)
                    productsList.add(product)
                    adapter.notifyDataSetChanged()
                }
            }
            val bounds_c: LatLngBounds = builder_c.build()
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds_c,ZoomLevel))
            val zoom :Float = mMap.cameraPosition.zoom - 0.5f
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom))
            true
        }

    }
    private fun clusterItemClick(mMap: GoogleMap){
        mClusterManager.setOnClusterItemClickListener { p0 ->
            val center : CameraUpdate = CameraUpdateFactory.newLatLng(p0.position)
            mMap!!.animateCamera(center)
            productsList.clear()
            var id = contentDTOs[p0.index].userId as String
            var name = contentDTOs[p0.index].product as String
            var place = contentDTOs[p0.index].place as String
            var image = contentDTOs[p0.index].imageUrl as String
            val participation = "현재 " + contentDTOs[p0.index].ParticipationCount.toString() + " / " + contentDTOs[p0.index].ParticipationTotal.toString()
            var price = contentDTOs[p0.index].price.toString()
            var totalNumber = contentDTOs[p0.index].totalNumber.toString()
            var cycle = contentDTOs[p0.index].cycle.toString()
            var unit = contentDTOs[p0.index].unit.toString()
            var url = contentDTOs[p0.index].url as String
            var uid = contentDTOs[p0.index].uid as String
            var timestamp = contentDTOs[p0.index].timestamp.toString()
            var participationCount = contentDTOs[p0.index].ParticipationCount.toString()
            var uidkey = contentDTOs[p0.index].Participation.containsKey(uid).toString()
            var participationTotal = contentDTOs[p0.index].ParticipationTotal.toString()
            var Listid = contentUidList[p0.index] as String
            var product = ProductData(id, name, place, image,participation , price, totalNumber, cycle, unit, url, uid, timestamp, participationCount, uidkey, participationTotal,Listid)
            productsList.add(product)
            adapter.notifyDataSetChanged()
            Log.d("qweqwe","wqe")
            //changeRenderer(p0)
            true
        }

    }

    inner class MarkerClusterRenderer(context : Context?, map : GoogleMap?, clusterManager: ClusterManager<LatLngData>?)
        :DefaultClusterRenderer<LatLngData>(context, map, clusterManager){
            private val context = context
        val resources : Resources = context!!.resources
        val bitmap2 = BitmapFactory.decodeResource(resources,R.drawable.blue_pin)

        override fun onBeforeClusterItemRendered(item: LatLngData, markerOptions: MarkerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap2))
        }

        override fun onClustersChanged(clusters: MutableSet<out Cluster<LatLngData>>?) {
            super.onClustersChanged(clusters)
            if(selectedMarker != null){
                selectedMarker!!.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap2))
            }
        }

        override fun shouldRenderAsCluster(cluster: Cluster<LatLngData>): Boolean {
            super.shouldRenderAsCluster(cluster)
            return cluster != null && cluster.size >= 3
        }
        }

}
