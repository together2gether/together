package com.techtown.matchingservice

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.ProductInfoBinding
import com.techtown.matchingservice.model.ContentDTO


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
        if(intent.getStringExtra("uidkey").toString()=="true"){
            binding.productInfoParticipation.isEnabled=false
        }
        binding.productInfoBack.setOnClickListener(){
            finish()
        }

        binding.productInfoParticipation.setOnClickListener(){
            favoriteEvent()
        }
    }

    fun favoriteEvent(){
        var docId = intent.getStringExtra("id").toString()
        var tsDoc = firestore?.collection("images")?.document(docId)
        firestore?.runTransaction {
                transition ->
            var contentDTO = transition.get(tsDoc!!).toObject(ContentDTO::class.java)
            if(contentDTO!!.Participation.containsKey(uid)){
                binding.productInfoParticipation.isEnabled = false
            }else{
                //참여를 누르지 않은 상태 -> 클릭시 참여
                contentDTO.ParticipationCount = contentDTO.ParticipationCount + 1
                contentDTO.Participation[uid] = true
            }
            transition.set(tsDoc,contentDTO)
            binding.productInfoParticipationNumber.text=contentDTO.ParticipationCount.toString()+" / "+contentDTO.ParticipationTotal
        }

    }
}

