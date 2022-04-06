package com.techtown.matchingservice

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.location.Geocoder
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.*

class AddressActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.address)
        val geocoder = Geocoder(this, Locale.getDefault())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        !=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "위치 권한을 설정해주세요.",Toast.LENGTH_LONG).show()
        }else{
            val button = findViewById<Button>(R.id.corToAddressButton)
            button.setOnClickListener {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    val address = geocoder.getFromLocation(it.latitude, it.longitude,1)
                    Toast.makeText(this, "주소 : ${address[0].subLocality}", Toast.LENGTH_LONG).show()
                }
            }
            val input1 = findViewById<EditText>(R.id.input1)
            val button1 = findViewById<Button>(R.id.addressToCorButton)
            button1.setOnClickListener {
                val cor = geocoder.getFromLocationName(input1.text.toString(),1)
                Toast.makeText(this, "좌표 : ${cor[0].latitude}, ${cor[0].longitude}", Toast.LENGTH_LONG).show()
            }
        }

    }
}