package com.techtown.matchingservice

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.FoodInfoBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO

class Delivery : AppCompatActivity() {
    private lateinit var binding: FoodInfoBinding
    lateinit var uid: String
    var firestore: FirebaseFirestore? = null
    var deliveryid : String? = null
    var deliveryuid : String? = null
    var item = DeliveryDTO()
    val db = Firebase.firestore
    val docRef = db.collection("delivery")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.food_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        binding.button23.setOnClickListener(){
            finish()
        }

        binding.foodinfoStore.text = intent.getStringExtra("store").toString()
        binding.foodinfoOrderprice.text = intent.getStringExtra("orderPrice").toString()
        binding.foodinfoDeliveryprice.text = intent.getStringExtra("deliveryPrice").toString()
        binding.foodinfoDeliveryaddress.text = intent.getStringExtra("deliveryAddress").toString()
        binding.foodinfoDeliverydetail.text = intent.getStringExtra("detail").toString()
        deliveryuid = intent.getStringExtra("deliveryuid").toString()
        deliveryid = intent.getStringExtra("deliveryid").toString()

        if(deliveryuid == uid){
            binding.foodinfoChat.setVisibility(View.INVISIBLE)
            binding.foodinfoParticipation.setVisibility(View.INVISIBLE)
            binding.foodEdit.setVisibility(View.VISIBLE)
            binding.foodRemove.setVisibility(View.VISIBLE)
        }else{
            binding.foodinfoChat.setVisibility(View.VISIBLE)
            binding.foodinfoParticipation.setVisibility(View.VISIBLE)
            binding.foodEdit.setVisibility(View.INVISIBLE)
            binding.foodRemove.setVisibility(View.INVISIBLE)
        }

        docRef.document("$deliveryid" ).get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(DeliveryDTO::class.java)!!
                    if(item?.deliveryParticipation.containsKey(uid)) binding.foodinfoParticipation.isEnabled = false
                    if(item?.delivery_ParticipationCount == 2 ) binding.foodinfoParticipation.isEnabled = false
                }
            }

        binding.foodinfoParticipation.setOnClickListener(){
            item.delivery_ParticipationCount+=1
            item.deliveryParticipation[uid] = true
            var tsDoc = firestore?.collection("delivery")?.document(deliveryid.toString())
            firestore?.runTransaction{
                    transition->
                transition.set(tsDoc!!,item)
            }
            binding.foodinfoParticipation.isEnabled=false
        }

        binding.foodEdit.setOnClickListener(){
            Intent(this, EditFood::class.java).apply{
                putExtra("store", binding.foodinfoStore.text)
                putExtra("delivery",  intent.getStringExtra("delivery").toString())
                putExtra("orderPrice", binding.foodinfoOrderprice.text)
                putExtra("deliveryPrice", binding.foodinfoDeliveryprice.text)
                putExtra("deliveryAddress", binding.foodinfoDeliveryaddress.text)
                putExtra("deliveryid", deliveryid)
                putExtra("deliveryuid", deliveryuid)
                putExtra("detail", binding.foodinfoDeliverydetail.text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
            finish()
        }

        binding.foodRemove.setOnClickListener(){
            RemovePopup()
        }
    }
    private fun RemovePopup(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("삭제")
            .setMessage("이 게시물을 삭제하시겠습니까?")
            .setPositiveButton("예",
                DialogInterface.OnClickListener{ dialog, id->
                    db.collection("delivery").document("$deliveryid").delete()
                    finish()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{ dialog, id->
                })
        builder.show()
    }
}