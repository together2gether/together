package com.techtown.matchingservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo
import java.util.*
import kotlin.collections.ArrayList

class SearchFood : AppCompatActivity(), OnMapReadyCallback {
    private val ZoomLevel : Int = 12
    private lateinit var mClusterManager : ClusterManager<LatLngData>
    private lateinit var clusterRenderer: DefaultClusterRenderer<LatLngData>
    private val builder : LatLngBounds.Builder = LatLngBounds.builder()
    private lateinit var mMap : GoogleMap
    val db = FirebaseFirestore.getInstance()
    var contentDTOs : ArrayList<DeliveryDTO> = arrayListOf()
    val geocoder = Geocoder(this, Locale.getDefault())
    private var mLastClickTime : Long =0
    private var size_check : Int =0
    var firestore: FirebaseFirestore? = null
    lateinit var recyclerView : RecyclerView
    companion object {
        var selectedMarker : Marker? = null
    }
    var i=0
    lateinit var uid: String
    var List : ArrayList<DeliveryData> = ArrayList<DeliveryData>()
    var price : Int =0
    var distance : Int =0
    var day :  Int =0
    var product :ArrayList<DeliveryDTO> = arrayListOf()
    var contentUidList: ArrayList<String?> = arrayListOf()
    var productUid : ArrayList<String> = arrayListOf()
    var productsList : ArrayList<DeliveryData> = arrayListOf<DeliveryData>()
    lateinit var adapter :DeliveryListAdapter
    lateinit var mRecyclerView: RecyclerView
    var latlngList : ArrayList<LatLngData> = arrayListOf()
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var mylat : Double = 0.0
    var mylon : Double = 0.0
    var mylocation : String = ""
    lateinit var mycor : List<Address>
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        var selectbutton = findViewById<Button>(R.id.btn_select)
        selectbutton.setVisibility(View.GONE)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        uid = FirebaseAuth.getInstance().uid!!

        adapter = DeliveryListAdapter(productsList)
        adapter.setItemClickListener(object :
            DeliveryListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val item = productsList[position]
                Intent(this@SearchFood, Delivery::class.java).apply {
                    //putExtra("store", productsList[position].store.toString())
                    //putExtra("name", productsList[position].place.toString())
                    //putExtra("delivery", productsList[position].delivery.toString())
                    //putExtra("orderPrice", productsList[position].orderprice.toString())
                    //putExtra("deliveryPrice", productsList[position].deliverprice.toString())
                    putExtra("deliveryid", productsList[position].Listid)
                    //putExtra("deliveryuid", productsList[position].userId)
                    //putExtra("detail", productsList[position].detail)
                    //putExtra("address", productsList[position].address)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {this@SearchFood?.startActivity(this)}
                adapter.notifyDataSetChanged()
            }
        })
        val Search = findViewById<ImageButton>(R.id.imageButton4)

        Search.setOnClickListener{
            val edit = findViewById<EditText>(R.id.productSearch)
            val string = edit.text.toString()
            if (string.isNullOrEmpty()) {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                search(string)
            }
        }
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
    fun getAddress(){
        val geocoder = Geocoder(this)
        val infoRef = database.getReference("usersInfo")
        val userRef = infoRef.child(uid.toString())
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var userInfo = snapshot.getValue<UsersInfo>()
                mylocation = userInfo!!.address.toString()
                mycor = geocoder.getFromLocationName(mylocation,1)
                mylat = mycor[0].latitude
                mylon = mycor[0].longitude
                setLastLocation(LatLng(mylat, mylon))
            }

        })
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
            setLastLocation(LatLng(mylat, mylon))
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            getAddress()
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
                productsList.clear()
                latlngList.clear()
                mClusterManager.clearItems()
                adapter.notifyDataSetChanged()
            }

            clusterItemClick(mMap)
            clusterClick(mMap)
            var i:Int =0
            db.collection("delivery")
                .get()
                .addOnSuccessListener { result ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    for(document in result){
                        //val cor = geocoder.getFromLocationName(document["place"] as String, 1)
                        //val id = document["id"] as String?
                        var item = document.toObject(DeliveryDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(document.id)
                        //var location = LatLng(cor[0].latitude, cor[0].longitude)
                        //addLatLngData(i,id, location)
                        i++
                    }
                }
            boundmap()
            setLastLocation(LatLng(mylat, mylon))

        } catch (e:Exception){

        }
    }
    fun setLastLocation(lastLocation : LatLng){
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
    private fun moveCamera(map: GoogleMap, marker: Marker){
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
    fun boundmap(){
        val bounds : LatLngBounds = builder.build()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, ZoomLevel))
        val zoom : Float = mMap.cameraPosition.zoom - 0.5f
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom))
    }
    fun addLatLngData(index : Int, id : String?, latlng : LatLng){
        val data = LatLngData(index, id, latlng)
        latlngList.add(data)
        if(size_check ==0){
            Handler(Looper.getMainLooper()).post{mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(data.latlng,12F))}
        }
        mClusterManager.addItem(data)
        builder.include(data.latlng)
        size_check++
        if(size_check == 100){
            Handler(Looper.getMainLooper()).post{boundmap()}
        }
        cameraInit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    fun getExtra(){
    }
    private fun clusterClick(mMap : GoogleMap){
        mClusterManager.setOnClusterClickListener { cluster ->
            val builder_c : LatLngBounds.Builder = LatLngBounds.builder()
            productsList.clear()
            for(item in cluster.items){
                if(item != null){
                    builder_c.include(item.position)
                    var id = contentDTOs[item.index].delivery_uid as String
                    var store = contentDTOs[item.index].store as String
                    var place = contentDTOs[item.index].name as String
                    var orderprice = contentDTOs[item.index].order_price.toString()
                    var deliverprice = contentDTOs[item.index].delivery_price.toString()
                    var detail = contentDTOs[item.index].delivery_detail.toString()
                    var Listid = contentUidList[item.index] as String
                    var category = contentDTOs[item.index].category as String
                    var participationCount = contentDTOs[item.index].delivery_ParticipationCount as String
                    var address = contentDTOs[item.index].delivery_address as String
                    var timestamp = contentDTOs[item.index].delivery_timestamp as String
                    var email = contentDTOs[item.index].delivery_userId as String
                    var delivery = contentDTOs[item.index].delivery as String
                    val uidkey = contentDTOs[item.index].deliveryParticipation.containsKey(uid).toString()
                    val imageurl = contentDTOs[item.index].imageURL.toString()
                    var product = DeliveryData(id, store, place, orderprice, deliverprice, detail, Listid,
                    category, participationCount, address, timestamp, email, delivery, uidkey, imageurl)
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
            var id = contentDTOs[p0.index].delivery_uid as String
            var name = contentDTOs[p0.index].name as String
            var store = contentDTOs[p0.index].store as String
            var orderprice = contentDTOs[p0.index].order_price.toString()
            var deliverprice = contentDTOs[p0.index].delivery_price.toString()
            var detail = contentDTOs[p0.index].delivery_detail.toString()
            var location = contentDTOs[p0.index].delivery_address.toString()
            var cor = geocoder.getFromLocationName(location, 1)
            var category = contentDTOs[p0.index].category.toString()
            var participationCount = contentDTOs[p0.index].delivery_ParticipationCount.toString()
            var address = contentDTOs[p0.index].delivery_address.toString()
            var timestamp = contentDTOs[p0.index].delivery_timestamp.toString()
            var email = contentDTOs[p0.index].delivery_userId.toString()
            var delivery = contentDTOs[p0.index].delivery.toString()
            var Listid = contentDTOs[p0.index].toString()
            var uidkey = contentDTOs[p0.index].deliveryParticipation.containsKey(uid).toString()
            var imageurl = contentDTOs[p0.index].imageURL.toString()
            var product = DeliveryData(
                id, store, name, orderprice, deliverprice, detail,
                Listid, category, participationCount, address, timestamp, email, delivery, uidkey, imageurl
            )
            productsList.add(product)
            adapter.notifyDataSetChanged()
            Log.d("qweqwe","wqe")
            //changeRenderer(p0)
            true
        }

    }

    fun search(searchWord : String){
        mClusterManager.clearItems()
        productsList.clear()
        latlngList.clear()
        firestore?.collection("delivery")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            i=0
            for(snapshot in querySnapshot!!.documents){
                var item = snapshot.toObject(DeliveryDTO::class.java)
                if(snapshot.getString("store")?.contains(searchWord)==true){
                    var id = item!!.delivery_uid as String
                    var name = item.name as String
                    var store = item.store as String
                    var orderprice = item.order_price.toString()
                    var deliverprice = item.delivery_price.toString()
                    var detail = item.delivery_detail.toString()
                    var location = item.delivery_address.toString()
                    var cor = geocoder.getFromLocationName(location, 1)
                    var category = item.category.toString()
                    var participationCount = item.delivery_ParticipationCount.toString()
                    var address = item.delivery_address.toString()
                    var timestamp = item.delivery_timestamp.toString()
                    var email = item.delivery_userId.toString()
                    var delivery = item.delivery.toString()
                    var Listid = snapshot.id
                    var uidkey = item.deliveryParticipation.containsKey(uid).toString()
                    var imageurl = item.imageURL.toString()
                    var product = DeliveryData(
                        id, store, name, orderprice, deliverprice, detail,
                        Listid, category, participationCount, address, timestamp, email, delivery, uidkey, imageurl
                    )
                    productsList.add(product)
                    var lat = cor[0].latitude
                    var lng = cor[0].longitude
                    var loc: LatLng = LatLng(lat, lng)
                    addLatLngData(i, id, loc)
                }
                i++
            }
            adapter.notifyDataSetChanged()
        }
    }
    inner class MarkerClusterRenderer(context : Context?, map : GoogleMap?, clusterManager: ClusterManager<LatLngData>?)
        : DefaultClusterRenderer<LatLngData>(context, map, clusterManager){
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
            return cluster != null && cluster.size >= 2
        }
    }

}