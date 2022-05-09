package com.techtown.matchingservice

import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.fragment1RecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binding.fragment1RecyclerView.adapter = Fragment1RecyclerviewAdapter()
        binding.fragment1RecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root

    }
    inner class CustomViewHolder(var binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class Fragment1RecyclerviewAdapter() : RecyclerView.Adapter<CustomViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")
                ?.orderBy("timestamp")
                ?.addSnapshotListener { value, error ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    if (value?.documents != null) {
                        for (snapshot in value!!.documents) {
                            var item = snapshot.toObject(ContentDTO::class.java)
                            var lat = item!!.location.latitude
                            var lon = item!!.location.longitude
                            var distance =
                                DistanceManager.getDistance(mylat, mylon, lat, lon).toDouble()
                            if (distance <= 2000) {
                                contentDTOs.add(item!!)
                                contentUidList.add(snapshot.id)
                            }
                        }
                    }
                    notifyDataSetChanged()

                }
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
            viewHolder.productitemTextviewProductName.text = contentDTOs[position].product
            //place
            viewHolder.productitemTextviewPlace.text =
                (Integer.parseInt(contentDTOs[position].price.toString())/Integer.parseInt(contentDTOs[position].ParticipationTotal.toString())).toString() + "원 (" + contentDTOs[position].unit.toString() + "개)"
            //Photo
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .into(viewHolder.productItemPhoto)

            var participationCount: String = contentDTOs[position].ParticipationCount.toString()

            //viewHolder.productitemParticipation.text =
            //    "현재 " + participationCount + " / " + contentDTOs[position].ParticipationTotal.toString()
            //click
            viewHolder.productitemCardView.setOnClickListener {

                Intent(context, Product::class.java).apply {
                    putExtra("position", position.toString())
                    putExtra("product", contentDTOs[position].product)
                    putExtra("imageUrl", contentDTOs[position].imageUrl)
                    putExtra("price", contentDTOs[position].price.toString())
                    putExtra("totalNumber", contentDTOs[position].totalNumber.toString())
                    putExtra("cycle", contentDTOs[position].cycle.toString())
                    putExtra("unit", contentDTOs[position].unit.toString())
                    putExtra("URL", contentDTOs[position].url)
                    putExtra("place", contentDTOs[position].place)
                    putExtra("timestamp", contentDTOs[position].timestamp.toString())
                    putExtra("participationCount", participationCount)
                    putExtra("id", contentUidList[position])
                    putExtra("position", position.toString())
                    putExtra(
                        "uidkey",
                        contentDTOs[position].Participation.containsKey(uid).toString()
                    )
                    putExtra(
                        "participationTotal",
                        contentDTOs[position].ParticipationTotal.toString()
                    )
                    putExtra("id", contentUidList[position])
                    putExtra("Uid", contentDTOs[position].uid.toString())

                }.run { context?.startActivity(this) }
            }

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
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
}