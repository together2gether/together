package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64.encode
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.techtown.matchingservice.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.wait
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        //ab.setDisplayHomeAsUpEnabled(true)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        //changeTitle("공동구매")

        binding.search.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.search2.setOnClickListener {
            val intent = Intent(this, SearchFood::class.java)
            startActivity(intent)
        }


        binding.categoryOpen.setOnClickListener {
            var fragment : Fragment2? = supportFragmentManager.findFragmentById(R.id.main_content) as Fragment2?
            fragment?.drawerLayout?.visibility = View.VISIBLE
                fragment?.open()
            binding.categoryOpen.visibility = View.GONE;
        }

        binding.search.setVisibility(View.VISIBLE)
        var page= intent.getStringExtra("page")
        binding.bottomNavigation.itemIconTintList = null
        when(page){
            "1" -> {
                val fragment1 = Fragment1()
                binding.bottomNavigation.selectedItemId = R.id.tab1
                supportFragmentManager.beginTransaction().add(R.id.main_content, fragment1).commit()
                changeTitle("공동구매")
                binding.search.setVisibility(View.VISIBLE)
                binding.search2.setVisibility(View.INVISIBLE)
                binding.categoryOpen.visibility = View.INVISIBLE
            }
            "2" -> {
                binding.bottomNavigation.selectedItemId = R.id.tab2
                var fragment2 = Fragment2()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, fragment2).commit()
                changeTitle("배달")
                binding.search.setVisibility(View.INVISIBLE)
                binding.search2.setVisibility(View.VISIBLE)
                binding.categoryOpen.visibility = View.GONE;
            }
            "3" -> {
                val fragment3 = ChatFragment()
                supportFragmentManager.beginTransaction().add(R.id.main_content, fragment3).commit()
                binding.bottomNavigation.selectedItemId = R.id.tab3
                changeTitle("채팅")
                binding.search.setVisibility(View.INVISIBLE)
                binding.search2.setVisibility(View.INVISIBLE)
                binding.categoryOpen.visibility = View.INVISIBLE
            }
            "4" -> {
                val fragment4 = Fragment4()
                supportFragmentManager.beginTransaction().add(R.id.main_content, fragment4).commit()
                binding.bottomNavigation.selectedItemId = R.id.tab4
                changeTitle("마이페이지")
                binding.search.setVisibility(View.INVISIBLE)
                binding.search2.setVisibility(View.INVISIBLE)
                binding.categoryOpen.visibility = View.INVISIBLE
            }
        }
        //supportFragmentManager.beginTransaction().add(R.id.main_content, fragment1).commit()
        initNavigationBar()
    }
    fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.tab1 -> {
                        changeTitle("공동구매")
                        binding.search.setVisibility(View.VISIBLE)
                        binding.search2.setVisibility(View.INVISIBLE)
                        var fragment1 = Fragment1()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment1).commit()
                        binding.categoryOpen.visibility = View.INVISIBLE
                    }
                    R.id.tab2 -> {
                        changeTitle("배달")
                        binding.search.setVisibility(View.INVISIBLE)
                        binding.search2.setVisibility(View.VISIBLE)
                        var fragment2 = Fragment2()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment2).commit()
                        binding.categoryOpen.visibility = View.GONE;
                    }
                    R.id.tab3-> {
                        changeTitle("채팅")
                        binding.search.setVisibility(View.INVISIBLE)
                        binding.search2.setVisibility(View.INVISIBLE)
                        var chatFragment = ChatFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, chatFragment).commit()
                        binding.categoryOpen.visibility = View.INVISIBLE
                    }
                    R.id.tab4 -> {
                        changeTitle("마이페이지")
                        binding.search.setVisibility(View.INVISIBLE)
                        binding.search2.setVisibility(View.INVISIBLE)
                        var fragment4 = Fragment4()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment4).commit()
                        binding.categoryOpen.visibility = View.INVISIBLE
                    }

                }
                true
            }
        }
    }
    fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_content, fragment).commit()
    }
    fun send(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    fun changeTitle(ti : String){
        var title = findViewById<TextView>(R.id.set_date)
        title.setText(ti)
    }

    fun moveModifyPage(){
        startActivity(Intent(this, ModifyInfo::class.java))
    }

}
