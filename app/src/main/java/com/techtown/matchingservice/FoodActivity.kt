package com.techtown.matchingservice

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.RegisterFoodBinding
import com.techtown.matchingservice.model.DeliveryDTO

class FoodActivity : AppCompatActivity() {
    lateinit var binding: RegisterFoodBinding
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
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

        binding.registerFoodRg.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.rb_delivery -> binding.registerFoodName.setText("가게 이름")
                R.id.rb_shopping -> binding.registerFoodName.setText("쇼핑몰 이름")
            }
        }

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

        if (binding.rbDelivery.isChecked) {
            deliveryDTO.delivery = true
        } else {
            deliveryDTO.delivery = false
        }

        deliveryDTO.delivery_ParticipationCount = 1

        deliveryDTO.store = binding.registerFoodStoreName.text.toString()

        deliveryDTO.order_price = Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

        deliveryDTO.delivery_price =
            Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())

        deliveryDTO.delivery_address = binding.registerFoodAddress.text.toString()

        deliveryDTO.delivery_detail = binding.registerFoodDetail.text.toString()

        deliveryDTO.delivery_timestamp = System.currentTimeMillis()

        firestore?.collection("delivery")?.document()?.set(deliveryDTO)

        setResult(Activity.RESULT_OK)
    }


}
