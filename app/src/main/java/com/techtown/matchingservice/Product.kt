package com.techtown.matchingservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.ProductInfoBinding
import com.techtown.matchingservice.model.ContentDTO


class Product : AppCompatActivity() {
    private lateinit var binding: ProductInfoBinding
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String
    var contentdto = ContentDTO()

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

        var docId = intent.getStringExtra("id").toString()
        var tsDoc = firestore?.collection("images")?.document(docId)
        firestore?.runTransaction{
            transition ->
            contentdto = transition.get(tsDoc!!).toObject(ContentDTO::class.java)!!
        }

        binding.productInfoBack.setOnClickListener(){
            finish()
        }

        binding.productInfoParticipation.setOnClickListener(){
            contentdto.ParticipationCount+=1
            contentdto.Participation[uid] = true
            binding.productInfoParticipation.isEnabled=false
            firestore?.runTransaction{
                transition->
                transition.set(tsDoc!!,contentdto!!)
            }
            binding.productInfoParticipationNumber.text=contentdto.ParticipationCount.toString()+" / "+contentdto.ParticipationTotal
        }
    }
}