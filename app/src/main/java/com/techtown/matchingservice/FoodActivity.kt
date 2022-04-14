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
import com.techtown.matchingservice.model.ShoppingDTO
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

        binding.registerFoodRg.setOnCheckedChangeListener { radioGroup, i ->
            when(i){
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
        if(binding.rbDelivery.isChecked){
            deliveryupload()
        }else if(binding.rbShopping.isChecked){
            shoppingupload()
        }
    }

    private fun deliveryupload(){
        var deliveryDTO = DeliveryDTO()

        //Insert uid of user
        deliveryDTO.delivery_uid = auth?.currentUser?.uid

        //Insert userId
        deliveryDTO.delivery_userId = auth?.currentUser?.email

        deliveryDTO.deliveryParticipation[uid] = true

        deliveryDTO.check = true

        deliveryDTO.delivery_ParticipationCount = 1

        deliveryDTO.store = binding.registerFoodStoreName.text.toString()

        deliveryDTO.order_price = Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

        deliveryDTO.delivery_price = Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())

        deliveryDTO.delivery_address = binding.registerFoodAddress.text.toString()

        deliveryDTO.delivery_detail = binding.registerFoodDetail.text.toString()

        deliveryDTO.delivery_timestamp = System.currentTimeMillis()

        firestore?.collection("delivery")?.document()?.set(deliveryDTO)

        setResult(Activity.RESULT_OK)
    }

    private fun shoppingupload(){
        var shoppingDTO = ShoppingDTO()

        //Insert uid of user
        shoppingDTO.shopping_uid = auth?.currentUser?.uid

        //Insert userId
        shoppingDTO.shopping_userId = auth?.currentUser?.email

        shoppingDTO.shoppingParticipation[uid] = true

        shoppingDTO.check=true

        shoppingDTO.shpopping_ParticipationCount = 1

        shoppingDTO.store = binding.registerFoodStoreName.text.toString()

        shoppingDTO.order_price = Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

        shoppingDTO.shopping_price = Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())

        shoppingDTO.shopping_address = binding.registerFoodAddress.text.toString()

        shoppingDTO.shopping_detail = binding.registerFoodDetail.text.toString()

        shoppingDTO.shopping_timestamp = System.currentTimeMillis()

        firestore?.collection("shopping")?.document()?.set(shoppingDTO)

        setResult(Activity.RESULT_OK)
    }

}
