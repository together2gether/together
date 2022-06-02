package com.techtown.matchingservice

import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.FoodInfoBinding
import com.techtown.matchingservice.model.ChatModel
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo
import com.techtown.matchingservice.util.FcmPush
import kotlinx.android.synthetic.main.food_info.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Delivery : AppCompatActivity() {
    private lateinit var binding: FoodInfoBinding
    lateinit var uid: String
    var deliveryid : String? = null
    var firestore: FirebaseFirestore? = null
    var deliveryuid : String? = null
    var item = DeliveryDTO()
    val db = Firebase.firestore
    val docRef = db.collection("delivery")
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = database.getReference("chatrooms")
    private val usersRef = database.getReference("usersInfo")
    var foodName : String? = null
    var mylat : Double= 0.0
    var mylng : Double = 0.0
    var yourlat : Double = 0.0
    var yourlng : Double = 0.0
    lateinit var yourcor : List<Address>
    lateinit var mycor : List<Address>
    var yourlocation : String = ""
    var mylocation : String = ""
    var nickname : String? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.food_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        deliveryid = intent.getStringExtra("deliveryid")

        binding.foodInfoBack.setOnClickListener(){
            finish()
        }
        val infoRef = database.getReference("usersInfo")
        val userRef = infoRef.child(uid)


        docRef.document("$deliveryid").get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(DeliveryDTO::class.java)!!
                    if(item.delivery_uid == uid) {
                        binding.foodInfoChat.setVisibility(View.INVISIBLE)
                        binding.foodInfoCancel.setVisibility(View.INVISIBLE)
                        binding.foodInfoParticipation.setVisibility(View.INVISIBLE)
                        binding.foodInfoParticipation.isEnabled = false;
                        binding.foodInfoRevice.setVisibility(View.VISIBLE)
                        binding.foodInfoGarbage.setVisibility(View.VISIBLE)
                    }
                    else{
                        binding.foodInfoChat.setVisibility(View.VISIBLE)
                        binding.foodInfoParticipation.setVisibility(View.VISIBLE)
                        //binding.foodInfoParticipation.isEnabled = true;
                        binding.foodInfoRevice.setVisibility(View.INVISIBLE)
                        binding.foodInfoGarbage.setVisibility(View.INVISIBLE)
                        if(item.deliveryParticipation[uid] == true){
                            binding.foodInfoParticipation.visibility = View.INVISIBLE
                            if(deliveryuid == uid){
                                binding.foodInfoCancel.visibility = View.INVISIBLE
                            }else {
                                binding.foodInfoCancel.visibility = View.VISIBLE
                            }
                        }
                    }
                    if(item.delivery_ParticipationCount == 2 ) binding.foodInfoParticipation.isEnabled = false
                    val time = item.delivery_timestamp
                    val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
                    val timeStr = dateFormat.format(Date(time!!)).toString()
                    foodregisterTime.setText(timeStr)

                    if(!item.delivery){
                        binding.foodInfoName.visibility = View.GONE
                    }
                    binding.foodInfoStore.text = item.store
                    binding.foodInfoName.text = item.name
                    binding.foodInfoOrderprice.text = item.order_price.toString()
                    binding.foodInfoDeliveryprice.text = item.delivery_price.toString()
                    binding.foodinfoDeliverydetail.text = item.delivery_detail
                    deliveryuid = item.delivery_uid
                    deliveryid = document.id
                    yourlocation = item.delivery_address.toString()
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                    val geocoder = Geocoder(this)

                    userRef.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            try{
                                var userInfo = snapshot.getValue<UsersInfo>()
                                nickname = userInfo!!.nickname.toString()
                                mylocation = userInfo.address.toString()
                                mycor = geocoder.getFromLocationName(mylocation,1)
                                mylat = mycor[0].latitude
                                mylng = mycor[0].longitude
                                yourcor = geocoder.getFromLocationName(yourlocation, 1)
                                yourlat = yourcor[0].latitude
                                yourlng = yourcor[0].longitude
                                val lat = ((mylat+ yourcor[0].latitude)/2).toString()
                                val lng = ((mylng + yourcor[0].longitude)/2).toString()
                                binding.recommend.setOnClickListener {
                                    Intent(applicationContext, RecommandLocation::class.java).apply {
                                        putExtra("mylat", mylat.toString())
                                        putExtra("mylng", mylng.toString())
                                        putExtra("yourlat", yourlat.toString())
                                        putExtra("yourlng", yourlng.toString())
                                        putExtra("lat", lat)
                                        putExtra("lng", lng)
                                        putExtra("delivery", "delivery")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }.run {applicationContext?.startActivity(this)}
                                }
                            }catch(e : IOException){
                                e.printStackTrace()

                            }                }



                    })
                    val myuid = item.delivery_uid
                    if(myuid == uid){
                        binding.recommend.visibility = View.GONE
                    }
                    usersRef.child(deliveryuid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userInfo = snapshot.getValue<UsersInfo>()
                            if(userInfo != null){
                                if(userInfo.profileImageUrl.toString() != ""){
                                    Glide.with(food_register_profile.context).load(userInfo.profileImageUrl)
                                        .apply(RequestOptions().circleCrop())
                                        .into(food_register_profile)
                                }
                                foodregisterUserName.setText(userInfo.nickname.toString())
                            }
                        }
                    })
                }
            }




        
        binding.foodInfoCancel.setOnClickListener {
            docRef.document(deliveryid.toString()).get()
                .addOnSuccessListener { document ->
                    var thisItem = document.toObject(DeliveryDTO::class.java)!!
                    thisItem.deliveryParticipation[uid] = false
                    thisItem.delivery_ParticipationCount -= 1
                    var tsDoc = docRef.document(deliveryid.toString())
                    firestore?.runTransaction {
                        transition->
                        transition.set(tsDoc, thisItem)
                    }
                }
            roomsRef.orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (room in snapshot.children){
                        val chatroom = room.getValue<ChatModel>()
                        if(chatroom?.productid == deliveryid.toString()){
                            var roomId = room.key
                            roomsRef.child(roomId.toString()).removeValue()
                        }
                    }
                }
            })
            binding.foodInfoCancel.visibility = View.INVISIBLE
            binding.foodInfoParticipation.visibility = View.VISIBLE
        }


        binding.foodInfoParticipation.setOnClickListener(){

            var str =binding.foodInfoStore.text.toString()+" 구매에 " + nickname + "님이 참여하셨습니다."
            FcmPush.instance.sendMessage(deliveryuid!!,"공동구매 참여", str)

            item.delivery_ParticipationCount+=1
            item.deliveryParticipation[uid] = true
            var tsDoc = firestore?.collection("delivery")?.document(deliveryid.toString())
            firestore?.runTransaction{
                    transition->
                transition.set(tsDoc!!,item)
            }
            binding.foodInfoParticipation.visibility = View.INVISIBLE
            binding.foodInfoCancel.visibility = View.VISIBLE
            //binding.foodInfoParticipation.isEnabled=false
            var roomId : String? = null
            val chatModel = ChatModel()
            chatModel.users.put(deliveryuid.toString(), true)
            chatModel.users.put(uid, true)
            chatModel.productid = deliveryid
            chatModel.delivery = true
            roomsRef.push().setValue(chatModel)

            roomsRef.orderByChild("users/$uid").equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val time = System.currentTimeMillis()
                        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
                        val curTime = dateFormat.format(Date(time)).toString()
                        val comment = ChatModel.Comment(deliveryuid.toString(), "안녕하세요. 이 곳은 '$foodName' 공동구매를 위한 채팅방 입니다.", curTime, time)

                        for (room in snapshot.children){
                            val chatModel = room.getValue<ChatModel>()
                            if(chatModel?.productid == deliveryid){
                                roomId = room.key
                                roomsRef.child(roomId.toString()).child("comments").push().setValue(comment)
                            }
                        }
                    }
                })

            Intent(this, chatting::class.java).apply {
                putExtra("groupchat","DY")
                putExtra("productid", deliveryid.toString())
                putExtra("destinationUid", deliveryuid.toString())
            }.run { startActivity(this) }
        }

        binding.foodInfoChat.setOnClickListener {
            Intent(this, chatting::class.java).apply {
                putExtra("groupchat","N")
                putExtra("destinationUid", deliveryuid.toString())
            }.run { startActivity(this) }
        }

        binding.foodInfoRevice.setOnClickListener(){
            Intent(this, EditFood::class.java).apply{
                putExtra("deliveryid", deliveryid)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
            finish()
        }

        binding.foodInfoGarbage.setOnClickListener(){
            RemovePopup()
        }


    }
    private fun RemovePopup(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("삭제")
            .setMessage("이 게시물을 삭제하시겠습니까?")
            .setPositiveButton("예",
                DialogInterface.OnClickListener{ dialog, id->
                    var roomId : String? = null
                    roomsRef.orderByChild("users/$uid").equalTo(true)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(item in snapshot.children){
                                    val chatmodel = item.getValue<ChatModel>()
                                    if(chatmodel?.productid == deliveryid){
                                        roomId = item.key
                                        roomsRef.child(roomId.toString()).removeValue()
                                    }
                                }
                            }
                        })
                    db.collection("delivery").document("$deliveryid").delete()
                    finish()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{ dialog, id->
                })
        builder.show()
    }
}