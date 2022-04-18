package com.techtown.matchingservice

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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
import com.techtown.matchingservice.databinding.ManageProductBinding
import com.techtown.matchingservice.model.ChatModel


class ProductManage : AppCompatActivity() {
    private lateinit var binding: ManageProductBinding
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
    val db = Firebase.firestore
    var productid : String? = null

    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = database.getReference("chatrooms")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.manage_product)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()


        Glide.with(this).load(intent.getStringExtra("imageUrl").toString()).into(binding.manageProductPhoto)
        binding.manageProductProduct.text = intent.getStringExtra("product").toString()
        binding.manageProductTotal.text = intent.getStringExtra("totalNumber").toString()+"개 ( "+intent.getStringExtra("price").toString()+" 원 )"
        var price:Int = Integer.parseInt(intent.getStringExtra("price").toString())/Integer.parseInt(intent.getStringExtra("participationTotal").toString())
        binding.manageProductUnit.text = price.toString() + "원 / "+intent.getStringExtra("unit").toString()+"개"
        binding.manageProductURL.text = intent.getStringExtra("URL").toString()
        binding.manageProductPlace.text = intent.getStringExtra("place").toString()
        binding.manageProductCycle.text = intent.getStringExtra("cycle").toString()+"일"
        binding.manageProductParticipationNumber.text = intent.getStringExtra("participationCount").toString()+" / "+intent.getStringExtra("participationTotal").toString()
        productid = intent.getStringExtra("id").toString()

        binding.button21.setOnClickListener(){
            finish()
        }

        binding.button22.setOnClickListener(){
            Intent(this, EditProduct::class.java).apply{
                putExtra("product", binding.manageProductProduct.text)
                putExtra("imageUrl", intent.getStringExtra("imageUrl").toString())
                putExtra("price", intent.getStringExtra("price").toString())
                putExtra("totalNumber", intent.getStringExtra("totalNumber").toString())
                putExtra("cycle", intent.getStringExtra("cycle").toString())
                putExtra("unit", intent.getStringExtra("unit").toString())
                putExtra("URL", binding.manageProductURL.text)
                putExtra("place", binding.manageProductPlace.text)
                putExtra("id", productid)
            }.run { startActivity(this) }
            finish()
        }

        binding.productRemove.setOnClickListener(){
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