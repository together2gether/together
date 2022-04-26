package com.techtown.matchingservice

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.daum.mf.map.api.*

class RecommandLocation : AppCompatActivity() {
    val PERMISSIONS_REQUEST_CODE = 100
    val REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    companion object {
        const val BASE_URL = "http://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 39c205c0e521e0d112dcefa5460592d8"
    }
    private val listItems = arrayListOf<ListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = ListAdapter(listItems)    // 리사이클러 뷰 어댑터
    private var pageNumber = 1      // 검색 페이지 번호
    private var keyword = ""        // 검색 키워드
    lateinit var mapView : MapView
    lateinit var button : Button
    lateinit var button1 : Button
    lateinit var lat : String
    lateinit var lng : String
    lateinit var mylat : String
    lateinit var mylng : String
    lateinit var yourlat : String
    lateinit var yourlng : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommend_location)
        mapView = findViewById(R.id.mapView)
        val layout = findViewById<RecyclerView>(R.id.rv_list)
        try{
            // 리사이클러 뷰
            layout.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            layout.adapter = listAdapter
            // 리스트 아이템 클릭 시 해당 위치로 이동
            listAdapter.setItemClickListener(object: ListAdapter.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    val mapPoint = MapPoint.mapPointWithGeoCoord(listItems[position].y, listItems[position].x)
                    mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                }
            })
        }catch (e: Exception){

        }
         lat = intent.getStringExtra("lat").toString()
         lng = intent.getStringExtra("lng").toString()
        mylat = intent.getStringExtra("mylat").toString()
        mylng = intent.getStringExtra("mylng").toString()
        yourlat = intent.getStringExtra("yourlat").toString()
        yourlng = intent.getStringExtra("yourlng").toString()

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                val uNowPosition = MapPoint.mapPointWithGeoCoord(mylat.toDouble(), mylng.toDouble())
                mapView.setMapCenterPoint(uNowPosition, true)
                mapView.setZoomLevel(1, true)
                val marker = MapPOIItem()
                marker.itemName = "내 위치"
                marker.mapPoint = uNowPosition
                marker.markerType = MapPOIItem.MarkerType.CustomImage
                marker.selectedMarkerType = MapPOIItem.MarkerType.CustomImage
                marker.customImageResourceId = R.drawable.red_pin
                //marker.customSelectedImageResourceId = R.drawable.blue_pin
                marker.isCustomImageAutoscale = false
                marker.setCustomImageAnchor(0.5f, 1.0f)
                mapView.addPOIItem(marker)
                /*val yourpos = MapPoint.mapPointWithGeoCoord(yourlat.toDouble(), yourlng.toDouble())
                mapView.setMapCenterPoint(yourpos, true)
                mapView.setZoomLevel(1, true)
                val marker2 = MapPOIItem()
                marker2.itemName = "상대방 위치"
                marker2.mapPoint = yourpos
                marker2.markerType = MapPOIItem.MarkerType.CustomImage
                marker2.selectedMarkerType = MapPOIItem.MarkerType.CustomImage
                marker2.customImageResourceId = R.drawable.blue_pin
                //marker.customSelectedImageResourceId = R.drawable.blue_pin
                marker2.isCustomImageAutoscale = false
                marker2.setCustomImageAnchor(0.5f, 1.0f)
                mapView.addPOIItem(marker2)*/
                var circle = MapCircle(MapPoint.mapPointWithGeoCoord(lat.toDouble(), lng.toDouble()),
                    500, Color.argb(0,255,255,255 ),
                    Color.argb(0,255, 255,255));
                circle.tag = 1234
                mapView.addCircle(circle)
                val array1 : Array<MapPointBounds> = arrayOf(circle.bound)
                val mapPointBounds = MapPointBounds(array1)
                var padding = 50
                mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds,padding))
                var circle2 = MapCircle(MapPoint.mapPointWithGeoCoord(yourlat.toDouble(), yourlng.toDouble()),
                    500, Color.argb(128,189,215,238 ),
                    Color.argb(128,189, 215,238));
                circle2.tag = 1234
                mapView.addCircle(circle2)
                //val array2 : Array<MapPointBounds> = arrayOf(circle2.bound)

            }catch(e: NullPointerException){
                Toast.makeText(applicationContext, "durl", Toast.LENGTH_LONG).show()
                Log.e("LOCATION_ERROR", e.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.finishAffinity(this)
                }else{
                    ActivityCompat.finishAffinity(this)
                }

                /*val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                System.exit(0)*/
            }
        }else{
            Toast.makeText(this, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
        }
        searchKeyword("편의점",1)
    }
    // 키워드 검색 함수
    private fun searchKeyword(keyword: String, page: Int) {
        val retrofit = Retrofit.Builder()          // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)            // 통신 인터페이스를 객체로 생성
        //val call = api.getSearchCategory(API_KEY, "CS2", "126.972526" ,"37.560452", 50000)
        val call = api.getSearchKeyword(API_KEY, "CS2",lng, lat,1000 ,1, "distance")
        // 검색 조건 입력

        //val call = api.getSearchKeyword(API_KEY, keyword, "126.972526" ,"37.560452", 50000 )
        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(call: Call<ResultSearchKeyword>, response: Response<ResultSearchKeyword>) {
                // 통신 성공
                addItemsAndMarkers(response.body())
                val call1 = api.getSearchKeyword(API_KEY, "SW8", lng, lat, 500, 1, "distance")
                call1.enqueue(object : Callback<ResultSearchKeyword> {
                    override fun onResponse(
                        call: Call<ResultSearchKeyword>,
                        response: Response<ResultSearchKeyword>
                    ) {
                        addItemsAndMarkers(response.body())
                    }

                    override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                        Log.w("LocalSearch", "통신 실패 : ${t.message}")
                    }
                })
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })

    }

    // 검색 결과 처리 함수
    private fun addItemsAndMarkers(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            //listItems.clear()                   // 리스트 초기화
            //mapView.removeAllPOIItems() // 지도의 마커 모두 제거
            for (document in searchResult!!.documents) {
                // 결과를 리사이클러 뷰에 추가
                val item = ListLayout(document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble())
                listItems.add(item)

                // 지도에 마커 추가
                val point = MapPOIItem()
                point.apply {
                    itemName = document.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(document.y.toDouble(),
                        document.x.toDouble())
                    markerType = MapPOIItem.MarkerType.BluePin
                    selectedMarkerType = MapPOIItem.MarkerType.RedPin
                }
                mapView.addPOIItem(point)
            }
            listAdapter.notifyDataSetChanged()
        } else {
            // 검색 결과 없음
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

}