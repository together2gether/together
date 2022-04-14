package com.techtown.matchingservice

import android.os.Bundle
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
    var item = DeliveryDTO()
    val db = Firebase.firestore
    val docRef = db.collection("delivery")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.food_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        binding.button23.setOnClickListener(){
            finish()
        }

        binding.foodinfoStore.text = intent.getStringExtra("store").toString()
        binding.foodinfoOrderprice.text = intent.getStringExtra("orderPrice").toString()
        binding.foodinfoDeliveryprice.text = intent.getStringExtra("deliveryPrice").toString()
        binding.foodinfoDeliveryaddress.text = intent.getStringExtra("deliveryAddress").toString()
        binding.oodinfoDeliverydetail.text = intent.getStringExtra("detail").toString()
        deliveryid = intent.getStringExtra("deliveryid").toString()
        docRef.document("$deliveryid" ).get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(DeliveryDTO::class.java)!!
                    if(item?.deliveryParticipation.containsKey(uid)) binding.foodinfoParticipation.isEnabled = false
                    if(item?.delivery_ParticipationCount == 2 ) binding.foodinfoParticipation.isEnabled = false
                }
            }

        binding.foodinfoParticipation.setOnClickListener(){
            item.delivery_ParticipationCount+=1
            item.deliveryParticipation[uid] = true
            var tsDoc = firestore?.collection("delivery")?.document(deliveryid.toString())
            firestore?.runTransaction{
                    transition->
                transition.set(tsDoc!!,item)
            }
            binding.foodinfoParticipation.isEnabled=false
        }
    }
}