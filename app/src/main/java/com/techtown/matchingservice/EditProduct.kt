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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.techtown.matchingservice.databinding.RegisterProductBinding
import com.techtown.matchingservice.model.ContentDTO
import kotlinx.android.synthetic.main.product_info.*
import kotlinx.android.synthetic.main.register_product.*
import java.text.SimpleDateFormat
import java.util.*


class EditProduct : AppCompatActivity() {
    private lateinit var binding: RegisterProductBinding
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var NP_value : String? = null
    var firestore: FirebaseFirestore? = null
    var productid: String? = null
    var contentdto = ContentDTO()
    var item = ContentDTO()

    val db = Firebase.firestore
    val docRef = db.collection("images")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.register_product)
        firestore = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        productid = intent.getStringExtra("productid").toString()

        docRef.document("$productid").get()
            .addOnSuccessListener { document ->
                if(document != null){
                    item = document.toObject(ContentDTO::class.java)!!
                }
            }

        Glide.with(this).load(item.imageUrl.toString())
            .into(binding.imageViewAddPhotoImage)
        binding.editTextProduct.setText(item.product.toString())
        binding.editTextTotalNumber.setText(item.totalNumber.toString())
        binding.editTextPrice.setText(item.price.toString())
        binding.editTextUnit.setText(item.unit.toString())
        binding.editTextURL.setText(item.url.toString())
        binding.editTextPlace.setText(item.place.toString())

        binding.button49.setOnClickListener {
            finish()
        }

        binding.btnAddphoto.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            getContent.launch(photoPickerIntent)
        }
        binding.registerProductStorage.setOnClickListener {
            if (photoUri != null) {
                imageUpload()
            }
            contentReUpload()
            finish()
        }

        binding.address.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            resultLauncher.launch(intent)
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
        NP_cycle?.value= Integer.parseInt(item.cycle.toString())
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

            //Insert ProductT
            contentdto.product = binding.editTextProduct.text.toString()

            //Insert price
            contentdto.price = Integer.parseInt(binding.editTextPrice.text.toString())

            //Insert totalNumber
            contentdto.totalNumber = Integer.parseInt(binding.editTextTotalNumber.text.toString())

            //Insert unit
            contentdto.unit = Integer.parseInt(binding.editTextUnit.text.toString())

            val NP_value = binding.NPCycle.displayedValues[binding.NPCycle.value].toString()

            //Insert cycle
            contentdto.cycle = Integer.parseInt(NP_value)

            //Insert url
            contentdto.url = binding.editTextURL.text.toString()

            //Insert place
            contentdto.place = binding.editTextPlace.text.toString()

            //Insert ParticipationTotal
            var participation: Int = contentdto.totalNumber / contentdto.unit
            contentdto.ParticipationTotal = participation

            transition.set(tsDoc!!, contentdto)
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val myData: Intent? = result.data
            val stringData = result.data?.getStringExtra("returnValue")
            binding.editTextPlace.setText(stringData)
        }
    }
}