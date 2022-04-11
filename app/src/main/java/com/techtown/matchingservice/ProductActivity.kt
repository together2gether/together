package com.techtown.matchingservice

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
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
    lateinit var uid: String
    var thisTime : Long? = null


    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = database.getReference("chatrooms")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_product)
        uid = FirebaseAuth.getInstance().uid!!
        
        binding.button49.setOnClickListener {
            finish()
        }
        binding.button10.setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)
        }
        binding.address.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            resultLauncher.launch(intent)
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
            binding.registerProductStorage.setOnClickListener {
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

            contentDTO.ParticipationCount = 1

            contentDTO.Participation[uid] = true
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
            thisTime = contentDTO.timestamp

            val geocoder = Geocoder(this, Locale.getDefault())
            val cor = geocoder.getFromLocationName(binding.editTextPlace.text.toString(),1)
            //var LATLNG = LatLng(cor[0].latitude, cor[0].longitude)

            //Insert ParticipationTotal
            var participation : Int = contentDTO.totalNumber / contentDTO.unit
            contentDTO.ParticipationTotal = participation

            contentDTO.location = GeoPoint(cor[0].latitude, cor[0].longitude)
            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val myData: Intent? = result.data
            val stringData = result.data?.getStringExtra("returnValue")
            binding.editTextPlace.setText(stringData)
        }
    }
}