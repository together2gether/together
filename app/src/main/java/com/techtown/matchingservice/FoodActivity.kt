package com.techtown.matchingservice

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.techtown.matchingservice.databinding.RegisterFoodBinding
import com.techtown.matchingservice.databinding.RegisterProductBinding
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo
import java.text.SimpleDateFormat
import java.util.*

class FoodActivity : AppCompatActivity() {
    lateinit var binding: RegisterFoodBinding
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_food)
        uid = FirebaseAuth.getInstance().uid!!

        binding.button48.setOnClickListener {
            finish()
        }
        //Initiate
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerFoodStorage.setOnClickListener {
            contentUpload()
            finish()
        }
    }

    private fun contentUpload() {
        var deliveryDTO = DeliveryDTO()

        //Insert uid of user
        deliveryDTO.delivery_uid = auth?.currentUser?.uid

        //Insert userId
        deliveryDTO.delivery_userId = auth?.currentUser?.email

        deliveryDTO.deliveryParticipation[uid] = true

        deliveryDTO.delivery_ParticipationCount = 1

        deliveryDTO.store = binding.registerFoodStoreName.text.toString()

        deliveryDTO.order_price = Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

        deliveryDTO.delivery_price = Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())

        deliveryDTO.delivery_address = binding.registerFoodAddress.text.toString()

        deliveryDTO.delivery_timestamp = System.currentTimeMillis()

        firestore?.collection("delivery")?.document()?.set(deliveryDTO)

        setResult(Activity.RESULT_OK)
    }

}
