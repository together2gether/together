package com.techtown.matchingservice

import android.content.DialogInterface
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
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.ShoppingDTO

class Shopping : AppCompatActivity() {
    private lateinit var binding: FoodInfoBinding
    lateinit var uid: String
    var firestore: FirebaseFirestore? = null
    var shoppingid : String? = null
    var shoppinguid : String? = null
    var item = ShoppingDTO()
    val db = Firebase.firestore
    val docRef = db.collection("shopping")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.food_info)
        uid = FirebaseAuth.getInstance().uid!!
        firestore = FirebaseFirestore.getInstance()

        binding.button23.setOnClickListener() {
            finish()
        }

        binding.foodinfoStore.text = intent.getStringExtra("store").toString()
        binding.foodinfoOrderprice.text = intent.getStringExtra("orderPrice").toString()
        binding.foodinfoDeliveryprice.text = intent.getStringExtra("ShoppingPrice").toString()
        binding.foodinfoDeliveryaddress.text = intent.getStringExtra("ShoppingAddress").toString()
        binding.foodinfoDeliverydetail.text = intent.getStringExtra("detail").toString()
        shoppinguid = intent.getStringExtra("Shoppinguid").toString()
        shoppingid = intent.getStringExtra("Shoppingid").toString()

        if (shoppinguid == uid) {
            binding.foodinfoChat.setVisibility(View.INVISIBLE)
            binding.foodinfoParticipation.setVisibility(View.INVISIBLE)
            binding.foodEdit.setVisibility(View.VISIBLE)
            binding.foodRemove.setVisibility(View.VISIBLE)
        } else {
            binding.foodinfoChat.setVisibility(View.VISIBLE)
            binding.foodinfoParticipation.setVisibility(View.VISIBLE)
            binding.foodEdit.setVisibility(View.INVISIBLE)
            binding.foodRemove.setVisibility(View.INVISIBLE)
        }

        docRef.document("$shoppingid").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    item = document.toObject(ShoppingDTO::class.java)!!
                    if (item?.shoppingParticipation.containsKey(uid)) binding.foodinfoParticipation.isEnabled =
                        false
                    if (item?.shopping_ParticipationCount == 2) binding.foodinfoParticipation.isEnabled =
                        false
                }
            }

        binding.foodinfoParticipation.setOnClickListener() {
            item.shopping_ParticipationCount += 1
            item.shoppingParticipation[uid] = true
            var tsDoc = firestore?.collection("shopping")?.document(shoppingid.toString())
            firestore?.runTransaction { transition ->
                transition.set(tsDoc!!, item)
            }
            binding.foodinfoParticipation.isEnabled = false
        }

        binding.foodRemove.setOnClickListener() {
            RemovePopup()
        }
    }

    private fun RemovePopup(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("삭제")
            .setMessage("이 게시물을 삭제하시겠습니까?")
            .setPositiveButton("예",
                DialogInterface.OnClickListener{dialog, id->
                    db.collection("shopping").document("$shoppingid").delete()
                    finish()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{dialog, id->
                })
        builder.show()
    }
}