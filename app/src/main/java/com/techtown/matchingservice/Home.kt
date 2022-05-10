package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.techtown.matchingservice.util.FcmPush

class Home :AppCompatActivity() {
    lateinit var message : FirebaseMessaging
    lateinit var firestore : FirebaseFirestore
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        message = FirebaseMessaging.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val page1 = findViewById<Button>(R.id.page1)
        val page2 = findViewById<Button>(R.id.page2)
        val page3 = findViewById<Button>(R.id.page3)
        val page4 = findViewById<Button>(R.id.page4)

        page1.setOnClickListener {
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("page", "1")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run{applicationContext?.startActivity(this)}
            finish()
        }
        page2.setOnClickListener {
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("page", "2")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run{applicationContext?.startActivity(this)}
            finish()
        }
        page3.setOnClickListener {
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("page", "3")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run{applicationContext?.startActivity(this)}
            finish()
        }
        page4.setOnClickListener {
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("page", "4")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run{applicationContext?.startActivity(this)}
            finish()
        }

        registerPushToken()
    }

    fun registerPushToken(){
        message.token.addOnCompleteListener { task ->
            if(task.isSuccessful){

                var token = task.result
                var map = mutableMapOf<String,Any>()
                map["token"] = token

                firestore.collection("pushtokens").document(auth?.uid!!).set(map)

            }

        }
    }
}