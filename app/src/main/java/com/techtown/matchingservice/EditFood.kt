package com.techtown.matchingservice

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    var item = DeliveryDTO()
    var deliveryid: String? = null
    var items = arrayOf("")
    lateinit var kind : String
    val del_items = arrayOf("한식", "중식","일식","양식", "치킨", "피자","분식","디저트","고기","패스트푸드", "기타")
    val shop_items = arrayOf("쿠팡","SSG.COM","마켓컬리","롯데ON","11번가","G마켓","옥션","기타")
    var kind_num : Int =0

    val db = Firebase.firestore
    val docRef = db.collection("delivery")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_food)
        firestore = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        deliveryid = intent.getStringExtra("deliveryid").toString()

        docRef.document("$deliveryid").get()
            .addOnSuccessListener { document ->
                item = document.toObject(DeliveryDTO::class.java)!!
                binding.registerFoodStoreName.setText(item.store.toString())
                binding.registerFoodName.setText(item.name.toString())
                binding.registerFoodOrderPrice.setText(item.order_price.toString())
                binding.registerFoodDeliveryPrice.setText(item.delivery_price.toString())
                binding.registerFoodDetail.setText(item.delivery_detail.toString())
                if(!item.delivery){
                    binding.registerFood.setText("쇼핑몰 이름")
                    items = shop_items
                    binding.deliverSpinner.visibility = View.GONE
                    binding.shoppingSpinner.visibility = View.VISIBLE
                    kind = "shopping"
                    binding.editTextStore.visibility = View.GONE
                    for(i in 0..shop_items.size-1){
                        if(item.category.toString() ==shop_items[i]){
                            kind_num = i
                        }
                    }
                    val myAdapter = ArrayAdapter(this, R.layout.item_spinner, items)
                    binding.shoppingSpinner.adapter = myAdapter
                    binding.shoppingSpinner.setSelection(kind_num)
                }
                else {
                    items = del_items
                    binding.deliverSpinner.visibility = View.VISIBLE
                    binding.shoppingSpinner.visibility = View.GONE
                    kind = "delivery"
                    for(i in  0..del_items.size-1){
                        if(item.category.toString()==del_items[i]){
                            kind_num = i
                        }
                    }
                    val myAdapter = ArrayAdapter(this, R.layout.item_spinner, items)
                    binding.deliverSpinner.adapter = myAdapter
                    binding.deliverSpinner.setSelection(kind_num)
                }
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

            deliverydto.store = binding.registerFoodStoreName.text.toString()

            deliverydto.name = binding.registerFoodName.text.toString()

            deliverydto.order_price =
                Integer.parseInt(binding.registerFoodOrderPrice.text.toString())

            deliverydto.delivery_price =
                Integer.parseInt(binding.registerFoodDeliveryPrice.text.toString())


            deliverydto.delivery_detail = binding.registerFoodDetail.text.toString()
            if(kind == "delivery"){
                var category_st : String = binding.deliverSpinner.selectedItem.toString()
                deliverydto.category = category_st
                if(category_st == "한식"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fkorea.png?alt=media&token=f930d88e-3873-4513-8bbe-c65decaa46f7"
                }
                else if(category_st == "중식"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fjajangmyeon%20(1).png?alt=media&token=53db0aed-993e-4da7-9ef8-02c1e9c363df"
                }
                else if(category_st == "일식"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fsushi.png?alt=media&token=8d1ec278-3bb7-4c39-8139-96ddfdea8f7c"
                }
                else if(category_st == "양식"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fspaguetti.png?alt=media&token=e7838877-73f8-4ed8-bfb6-74060cf5f6c5"
                }
                else if(category_st == "치킨"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Ffried-chicken.png?alt=media&token=fe9eda32-9d9f-4c1b-b193-a37ef23d1b40"
                }
                else if(category_st == "피자"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fpizza.png?alt=media&token=adc8086a-b736-4614-bc30-86c0c14d71c0"
                }
                else if(category_st == "분식"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Ftteok.png?alt=media&token=450a2bdd-97f1-47bb-950c-a68ef31b31d7"
                }
                else if(category_st == "디저트"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fcupcake.png?alt=media&token=b74c68c4-4696-4c28-b197-21972dae0639"
                }
                else if(category_st == "고기"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fbeef.png?alt=media&token=d98c05cd-bc2d-48bf-a276-6d1155506c5d"
                }
                else if(category_st == "패스트푸드"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fburger%20(3).png?alt=media&token=28c72908-a065-4dab-a722-19cc3bf6a1d1"
                }
                else{
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fgita.png?alt=media&token=517e2c6a-5ae2-4115-8b02-167b1439ad7c"
                }
            }
            else {
                var category_st : String = binding.shoppingSpinner.selectedItem.toString()
                deliverydto.category = category_st
                if(category_st == "쿠팡"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fcou.png?alt=media&token=d17e4494-09a6-4da5-8221-7176b41bb284"
                }
                else if(category_st == "이마트몰"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Femart.png?alt=media&token=8b5e04f9-3ef2-40b8-8c44-b37019c6c492"
                }
                else if(category_st == "마켓컬리"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fkurly.png?alt=media&token=c3f8e525-4372-4366-91c3-f69d3b71b8f5"
                }
                else if(category_st == "롯데ON"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Flotte.jpg?alt=media&token=e56ba83a-8556-46d3-8216-a2fc137c00d7"
                }
                else if(category_st == "11번가"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fbunga11.png?alt=media&token=f5260297-a03f-4e00-a62f-7a752f9d9b98"
                }
                else if(category_st == "G마켓"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fgmarket.jpg?alt=media&token=a153e381-ac78-4a06-9340-ec4261782308"
                }
                else if(category_st == "옥션"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fauction.png?alt=media&token=b1a1359a-4984-4267-91d7-6f4021a097e5"
                }
                else if(category_st == "기타"){
                    deliverydto.imageURL = "https://firebasestorage.googleapis.com/v0/b/matchingservice-ac54b.appspot.com/o/categoryImage%2Fgita.png?alt=media&token=517e2c6a-5ae2-4115-8b02-167b1439ad7c"
                }

            }
            transition.set(tsDoc, deliverydto)
        }
        setResult(Activity.RESULT_OK)
    }
}