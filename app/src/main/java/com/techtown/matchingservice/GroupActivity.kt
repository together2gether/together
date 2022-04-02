package com.techtown.matchingservice

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)
        val finish = findViewById<Button>(R.id.button47)
        finish.setOnClickListener {
            finish()
        }
    }
}