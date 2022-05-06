package com.techtown.matchingservice

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import com.techtown.matchingservice.model.UsersInfo
import kotlinx.android.synthetic.main.product_info.*
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
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = database.getReference("chatrooms")
    private val usersRef = database.getReference("usersInfo")

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
        binding.productInfoTotal.text = "총 " +intent.getStringExtra("totalNumber").toString() + " 개"
        var price:Int = Integer.parseInt(intent.getStringExtra("price").toString())/Integer.parseInt(intent.getStringExtra("participationTotal").toString())
        binding.productInfoUnit.text =price.toString() + "원 ( "+intent.getStringExtra("unit").toString()+ " 개 )"
        binding.productInfoURL.text = intent.getStringExtra("URL").toString()
        binding.productInfoPlace.text = intent.getStringExtra("place").toString()
        binding.productInfoCycle.text = intent.getStringExtra("cycle").toString()
        binding.productInfoParticipationNumber.text = intent.getStringExtra("participationCount").toString()+" / "+intent.getStringExtra("participationTotal").toString()
        regist_userid = intent.getStringExtra("Uid").toString()
        productid = intent.getStringExtra("id").toString()
        product_name = intent.getStringExtra("product").toString()

        usersRef.child(regist_userid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue<UsersInfo>()
                if(userInfo != null){
                    if(userInfo!!.profileImageUrl.toString() != ""){
                        Glide.with(product_register_profile.context).load(userInfo?.profileImageUrl)
                            .apply(RequestOptions().circleCrop())
                            .into(product_register_profile)

                    }
                    ProductregisterUserName.setText(userInfo.nickname.toString())
                }
            }
        })

        if(regist_userid == uid){
            binding.buttonChat.setVisibility(View.INVISIBLE)
            binding.productInfoParticipation.setVisibility(View.INVISIBLE)
            binding.productInfoParticipationuser.setVisibility(View.VISIBLE)
            binding.productInfoGarbage.setVisibility(View.VISIBLE)
            binding.productInfoRevice.setVisibility(View.VISIBLE)

        }else{
            binding.buttonChat.setVisibility(View.VISIBLE)
            binding.productInfoParticipation.setVisibility(View.VISIBLE)
            binding.productInfoParticipationuser.setVisibility(View.INVISIBLE)
            binding.productInfoGarbage.setVisibility(View.INVISIBLE)
            binding.productInfoRevice.setVisibility(View.INVISIBLE)
        }

        binding.productInfoRevice.setOnClickListener(){
            Intent(this, EditProduct::class.java).apply{
                putExtra("product", binding.productInfoProduct.text)
                putExtra("imageUrl", intent.getStringExtra("imageUrl").toString())
                putExtra("price", intent.getStringExtra("price").toString())
                putExtra("totalNumber", intent.getStringExtra("totalNumber").toString())
                putExtra("cycle", binding.productInfoCycle.text)
                putExtra("unit", intent.getStringExtra("unit").toString())
                putExtra("URL", binding.productInfoURL.text)
                putExtra("place", binding.productInfoPlace.text)
                putExtra("id", productid)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
            finish()
        }

        val intent = Intent(this, chatting::class.java)

        docRef.document("$productid").get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(ContentDTO::class.java)!!
                    if(item?.Participation?.get(uid) == true) binding.productInfoParticipation.isEnabled=false
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

        binding.productInfoParticipationuser.setOnClickListener(){
            val dialog = ParticipantDialog()
            var bundle = Bundle()
            bundle.putString("productid",productid)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "ParticipantDialog")

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
                val chatModel = ChatModel()
                chatModel.users.put(regist_userid.toString(), true)
                chatModel.users.put(uid, true)
                chatModel.productid = productid
                roomsRef.push().setValue(chatModel)

                roomsRef.orderByChild("users/$uid").equalTo(true)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val time = System.currentTimeMillis()
                            val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
                            val curTime = dateFormat.format(Date(time)).toString()
                            val comment = ChatModel.Comment(regist_userid.toString(), "안녕하세요. 이 곳은 '$product_name' 공동구매를 위한 채팅방 입니다.", curTime)

                            for(room in snapshot.children){
                                val chatmodel = room.getValue<ChatModel>()
                                if(chatmodel?.productid == productid){
                                    roomId = room.key
                                    roomsRef.child(roomId.toString()).child("comments").push().setValue(comment)
                                }
                            }
                        }
                    })
            } else {
                roomsRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(room in snapshot.children){
                            val chatmodel = room.getValue<ChatModel>()
                            if(chatmodel?.productid == productid){
                                roomId = room.key
                                chatmodel?.users!!.put(uid, true)
                                roomsRef.child(roomId.toString()).setValue(chatmodel)
                            }
                        }
                    }
                })
            }

            intent.putExtra("groupchat", "Y")
            intent.putExtra("productid", productid)
            startActivity(intent)
            binding.productInfoParticipation.isEnabled=false
        }

        binding.productInfoGarbage.setOnClickListener(){
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
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(item in snapshot.children){
                                    val chatmodel = item.getValue<ChatModel>()
                                    if(chatmodel?.productid == productid){
                                        roomId = item.key
                                        roomsRef.child(roomId.toString()).removeValue()
                                    }
                                }
                            }
                        })
                    db.collection("images").document("$productid").delete()
                    finish()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{ dialog, id->
                })
        builder.show()
    }

}