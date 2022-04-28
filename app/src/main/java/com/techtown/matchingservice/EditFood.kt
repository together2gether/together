package com.techtown.matchingservice

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.techtown.matchingservice.databinding.RegisterFoodBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO


class EditFood : AppCompatActivity() {
    private lateinit var binding: RegisterFoodBinding
    var storage: FirebaseStorage? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var deliverydto = DeliveryDTO()
    var deliveryid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_food)
        firestore = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerFoodTitle.setText("편집")

        binding.registerFoodStoreName.setText(intent.getStringExtra("store").toString())
        binding.registerFoodOrderPrice.setText(intent.getStringExtra("orderPrice").toString())
        binding.registerFoodDeliveryPrice.setText(intent.getStringExtra("deliveryPrice").toString())
        binding.registerFoodAddress.setText(intent.getStringExtra("deliveryAddress").toString())
        binding.registerFoodDetail.setText(intent.getStringExtra("detail").toString())
        deliveryid = intent.getStringExtra("deliveryid").toString()

        if (intent.getStringExtra("delivery").toString() == "false") {
            binding.registerFoodName.setText("쇼핑몰 이름")
            binding.rbShopping.isChecked = true
        }

        binding.button48.setOnClickListener() {
            finish()
        }

        binding.registerFoodStorage.setOnClickListener() {
            deliveryReUpload()
            finish()
        }
    }

    fun deliveryReUpload() {
        var tsDoc = firestore?.collection("delivery")?.document(deliveryid.toString())
        firestore?.runTransaction { transition ->
            deliverydto = transition.get(tsDoc!!).toObject(DeliveryDTO::class.java)!!

            if (binding.rbDelivery.isChecked) {
                deliverydto.delivery = true
            } else {
                deliverydto.delivery = false
            }

            deliverydto.store = binding.registerFoodStoreName.text.toString()

            deliverydto.order_price =
                Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

            deliverydto.delivery_price =
                Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())

            deliverydto.delivery_address = binding.registerFoodAddress.text.toString()

            deliverydto.delivery_detail = binding.registerFoodDetail.text.toString()

            deliverydto.delivery_timestamp = System.currentTimeMillis()

            transition.set(tsDoc!!, deliverydto)
        }
        setResult(Activity.RESULT_OK)
    }
}