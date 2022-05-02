package com.techtown.matchingservice

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.FoodInfoBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO

class Delivery : AppCompatActivity() {
    private lateinit var binding: FoodInfoBinding
    lateinit var uid: String
    var firestore: FirebaseFirestore? = null
    var deliveryid : String? = null
    var deliveryuid : String? = null
    var item = DeliveryDTO()
    val db = Firebase.firestore
    val docRef = db.collection("delivery")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.food_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        binding.foodInfoBack.setOnClickListener(){
            finish()
        }

        binding.foodInfoStore.text = intent.getStringExtra("store").toString()
        binding.foodInfoName.text = intent.getStringExtra("name").toString()
        binding.foodInfoOrderprice.text = intent.getStringExtra("orderPrice").toString()
        binding.foodInfoDeliveryprice.text = intent.getStringExtra("deliveryPrice").toString()
        binding.foodinfoDeliverydetail.text = intent.getStringExtra("detail").toString()
        deliveryuid = intent.getStringExtra("deliveryuid").toString()
        deliveryid = intent.getStringExtra("deliveryid").toString()

        if(deliveryuid == uid){
            binding.foodInfoChat.setVisibility(View.INVISIBLE)
            binding.foodInfoParticipation.setVisibility(View.INVISIBLE)
            binding.foodInfoRevice.setVisibility(View.VISIBLE)
            binding.foodInfoGarbage.setVisibility(View.VISIBLE)
        }else{
            binding.foodInfoChat.setVisibility(View.VISIBLE)
            binding.foodInfoParticipation.setVisibility(View.VISIBLE)
            binding.foodInfoRevice.setVisibility(View.INVISIBLE)
            binding.foodInfoGarbage.setVisibility(View.INVISIBLE)
        }

        docRef.document("$deliveryid" ).get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(DeliveryDTO::class.java)!!
                    if(item?.deliveryParticipation.containsKey(uid)) binding.foodInfoParticipation.isEnabled = false
                    if(item?.delivery_ParticipationCount == 2 ) binding.foodInfoParticipation.isEnabled = false
                }
            }

        binding.foodInfoParticipation.setOnClickListener(){
            item.delivery_ParticipationCount+=1
            item.deliveryParticipation[uid] = true
            var tsDoc = firestore?.collection("delivery")?.document(deliveryid.toString())
            firestore?.runTransaction{
                    transition->
                transition.set(tsDoc!!,item)
            }
            binding.foodInfoParticipation.isEnabled=false
        }

        binding.foodInfoRevice.setOnClickListener(){
            Intent(this, EditFood::class.java).apply{
                putExtra("store", binding.foodInfoStore.text)
                putExtra("name", binding.foodInfoName.text)
                putExtra("delivery",  intent.getStringExtra("delivery").toString())
                putExtra("orderPrice", binding.foodInfoOrderprice.text)
                putExtra("deliveryPrice", binding.foodInfoDeliveryprice.text)
                putExtra("deliveryid", deliveryid)
                putExtra("deliveryuid", deliveryuid)
                putExtra("detail", binding.foodinfoDeliverydetail.text)
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
                    db.collection("delivery").document("$deliveryid").delete()
                    finish()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{ dialog, id->
                })
        builder.show()
    }
}