package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.techtown.matchingservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    //lateinit var auth: FirebaseAuth

    //val database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
   // val infoRef = database.getReference("usersInfo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val uid = auth.currentUser!!.uid.toString()
        infoRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue<UsersInfo>()
                if(userInfo == null){
                    val info = UsersInfo("","","","","",uid)
                    infoRef.child(uid).setValue(info)
                    //moveModifyPage()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })*/


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        //ab.setDisplayHomeAsUpEnabled(true)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        var fragment1 = Fragment1()
        changeTitle("정기구매")
        supportFragmentManager.beginTransaction().add(R.id.main_content, fragment1).commit()
        initNavigationBar()
    }

    fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.tab1 -> {
                        changeTitle("정기구매")
                        var fragment1 = Fragment1()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment1).commit()
                    }
                    R.id.tab2 -> {
                        changeTitle("배달")
                        var fragment2 = Fragment2()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment2).commit()
                    }
                    R.id.tab3-> {
                        changeTitle("채팅")
                        var chatFragment = ChatFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, chatFragment).commit()
                    }
                    R.id.tab4 -> {
                        changeTitle("마이페이지")
                        var fragment4 = Fragment4()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, fragment4).commit()
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

