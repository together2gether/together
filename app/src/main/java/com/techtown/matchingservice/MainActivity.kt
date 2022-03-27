package com.techtown.matchingservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.techtown.matchingservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        initNavigationBar()
    }

    fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.tab1 -> {
                        var fragment1 = Fragment1()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment1).commit()
                    }
                    R.id.tab2 -> {
                        var fragment2 = Fragment2()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment2).commit()
                    }
                    R.id.tab3-> {
                        var chatFragment = ChatFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, chatFragment).commit()
                    }
                    R.id.tab4 -> {
                        var fragment4 = Fragment4()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment4).commit()
                    }

                }
                true
            }
        }
    }
}