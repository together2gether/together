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
        val check2 = findViewById<Button>(R.id.check2)
        check2.setOnClickListener {
            finish()
        }
    }
}