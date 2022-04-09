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


class Product : AppCompatActivity() {
    private lateinit var binding: ProductInfoBinding
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String
    var contentdto = ContentDTO()
    var productid : String? = null
    var regist_userid : String? = null

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
        regist_userid = intent.getStringExtra("Uid").toString()
        productid = intent.getStringExtra("id").toString()

        val intent = Intent(this, chatting::class.java)


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

        binding.buttonChat.setOnClickListener {
            intent.putExtra("groupchat", "N")
            intent.putExtra("destinationUid", regist_userid)
            startActivity(intent)
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
            //enterChatroom()
            intent.putExtra("groupchat", "Y")
            intent.putExtra("productid", productid)
            startActivity(intent)
        }
    }
    fun enterChatroom(){
        Intent(this, GroupChat::class.java).apply {
            putExtra("productid", productid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.run { startActivity(this) }
        Toast.makeText(this, "확인", Toast.LENGTH_LONG).show()
        /*val intent2 = Intent(this, GroupChat::class.java)
        intent2.putExtra("productid", productid)
        //Toast.makeText(this, "확인", Toast.LENGTH_LONG).show()
        startActivity(intent2)*/
        /*val db = Firebase.firestore
        val docRef = db.collection("images")
        docRef.document("$productid").get()
            .addOnSuccessListener { document ->
                if(document != null){
                    var item = document.toObject(ContentDTO::class.java)
                    Toast.makeText(this, item?.product, Toast.LENGTH_LONG).show()
                }
            }*/

    }
}