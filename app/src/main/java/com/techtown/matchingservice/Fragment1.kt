package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.Fragment1Binding
import com.techtown.matchingservice.databinding.ProductItemBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.UsersInfo
import java.security.AccessController.getContext
import kotlin.math.pow
import com.techtown.matchingservice.MainActivity as MainActivity

class Fragment1 : Fragment() {
    private lateinit var binding: Fragment1Binding
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
    var mylat : Double = 0.0
    var mylon : Double = 0.0
    var mylocation : String = ""
    lateinit var mycor : List<Address>
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var items = arrayOf(" 최신 순 ", " 가격 순 ", " 거리 순 ")
    var items_filter = arrayOf(" 1km 이내 ", " 2km 이내 ", " 3km 이내 ")
    var contentList = mutableListOf<Triple<String, ContentDTO, Double>>()
    var contentList2 = mutableListOf<Triple<String, ContentDTO, Double>>()
    var filteringList = mutableListOf<Triple<String, ContentDTO, Double>>()
    lateinit var geocoder : Geocoder
    var dist =1000
    var isopen = "close"
    var item : ContentDTO? = null
    //var cor : String? = null
    var lat : Double? = null
    var lon : Double? = null

    //var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
    //var contentUidList: ArrayList<String> = arrayListOf()
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        //LoadingDialog(requireContext()).show()
        binding = Fragment1Binding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val infoRef = database.getReference("usersInfo")
        geocoder = Geocoder(context)
        val userRef = infoRef.child(uid.toString())

        firestore?.collection("images")
            ?.orderBy("timestamp")
            ?.addSnapshotListener { value, error ->
                //contentDTOs.clear()
                //contentUidList.clear()

                contentList.clear()
                if (value?.documents != null) {
                    //LoadingDialog(requireContext()).show()
                    for (snapshot in value!!.documents) {
                        item = snapshot.toObject(ContentDTO::class.java)

                        /*if (distance <= 3000!!.toInt()) {
                            //contentDTOs.add(item!!)
                            //contentUidList.add(snapshot.id)
                            contentList.add(Triple(snapshot.id, item, distance))
                        }*/
                        contentList.add(Triple(snapshot.id, item, null) as Triple<String, ContentDTO, Double>)
                        Log.e("product my loc", mylat.toString() + ", " + mylon.toString())
                        Log.e("product loc", lat.toString() + ", " + lon.toString())
                        //Log.e("product", item!!.place + ", " + distance.toString())
                    }
                    contentList.reverse()
                    //LoadingDialog(requireContext()).dismiss()
                }
            }


        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var userInfo = snapshot.getValue<UsersInfo>()
                mylocation = userInfo!!.address.toString()
                Log.e("mylocation", mylocation!!)
                mycor = geocoder.getFromLocationName(mylocation,1)
                mylat = mycor[0].latitude
                mylon = mycor[0].longitude
                Log.e("mylat", mylat.toString() + ", " + mylon.toString())
                contentList2.clear()
                for(content in contentList!!){
                    item = content.second
                    var cor = geocoder.getFromLocationName(item!!.place.toString(), 1)
                    lat = cor[0].latitude
                    lon = cor[0].longitude
                    var distance = Fragment2.DistanceManager.getDistance(mylat!!, mylon!!, lat!!, lon!!).toDouble()
                    contentList2.add(Triple(content.first, item, distance) as Triple<String, ContentDTO, Double>)
                    Log.e("product my loc", mylat.toString() + ", " + mylon.toString())
                    Log.e("product loc", lat.toString() + ", " + lon.toString())
                    Log.e("product", item!!.place + ", " + distance.toString())
                }
                /*firestore?.collection("images")
                    ?.orderBy("timestamp")
                    ?.addSnapshotListener { value, error ->
                        //contentDTOs.clear()
                        //contentUidList.clear()

                        contentList2.clear()
                        if (value?.documents != null) {
                            //LoadingDialog(requireContext()).show()
                            for (snapshot in value!!.documents) {
                                item = snapshot.toObject(ContentDTO::class.java)
                                var cor = geocoder.getFromLocationName(item!!.place.toString(), 1)
                                lat = cor[0].latitude
                                lon = cor[0].longitude
                                var distance = Fragment2.DistanceManager.getDistance(mylat!!, mylon!!, lat!!, lon!!).toDouble()
                                /*if (distance <= 3000!!.toInt()) {
                                    //contentDTOs.add(item!!)
                                    //contentUidList.add(snapshot.id)
                                    contentList.add(Triple(snapshot.id, item, distance))
                                }*/
                                contentList2.add(Triple(snapshot.id, item, distance) as Triple<String, ContentDTO, Double>)
                                Log.e("product my loc", mylat.toString() + ", " + mylon.toString())
                                Log.e("product loc", lat.toString() + ", " + lon.toString())
                                Log.e("product", item!!.place + ", " + distance.toString())
                            }
                            //LoadingDialog(requireContext()).dismiss()
                        }
                    }*/
            }

        })





        //LoadingDialog(requireContext()).show()
        //LoadingDialog(requireContext()).dismiss()

        //LoadingDialog(requireContext()).dismiss()
        binding.fragment1ProductRegistration.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java)
            startActivity(intent)
        }
        binding.low.setOnClickListener {
            val lowpriceitemIntent = Intent(context, RecommendActivity::class.java)
            startActivity(lowpriceitemIntent)

        }
        binding.menu.setOnFloatingActionsMenuUpdateListener(object: FloatingActionsMenu.OnFloatingActionsMenuUpdateListener{
            override fun onMenuExpanded() {
                binding.background.setBackgroundColor(Color.parseColor("#80000000"))
                isopen = "open"
            }

            override fun onMenuCollapsed() {
                binding.background.setBackgroundColor(Color.parseColor("#00000000"))
                isopen = "close"
            }
        })


        binding.button3.setOnClickListener {
            val newintent = Intent(context, CityspinnerActivity::class.java)
            startActivity(newintent)
            //val string = binding.edit.text
            /*if (string.isNullOrEmpty()) {
                Toast.makeText(context, "chip 이름을 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                binding.chipGroup.addView(Chip(context).apply {
                    text = string
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#cdd9f1"))
                    chipStrokeWidth = 4f
                    setTextColor(
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    )
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { binding.chipGroup.removeView(this) }
                })
            }*/
        }


        val myAdapter = context?.let { ArrayAdapter(it, R.layout.item_spinner, items) }
        binding.sort.adapter = myAdapter

        binding.sort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(p2){
                    0 -> {
                        //filteringList.sortBy { it.second.timestamp }
                        //filteringList.reverse()
                        contentList.sortBy { it.second.timestamp }
                        contentList.reverse()
                    }
                    1 -> {
                        //filteringList.sortBy { it.second.price / it.second.ParticipationTotal }
                        contentList.sortBy { it.second.price / it.second.ParticipationTotal }
                    }
                    2->{
                        contentList2.sortBy { it.third }
                        contentList = contentList2
                    }
                }
                binding.fragment1RecyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        //val myAdapter2 = context?.let { ArrayAdapter(it, R.layout.item_spinner, items_filter) }
        //binding.spinnerFilter.adapter = myAdapter2
        //filtering(1000)
        /*binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0->{
                        filtering(1000)
                    }
                    1->{
                        filtering(2000)
                    }
                    2->{
                        filtering(3000)
                    }
                }
                if(binding.sort.selectedItemPosition == 0){
                    filteringList.sortBy { it.second.timestamp }
                    filteringList.reverse()
                } else {
                    filteringList.sortBy { it.second.price / it.second.ParticipationTotal }
                }
                binding.fragment1RecyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }*/

        binding.fragment1RecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binding.fragment1RecyclerView.adapter = Fragment1RecyclerviewAdapter()
        binding.fragment1RecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root

    }

    fun filtering(dist : Int? = 1000){
        filteringList.clear()
        for(i in contentList){
            if(i.third <= dist!!){
                filteringList.add(i)
            }
        }

    }
    inner class CustomViewHolder(var binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class Fragment1RecyclerviewAdapter() : RecyclerView.Adapter<CustomViewHolder>() {
        init {
            //filtering(1000)
            contentList.sortBy { it.second.timestamp }
            contentList.reverse()
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var view =
                ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

            var viewHolder = holder.binding
            //UserId
            //ProductName
            viewHolder.productitemTextviewProductName.text = contentList[position].second.product.toString()
            viewHolder.textNumofProduct.text = "1인당 " +contentList[position].second.unit.toString() + "개"
            //place
            viewHolder.productitemTextviewPlace.text =
                (Integer.parseInt(contentList[position].second.price.toString())/Integer.parseInt(contentList[position].second.ParticipationTotal.toString())).toString() + "원"
            //Photo
            Glide.with(holder.itemView.context).load(contentList[position].second.imageUrl)
                .into(viewHolder.productItemPhoto)

            var participationCount: String = contentList[position].second.ParticipationCount.toString()
            var timeLong : Long? = contentList[position].second.timestamp
            viewHolder.textTime.text = timeDiff(timeLong!!)

            //viewHolder.productitemParticipation.text =
            //    "현재 " + participationCount + " / " + contentDTOs[position].ParticipationTotal.toString()
            //click
            viewHolder.productitemCardView.setOnClickListener {
                if(isopen == "open"){

                }else{
                    Intent(context, Product::class.java).apply {
                        putExtra("position", position.toString())
                        putExtra("product", contentList[position].second.product)
                        putExtra("imageUrl", contentList[position].second.imageUrl)
                        putExtra("price", contentList[position].second.price.toString())
                        putExtra("totalNumber", contentList[position].second.totalNumber.toString())
                        putExtra("cycle", contentList[position].second.cycle.toString())
                        putExtra("unit", contentList[position].second.unit.toString())
                        putExtra("URL", contentList[position].second.url)
                        putExtra("place", contentList[position].second.place)
                        putExtra("timestamp", contentList[position].second.timestamp.toString())
                        putExtra("participationCount", participationCount)
                        putExtra("id", contentList[position].first)
                        putExtra("position", position.toString())
                        putExtra(
                            "uidkey",
                            contentList[position].second.Participation.containsKey(uid).toString()
                        )
                        putExtra(
                            "participationTotal",
                            contentList[position].second.ParticipationTotal.toString()
                        )
                        //putExtra("id", contentUidList[position])
                        putExtra("Uid", contentList[position].second.uid.toString())

                    }.run { context?.startActivity(this) }
                }

            }


        }

        override fun getItemCount(): Int {
            return contentList.size
        }
    }
    object DistanceManager{
        private const val R = 6372.8 * 1000
        fun getDistance(lat1 : Double, lon1:Double, lat2:Double, lon2 : Double) : Int{
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2-lon1)
            val a = Math.sin(dLat / 2).pow(2.0) + Math.sin(dLon / 2)
                .pow(2.0)* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            val c = 2* Math.asin(Math.sqrt(a))
            return (R*c).toInt()
        }
    }
    enum class TimeValue(val value: Int, val maximum : Int, val msg : String){
        SEC(60,60,"분 전"),
        MIN(60,24,"시간 전"),
        HOUR(24,30,"일 전"),
        DAY(30,12,"달 전"),
        MONTH(12,Int.MAX_VALUE,"년 전")
    }

    fun timeDiff(time : Long): String? {
        val curTime = System.currentTimeMillis()
        var diffTime = (curTime- time)/1000
        var msg:String? = null
        if(diffTime < TimeValue.SEC.value)
            msg = "방금 전"
        else {
            for(i in TimeValue.values()){
                diffTime /= i.value
                if(diffTime < i.maximum){
                    msg = diffTime.toString() + i.msg
                    break
                }
            }
        }
        return msg
    }
}