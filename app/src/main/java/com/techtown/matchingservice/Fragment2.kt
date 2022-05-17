package com.techtown.matchingservice


import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.graphics.drawable.toDrawable
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.FoodItemBinding
import com.techtown.matchingservice.databinding.Fragment2Binding
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_2.*
import java.lang.Math.*
import kotlin.math.pow

class Fragment2 : Fragment() {
    private lateinit var binding: Fragment2Binding
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
    var deliverycheck: Int = 1
    var deliverycate: String = "전체"
    var shoppingcate: String = "전체"
    var mylat: Double = 0.0
    var mylon: Double = 0.0
    var mylocation: String = ""
    lateinit var mycor: List<Address>
    var delivery_lat: Double = 0.0
    var delivery_lon: Double = 0.0
    var delivery_location: String = ""
    lateinit var delivery_cor: List<Address>
    var isopen : String = "close"
    private var database =
        Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    lateinit var infoRef: DatabaseReference
    lateinit var drawerLayout: DrawerLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = Fragment2Binding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        geocoder = Geocoder(context)
        infoRef = database.getReference("usersInfo")
        val userRef = infoRef.child(uid.toString())
        drawerLayout = binding.drawerLayout
        val drawerView = binding.drawer
        var cate: String = arguments?.getString("category").toString()
        if (cate == "open") {
            if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
                drawerLayout.closeDrawers()
            }else{
                drawerLayout.openDrawer(Gravity.LEFT)
            }
        }
        drawerLayout.addDrawerListener(MyDrawerListener())


        if(drawerLayout.isDrawerOpen(Gravity.LEFT)==true){
            drawerLayout.visibility = View.VISIBLE;
            Toast.makeText(context, "VISIBLE", Toast.LENGTH_LONG).show()

        }else {
            drawerLayout.visibility =View.GONE;
            Toast.makeText(context, "GONE", Toast.LENGTH_LONG).show()
        }
        binding.categoryFrag2.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue<UsersInfo>()
                mylocation = userInfo!!.address.toString()
                mycor = geocoder.getFromLocationName(mylocation, 1)
                mylat = mycor[0].latitude
                mylon = mycor[0].longitude
            }
        })
        binding.button3.setOnClickListener {
            //val string = binding.edit.text
            /*if (string.isNullOrEmpty()) {
                Toast.makeText(context, "chip 이름을 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                binding.chipGroup2.addView(Chip(context).apply {
                    text = string
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))

                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#cdd9f1"))
                    chipStrokeWidth = 4f
                    setTextColor(
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    )
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { binding.chipGroup2.removeView(this) }
                })
            }*/
        }
        binding.fragment2ProductRegistration.setOnClickListener {
            Intent(context, FoodActivity::class.java).apply {
                putExtra("kind", "delivery".toString())

            }.run { context?.startActivity(this) }
        }
        binding.shop.setOnClickListener {
            Intent(context, FoodActivity::class.java).apply {
                putExtra("kind", "shop".toString())

            }.run { context?.startActivity(this) }
        }
        binding.menu2.setOnFloatingActionsMenuUpdateListener(object :
            FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
            override fun onMenuExpanded() {
                isopen = "open"
                binding.dark.setBackgroundColor(Color.parseColor("#80000000"))
                binding.dark.visibility = View.VISIBLE;
                binding.all.isEnabled = false;
                binding.button4.isEnabled = false;
                binding.button9.isEnabled = false;
                binding.button10.isEnabled = false;
                binding.button11.isEnabled = false;
                binding.chicken.isEnabled = false;
                binding.pizza.isEnabled = false;
                binding.bunsik.isEnabled = false;
                binding.desert.isEnabled = false;
                binding.meat.isEnabled = false;
                binding.fast.isEnabled = false;
                binding.delGita.isEnabled = false;
                binding.coupang.isEnabled = false;
                binding.emart.isEnabled = false;
                binding.marketkurly.isEnabled = false;
                binding.lotte.isEnabled = false;
                binding.bunga11.isEnabled = false;
                binding.gmarket.isEnabled = false;
                binding.auction.isEnabled = false;
                binding.gita.isEnabled = false;
                binding.all2.isEnabled = false;
            }

            override fun onMenuCollapsed() {
                isopen = "close"
                binding.dark.visibility = View.GONE;
                binding.drawerLayout.isEnabled = true;
                binding.all.isEnabled = true;
                binding.button4.isEnabled = true;
                binding.button9.isEnabled = true;
                binding.button10.isEnabled = true;
                binding.button11.isEnabled = true;
                binding.chicken.isEnabled = true;
                binding.pizza.isEnabled = true;
                binding.bunsik.isEnabled = true;
                binding.desert.isEnabled = true;
                binding.meat.isEnabled = true;
                binding.fast.isEnabled = true;
                binding.delGita.isEnabled = true;
                binding.coupang.isEnabled = true;
                binding.emart.isEnabled = true;
                binding.marketkurly.isEnabled = true;
                binding.lotte.isEnabled = true;
                binding.bunga11.isEnabled = true;
                binding.gmarket.isEnabled = true;
                binding.auction.isEnabled = true;
                binding.gita.isEnabled = true;
                binding.all2.isEnabled = true;
            }
        })
        binding.fragment2RecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
        /*binding.deliver.setOnClickListener {
            deliverycheck = 1
            binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.shopping.setOnClickListener {
            deliverycheck = 2
            binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }*/
        binding.all.setOnClickListener {
            deliverycheck = 1
            deliverycate = "전체"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)

        }
        binding.button4.setOnClickListener {
            deliverycheck = 1
            deliverycate = "한식"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.button9.setOnClickListener {
            deliverycheck = 1
            deliverycate = "중식"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.button10.setOnClickListener {
            deliverycheck = 1
            deliverycate = "일식"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.button11.setOnClickListener {
            deliverycheck = 1
            deliverycate = "양식"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.chicken.setOnClickListener {
            deliverycheck = 1
            deliverycate = "치킨"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.pizza.setOnClickListener {
            deliverycheck = 1
            deliverycate = "피자"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.bunsik.setOnClickListener {
            deliverycheck = 1
            deliverycate = "분식"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.desert.setOnClickListener {
            deliverycheck = 1
            deliverycate = "디저트"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.meat.setOnClickListener {
            deliverycheck = 1
            deliverycate = "고기"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.fast.setOnClickListener {
            deliverycheck = 1
            deliverycate = "패스트푸드"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.delGita.setOnClickListener {
            deliverycheck = 1
            deliverycate = "기타"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }

        binding.coupang.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "쿠팡"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.emart.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "이마트몰"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.marketkurly.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "마켓컬리"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.lotte.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "롯데ON"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.bunga11.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "11번가"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.gmarket.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "G마켓"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.auction.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "옥션"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.gita.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "기타"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.all2.setOnClickListener {
            deliverycheck = 2
            shoppingcate = "전체"
            binding.fragment2RecyclerView.adapter = Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }

        binding.fragment2RecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }
    private inner class MyDrawerListener() : DrawerLayout.DrawerListener{
        override fun onDrawerClosed(drawerView: View) {
            drawerView.visibility = View.GONE;
            drawerLayout.visibility = View.GONE;
        }

        override fun onDrawerOpened(drawerView: View) {
            drawerView.visibility = View.VISIBLE
            drawerLayout.visibility = View.VISIBLE;

        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

        }

        override fun onDrawerStateChanged(newState: Int) {

        }
    }
    inner class DeliveryViewHolder(var binding: FoodItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ShoppingViewHolder(var binding: FoodItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class Fragment2DeliveryRecyclerviewAdapter() :
        RecyclerView.Adapter<DeliveryViewHolder>() {

        var deliveryDTOs: ArrayList<DeliveryDTO> = arrayListOf()
        var deliveryUidList: ArrayList<String> = arrayListOf()

        init {
            if (deliverycheck == 1) {
                if (deliverycate == "전체") {
                    //Toast.makeText(context, "1", Toast.LENGTH_LONG).show()
                    firestore?.collection("delivery")
                        ?.orderBy("delivery_timestamp")
                        ?.addSnapshotListener { value, error ->
                            deliveryDTOs.clear()
                            deliveryUidList.clear()
                            if (value?.documents != null) {
                                for (snapshot in value!!.documents) {
                                    var item = snapshot.toObject(DeliveryDTO::class.java)
                                    //Toast.makeText(context, item!!.delivery_address.toString(), Toast.LENGTH_LONG).show()
                                    var location = item!!.delivery_address
                                    var cor = geocoder.getFromLocationName(location, 1)
                                    delivery_lat = cor[0].latitude
                                    delivery_lon = cor[0].longitude
                                    var distance = DistanceManager.getDistance(
                                        mylat,
                                        mylon,
                                        delivery_lat,
                                        delivery_lon
                                    ).toDouble()
                                    if (item!!.delivery) {
                                        if (distance <= 2000) {
                                            deliveryDTOs.add(item)
                                            deliveryUidList.add(snapshot.id)
                                        }
                                    }
                                }
                                deliveryDTOs.reverse()
                                deliveryUidList.reverse()
                                notifyDataSetChanged()
                            }

                        }

                } else {
                    firestore?.collection("delivery")
                        ?.orderBy("delivery_timestamp")
                        ?.addSnapshotListener { value, error ->
                            deliveryDTOs.clear()
                            deliveryUidList.clear()
                            if (value?.documents != null) {
                                for (snapshot in value!!.documents) {
                                    var item = snapshot.toObject(DeliveryDTO::class.java)
                                    var delivery_uid = item!!.delivery_uid
                                    var deliveryRef = infoRef.child(delivery_uid.toString())
                                    deliveryRef.addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            var deliveryInfo = snapshot.getValue<UsersInfo>()
                                            delivery_location = deliveryInfo!!.address.toString()
                                            delivery_cor =
                                                geocoder.getFromLocationName(delivery_location, 1)
                                            delivery_lat = delivery_cor[0].latitude
                                            delivery_lon = delivery_cor[0].longitude
                                        }
                                    })
                                    var distance = DistanceManager.getDistance(
                                        mylat,
                                        mylon,
                                        delivery_lat,
                                        delivery_lon
                                    ).toDouble()
                                    if (item!!.category == deliverycate) {
                                        if (distance <= 2000) {
                                            deliveryDTOs.add(item)
                                            deliveryUidList.add(snapshot.id)
                                        }
                                    }
                                }
                                deliveryDTOs.reverse()
                                deliveryUidList.reverse()
                                notifyDataSetChanged()
                            }

                        }
                }
            } else {
                if (shoppingcate == "전체") {
                    firestore?.collection("delivery")
                        ?.orderBy("delivery_timestamp")
                        ?.addSnapshotListener { value, error ->
                            deliveryDTOs.clear()
                            deliveryUidList.clear()
                            for (snapshot in value!!.documents) {
                                var item = snapshot.toObject(DeliveryDTO::class.java)
                                var delivery_uid = item!!.delivery_uid
                                var deliveryRef = infoRef.child(delivery_uid.toString())
                                deliveryRef.addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var deliveryInfo = snapshot.getValue<UsersInfo>()
                                        delivery_location = deliveryInfo!!.address.toString()
                                        delivery_cor =
                                            geocoder.getFromLocationName(delivery_location, 1)
                                        delivery_lat = delivery_cor[0].latitude
                                        delivery_lon = delivery_cor[0].longitude
                                    }
                                })
                                var distance = DistanceManager.getDistance(
                                    mylat,
                                    mylon,
                                    delivery_lat,
                                    delivery_lon
                                ).toDouble()
                                if (!item!!.delivery) {
                                    if (distance <= 2000) {
                                        deliveryDTOs.add(item)
                                        deliveryUidList.add(snapshot.id)
                                    }
                                }
                            }
                            deliveryDTOs.reverse()
                            deliveryUidList.reverse()
                            notifyDataSetChanged()
                        }

                } else {
                    firestore?.collection("delivery")
                        ?.orderBy("delivery_timestamp")
                        ?.addSnapshotListener { value, error ->
                            deliveryDTOs.clear()
                            deliveryUidList.clear()
                            for (snapshot in value!!.documents) {
                                var item = snapshot.toObject(DeliveryDTO::class.java)
                                var delivery_uid = item!!.delivery_uid
                                var deliveryRef = infoRef.child(delivery_uid.toString())
                                deliveryRef.addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var deliveryInfo = snapshot.getValue<UsersInfo>()
                                        delivery_location = deliveryInfo!!.address.toString()
                                        delivery_cor =
                                            geocoder.getFromLocationName(delivery_location, 1)
                                        delivery_lat = delivery_cor[0].latitude
                                        delivery_lon = delivery_cor[0].longitude
                                    }
                                })
                                var distance = DistanceManager.getDistance(
                                    mylat,
                                    mylon,
                                    delivery_lat,
                                    delivery_lon
                                ).toDouble()
                                if (item!!.category == shoppingcate) {
                                    if (distance <= 2000) {
                                        deliveryDTOs.add(item)
                                        deliveryUidList.add(snapshot.id)
                                    }
                                }
                            }
                            deliveryDTOs.reverse()
                            deliveryUidList.reverse()
                            notifyDataSetChanged()
                        }
                }

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
            var view =
                FoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DeliveryViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
            var viewHolder = holder.binding
            //store
            viewHolder.fooditemTextviewstore.text = deliveryDTOs[position].store
            //order price
            viewHolder.fooditemTextvieworderprice.text =
                deliveryDTOs[position].order_price.toString()
            //delivery price
            var pdel : Int = deliveryDTOs[position].delivery_price / 2
            viewHolder.fooditemTextviewdeliveryprice.text = pdel.toString()
            Glide.with(viewHolder.foodimage.context).load(deliveryDTOs[position]?.imageURL)
                .apply(RequestOptions().circleCrop())
                .into(viewHolder.foodimage)

            //click
                viewHolder.fooditemCardView.setOnClickListener {
                    if(isopen == "close"){
                        Intent(context, Delivery::class.java).apply {
                            putExtra("store", deliveryDTOs[position].store.toString())
                            putExtra("name", deliveryDTOs[position].name.toString())
                            putExtra("delivery", deliveryDTOs[position].delivery.toString())
                            putExtra("orderPrice", deliveryDTOs[position].order_price.toString())
                            putExtra("deliveryPrice", deliveryDTOs[position].delivery_price.toString())
                            putExtra("deliveryid", deliveryUidList[position])
                            putExtra("deliveryuid", deliveryDTOs[position].delivery_uid)
                            putExtra("detail", deliveryDTOs[position].delivery_detail)
                            putExtra("address", deliveryDTOs[position].delivery_address)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.run { context?.startActivity(this) }
                    }
                    else{

                    }
                }
        }

        override fun getItemCount(): Int {
            return deliveryDTOs.size
        }
    }

    object DistanceManager {
        private const val R = 6372.8 * 1000
        fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a =
                sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(
                    Math.toRadians(lat2)
                )
            val c = 2 * asin(sqrt(a))
            return (R * c).toInt()
        }
    }
}