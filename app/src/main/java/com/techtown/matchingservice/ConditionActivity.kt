package com.techtown.matchingservice

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ConditionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.condition)
        val finish = findViewById<Button>(R.id.button31)
        finish.setOnClickListener {
            finish()
        }
    }
}