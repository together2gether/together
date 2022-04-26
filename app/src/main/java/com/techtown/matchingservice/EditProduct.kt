package com.techtown.matchingservice

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.techtown.matchingservice.databinding.EditProductBinding
import com.techtown.matchingservice.databinding.RegisterProductBinding
import com.techtown.matchingservice.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.*


class EditProduct : AppCompatActivity() {
    private lateinit var binding: EditProductBinding
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var productid: String? = null
    var contentdto = ContentDTO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_product)
        firestore = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Glide.with(this).load(intent.getStringExtra("imageUrl").toString())
            .into(binding.imageVieweditProductAddPhoto)
//        binding.editTextProduct.setText(intent.getStringExtra("product").toString())
//        binding.editTextTotalNumber.setText(intent.getStringExtra("totalNumber").toString())
//        binding.editTextPrice.setText(intent.getStringExtra("price").toString())
//        binding.editTextUnit.setText(intent.getStringExtra("unit").toString())
//        binding.editTextURL.setText(intent.getStringExtra("URL").toString())
//        binding.editTextPlace.setText(intent.getStringExtra("place").toString())
//        binding.NPCycle.(intent.getStringExtra("cycle").toString())
//        productid = intent.getStringExtra("id").toString()

        binding.button14.setOnClickListener {
            finish()
        }

        binding.editProductAddphoto.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            getContent.launch(photoPickerIntent)
        }
        binding.editProductStorage.setOnClickListener {
            if (photoUri != null) {
                imageUpload()
            }
            contentReUpload()
            finish()
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                photoUri = result.data?.data
                binding.imageVieweditProductAddPhoto.setImageURI(photoUri)
                Log.d("이미지", "성공")
            } else {
                Log.d("이미지", "실패")
            }
        }

    fun imageUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //FileUpload
        storageRef?.putFile(photoUri!!)
            ?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener { uri ->
                var tsDoc = firestore?.collection("images")?.document(productid.toString())
                firestore?.runTransaction { transition ->
                    contentdto = transition.get(tsDoc!!).toObject(ContentDTO::class.java)!!
                    contentdto.imageUrl = uri.toString()
                    transition.set(tsDoc!!, contentdto)
                }
            }
    }

    fun contentReUpload() {
        var tsDoc = firestore?.collection("images")?.document(productid.toString())
        firestore?.runTransaction { transition ->
            contentdto = transition.get(tsDoc!!).toObject(ContentDTO::class.java)!!


            //Insert uid of user
            contentdto.uid = auth?.currentUser?.uid

            //Insert userId
            contentdto.userId = auth?.currentUser?.email

            //Insert Product
            contentdto.product = binding.editProductProduct.text.toString()

            //Insert price
            contentdto.price = Integer.parseInt(binding.editProductPrice.text.toString())

            //Insert totalNumber
            contentdto.totalNumber = Integer.parseInt(binding.editProductTotal.text.toString())

            //Insert unit
            contentdto.unit = Integer.parseInt(binding.editProductUnit.text.toString())

            //Insert cycle
            contentdto.cycle = Integer.parseInt(binding.editProductCycle.text.toString())

            //Insert url
            contentdto.url = binding.editProductUrl.text.toString()

            //Insert place
            contentdto.place = binding.editProductPlace.text.toString()

            //Insert timestamp
            contentdto.timestamp = System.currentTimeMillis()

            //Insert ParticipationTotal
            var participation: Int = contentdto.totalNumber / contentdto.unit
            contentdto.ParticipationTotal = participation

            transition.set(tsDoc!!, contentdto)
        }

        setResult(Activity.RESULT_OK)

        finish()
    }
}