package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_product)
        val finish = findViewById<Button>(R.id.button49)
        finish.setOnClickListener {
            finish()
        }
        val check = findViewById<Button>(R.id.button53)
        check.setOnClickListener {
            finish()
        }
        val low = findViewById<Button>(R.id.button10)
        low.setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)
        }
    }
}