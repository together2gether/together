package com.techtown.matchingservice

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class FoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_food)
        val finish = findViewById<Button>(R.id.button48)
        finish.setOnClickListener {
            finish()
        }
        val save = findViewById<Button>(R.id.button54)
        save.setOnClickListener {
            finish()
        }
    }
}