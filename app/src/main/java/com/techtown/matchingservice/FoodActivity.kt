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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
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
    val shop_items = arrayOf("쿠팡","SSG.COM","마켓컬리","롯데ON","11번가","G마켓","옥션","기타")
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
            var category_st : String = binding.deliverSpinner.selectedItem.toString()
            deliveryDTO.category = category_st
            if(category_st == "한식"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fkorea.png?alt=media&token=f930d88e-3873-4513-8bbe-c65decaa46f7"
            }
            else if(category_st == "중식"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fjajangmyeon%20(1).png?alt=media&token=53db0aed-993e-4da7-9ef8-02c1e9c363df"
            }
            else if(category_st == "일식"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fsushi.png?alt=media&token=8d1ec278-3bb7-4c39-8139-96ddfdea8f7c"
            }
            else if(category_st == "양식"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fspaguetti.png?alt=media&token=e7838877-73f8-4ed8-bfb6-74060cf5f6c5"
            }
            else if(category_st == "치킨"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Ffried-chicken.png?alt=media&token=fe9eda32-9d9f-4c1b-b193-a37ef23d1b40"
            }
            else if(category_st == "피자"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fpizza.png?alt=media&token=adc8086a-b736-4614-bc30-86c0c14d71c0"
            }
            else if(category_st == "분식"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Ftteok.png?alt=media&token=450a2bdd-97f1-47bb-950c-a68ef31b31d7"
            }
            else if(category_st == "디저트"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fcupcake.png?alt=media&token=b74c68c4-4696-4c28-b197-21972dae0639"
            }
            else if(category_st == "고기"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fbeef.png?alt=media&token=d98c05cd-bc2d-48bf-a276-6d1155506c5d"
            }
            else if(category_st == "패스트푸드"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fburger%20(3).png?alt=media&token=28c72908-a065-4dab-a722-19cc3bf6a1d1"
            }
            else{
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fgita.png?alt=media&token=517e2c6a-5ae2-4115-8b02-167b1439ad7c"
            }
        }
        else {
            var category_st : String = binding.shoppingSpinner.selectedItem.toString()
            deliveryDTO.category = category_st
            if(category_st == "쿠팡"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fcou.png?alt=media&token=d17e4494-09a6-4da5-8221-7176b41bb284"
            }
            else if(category_st == "SSG.COM"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fssg.png?alt=media&token=4b287690-da10-45b9-b058-b684a0429968"
            }
            else if(category_st == "마켓컬리"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fkurly.png?alt=media&token=c3f8e525-4372-4366-91c3-f69d3b71b8f5"
            }
            else if(category_st == "롯데ON"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Flotte.jpg?alt=media&token=e56ba83a-8556-46d3-8216-a2fc137c00d7"
            }
            else if(category_st == "11번가"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fbunga11.png?alt=media&token=f5260297-a03f-4e00-a62f-7a752f9d9b98"
            }
            else if(category_st == "G마켓"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fgmarket.jpg?alt=media&token=a153e381-ac78-4a06-9340-ec4261782308"
            }
            else if(category_st == "옥션"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fauction.png?alt=media&token=b1a1359a-4984-4267-91d7-6f4021a097e5"
            }
            else if(category_st == "기타"){
                deliveryDTO.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fgita.png?alt=media&token=517e2c6a-5ae2-4115-8b02-167b1439ad7c"
            }

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
