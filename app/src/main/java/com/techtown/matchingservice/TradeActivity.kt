package com.techtown.matchingservice

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TradeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trade)
        val finish = findViewById<Button>(R.id.button46)
        finish.setOnClickListener {
            finish()
        }
    }
}