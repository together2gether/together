package com.techtown.matchingservice

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.RegisterFoodBinding
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo

class FoodActivity : AppCompatActivity() {
    lateinit var binding: RegisterFoodBinding
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
    lateinit var kind : String
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    var items = arrayOf("")
    val del_items = arrayOf("한식", "중식","일식","양식", "치킨", "피자","분식","디저트","고기","패스트푸드", "기타")
    val shop_items = arrayOf("쿠팡","이마트몰","마켓컬리","롯데ON","11번가","G마켓","옥션","기타")
    var address : String= ""
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
                    binding.textView193.setText("* 현재 배달 주소는 회원정보의 주소로 되어 있습니다.\n  해당 위치가 아닌 경우 회원정보 주소를 수정해주세요.")
                    binding.deliverSpinner.setVisibility(View.INVISIBLE)
                    binding.shoppingSpinner.setVisibility(View.VISIBLE)
                }
            }
        val userRef = database.getReference("usersInfo").child(auth?.currentUser?.uid.toString())
        userRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var userInfo = snapshot.getValue<UsersInfo>()
                address = userInfo!!.address.toString()

                //Toast.makeText(applicationContext, add, Toast.LENGTH_LONG).show()
                /*if(add!= null&&add!= ""){
                    Toast.makeText(applicationContext, add, Toast.LENGTH_LONG).show()
                    deliveryDTO.delivery_address = add
                    Toast.makeText(applicationContext, add, Toast.LENGTH_LONG).show()
                }*/
            }
        })

        binding.registerFoodStorage.setOnClickListener {
            Toast.makeText(applicationContext, address, Toast.LENGTH_LONG).show()
            contentUpload()
            finish()
        }

        if(kind == "delivery"){
            items = del_items
        } else{
            items = shop_items
        }
        val myAdapter = ArrayAdapter(this, R.layout.item_spinner, items)

        binding.deliverSpinner.adapter = myAdapter
        /*binding.deliverSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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
        }*/
        binding.shoppingSpinner.adapter = myAdapter
        if(kind == "shop"){
            binding.shoppingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    when(p2) {
                        7 -> {
                            binding.registerFoodStoreName.setText("")
                        }
                        else -> {
                            binding.registerFoodStoreName.setText(shop_items[p2])
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

    }

    private fun contentUpload() {
        var deliveryDTO = DeliveryDTO()

        //Insert uid of user
        deliveryDTO.delivery_uid = auth?.currentUser?.uid

        //Insert userId
        deliveryDTO.delivery_userId = auth?.currentUser?.email



        deliveryDTO.delivery_address = address
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
