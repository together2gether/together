package com.techtown.matchingservice

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RecommendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommend)
        val finish = findViewById<Button>(R.id.button50)
        finish.setOnClickListener {
            finish()
        }
    }
}