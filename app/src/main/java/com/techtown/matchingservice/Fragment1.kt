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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.protobuf.Value
import com.techtown.matchingservice.databinding.Fragment1Binding
import com.techtown.matchingservice.databinding.ProductItemBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.UsersInfo
import kotlin.math.pow

class Fragment1 : Fragment() {
    private lateinit var binding: Fragment1Binding
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
    var mylat : Double =0.0
    var mylon : Double = 0.0
    var mylocation : String = ""
    lateinit var mycor : List<Address>
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var items = arrayOf(" 최신 순 ", " 가격 순 ")
    var items_filter = arrayOf(" 1km 이내 ", " 2km 이내 ", " 3km 이내 ")
    var contentList = mutableListOf<Triple<String, ContentDTO, Double>>()
    var filteringList = mutableListOf<Triple<String, ContentDTO, Double>>()
    //var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
    //var contentUidList: ArrayList<String> = arrayListOf()
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = Fragment1Binding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val geocoder = Geocoder(context)
        val infoRef = database.getReference("usersInfo")
        val userRef = infoRef.child(uid.toString())
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var userInfo = snapshot.getValue<UsersInfo>()
                mylocation = userInfo!!.address.toString()
                mycor = geocoder.getFromLocationName(mylocation,1)
                mylat = mycor[0].latitude
                mylon = mycor[0].longitude
            }

        })

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
            }

            override fun onMenuCollapsed() {
                binding.background.setBackgroundColor(Color.parseColor("#00000000"))
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
                        filteringList.sortBy { it.second.timestamp }
                        filteringList.reverse()
                    }
                    else -> {
                        filteringList.sortBy { it.second.price / it.second.ParticipationTotal }

                    }
                }
                binding.fragment1RecyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val myAdapter2 = context?.let { ArrayAdapter(it, R.layout.item_spinner, items_filter) }
        binding.spinnerFilter.adapter = myAdapter2

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
                binding.fragment1RecyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        firestore?.collection("images")
            ?.orderBy("timestamp")
            ?.addSnapshotListener { value, error ->
                //contentDTOs.clear()
                //contentUidList.clear()
                contentList.clear()
                if (value?.documents != null) {
                    for (snapshot in value!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        var lat = item!!.location.latitude
                        var lon = item!!.location.longitude
                        var distance =
                            DistanceManager.getDistance(mylat, mylon, lat, lon).toDouble()
                        if (distance <= 3000) {
                            //contentDTOs.add(item!!)
                            //contentUidList.add(snapshot.id)
                            contentList.add(Triple(snapshot.id, item, distance))
                        }
                    }
                    contentList.reverse()
                }
            }

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

            filtering(1000)
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
            viewHolder.productitemTextviewProductName.text = filteringList[position].second.product.toString()
            viewHolder.textNumofProduct.text = "1인당 " +filteringList[position].second.unit.toString() + "개"
            //place
            viewHolder.productitemTextviewPlace.text =
                (Integer.parseInt(filteringList[position].second.price.toString())/Integer.parseInt(filteringList[position].second.ParticipationTotal.toString())).toString() + "원"
            //Photo
            Glide.with(holder.itemView.context).load(filteringList[position].second.imageUrl)
                .into(viewHolder.productItemPhoto)

            var participationCount: String = filteringList[position].second.ParticipationCount.toString()
            var timeLong : Long? = filteringList[position].second.timestamp
            viewHolder.textTime.text = timeDiff(timeLong!!)

            //viewHolder.productitemParticipation.text =
            //    "현재 " + participationCount + " / " + contentDTOs[position].ParticipationTotal.toString()
            //click
            viewHolder.productitemCardView.setOnClickListener {

                Intent(context, Product::class.java).apply {
                    putExtra("position", position.toString())
                    putExtra("product", filteringList[position].second.product)
                    putExtra("imageUrl", filteringList[position].second.imageUrl)
                    putExtra("price", filteringList[position].second.price.toString())
                    putExtra("totalNumber", filteringList[position].second.totalNumber.toString())
                    putExtra("cycle", filteringList[position].second.cycle.toString())
                    putExtra("unit", filteringList[position].second.unit.toString())
                    putExtra("URL", filteringList[position].second.url)
                    putExtra("place", filteringList[position].second.place)
                    putExtra("timestamp", filteringList[position].second.timestamp.toString())
                    putExtra("participationCount", participationCount)
                    putExtra("id", filteringList[position].first)
                    putExtra("position", position.toString())
                    putExtra(
                        "uidkey",
                        filteringList[position].second.Participation.containsKey(uid).toString()
                    )
                    putExtra(
                        "participationTotal",
                        filteringList[position].second.ParticipationTotal.toString()
                    )
                    //putExtra("id", contentUidList[position])
                    putExtra("Uid", filteringList[position].second.uid.toString())

                }.run { context?.startActivity(this) }
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
                    msg = i.msg
                    break
                }
            }
        }
        return diffTime.toString() + msg
    }
}