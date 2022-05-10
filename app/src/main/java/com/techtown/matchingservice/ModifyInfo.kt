package com.techtown.matchingservice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.techtown.matchingservice.model.UsersInfo

lateinit var auth: FirebaseAuth

class ModifyInfo : AppCompatActivity() {
    val user = Firebase.auth.currentUser
    val userId = user?.uid
    val userIdSt = userId.toString()
    val database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val infoRef = database.getReference("usersInfo")
    val userRef = infoRef.child(userIdSt)
    var imageUri : Uri? = null
    var profileCheck = false

    lateinit var searchButton :Button
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
            if(result.resultCode == RESULT_OK) {
                imageUri = result.data?.data//이미지 경로 원본
                var prof_iv = findViewById<ImageView>(R.id.profile_img)
                prof_iv.setImageURI(imageUri)
                Log.d("이미지", "성공")
                profileCheck = true
            } else {
                Log.d("이미지", "실패")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modify_info)
        auth = FirebaseAuth.getInstance()

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        var profile_img = findViewById<ImageView>(R.id.profile_img)
        var edit_nickname = findViewById<EditText>(R.id.edit_nickname)
        var edit_name = findViewById<EditText>(R.id.edit_Name)
        var edit_phonenumber = findViewById<EditText>(R.id.edit_phoneNumber)
        var edit_address = findViewById<EditText>(R.id.edit_Address)
        val btn_changeImg = findViewById<Button>(R.id.btn_changeImg)
        val modify = findViewById<Button>(R.id.btn_modify)
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener{
            val intent = Intent(this, AddressActivity::class.java)
            resultLauncher.launch(intent)
        }
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue<UsersInfo>()
                if(userInfo == null){

                } else {
                    if(userInfo!!.profileImageUrl.toString() != ""){
                        Glide.with(profile_img.context).load(userInfo?.profileImageUrl)
                            .apply(RequestOptions().circleCrop())
                            .into(profile_img)
                        //val uri = Uri.parse(userInfo.profileImageUrl.toString())
                        //profile_img.setImageURI(uri)
                    }
                    edit_nickname.setText(userInfo.nickname.toString())
                    edit_name.setText(userInfo.name.toString())
                    edit_address.setText(userInfo.address.toString())
                    edit_phonenumber.setText(userInfo.phonenumber.toString())

                }

            }
        })


        btn_changeImg.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)
        }



        modify.setOnClickListener {
            if(edit_nickname.text.isEmpty()||edit_name.text.isEmpty()||edit_phonenumber.text.isEmpty()||edit_address.text.isEmpty()){
                Toast.makeText(this, "닉네임, 이름, 전화번호, 주소를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userInfo = snapshot.getValue<UsersInfo>()
                        if(userInfo == null){
                            val newInfo = UsersInfo("", "","","","",userIdSt)
                            userRef.setValue(newInfo)
                        }
                        userRef.child("nickname").setValue(edit_nickname.text.toString())
                        userRef.child("name").setValue(edit_name.text.toString())
                        userRef.child("phonenumber").setValue(edit_phonenumber.text.toString())
                        userRef.child("address").setValue(edit_address.text.toString())
                        if(profileCheck){
                            FirebaseStorage.getInstance()
                                .reference.child("userImages").child("$userIdSt/photo").putFile(imageUri!!).addOnSuccessListener {
                                    var userProfile: Uri? = null
                                    FirebaseStorage.getInstance().reference.child("userImages").child("$userIdSt/photo").downloadUrl
                                        .addOnSuccessListener {
                                            userProfile = it
                                            userRef.child("profileImageUrl").setValue(userProfile.toString())
                                            Log.d("profile",userProfile.toString())
                                            finish()
                                        }
                                }
                        }
                        else {
                            finish()
                        }


                    }
                })



            }
        }
        val finish = findViewById<Button>(R.id.button45)
        finish.setOnClickListener {
            finish()
        }
    }
    public override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload()
        }
    }
    private fun reload(){

    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val myData: Intent? = result.data
            val stringData = result.data?.getStringExtra("returnValue")
            val editAddress = findViewById<EditText>(R.id.edit_Address)
            editAddress.setText(stringData)
        }
    }
}