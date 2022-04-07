package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecommendActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommend)
        val btn_search = findViewById<Button>(R.id.btn_search)
        btn_search.setOnClickListener {
            var edit_search = findViewById<TextView>(R.id.edit_search)
            var keyword = edit_search.text.toString()
            //val intent = Intent(this, )

        }
        val finish = findViewById<Button>(R.id.button50)
        finish.setOnClickListener {
            finish()
        }
    }
}