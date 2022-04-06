package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.ProductInfoBinding
import com.techtown.matchingservice.model.ContentDTO
import java.util.ArrayList

class Product : AppCompatActivity() {
    private lateinit var binding: ProductInfoBinding
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.product_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        Glide.with(this).load(intent.getStringExtra("imageUrl").toString())
            .into(binding.productInfoPhoto)
        binding.productInfoProduct.text = intent.getStringExtra("product").toString()
        binding.productInfoTotal.text = intent.getStringExtra("totalNumber").toString()+"개 ( "+intent.getStringExtra("price").toString()+" 원 )"
        var price:Int = Integer.parseInt(intent.getStringExtra("price").toString())/Integer.parseInt(intent.getStringExtra("participationTotal").toString())
        binding.productInfoUnit.text =price.toString() + "원 / "+intent.getStringExtra("unit").toString()+"개"
        binding.productInfoURL.text = intent.getStringExtra("URL").toString()
        binding.productInfoPlace.text = intent.getStringExtra("place").toString()
        binding.productInfoCycle.text = intent.getStringExtra("cycle").toString()
        binding.productInfoParticipationNumber.text = intent.getStringExtra("participationCount").toString()+" / "+intent.getStringExtra("participationTotal").toString()

        binding.productInfoBack.setOnClickListener(){
            finish()
        }


    }
}