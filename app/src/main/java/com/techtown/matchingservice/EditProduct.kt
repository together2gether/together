package com.techtown.matchingservice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.techtown.matchingservice.databinding.EditProductBinding
import java.util.*


class EditProduct : AppCompatActivity() {
    private lateinit var binding: EditProductBinding
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_product)
        firestore = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.button14.setOnClickListener{
            finish()
        }

//        binding.editProductAddphoto.setOnClickListener{
//            var photoPickerIntent = Intent(Intent.ACTION_PICK)
//            photoPickerIntent.type = "image/*"
//            getContent.launch(photoPickerIntent)
//            binding.editProductStorage.setOnClickListener{
//                contentUpload()
//                finish()
//            }
//        }

    }
}