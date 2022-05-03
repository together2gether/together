package com.techtown.matchingservice

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
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
    lateinit var kind : String
    val items = arrayOf("한식", "중식","일식","양식", "치킨", "피자","분식","디저트","고기","패스트푸드")
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
        kind= intent.getStringExtra("kind").toString()
            when (kind) {
                "delivery" -> binding.registerFood.setText("가게 이름")
                "shop" -> {
                    binding.registerFood.setText("쇼핑몰 이름")
                    binding.editTextStore.setVisibility(View.GONE)
                }
            }
        binding.registerFoodStorage.setOnClickListener {
            contentUpload()
            finish()
        }
        val myAdapter = ArrayAdapter(this, R.layout.item_spinner, items)
        binding.deliverSpinner.adapter = myAdapter
        binding.deliverSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(p2) {
                    0 -> {

                    }
                    else -> {

                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        binding.shoppingSpinner.adapter = myAdapter
        binding.shoppingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(p2) {
                    0 -> {

                    }
                    else -> {

                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun contentUpload() {
        var deliveryDTO = DeliveryDTO()

        //Insert uid of user
        deliveryDTO.delivery_uid = auth?.currentUser?.uid

        //Insert userId
        deliveryDTO.delivery_userId = auth?.currentUser?.email

        deliveryDTO.deliveryParticipation[uid] = true

        if (kind == "delivery") {
            deliveryDTO.delivery = true
        } else {
            deliveryDTO.delivery = false
        }

        deliveryDTO.delivery_ParticipationCount = 1
        if(kind == "delivery"){
            deliveryDTO.category = binding.deliverSpinner.selectedItem.toString()
        }
        else {
            deliveryDTO.category = binding.shoppingSpinner.selectedItem.toString()
        }
        deliveryDTO.store = binding.registerFoodStoreName.text.toString()

        deliveryDTO.name = binding.registerFoodName.text.toString()

        deliveryDTO.order_price = Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

        deliveryDTO.delivery_price =
            Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())


        deliveryDTO.delivery_detail = binding.registerFoodDetail.text.toString()

        deliveryDTO.delivery_timestamp = System.currentTimeMillis()

        firestore?.collection("delivery")?.document()?.set(deliveryDTO)

        setResult(Activity.RESULT_OK)
    }


}
