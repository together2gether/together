package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.ProductInfoBinding
import com.techtown.matchingservice.model.ChatModel
import com.techtown.matchingservice.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.*


class Product : AppCompatActivity() {
    private lateinit var binding: ProductInfoBinding
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String
    var item = ContentDTO()
    var productid : String? = null
    var product_name : String? = null
    var regist_userid : String? = null
    private var databse = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = databse.getReference("chatrooms")

    val db = Firebase.firestore
    val docRef = db.collection("images")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.product_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        Glide.with(this).load(intent.getStringExtra("imageUrl").toString())
            .into(binding.productInfoPhoto)
        binding.productInfoProduct.text = intent.getStringExtra("product").toString()
        binding.productInfoTotal.text = intent.getStringExtra("totalNumber").toString()+"개 ( "+intent.getStringExtra("price").toString()+" 원 )"
        var price:Int = Integer.parseInt(intent.getStringExtra("price").toString())/Integer.parseInt(intent.getStringExtra("participationTotal").toString())
        binding.productInfoUnit.text =price.toString() + "원 / "+intent.getStringExtra("unit").toString()+"개"
        binding.productInfoURL.text = intent.getStringExtra("URL").toString()
        binding.productInfoPlace.text = intent.getStringExtra("place").toString()
        binding.productInfoCycle.text = intent.getStringExtra("cycle").toString()+"주"
        binding.productInfoParticipationNumber.text = intent.getStringExtra("participationCount").toString()+" / "+intent.getStringExtra("participationTotal").toString()
        regist_userid = intent.getStringExtra("Uid").toString()
        productid = intent.getStringExtra("id").toString()
        product_name = intent.getStringExtra("product").toString()

        val intent = Intent(this, chatting::class.java)

        docRef.document("$productid").get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(ContentDTO::class.java)!!
                    if(item?.Participation!!.containsKey(uid)) binding.productInfoParticipation.isEnabled=false
                    if(item?.ParticipationCount == item?.ParticipationTotal){
                        binding.productInfoParticipation.isEnabled=false
                    }
                }
            }

        binding.productInfoBack.setOnClickListener(){
            finish()
        }

        binding.buttonChat.setOnClickListener {
            intent.putExtra("groupchat", "N")
            intent.putExtra("destinationUid", regist_userid)
            startActivity(intent)
        }

        binding.productInfoParticipation.setOnClickListener(){

            item.ParticipationCount+=1
            item.Participation[uid] = true
            var tsDoc = firestore?.collection("images")?.document(productid.toString())
            firestore?.runTransaction{
                    transition->
                transition.set(tsDoc!!,item)
            }
            binding.productInfoParticipationNumber.text=item.ParticipationCount.toString()+" / "+item.ParticipationTotal
            var roomId : String? = null
            if(item.ParticipationCount == 2){
                val time = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
                val curTime = dateFormat.format(Date(time)).toString()

                val chatModel = ChatModel()
                chatModel.users.put(regist_userid.toString(), true)
                chatModel.users.put(uid, true)
                chatModel.productid = productid
                roomsRef.push().setValue(chatModel)
                val comment = ChatModel.Comment(regist_userid.toString(), "안녕하세요. 이 곳은 '$product_name' 공동구매를 위한 채팅방 입니다.", curTime)
                roomsRef.orderByChild("users/$uid").equalTo(true)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(item in snapshot.children){
                                val newChatModel = item.getValue<ChatModel>()
                                if(newChatModel?.productid == productid){
                                    roomId = item.key
                                    roomsRef.child(roomId.toString()).child("comments").push().setValue(comment)
                                }
                            }
                        }
                    })
            } else {
                roomsRef.child(roomId.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var chatModel = snapshot.getValue<ChatModel>()
                        chatModel?.users!!.put(uid, true)
                        roomsRef.child(roomId.toString()).setValue(chatModel)
                    }
                })
            }

            intent.putExtra("groupchat", "Y")
            intent.putExtra("productid", productid)
            startActivity(intent)
            binding.productInfoParticipation.isEnabled=false
        }
    }
}