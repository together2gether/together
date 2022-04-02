package com.techtown.matchingservice

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ModifyInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modify_info)
        val modify = findViewById<Button>(R.id.modify)
        modify.setOnClickListener {
            finish()
        }
        val finish = findViewById<Button>(R.id.button45)
        finish.setOnClickListener {
            finish()
        }
    }
}