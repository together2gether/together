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
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.techtown.matchingservice.databinding.RegisterProductBinding
import com.techtown.matchingservice.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : AppCompatActivity() {
    lateinit var binding: RegisterProductBinding
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_product)

        binding.button49.setOnClickListener {
            finish()
        }
        binding.button10.setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)

        }
        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //Open the album
        binding.btnAddphoto.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            getContent.launch(photoPickerIntent)
            binding.button53.setOnClickListener {
                contentUpload()
                finish()
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                photoUri = result.data?.data
                binding.imageViewAddPhotoImage.setImageURI(photoUri)
                Log.d("이미지", "성공")
            } else {
                Log.d("이미지", "실패")
            }
        }

    fun contentUpload() {
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //FileUpload
        storageRef?.putFile(photoUri!!)?.continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert douwnloadUrl of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert Product
            contentDTO.product = binding.editTextProduct.text.toString()

            //Insert price
            contentDTO.price =Integer.parseInt(binding.editTextPrice.text.toString())

            //Insert totalNumber
            contentDTO.totalNumber =Integer.parseInt(binding.editTextTotalNumber.text.toString())

            //Insert unit
            contentDTO.unit =Integer.parseInt(binding.editTextUnit.text.toString())

            //Insert cycle
            contentDTO.cycle =Integer.parseInt(binding.editTextCycle.text.toString())

            //Insert url
            contentDTO.url = binding.editTextURL.text.toString()

            //Insert place
            contentDTO.place =binding.editTextPlace.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            //Insert ParticipationTotal
            var participation : Int = contentDTO.totalNumber / contentDTO.unit
            contentDTO.ParticipationTotal = participation

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }
    }
}