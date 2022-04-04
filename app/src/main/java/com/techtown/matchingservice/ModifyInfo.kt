package com.techtown.matchingservice

import android.Manifest
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

private lateinit var auth: FirebaseAuth

class ModifyInfo : AppCompatActivity() {
    val database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val infoRef = database.getReference("usersInfo")
    var imageUri : Uri? = null
    var profileCheck = false

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
        var edit_nickname = findViewById<EditText>(R.id.edit_nickname).text
        var edit_name = findViewById<EditText>(R.id.edit_Name).text
        var edit_phonenumber = findViewById<EditText>(R.id.edit_phoneNumber).text
        var edit_address = findViewById<EditText>(R.id.edit_Address).text
        val btn_changeImg = findViewById<Button>(R.id.btn_changeImg)
        val modify = findViewById<Button>(R.id.btn_modify)


        btn_changeImg.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)

        }

        //val intent = Intent(this, MainActivity::class.java)
        modify.setOnClickListener {
            if(edit_nickname.isEmpty()||edit_name.isEmpty()||edit_phonenumber.isEmpty()||edit_address.isEmpty()){
                Toast.makeText(this, "닉네임, 이름, 전화번호, 주소를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val user = Firebase.auth.currentUser
                val userId = user?.uid
                val userIdSt = userId.toString()
                if(profileCheck){

                    //프로필 변경 하면
                    FirebaseStorage.getInstance()
                        .reference.child("userImages").child("$userIdSt/photo").putFile(imageUri!!).addOnSuccessListener {
                            var userProfile: Uri? = null
                            //Toast.makeText(this, "확인", Toast.LENGTH_SHORT).show()
                            FirebaseStorage.getInstance().reference.child("userImages").child("$userIdSt/photo").downloadUrl
                                .addOnSuccessListener {

                                    userProfile = it
                                    Toast.makeText(this, userProfile.toString(), Toast.LENGTH_SHORT).show()
                                    Log.d("이미지 URL", "$userProfile")
                                    //val info = UsersInfo(edit_nickname.toString(), edit_name.toString(), edit_phonenumber.toString(), edit_address.toString(), userProfile.toString(), userIdSt)
                                    //infoRef.child(userIdSt).setValue(info)
                                    infoRef.child(userIdSt).child("profileImageUrl").setValue(userProfile.toString())
                                }
                        }
                }
                infoRef.child(userIdSt).child("nickname").setValue(edit_nickname.toString())
                infoRef.child(userIdSt).child("name").setValue(edit_name.toString())
                infoRef.child(userIdSt).child("phonenumber").setValue(edit_phonenumber.toString())
                infoRef.child(userIdSt).child("address").setValue(edit_address.toString())
                finish()
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

}