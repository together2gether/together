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
import android.widget.*
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
//import com.techtown.matchingservice.databinding.ProductItemBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.UsersInfo
import kotlinx.android.synthetic.main.condition.*
import java.sql.Types.NULL
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : AppCompatActivity(), OnMapReadyCallback, ConditionDialog.OnDataPassListener{
    var search : String = "none"
    var condition : String = "none"
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
    var i=0
    lateinit var uid: String
    var List : ArrayList<ProductData> = ArrayList<ProductData>()
    var LatList : ArrayList<LatLngData> = ArrayList()
    var price : Int? = null
    var distance : Int? = null
    var day :  Int? = null
    var product :ArrayList<ContentDTO> = arrayListOf()
    var contentUidList: ArrayList<String?> = arrayListOf()
    var productUid : ArrayList<String> = arrayListOf()
    var productsList : ArrayList<ProductData> = arrayListOf<ProductData>()
    lateinit var adapter :ProductListAdapter
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
    override fun onDataPass(p1 : Float?, p2 : Float?, p3 : Float?) {
        price = p1!!.toInt()
        distance = p2!!.toInt()
        day = p3!!.toInt()
        condition()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        var selectbutton = findViewById<Button>(R.id.btn_select)
        selectbutton.setOnClickListener{
            val dialog = ConditionDialog()
            dialog.show(supportFragmentManager, "ConditionDialog")
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        uid = FirebaseAuth.getInstance().uid!!
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
                if(mycor != null){
                    mylat = mycor[0].latitude
                    mylon = mycor[0].longitude
                    if(mylat !=0.0 && ::mMap.isInitialized){
                        setLastLocation(LatLng(mylat, mylon))
                    }
                }
            }

        })
        adapter = ProductListAdapter(productsList)
        adapter.setItemClickListener(object :
            ProductListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val item = productsList[position]
                Intent(applicationContext, Product::class.java).apply {
                    putExtra("productid", productsList[position].Listid)

                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {applicationContext?.startActivity(this)}
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
                productsList.clear()
                latlngList.clear()
                mClusterManager.clearItems()
                adapter.notifyDataSetChanged()
                //setLastLocation(LatLng(mylat, mylon))
                //updateLocation()
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
                        //val cor = geocoder.getFromLocationName(document["place"] as String, 1)
                        //val id = document["id"] as String?
                        var item = document.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(document.id)
                        i++
                    }
                }
            setLastLocation(LatLng(mylat, mylon))
            boundmap()
            //updateLocation()

        } catch (e:Exception){

        }
    }

    @SuppressLint("MissingPermission")
    /*fun updateLocation(){
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
    }*/
    fun setLastLocation(lastLocation : LatLng){
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        val resources : Resources = this!!.resources
        val bitmap2 = BitmapFactory.decodeResource(resources,R.drawable.red_pin)
        val markerOptions = MarkerOptions().position(LATLNG).title("주소 상의 위치").icon(BitmapDescriptorFactory.fromBitmap(bitmap2))

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
            var participation = "현재 " + contentDTOs[p0.index].ParticipationCount.toString() + " / " + contentDTOs[p0.index].ParticipationTotal.toString()
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
    fun condition(){
        var n=0
        condition = "condition"
        for(z in latlngList){
            mClusterManager.removeItem(z)
        }
        mClusterManager.clearItems()
        List.clear()
        LatList.clear()
        if(search == "search"){
            List.addAll(productsList)
            LatList.addAll(latlngList)
            latlngList.clear()
            productsList.clear()
            i=0
            var j =0
            for(item in List){
                var product_price = (item.price.toInt()/item.participationTotal.toInt())
                var product_day = item.unit.toInt()
                var location = item.place
                var cor = geocoder.getFromLocationName(location,1)
                var pro_lat = cor[0].latitude
                var pro_lon = cor[0].longitude
                var product_distance =
                    Fragment2.DistanceManager.getDistance(pro_lat, pro_lon, mylat, mylon)
                if(price!!.toInt() >= product_price && day!!.toInt() >= product_day && distance!!.toInt() >= product_distance){
                    n++
                    var product = ProductData(
                        item.userId, item.name, item.place, item.imageUri, item.participation,
                        item.price, item.totalNumber, item.cycle, item.unit, item.url,
                        item.uid, item.timestamp, item.participationCount,
                        item.uidKey, item.participationTotal, item.Listid
                    )
                    productsList.add(product)
                    var loc :LatLng = LatLng(pro_lat, pro_lon)
                    addLatLngData(LatList[j].index, item.Listid, loc)
                }
                i++
                j++
            }
            if(n==0){
                Toast.makeText(this, "조건에 맞는 상품이 없습니다.",Toast.LENGTH_LONG).show()
            }
            adapter.notifyDataSetChanged()
        }
        else{
            latlngList.clear()
            productsList.clear()
            i=0
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    var id = item!!.userId as String
                    var product_price = (item.price.toInt()/item.ParticipationTotal.toInt())
                    var product_day = item.unit.toInt()
                    var pro_lat = item.location.latitude
                    var pro_lon = item.location.longitude
                    var product_distance =
                        Fragment2.DistanceManager.getDistance(pro_lat, pro_lon, mylat, mylon)
                    if (price!!>= product_price && day!! >= product_day && distance!! >= product_distance) {
                        n++
                        var name = item.product as String
                        var place = item.place as String
                        var image = item.imageUrl as String
                        val participation =
                            "현재 " + item.ParticipationCount.toString() + " / " + item.ParticipationTotal.toString()
                        var price = item.price.toString()
                        var totalNumber = item.totalNumber.toString()
                        var cycle = item.cycle.toString()
                        var unit = item.unit.toString()
                        var url = item.url as String
                        var uid = item.uid as String
                        var timestamp = item.timestamp.toString()
                        var participationCount = item.ParticipationCount.toString()
                        var uidkey = item.Participation.containsKey(uid).toString()
                        var participationTotal = item.ParticipationTotal.toString()
                        var Listid = snapshot.id as String
                        var product = ProductData(
                            id,
                            name,
                            place,
                            image,
                            participation,
                            price,
                            totalNumber,
                            cycle,
                            unit,
                            url,
                            uid,
                            timestamp,
                            participationCount,
                            uidkey,
                            participationTotal,
                            Listid
                        )
                        productsList.add(product)
                        var lat = item.location.latitude
                        var lng = item.location.longitude
                        var location: LatLng = LatLng(lat, lng)
                        //boundmap()
                        addLatLngData(i, Listid, location)
                    }
                    i++
                }
                if(n==0) {
                    Toast.makeText(this, "조건에 맞는 상품이 없습니다.", Toast.LENGTH_LONG).show()
                }
                adapter.notifyDataSetChanged()
            }

        }
    }
    fun search(searchWord : String){
        var j=0
        search = "search"
        mClusterManager.clearItems()
        latlngList.clear()
        productsList.clear()
        if(condition == "condition"){
            i=0
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    if(snapshot.getString("product")?.contains(searchWord)==true){
                        var product_price = item!!.price.toInt()/item!!.ParticipationTotal
                        var product_day = item.unit.toInt()
                        var pro_lat = item.location.latitude
                        var pro_lon = item.location.longitude
                        var product_distance =
                            Fragment2.DistanceManager.getDistance(pro_lat, pro_lon, mylat, mylon)
                        if (price!!.toInt() >= product_price && day!!.toInt() >= product_day && distance!!.toInt() >= product_distance) {
                            j++
                            var item = snapshot.toObject(ContentDTO::class.java)
                            var id = item!!.userId as String
                            var name = item.product as String
                            var place = item.place as String
                            var image = item.imageUrl as String
                            val participation =
                                "현재 " + item.ParticipationCount.toString() + " / " + item.ParticipationTotal.toString()
                            var price = item.price.toString()
                            var totalNumber = item.totalNumber.toString()
                            var cycle = item.cycle.toString()
                            var unit = item.unit.toString()
                            var url = item.url as String
                            var uid = item.uid as String
                            var timestamp = item.timestamp.toString()
                            var participationCount = item.ParticipationCount.toString()
                            var uidkey = item.Participation.containsKey(uid).toString()
                            var participationTotal = item.ParticipationTotal.toString()
                            var Listid = snapshot.id as String
                            var product = ProductData(
                                id,
                                name,
                                place,
                                image,
                                participation,
                                price,
                                totalNumber,
                                cycle,
                                unit,
                                url,
                                uid,
                                timestamp,
                                participationCount,
                                uidkey,
                                participationTotal,
                                Listid
                            )
                            productsList.add(product)
                            var lat = item.location.latitude
                            var lng = item.location.longitude
                            var location: LatLng = LatLng(lat, lng)
                            addLatLngData(i, Listid, location)
                        }
                    }
                    i++
                }
                if(j==0){
                    Toast.makeText(this, "해당 상품에 대한 정보가 없습니다.",Toast.LENGTH_LONG).show()
                }
                adapter.notifyDataSetChanged()
            }
        }
        else{
            i=0
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    if(snapshot.getString("product")?.contains(searchWord)==true){
                        j++
                        var id = item!!.userId as String
                        var name = item.product as String
                        var place = item.place as String
                        var image = item.imageUrl as String
                        val participation =
                            "현재 " + item.ParticipationCount.toString() + " / " + item.ParticipationTotal.toString()
                        var price = item.price.toString()
                        var totalNumber = item.totalNumber.toString()
                        var cycle = item.cycle.toString()
                        var unit = item.unit.toString()
                        var url = item.url as String
                        var uid = item.uid as String
                        var timestamp = item.timestamp.toString()
                        var participationCount = item.ParticipationCount.toString()
                        var uidkey = item.Participation.containsKey(uid).toString()
                        var participationTotal = item.ParticipationTotal.toString()
                        var Listid = snapshot.id as String
                        var product = ProductData(
                            id,
                            name,
                            place,
                            image,
                            participation,
                            price,
                            totalNumber,
                            cycle,
                            unit,
                            url,
                            uid,
                            timestamp,
                            participationCount,
                            uidkey,
                            participationTotal,
                            Listid
                        )
                        productsList.add(product)
                        var lat = item.location.latitude
                        var lng = item.location.longitude
                        var location= LatLng(lat, lng)
                        addLatLngData(i, Listid, location)
                    }
                    i++
                }
                if(j==0){
                    Toast.makeText(this, "해당 상품에 대한 정보가 없습니다.",Toast.LENGTH_LONG).show()
                }
                adapter.notifyDataSetChanged()
            }
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
            return cluster != null && cluster.size >= 2
        }
    }


}