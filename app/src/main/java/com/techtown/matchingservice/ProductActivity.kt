package com.techtown.matchingservice

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.techtown.matchingservice.databinding.RegisterProductBinding
import com.techtown.matchingservice.model.ContentDTO
import kotlinx.android.synthetic.main.register_product.*
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
    var NP_value : String? = "0"
    var bol : String ?= ""
    var bo : String = ""
    //private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    //private val roomsRef = database.getReference("chatrooms")

    val s_unit = arrayOf( " 개 ", " g ", " l ", " 박스 ", " 봉지 ", " 묶음 ", " 기타 " )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_product)
        uid = FirebaseAuth.getInstance().uid!!

        binding.button49.setOnClickListener {
            finish()
        }

        binding.address.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            resultLauncher.launch(intent)
        }
        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        bol = intent.getStringExtra("bol").toString()
        if(bol=="shop"){
            var title = intent.getStringExtra("title").toString()
            var imageURL = intent.getStringExtra("imageURL").toString()
            var lprice = intent.getStringExtra("lprice").toString()
            var link = intent.getStringExtra("link").toString()
            binding.editTextProduct.setText(title)
            binding.editTextPrice.setText(lprice)
            binding.editTextURL.setText(link)
            println("image" + imageURL)
            Glide.with(this).load(imageURL.toString())
                .into(binding.imageViewAddPhotoImage)


            binding.registerProductStorage.setOnClickListener {
                contentUpload(imageURL)
                finish()
            }
            bol = ""
        }

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

        val myAdapter = ArrayAdapter(this, R.layout.item_spinner, s_unit)
        binding.unitSpinner.adapter = myAdapter
        binding.unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(p2) {
                    0->{
                        binding.textView15.setText(" 개")
                    }
                    1->{
                        binding.textView15.setText(" g")
                    }
                    2->{
                        binding.textView15.setText(" l")
                    }
                    3->{
                        binding.textView15.setText(" 박스")
                    }
                    4 ->{
                        binding.textView15.setText(" 봉지")
                    }
                    5 -> {
                        binding.textView15.setText(" 묶음")
                    }
                    6->{
                        binding.textView15.setText(" ")
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                binding.unitSpinner.selectedItem == " 개"
            }
        }
    }

    override fun onStart() {
        super.onStart()

        initNumberPicker()
        numberPickerListener()
    }

    private fun initNumberPicker(){
        val data1: Array<String> = Array(181){
            i -> i.toString()
        }

        NP_cycle?.minValue = 0
        NP_cycle?.maxValue = data1.size-1
        NP_cycle?.wrapSelectorWheel = true
        NP_cycle?.displayedValues = data1
    }

    private fun numberPickerListener(){
        NP_cycle.setOnValueChangedListener { picker, oldVal, newVal ->
            Log.d("test", "oldVal : ${oldVal}, newVal : $newVal")
            Log.d("test", "picker.displayedValues ${picker.displayedValues[picker.value]}")
            NP_value = picker.displayedValues[picker.value].toString()
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

    fun contentUpload(imageURL : String? = null) {
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        if(imageURL == null){
            //FileUpload
            storageRef?.putFile(photoUri!!)?.continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()
                //Insert douwnloadUrl of image
                contentDTO.imageUrl = uri.toString()

                contentDTO.button = 0
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
                contentDTO.s_unit = binding.unitSpinner.selectedItem.toString()
                //Insert cycle
                contentDTO.cycle =Integer.parseInt(NP_value)
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
        } else {
            var contentDTO = ContentDTO()
            //Insert douwnloadUrl of image
            contentDTO.imageUrl = imageURL
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
            contentDTO.cycle =Integer.parseInt(NP_value)
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