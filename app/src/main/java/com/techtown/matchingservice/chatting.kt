package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.model.ChatModel
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo
import kotlinx.android.synthetic.main.chatting.*
import java.text.SimpleDateFormat
import java.util.*

class chatting : AppCompatActivity() {
    private var chatRoomUid : String? = null
    private var destinationUid : String? = null
    private var productid : String? = null
    private var groupchat : String? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = database.getReference("chatrooms")
    private val usersRef = database.getReference("usersInfo")
    val db = Firebase.firestore
    val docRef = db.collection("images")
    val delRef = db.collection("delivery")
    var mylocation : String = ""
    var yourlocation : String = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var firestore: FirebaseFirestore? = null
    private lateinit var databaseRef : DatabaseReference
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatting)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val imageView = findViewById<Button>(R.id.btn_input)
        val editText = findViewById<EditText>(R.id.editText_msg)
        val recommend = findViewById<ImageButton>(R.id.imageButton2)

        //메시지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()
        val button = findViewById<Button>(R.id.back_botton)
        button.setOnClickListener {
            finish()
        }
        val geocoder = Geocoder(this)
        groupchat = intent.getStringExtra("groupchat")
        uid = Firebase.auth.currentUser?.uid.toString()
        val userRef = usersRef.child(uid.toString())
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue<UsersInfo>()
                mylocation = userInfo!!.address.toString()
            }
        })
        recyclerView = findViewById(R.id.msg_recyclerview)
        if(groupchat == "N"){
            destinationUid = intent.getStringExtra("destinationUid")
            productid = ""
            val destinationRef = usersRef.child(destinationUid.toString())
            destinationRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val destinationInfo = snapshot.getValue<UsersInfo>()
                    yourlocation = destinationInfo!!.address.toString()
                    val mycor = geocoder.getFromLocationName(mylocation,1)
                    val yourcor = geocoder.getFromLocationName(yourlocation,1)
                    val mylat = mycor[0].latitude.toString()
                    val mylng = mycor[0].longitude.toString()
                    val yourlat = yourcor[0].latitude.toString()
                    val yourlng = yourcor[0].longitude.toString()
                    val lat = ((mycor[0].latitude + yourcor[0].latitude)/2).toString()
                    val lng = ((mycor[0].longitude + yourcor[0].longitude)/2).toString()

                    recommend.setOnClickListener {
                        Intent(applicationContext, RecommandLocation::class.java).apply {
                            putExtra("mylat", mylat)
                            putExtra("mylng", mylng)
                            putExtra("yourlat", yourlat)
                            putExtra("yourlng", yourlng)
                            putExtra("lat", lat)
                            putExtra("lng", lng)
                            if(chatRoomUid == null){
                                putExtra("roomId", "null")
                            } else {
                                putExtra("roomId", chatRoomUid.toString())
                            }
                            putExtra("destinationUid", destinationUid.toString())
                            putExtra("Uid", uid.toString())
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.run {applicationContext?.startActivity(this)}
                    }
                    textView_topName.setText(destinationInfo.nickname.toString())
                }
            })
        } else if (groupchat == "Y"){
            productid = intent.getStringExtra("productid")
            destinationUid = ""
        } else if(groupchat == "DY"){
            productid = intent.getStringExtra("productid")
            destinationUid = ""
        } else {
            Toast.makeText(this, "오류", Toast.LENGTH_LONG).show()
        }

        firestore = FirebaseFirestore.getInstance()

        checkChatRoom()

        imageView.setOnClickListener{
            //Log.d("클릭 시 dest", "$destinationUid")
            val chatModel = ChatModel()
            chatModel.users.put(uid.toString(), true)
            if(groupchat == "N"){
                chatModel.users.put(destinationUid!!, true)
                chatModel.productid = ""
            } else if(groupchat == "Y"){
                docRef.document("$productid").get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            var groupItem = document.toObject(ContentDTO::class.java)!!
                            for(users in groupItem!!.Participation.keys){
                                chatModel.users.put(users, true)
                            }
                            chatModel.productid = productid
                        }
                    }
            } else if(groupchat == "DY"){
                delRef.document("$productid").get()
                    .addOnSuccessListener { doc ->
                        if(doc != null){
                            var groupItem = doc.toObject(DeliveryDTO::class.java)!!
                            for(users in groupItem!!.deliveryParticipation.keys){
                                chatModel.users.put(users, true)
                            }
                            chatModel.productid = productid
                        }}
            }

            val comment = ChatModel.Comment(uid, editText.text.toString(), curTime)
            if(chatRoomUid == null){
                imageView.isEnabled = false
                roomsRef.push().setValue(chatModel).addOnSuccessListener {
                    //채팅방 생성
                    checkChatRoom()
                    //메시지 보내기
                    Handler().postDelayed({
                        roomsRef.child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                        editText.text = null
                    }, 1000L)
                    //Log.d("chatUidNull dest", "$destinationUid")
                }
            } else {
                roomsRef.child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                editText.text = null
                //Log.d("chatUidNotNull dest", "$destinationUid")
            }
        }

    }
    private fun checkChatRoom(){
        if(groupchat == "N"){
            val image = findViewById<ImageButton>(R.id.imageButton2)
            image.setVisibility(View.VISIBLE)
            roomsRef.orderByChild("users/$uid").equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(item in snapshot.children){
                            val chatModel = item.getValue<ChatModel>()
                            if(chatModel?.users!!.containsKey(destinationUid) && chatModel.productid == ""){
                                chatRoomUid = item.key
                                val button = findViewById<Button>(R.id.btn_input)
                                button.isEnabled = true
                                recyclerView?.layoutManager = LinearLayoutManager(this@chatting)
                                recyclerView?.adapter = RecyclerViewAdapter()
                            }
                        }
                    }
                })
        } else if(groupchat == "Y"){
            val image = findViewById<ImageButton>(R.id.imageButton2)
            image.setVisibility(View.GONE)
            roomsRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for(item in snapshot.children){
                        val chatModel = item.getValue<ChatModel>()
                        if(chatModel?.productid == productid){
                            chatRoomUid = item.key
                            val button = findViewById<Button>(R.id.btn_input)
                            button.isEnabled = true
                            recyclerView?.layoutManager = LinearLayoutManager(this@chatting)
                            recyclerView?.adapter = RecyclerViewAdapter()
                        }
                    }
                }
            })
        } else if(groupchat == "DY"){
            val image = findViewById<ImageButton>(R.id.imageButton2)
            image.setVisibility(View.GONE)
            roomsRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for(item in snapshot.children){
                        val chatModel = item.getValue<ChatModel>()
                        if(chatModel?.productid == productid){
                            chatRoomUid = item.key
                            val button = findViewById<Button>(R.id.btn_input)
                            button.isEnabled = true
                            recyclerView?.layoutManager = LinearLayoutManager(this@chatting)
                            recyclerView?.adapter = RecyclerViewAdapter()
                        }
                    }
                }
            })
        }
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>(){
        private val comments = ArrayList<ChatModel.Comment>()
        private var user : UsersInfo? = null
        init{

            if(groupchat == "N"){
                usersRef.child(destinationUid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        user = snapshot.getValue<UsersInfo>()
                        var topName = findViewById<TextView>(R.id.textView_topName)
                        topName.text = user?.nickname
                        getMessageList()
                    }
                })
            } else if(groupchat == "Y"){
                docRef.document("$productid").get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            var groupItem = document.toObject(ContentDTO::class.java)!!
                            var productname = groupItem.product.toString()
                            var topName = findViewById<TextView>(R.id.textView_topName)
                            topName.text = productname
                        }
                    }
                getMessageList()
            } else if(groupchat == "DY"){
                delRef.document("$productid").get()
                    .addOnSuccessListener { doc ->
                        if(doc != null){
                            var groupItem = doc.toObject(DeliveryDTO::class.java)!!
                            var deliveryname = groupItem.store.toString()
                            var topName = findViewById<TextView>(R.id.textView_topName)
                            topName.text = deliveryname
                        }
                    }
                getMessageList()
            }
        }
        fun getMessageList(){
            roomsRef.child(chatRoomUid.toString()).child("comments").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for(data in snapshot.children){
                        val item = data.getValue<ChatModel.Comment>()
                        comments.add(item!!)
                    }
                    notifyDataSetChanged()
                    //메시지를 보낼 시 화면을 맨 밑으로 내림
                    recyclerView?.scrollToPosition(comments.size -1)
                }
            })
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerViewAdapter.MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.message_left_item, parent, false)
            return MessageViewHolder(view)
        }

        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(
            holder: RecyclerViewAdapter.MessageViewHolder,
            position: Int
        ) {
            holder.textView_message.textSize = 16F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if(comments[position].uid.equals(uid)){
                holder.textView_message.setBackgroundResource(R.drawable.right_item_message)
                holder.layout_destination.visibility = View.INVISIBLE
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
                holder.layout_sub.gravity = Gravity.RIGHT
                holder.textView_time.gravity = Gravity.RIGHT
                //margin값 설정
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                holder.layout_main.layoutParams = layoutParams

                layoutParams.setMargins(0,0,50,0)
            } else {
                if(groupchat == "Y" || groupchat == "DY"){
                    usersRef.child(comments[position].uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            user = snapshot.getValue<UsersInfo>()
                            Glide.with(holder.itemView.context)
                                .load(user?.profileImageUrl)
                                .apply(RequestOptions().circleCrop())
                                .into(holder.imageView_profile)
                            holder.textView_name.text = user?.nickname

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                } else {
                    Glide.with(holder.itemView.context)
                        .load(user?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.imageView_profile)
                    holder.textView_name.text = user?.nickname
                }
                holder.layout_destination.visibility=View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.left_item_message)
                //holder.layout_main.gravity = Gravity.LEFT
                //margin값 설정
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                holder.layout_main.layoutParams = layoutParams
                layoutParams.setMargins(0,0,50,0)
            }
        }

        inner class MessageViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val textView_message: TextView = view.findViewById(R.id.tv_comment)
            val textView_name: TextView = view.findViewById(R.id.tv_name)
            val imageView_profile: ImageView = view.findViewById(R.id.iv_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.linear_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.linear_main)
            val layout_sub : LinearLayout = view.findViewById(R.id.layout_sub)
            val textView_time: TextView = view.findViewById(R.id.tv_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }


    }
}
